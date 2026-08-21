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
compact quad list (`tools/genquads.js` -> `assets/tbextra/geometry/*.bin`) and drawn directly.

### Adding or changing a skin

1. Re-export the `.gltf` from Blockbench.
2. `node tools/genquads.js "<path to models folder>"`
3. Add the skin id to `BackpackSkins`, a recipe under `data/tbextra/recipe/`, and a name in
   the language file.
4. Rebuild.

`TARGET_HEIGHT_PX` at the top of `tools/genquads.js` sets how tall each pack stands
(Traveler's Backpack's own pack is about 10px).

## Building

```
./gradlew build
```

`libs/travelersbackpack-fabric-26.2-11.3.1.jar` is vendored for compilation only; it is not
bundled into the output jar.
