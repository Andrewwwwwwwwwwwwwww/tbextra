# Changelog

## 1.0.0 - 2026-08-20
- Reworked into a reskinner. The mod no longer adds backpacks of its own; instead any
  Traveler's Backpack can be crafted into a new look. The result is the same backpack it
  was made from, so its ability, tier, upgrades and contents are untouched - only the name
  and model change, and shift still shows the ability as usual.
- Added the **Firewatch Backpack** (campfires) and **Trapper's Backpack** (iron ingots),
  each crafted from any backpack plus an empty bundle.
- Reskins are one-way.
- Custom geometry is rendered directly from the artist's Blockbench export, because both
  models use sub-assemblies rotated on all three axes, which the vanilla model format
  cannot represent. Hitboxes are unchanged from the base backpack, since the item is too.
