// Regenerates everything derived from the artists' models.
//
//   node tools/build-skins.js
//
// Reads skins.json plus models/<id>/ and writes:
//   assets/tbextra/geometry/<id>.bin          baked quads
//   assets/tbextra/textures/entity/<id>.png   copied texture
//   assets/tbextra/lang/en_us.json            display names
//   data/tbextra/recipe/<id>.json             reskin recipe
//   BackpackBounds.java, BackpackSkins.java   generated constants
//
// Adding a skin is: drop the files into models/<id>/, add an entry to skins.json, run this.
const fs = require('fs');
const path = require('path');
const { readGltf } = require('./gltfread.js');

// Each pack is scaled to stand this tall. Traveler's Backpack's own pack is about 10px;
// these are larger hiking packs. Change this and re-run to resize every skin at once.
const TARGET_HEIGHT_PX = 14;

const ROOT = path.join(__dirname, '..');
const MODELS = path.join(ROOT, 'models');
const MAIN = path.join(ROOT, 'src', 'main');
const ASSETS = path.join(MAIN, 'resources', 'assets', 'tbextra');
const DATA = path.join(MAIN, 'resources', 'data', 'tbextra');
const JAVA = path.join(MAIN, 'java', 'io', 'github', 'andrewwwwwwwwwwwwwww', 'tbextra');

function fail(message) {
  console.error('\nerror: ' + message);
  process.exit(1);
}

const asFloat = n => {
  const v = Math.round(n * 100000) / 100000;
  return (Number.isInteger(v) ? v + '.0' : String(v)) + 'F';
};

const sub = (a, b) => [a[0] - b[0], a[1] - b[1], a[2] - b[2]];
const len = a => Math.hypot(a[0], a[1], a[2]);
const dot = (a, b) => a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
const cross = (a, b) => [
  a[1] * b[2] - a[2] * b[1],
  a[2] * b[0] - a[0] * b[2],
  a[0] * b[1] - a[1] * b[0]
];
const quadArea = q => len(cross(sub(q.pos[1], q.pos[0]), sub(q.pos[2], q.pos[1])));

const manifest = JSON.parse(fs.readFileSync(path.join(ROOT, 'skins.json'), 'utf8'));
const skins = Object.keys(manifest).filter(key => !key.startsWith('_'));
if (skins.length === 0) {
  fail('skins.json lists no skins');
}

for (const dir of [
  path.join(ASSETS, 'geometry'),
  path.join(ASSETS, 'textures', 'entity'),
  path.join(ASSETS, 'lang'),
  path.join(DATA, 'recipe')
]) {
  fs.mkdirSync(dir, { recursive: true });
}

const bounds = [];
const names = {};
let warnings = 0;

