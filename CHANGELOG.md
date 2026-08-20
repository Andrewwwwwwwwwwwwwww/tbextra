# Changelog

## 1.0.0 - 2026-08-20
- Each pack now has its own collision shape derived from its model, instead of reusing
  Traveler's Backpack's one-size box. The shape rotates with the block and is clamped to
  the block it sits on, so a pack that overhangs visually still has a sane hitbox.
- Fixed items rendering small and off-centre when dropped or in the inventory. The geometry
  is now laid out in block space for every view, matching how Traveler's Backpack does it,
  and uses TB's own display transforms with a per-pack scale.
- Fixed a crash on startup: Fabric invokes entrypoints alphabetically, so this mod
  initialised before Traveler's Backpack and its block entity type was still null.
  Linking is now deferred to a point where TB is guaranteed loaded.

## 1.0.0 - 2026-08-19
- Initial release.
- Adds two backpacks to Traveler's Backpack: **Firewatch Backpack** and **Trapper's Backpack**.
- Both reuse Traveler's Backpack's block, item, block entity, inventory, upgrades and screens.
  Only the model is new; storage and behaviour are unchanged.
- Custom geometry is rendered directly from the artist's Blockbench export, because both packs
  use sub-assemblies rotated on all three axes, which the vanilla model format cannot represent.
