# Traveler's Backpack Extras

Adds new backpacks to [Traveler's Backpack](https://modrinth.com/mod/travelersbackpack) for
Minecraft 26.2 (Fabric). Purely cosmetic additions: every pack uses Traveler's Backpack's own
block, item, inventory, upgrades, tiers and UI. Only the model is new.

| Backpack | Crafted from |
| --- | --- |
| Firewatch Backpack | Standard backpack + lantern + leather |
| Trapper's Backpack | Standard backpack + iron ingot + leather |

## Requires

- Minecraft 26.2, Fabric Loader 0.19.3+
- Fabric API
- Traveler's Backpack 11.3.0+

## How the models work

Traveler's Backpack draws its own packs by painting a 64x64 texture onto one fixed shape, so
new shapes cannot be added through it. It also cannot be done with vanilla model JSON: both of
these packs hang sub-assemblies (a bear trap, antlers, straps, a shovel) at free angles on all
three axes, and the vanilla format allows a single axis at fixed 22.5/45 degree steps.

So the geometry is baked out of the Blockbench glTF export into a compact quad list
(`tools/genquads.js` -> `assets/tbextra/geometry/*.bin`) and drawn directly, through a custom
item model type for the item and a block entity renderer for placed packs. Because Traveler's
Backpack renders the worn pack from the item model, wearing it works with no extra code.

### Changing a model

1. Re-export the `.gltf` from Blockbench.
2. `node tools/genquads.js "<path to models folder>"`
3. Rebuild.

`TARGET_HEIGHT_PX` at the top of `tools/genquads.js` sets how tall each pack stands
(Traveler's Backpack's own pack is about 10px).

## Building

```
./gradlew build
```

`libs/travelersbackpack-fabric-26.2-11.3.1.jar` is vendored for compilation only; it is not
bundled into the output jar.