for (const id of skins) {
  const entry = manifest[id];

  if (!/^[a-z0-9_]+$/.test(id)) {
    fail('skin id "' + id + '" must be lowercase letters, digits or underscores');
  }
  for (const field of ['name', 'material']) {
    if (!entry[field]) {
      fail('skin "' + id + '" is missing "' + field + '" in skins.json');
    }
  }

  const dir = path.join(MODELS, id);
  if (!fs.existsSync(dir)) {
    fail('skin "' + id + '" has no models/' + id + '/ folder');
  }
  const pick = extension => {
    const found = fs.readdirSync(dir).filter(f => f.toLowerCase().endsWith(extension));
    if (found.length === 0) {
      fail('models/' + id + '/ contains no ' + extension + ' file');
    }
    if (found.length > 1) {
      fail('models/' + id + '/ contains more than one ' + extension + ' file: ' + found.join(', '));
    }
    return path.join(dir, found[0]);
  };
  const gltfFile = pick('.gltf');
  const textureFile = pick('.png');

  // Geometry. Flat cubes contribute zero-area side faces that draw nothing.
  const quads = readGltf(gltfFile).filter(q => quadArea(q) > 1e-9);
  if (quads.length === 0) {
    fail('skin "' + id + '" produced no geometry from ' + path.basename(gltfFile));
  }

  // Every face should be a flat rectangle wound to agree with its normal. This is what
  // catches a bad export, or a bad triangle-to-quad rebuild.
  let notRectangular = 0;
  let woundInsideOut = 0;
  for (const q of quads) {
    const e1 = sub(q.pos[1], q.pos[0]);
    const e2 = sub(q.pos[2], q.pos[1]);
    const e3 = sub(q.pos[3], q.pos[2]);
    const e4 = sub(q.pos[0], q.pos[3]);
    if (Math.abs(len(e1) - len(e3)) > 1e-4 || Math.abs(len(e2) - len(e4)) > 1e-4) {
      notRectangular++;
    }
    const normal = cross(e1, e2);
    const scale = len(normal) || 1;
    if (Math.abs(dot([normal[0] / scale, normal[1] / scale, normal[2] / scale], q.normal) - 1) > 0.02) {
      woundInsideOut++;
    }
    for (const uv of q.uv) {
      if (uv[0] < -0.001 || uv[0] > 1.001 || uv[1] < -0.001 || uv[1] > 1.001) {
        fail('skin "' + id + '" has a face mapped outside its texture');
      }
    }
  }
  if (woundInsideOut > 0) {
    fail('skin "' + id + '": ' + woundInsideOut + ' faces are wound inside out - the export looks wrong');
  }
  if (notRectangular > 0) {
    console.warn('  note: ' + id + ' has ' + notRectangular + ' non-rectangular faces (usually harmless slivers)');
    warnings++;
  }

  const points = quads.flatMap(q => q.pos);
  const extent = [0, 1, 2].map(axis => [
    Math.min(...points.map(p => p[axis])),
    Math.max(...points.map(p => p[axis]))
  ]);
  const heightPx = (extent[1][1] - extent[1][0]) * 16;
  const scale = TARGET_HEIGHT_PX / heightPx;
  const centreX = (extent[0][0] + extent[0][1]) / 2;
  const centreZ = (extent[2][0] + extent[2][1]) / 2;
  const floorY = extent[1][0];
  // Centre on X/Z and stand on y=0, in block units.
  const place = p => [
    (p[0] - centreX) * scale,
    (p[1] - floorY) * scale,
    (p[2] - centreZ) * scale
  ];

  // Big-endian throughout so the mod can read it with a plain DataInputStream.
  const buffer = Buffer.alloc(12 + quads.length * 23 * 4);
  buffer.writeInt32BE(0x54424758, 0);
  buffer.writeInt32BE(1, 4);
  buffer.writeInt32BE(quads.length, 8);
  let offset = 12;
  for (const q of quads) {
    for (let i = 0; i < 4; i++) {
      const p = place(q.pos[i]);
      buffer.writeFloatBE(p[0], offset); offset += 4;
      buffer.writeFloatBE(p[1], offset); offset += 4;
      buffer.writeFloatBE(p[2], offset); offset += 4;
      buffer.writeFloatBE(q.uv[i][0], offset); offset += 4;
      buffer.writeFloatBE(q.uv[i][1], offset); offset += 4;
    }
    buffer.writeFloatBE(q.normal[0], offset); offset += 4;
    buffer.writeFloatBE(q.normal[1], offset); offset += 4;
    buffer.writeFloatBE(q.normal[2], offset); offset += 4;
  }

  fs.writeFileSync(path.join(ASSETS, 'geometry', id + '.bin'), buffer);
  fs.copyFileSync(textureFile, path.join(ASSETS, 'textures', 'entity', id + '.png'));

  bounds.push({
    id,
    x0: (extent[0][0] - centreX) * scale + 0.5,
    x1: (extent[0][1] - centreX) * scale + 0.5,
    y0: 0,
    y1: (extent[1][1] - floorY) * scale,
    z0: (extent[2][0] - centreZ) * scale + 0.5,
    z1: (extent[2][1] - centreZ) * scale + 0.5
  });
  names['item.tbextra.' + id] = entry.name;

  fs.writeFileSync(path.join(DATA, 'recipe', id + '.json'), JSON.stringify({
    type: 'tbextra:reskin',
    skin: id,
    material: entry.material,
    backpack: '#travelersbackpack:custom_travelers_backpack'
  }, null, 2) + '\n');

  const width = (extent[0][1] - extent[0][0]) * 16 * scale;
  const depth = (extent[2][1] - extent[2][0]) * 16 * scale;
  console.log(
    id.padEnd(14) + String(quads.length).padStart(4) + ' quads   ' +
    width.toFixed(1) + ' x ' + TARGET_HEIGHT_PX + ' x ' + depth.toFixed(1) + ' px   ' +
    '"' + entry.name + '"  from ' + entry.material
  );
}

