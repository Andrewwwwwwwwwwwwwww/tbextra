// Bakes the Blockbench glTF exports into a compact quad list the mod loads at runtime.
// Run:  node tools/genquads.js <models-dir>
// Output: src/main/resources/assets/tbextra/geometry/<id>.bin
const fs=require('fs'),path=require('path');
const {readGltf}=require('./gltfread.js');

// Each pack is scaled so it stands TARGET_HEIGHT_PX tall. Traveler's Backpack's own
// pack is ~10px; these are hiking packs so they sit a little taller. Change this
// number and re-run to resize.
const TARGET_HEIGHT_PX = 14;

const MODELS=[
  {id:'firewatch', dir:'Firewatch Backpack', gltf:'Backpack.gltf'},
  {id:'trapper',   dir:'Trapper Backpack',   gltf:'Trap Pack.gltf'},
];
const SRC=process.argv[2];
const OUT=path.join(__dirname,'..','src','main','resources','assets','tbextra','geometry');

for(const m of MODELS){
  // Flat (zero-thickness) cubes contribute four zero-area side faces that draw nothing.
  const area=q=>{
    const sub=(a,c)=>[a[0]-c[0],a[1]-c[1],a[2]-c[2]];
    const e1=sub(q.pos[1],q.pos[0]), e2=sub(q.pos[2],q.pos[1]);
    const cx=[e1[1]*e2[2]-e1[2]*e2[1], e1[2]*e2[0]-e1[0]*e2[2], e1[0]*e2[1]-e1[1]*e2[0]];
    return Math.hypot(...cx);
  };
  const raw=readGltf(path.join(SRC,m.dir,m.gltf));
  const quads=raw.filter(q=>area(q)>1e-9);
  if(raw.length!==quads.length) console.log(`  (dropped ${raw.length-quads.length} zero-area faces from flat cubes)`);
  const all=quads.flatMap(q=>q.pos);
  const bounds=[0,1,2].map(a=>[Math.min(...all.map(p=>p[a])),Math.max(...all.map(p=>p[a]))]);
  const heightPx=(bounds[1][1]-bounds[1][0])*16;
  const scale=TARGET_HEIGHT_PX/heightPx;
  const cx=(bounds[0][0]+bounds[0][1])/2, cz=(bounds[2][0]+bounds[2][1])/2, y0=bounds[1][0];
  // centre on X/Z, sit on y=0, in block units
  const tf=p=>[(p[0]-cx)*scale,(p[1]-y0)*scale,(p[2]-cz)*scale];

  const buf=Buffer.alloc(12+quads.length*23*4);
  buf.writeInt32BE(0x54424758,0); buf.writeInt32BE(1,4); buf.writeInt32BE(quads.length,8);
  let o=12;
  for(const q of quads){
    for(let i=0;i<4;i++){
      const p=tf(q.pos[i]);
      buf.writeFloatBE(p[0],o);o+=4; buf.writeFloatBE(p[1],o);o+=4; buf.writeFloatBE(p[2],o);o+=4;
      buf.writeFloatBE(q.uv[i][0],o);o+=4; buf.writeFloatBE(q.uv[i][1],o);o+=4;
    }
    buf.writeFloatBE(q.normal[0],o);o+=4; buf.writeFloatBE(q.normal[1],o);o+=4; buf.writeFloatBE(q.normal[2],o);o+=4;
  }
  fs.writeFileSync(path.join(OUT,m.id+'.bin'),buf);
  const w=(bounds[0][1]-bounds[0][0])*16*scale, d=(bounds[2][1]-bounds[2][0])*16*scale;
  console.log(`${m.id}: ${quads.length} quads -> ${m.id}.bin (${buf.length} bytes), `
    +`scaled ${scale.toFixed(4)} to ${w.toFixed(1)} x ${TARGET_HEIGHT_PX} x ${d.toFixed(1)} px`);
}
