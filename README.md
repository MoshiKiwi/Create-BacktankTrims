# Create: Backtank Trims

A small standalone [NeoForge](https://neoforged.net/) mod that lets you apply **armor trims**
to [Create](https://modrinth.com/mod/create)'s diving gear and backtanks in a smithing table —
and makes those trims actually render on the worn equipment!

I got annoyed after the [Create issue #6213](https://github.com/Creators-of-Create/Create/issues/6213) never got fixed, and just did it myself.

## The problem

Create's copper/netherite diving helmets, diving boots and backtanks are armor, but Create
explicitly opts them out of the `minecraft:trimmable_armor` tag. As a result:

- the smithing table refuses to trim them, and
- even if a trim is forced onto them (Applied before conversion, NBT editors, Almost Unified, …), the trim never renders.

## What this mod does

- **Re-enables trimming** for all six pieces by re-adding them to the `trimmable_armor` tag.
  The mod loads *after* Create, so this addition wins over Create's removal.
- **Renders the trim on the netherite backtank.** The five other pieces render through
  vanilla's armor layer, so their trims appear automatically once trimmable. The netherite
  backtank is drawn by Create's own custom renderer, which skips the vanilla trim pass — so
  the mod adds a dedicated render layer that draws the trim decal over the diving suit.
- **Adds inventory trim icons** for the four flat diving items (helmets and boots), so a
  trimmed piece shows its trim overlay in inventories and tooltips.

### Covered pieces

| Item | Smithing table | Worn trim | Inventory icon |
|------|:---:|:---:|:---:|
| Copper / Netherite Diving Helmet | ✅ | ✅ | ✅ |
| Copper / Netherite Diving Boots  | ✅ | ✅ | ✅ |
| Copper Backtank                  | ✅ | ✅ | ✅ |
| Netherite Backtank               | ✅ | ✅ | ⚠️ |

⚠️ The netherite backtank's *inventory icon* does not show a trim overlay — backtanks use a
3D block model, which vanilla's flat trim-overlay system cannot decorate. The trim still
renders correctly on the worn backtank.

## Requirements

- Minecraft **1.21.1**
- NeoForge **21.1.0** or newer
- Create **6.0.0** or newer

This is a client + server mod. Install it on both sides of a multiplayer setup.
(client for the renderer, server for the crafting recipe)


## Installation

On Modrinth, just hit "install" ! If you're not using Modrinth, just drop the jar into your `mods/` folder alongside Create. No configuration needed.

## License

[MIT](LICENSE). Create is a separate project with its own license; this mod only depends on
it at runtime.