fs.writeFileSync(path.join(JAVA, 'BackpackBounds.java'),
`package io.github.andrewwwwwwwwwwwwwww.tbextra;

/**
 * Bounding box of each backpack model, in block space, matching the geometry the client
 * draws. Generated by tools/build-skins.js - do not edit by hand.
 */
public final class BackpackBounds {
    /** Corner-to-corner extent of a pack, in block units. */
    public record Bounds(float x0, float y0, float z0, float x1, float y1, float z1) {
        public float width() {
            return x1 - x0;
        }

        public float height() {
            return y1 - y0;
        }

        public float depth() {
            return z1 - z0;
        }

        /** Largest dimension, used to size packs consistently against each other. */
        public float longestEdge() {
            return Math.max(width(), Math.max(height(), depth()));
        }
    }

    private BackpackBounds() {
    }

    public static Bounds of(String backpack) {
        return switch (backpack) {
${bounds.map(b =>
`            case "${b.id}" -> new Bounds(${asFloat(b.x0)}, ${asFloat(b.y0)}, ${asFloat(b.z0)}, ${asFloat(b.x1)}, ${asFloat(b.y1)}, ${asFloat(b.z1)});`
).join('\n')}
            default -> throw new IllegalArgumentException("Unknown backpack: " + backpack);
        };
    }
}
`);

fs.writeFileSync(path.join(JAVA, 'BackpackSkins.java'),
`package io.github.andrewwwwwwwwwwwwwww.tbextra;

import java.util.List;

/**
 * The reskins this mod adds. A skin only changes a backpack's name and model - the item
 * itself stays whichever Traveler's Backpack it was crafted from, so its ability, tier,
 * upgrades and contents are untouched.
 *
 * Generated by tools/build-skins.js from skins.json - do not edit by hand.
 */
public final class BackpackSkins {
    public static final List<String> ALL = List.of(
${skins.map(id => `            "${id}"`).join(',\n')});

    private BackpackSkins() {
    }

    public static boolean isKnown(String skin) {
        return ALL.contains(skin);
    }

    /** Translation key used for the renamed backpack, e.g. "item.tbextra.firewatch". */
    public static String nameKey(String skin) {
        return "item." + TbExtra.MODID + "." + skin;
    }
}
`);

// Refresh the generated names, and drop any left over from a removed skin.
const langPath = path.join(ASSETS, 'lang', 'en_us.json');
const lang = fs.existsSync(langPath) ? JSON.parse(fs.readFileSync(langPath, 'utf8')) : {};
for (const key of Object.keys(lang)) {
  if (key.startsWith('item.tbextra.') && !(key in names)) {
    delete lang[key];
  }
}
fs.writeFileSync(langPath, JSON.stringify({ ...lang, ...names }, null, 2) + '\n');

console.log('\n' + skins.length + ' skin(s) generated' +
  (warnings > 0 ? ', ' + warnings + ' note(s)' : '') + '. Next: gradlew build');
