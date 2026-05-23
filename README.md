# Create: Backtank Trims

A small standalone [Forge](https://files.minecraftforge.net/) mod for
[Create](https://modrinth.com/mod/create)'s diving gear. It does two things:

- **Armor trims** — re-enables smithing-table trims on Create's diving helmets, diving
  boots and backtanks, and makes them render on the worn equipment.
- **Dyeable visors** — lets you recolour the eye-visor of the diving helmets, with an
  optional slow pulsing effect.

I got annoyed after [Create issue #6213](https://github.com/Creators-of-Create/Create/issues/6213)
never got fixed, and just did it myself.

## Armor trims

### The problem

Create's copper/netherite diving helmets, diving boots and backtanks are armor, but Create
explicitly opts them out of the `minecraft:trimmable_armor` tag. As a result:

- the smithing table refuses to trim them, and
- even if a trim is forced onto them (applied before conversion, NBT editors, Almost
  Unified, …), the trim never renders.

### What the mod does

- **Re-enables trimming** for all six pieces by re-adding them to the `trimmable_armor`
  tag. The mod loads *after* Create, so this addition wins over Create's removal.
- **Renders the trim on the netherite backtank.** The five other pieces render through
  vanilla's armor layer, so their trims appear automatically once trimmable. The netherite
  backtank is drawn by Create's own custom renderer, which skips the vanilla trim pass — so
  the mod adds a dedicated render layer that draws the trim decal over the diving suit.
  The same layer also covers add-on chestpieces that reuse Create's `LayeredArmorItem`
  path; currently that means the [create_jetpack](https://modrinth.com/mod/create-jetpack)
  mod's netherite jetpack.
- **Adds inventory trim icons** for the four flat diving items (helmets and boots), so a
  trimmed piece shows its trim overlay in inventories and tooltips.

| Item                             | Smithing table | Worn trim | Inventory icon |
|----------------------------------|:---:|:---:|:---:|
| Copper / Netherite Diving Helmet | ✅ | ✅ | ✅ |
| Copper / Netherite Diving Boots  | ✅ | ✅ | ✅ |
| Backtanks                        | ✅ | ✅ | ⚠️ |

⚠️ The backtank's *inventory icon* does not show a trim overlay — backtanks use a
3D block model, which vanilla's flat trim-overlay system cannot decorate. The trim still
renders correctly on the worn backtank.

## Dyeable visors

The copper and netherite diving helmets have a coloured band (a "visor") around the eyes.
This mod lets you recolour just that visor, leaving the metal of the helmet untouched.

- **Dye it** like leather armor: put a diving helmet and any dye(s) together in a crafting
  grid. Mix several dyes for blended colours.
- **Make it pulse** by crafting a dyed helmet together with an **amethyst shard**. The
  visor then slowly breathes through lighter and darker shades of its colour, and a
  *Pulsating* line appears in the tooltip.
- **Wash it clean** by right-clicking a water cauldron while holding the helmet. That
  removes the dye and the pulse.

Dyeing affects the worn helmet's visor only; the flat inventory sprite is unchanged.

## Requirements

- Minecraft **1.20.1**
- Forge **47.0.0** or newer — or NeoForge for 1.20.1, which loads the same jar
- Create **6.0.0** or newer

This is a client + server mod. Install it on both sides of a multiplayer setup.

## Installation

On Modrinth, just hit "Install". Otherwise, drop the jar into your `mods/` folder alongside
Create. No configuration needed.

## License

[Apache 2.0](LICENSE). Create is a separate project with its own license; this mod only depends on
it at runtime.
