# Traveler's Backpack Extras

Reskins for [Traveler's Backpack](https://modrinth.com/mod/travelersbackpack) on Minecraft
26.2 (Fabric).

Craft any backpack into a new look. The pack you get back is *the same backpack* - same
ability, same tier, same upgrades, same contents. Only its name and model change, so a
bookshelf backpack reskinned as a Firewatch still does what a bookshelf backpack does.

## Recipes

Both are crafted from any Traveler's Backpack plus an empty bundle:

| | | |
| --- | --- | --- |
| material | material | material |
| material | any backpack | material |
| material | empty bundle | material |

- **Firewatch Backpack** - material is a campfire
- **Trapper's Backpack** - material is an iron ingot

Reskinning is one-way. For a pack with no ability, reskin a plain backpack.

## Requires

- Minecraft 26.2, Fabric Loader 0.19.3+
- Fabric API
- Traveler's Backpack 11.3.0+

## How it works

A reskin is a data component on Traveler's Backpack's own item. That matters: TB picks a
pack's ability by item identity (`if (item == BOOKSHELF_TRAVELERS_BACKPACK)`), so a separate
item could never keep the buff. Keeping the original item and changing only its presentation
is what makes the ability survive.

Rendering is intercepted rather than replaced. Traveler's Backpack draws its packs by
painting a 64x64 texture onto one fixed shape, which cannot express new shapes, and neither
can vanilla model JSON - both of these models hang sub-assemblies (a bear trap, antlers,
straps, a shovel) at free angles on all three axes, while the format allows a single axis at
fixed 22.5/45 degree steps. So the geometry is baked out of the Blockbench glTF export into a
compact quad list (`tools/build-skins.js` -> `assets/tbextra/geometry/*.bin`) and drawn directly.

### Adding a skin

1. Put the artist's files in `models/<id>/` - the `.gltf` export, the `.png` texture, and
   the `.bbmodel` alongside them for reference. Filenames do not matter; the id is the
   folder name, lowercase.
2. Add an entry to `skins.json`:

   ```json
   "mushroom": {
     "name": "Mushroom Backpack",
     "material": "minecraft:red_mushroom"
   }
   ```

3. `node tools/build-skins.js`
4. `gradlew build`

That regenerates the geometry, texture, display name, recipe and both generated Java files.
Nothing else is edited by hand, and removing a skin is the reverse: delete the folder and the
entry, re-run, and its leftovers are cleaned up.

**Fields**

| field | required | meaning |
| --- | --- | --- |
| `name` | yes | what the reskinned pack is called in game |
| `material` | yes | the item filling the other eight slots of the recipe. An item id, or a tag like `#minecraft:logs` |
| `height` | no | how tall the pack stands, in pixels. Defaults to 14; Traveler's Backpack's own pack is about 10 |

**What the generator checks**

It stops and tells you if a model is missing, if a folder holds two `.gltf`s, if a face is
mapped outside its texture, if faces are wound inside out, or if `material` is not a valid
item or tag id. It warns, without stopping, about a folder in `models/` that no `skins.json`
entry claims - the easy mistake when dropping new art in.

The one thing it cannot check is whether the item id in `material` actually exists, since
that is only known when the game loads. A typo there shows up as a recipe that never appears.

**Changing every pack's size at once**

`DEFAULT_HEIGHT_PX` at the top of `tools/build-skins.js` sets the height for skins that do
not specify their own.

**The recipe shape is fixed**

Every skin uses the same grid - eight of the material around any backpack, with an empty
bundle beneath it. Only the material varies. A skin needing a different layout would need a
change to `SkinRecipe`.

## Building

```
./gradlew build
```

`libs/travelersbackpack-fabric-26.2-11.3.1.jar` is vendored to compile against and is not
bundled into the output jar. It is not committed either - download it from Modrinth into
`libs/` to build.
