# Traveler's Backpack Extras — listing copy

**GitHub description:**

Reskins for Traveler's Backpack. Craft any backpack into a new look and it keeps its ability, tier, upgrades and contents.

**GitHub topics:**

minecraft, minecraft-mod, fabric, fabricmc, travelersbackpack, addon, cosmetic, skins, java

**Summary (CurseForge summary field, 255 char max):**

Reskins for Traveler's Backpack. Craft any backpack into a new look and get the same backpack back: same ability, same tier, same upgrades, same contents. Only the name and the model change.

---

# Description (paste into the CurseForge description editor in Markdown mode)

## Traveler's Backpack Extras

New looks for the pack you already have

An add-on for **Traveler's Backpack** that adds backpack skins. Craft any backpack into a new look and what you get back is *the same backpack*: same ability, same tier, same upgrades, same contents. Only its name and its model change.

So a bookshelf backpack reskinned as a Firewatch still does everything a bookshelf backpack does.

## The skins

**Firewatch Backpack** and **Trapper's Backpack**, each a purpose-built model rather than a repaint.

## Recipes

Both are crafted from any Traveler's Backpack plus an empty bundle, ringed with the skin's material:

| | | |
|---|---|---|
| material | material | material |
| material | any backpack | material |
| material | empty bundle | material |

- **Firewatch Backpack** — the material is a **campfire**
- **Trapper's Backpack** — the material is an **iron ingot**

Reskinning is one-way. If you want a plain-looking pack with no ability, reskin a plain backpack.

## Why it keeps the ability

A reskin is a data component on Traveler's Backpack's own item, not a new item of ours.

That distinction is the whole design. Traveler's Backpack chooses a pack's ability by item identity, checking whether the item *is* the bookshelf backpack and so on, which means a separate item could never carry the buff across. Keeping the original item and changing only how it is presented is what lets the ability survive the craft.

The rendering is intercepted rather than replaced, because neither Traveler's Backpack's own renderer nor vanilla model JSON can express these shapes. Traveler's Backpack paints a flat texture onto one fixed silhouette. Vanilla model JSON allows rotation on a single axis at fixed steps. Both of these models hang sub-assemblies at free angles on all three axes: a bear trap, antlers, straps, a shovel. So the geometry is baked out of the Blockbench export into a compact quad list and drawn directly.

## Requirements

- Minecraft **26.2**, **Fabric** with Fabric API
- **Traveler's Backpack 11.3.0 or newer**

Traveler's Backpack is required. This mod does nothing on its own.

## Installation

1. Install [Traveler's Backpack](https://www.curseforge.com/minecraft/mc-mods/travelers-backpack) and [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
2. Drop this jar into the same `mods` folder
3. Craft one of the recipes above

Not affiliated with or endorsed by Mojang, or by the Traveler's Backpack team.
