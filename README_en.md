# Carpet LSC Addition

> A Fabric Carpet extension: totem wrench, accurate block placement, fast leaf decay and 12 more rules.

[![Minecraft](https://img.shields.io/badge/minecraft-26.1--26.3-blue)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/fabric-%3E%3D0.19.5-blue)](https://fabricmc.net/)
[![Carpet](https://img.shields.io/badge/carpet-%3E%3D26.1-blue)](https://github.com/gnembon/fabric-carpet)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

[简体中文](README.md) | **English** (this page)

A server-side mod; players do not need to install it. All 15 rules are off by default and are
enabled one at a time with `/carpet <rule> <true|false>`. In `/carpet` they are grouped under the
`LSC` category. Installing the mod on a client is optional and only adds chat item icons.

## Basic information

| Item | Value |
| --- | --- |
| Minecraft | 26.1 – 26.3 |
| Fabric Loader | >= 0.19.5 |
| fabric-carpet | >= 26.1 |
| Java | >= 25 |
| Environment | Server-side (client optional) |
| Language | Simplified Chinese / English, follows `/carpet language` |

## Rules

| Option | Default | Description |
| --- | --- | --- |
| `AccurateBlockPlacement` | `false` | Accurate block placement protocol, from Carpet Extra. |
| `AfkMarker` | `false` | Adds an `[AFK]` prefix after five idle minutes. |
| `AlwaysUnlockedRecipes` | `false` | Unlocks every recipe when a player joins; existing unlocks are kept. |
| `BundleAutoEat` | `off` | Silent eating from a bundle: `off` disables it, `saturation` / `slot` pick the order. |
| `ChatHeldItem` | `false` | A `#` in chat is replaced by the held item; the client mod draws its icon. |
| `CommandPing` | `false` | Enables the `/ping` command. |
| `DaytimeSleep` | `false` | Players can sleep during the day, skipping to nightfall. |
| `DeathCoordinates` | `off` | Announces death coordinates; `self` or `broadcast`. |
| `EmeraldTemptsVillagers` | `false` | Villagers follow players holding an emerald. |
| `FastEating` | `false` | Cap for eating and drinking: `true` means 8 ticks, or pick `4` / `8` / `16`. |
| `FastLeafDecay` | `false` | Leaves decay layer by layer within a few seconds once their tree is chopped down. |
| `FletchingTable` | `false` | Right-click a fletching table to craft arrows; the slots only take arrows and arrow materials. |
| `HappyGhastBoost` | `off` | A firework rocket dashes the happy ghast you ride; levels `1` / `2` / `3`. |
| `InventoryTotem` | `false` | A totem of undying in the inventory prevents death. |
| `McdrCommandBridge` | `false` | Enables `/!!` so MCDR commands run as a command instead of chat. |
| `NoFarmlandTrample` | `false` | `true` never breaks farmland; a number allows that many tramples before it breaks. |
| `NoFarmlandTrampleScope` | `players` | Who the trample rule protects; `all` includes mobs. |
| `QuickPaintingSwap` | `false` | Sneak and right-click a painting to cycle to another painting of the same size. |
| `RightClickHarvest` | `false` | Empty-hand or crops/seeds in hand: right-click harvests and replants a grown crop. |
| `SkipCommandConfirm` | `false` | Privileged commands no longer ask for confirmation. |
| `TotemWrench` | `false` | Use a totem of undying as a wrench. |
| `TradePreview` | `false` | Trade preview: the backpack lists every item the villager will ever sell, and the header becomes All trades. |
| `VehicleFireworkBoost` | `off` | A firework rocket pushes the boat or minecart you ride along your look direction; levels `1` / `2` / `3`. |
| `VehicleInstantStop` | `off` | The vehicle stops when you get off; `all` / `minecart` / `boat`. |
| `VillagerBackpack` | `false` | Sneak and right-click a villager renamed with a name tag to open its backpack. |

### `TotemWrench`

Hold a totem of undying to operate on blocks. It applies to the same blocks as Carpet's
`flippinCactus` (pistons, observers, dispensers, repeaters, stairs, slabs, glazed terracotta,
pillars, rails, hoppers and other directional blocks).

| Hand holding the totem | Effect |
| --- | --- |
| Main hand | Right-clicking a block rotates that block in place. |
| Off hand | The facing of the block being placed is inverted. |

Independent of `flippinCactus`.

### `InventoryTotem`

| Totem position | Effect |
| --- | --- |
| Anywhere in the inventory | Consumed when the player would die. |
| Main hand / off hand | Same as vanilla. |

### `AccurateBlockPlacement`

The client (Litematica easy place, Tweakeroo flexible placement) encodes the desired block state
into the hit vector and the server honours it.

| Supported property | Values |
| --- | --- |
| Facing | all six directions |
| Axis | `x`, `y`, `z` |
| Half | `top`, `bottom` |
| Slab type | `top`, `bottom` |
| Repeater delay | `1` – `4` |
| Comparator mode | `compare`, `subtract` |

### `FastLeafDecay`

Breaking a log decays the leaves of that tree over a few seconds instead of wiping the whole
canopy in a single tick.

| Item | Behaviour |
| --- | --- |
| Start | Leaves touching the stump start dropping after about 0.2 s. |
| Spread | The wave travels outwards layer by layer, roughly 0.25 s per block, so a tree usually finishes in 1 – 3 s. |
| Ceiling | At most 6 s per break; a canopy larger than 4096 leaves is left entirely to vanilla rather than being torn down halfway. |
| Drops | Removed through vanilla's own leaf random tick, so drops, tool bonuses and statistics match vanilla. |

Leaves of other trees are unaffected, and player-placed persistent leaves are never destroyed.
Switching the rule off stops any decay that is still pending.

### `EmeraldTemptsVillagers`

Villagers follow a player holding an emerald, in the same way cows and sheep follow wheat. The
rule can be toggled at any time and takes effect immediately.

The follow speed is about 1.2× that of a wheat-tempted cow (measured: 3.2 blocks/s versus 2.8).

A villager that gets attacked runs away instead, and ignores emeralds for five seconds after being
hurt.

### `DaytimeSleep`

Players can get into a bed during the day and skip time to nightfall (the `night` time marker,
13000 ticks).

| Item | Behaviour |
| --- | --- |
| `players_sleeping_percentage` | Unchanged from vanilla; `0` means one sleeping player is enough. |
| Sleeping at night | Unchanged; everyone wakes up in the morning. |
| Dimensions without a day/night cycle | Unaffected. |

### `QuickPaintingSwap`

Sneaking while right-clicking a placed painting cycles it to the next painting of the **same
size**, ordered by variant id.

| Item | Behaviour |
| --- | --- |
| Trigger | Sneak + right-click a placed painting with the main hand. |
| Range | Only paintings with identical width and height, so the entity's size and position never change. |
| Order | Sorted by painting id, wrapping around. |
| Not sneaking | A plain right-click leaves the painting alone. |

### `McdrCommandBridge`

Enables `/!!` so an MCDReforged command is run as a command instead of being typed into chat,
which keeps the command text out of the chat box.

| Item | Behaviour |
| --- | --- |
| Usage | `/!! <MCDR command>`, for example `/!! ig check`. |
| Forwarding | Emits a `<player> !!<command>` line to the server log, which MCDR parses as player chat and dispatches. |
| Requires | MCDR's default `vanilla` handler and `command_prefix` still set to `!!`. |
| Without MCDR | The command runs but nothing answers it. |

### `ChatHeldItem`

A `#` in chat is replaced by the held item.

| Item | Behaviour |
| --- | --- |
| Trigger | `#`; a message may contain several. |
| Server and client | Draws the item's 16×16 icon where the name would be; hovering shows the complete item tooltip (count, durability, …). |
| Server only | Shows `[Item name]`, entirely in light purple, with the vanilla item tooltip on hover. |
| Empty hand | Shows `[Empty hand]`, also light purple. |
| Source | Main hand first, off hand when the main hand is empty. |

### `SkipCommandConfirm`

Commands that need elevated permissions run straight away, without the client's "Confirm Command
Execution" window.

| Item | Behaviour |
| --- | --- |
| Applies to | Commands that require additional permissions, such as admin commands. |
| Unchanged | The permissions themselves; a player without the level is still rejected by the server. |
| Does not affect | The "unrecognised command" prompt shown for a typo. |

### `AlwaysUnlockedRecipes`

Unlocks every recipe when a player joins. Recipes already unlocked are kept as they are, and turning
the rule off does not take anything back.

### `BundleAutoEat`

Eats one item out of a bundle when everything in that bundle is food and a single item is enough to
fill the hunger bar. Survival only; no eating sound or animation is played, and the food's own
effects still apply.

| Condition | Requirement |
| --- | --- |
| Game mode | Survival; creative and spectator never trigger it. |
| The bundle | Every item in it is food; a mixed bundle is left alone. |
| Hunger | Not full, and adding that food's nutrition reaches the full bar. |

| Value | Effect |
| --- | --- |
| `off` | Disabled. |
| `saturation` | Eats the lowest-saturation food first. |
| `slot` | Follows the bundle's storage order. |

Food with its own effects (golden apple, rotten flesh, chorus fruit, good or bad) always comes after
plain food, whatever the value.

### `FastEating`

| Value | Effect |
| --- | --- |
| `false` | Disabled. |
| `true` | Cap of 8 ticks. |
| `4` / `8` / `16` | Use that many ticks as the cap. |

Only items with a `CONSUMABLE` component are affected (food, potions, milk); bows, shields and other
use actions are untouched, and items that are already faster keep their own time.

### `NoFarmlandTrample`

| Value | Effect |
| --- | --- |
| `false` | Disabled, vanilla behaviour. |
| `true` | Farmland never turns into dirt. |
| `1` / `3` / `5` / `10` | The block breaks once it has been trampled more than that many times in a row. The streak resets after 60 seconds without a trample. |

### `FletchingTable`

Right-click a fletching table to open the arrow crafting screen. It reuses the vanilla anvil look, so
the client does not need Fabric API.

| Item | Behaviour |
| --- | --- |
| Title | Craft Arrows |
| The two input slots | Arrows, potions (regular / splash / lingering) and glowstone dust - the materials that put an effect on arrows; plain arrows cannot be made here |
| Result slot | Follows the vanilla recipes, e.g. 8 arrows + 1 lingering potion -> 8 tipped arrows |
| Rename row | Display only, not editable |

Needs the mod on both the client and the server.

## Installation

1. Install Fabric Loader 0.19.5 or newer, plus Fabric API for your Minecraft version.
2. Server: put `fabric-carpet` and this mod in the server's `mods` directory.
3. Client (optional): put this mod in the client's `mods` directory to get the item icons; it needs
   Fabric API (which loads mod resources) but not `fabric-carpet`.

### `VillagerBackpack`

Sneak and right-click a villager that has been renamed with a name tag to open its backpack. It is a
plain vanilla chest screen, so a client without this mod still sees it.

| Row | Contents |
| --- | --- |
| Chest column (from column 2) | The villager's own eight slots, which can be taken from and put into |
| Ender chest column (columns 7-8) | The current trades, eight per page, paged, duplicates merged |
| Middle column | Profession (workstation), trade level (bottle o' enchanting), price situation (emerald / totem / ominous bottle) |
| Bottom | A barrier closes the screen; page arrows appear only when the trades need more than one page |

Only items that villager would pick up on its own can go in (vanilla `wantsToPickUp`); the three icon
slots are read-only.

## Commands

| Command | Description |
| --- | --- |
| `/ping` | Show your latency to the server. Requires `commandPing`. |
| `/!! <command>` | Forward an MCDR command to MCDR. Requires `mcdrCommandBridge`. |

## Configuration

Rules are set with `/carpet` and `config/carpet-lsc-addition.conf`; see the Rules table above.

## Notes

- The rules themselves only run on the server, and vanilla clients can join normally. A client
  that installs this mod additionally gets the `chatHeldItem` icons; it does not need
  `fabric-carpet`.
- `accurateBlockPlacement` needs Litematica or Tweakeroo on the client; the other rules have no
  client requirement.
- `totemWrench` rotating does not cause block updates, matching `flippinCactus`.
- `mcdrCommandBridge` depends on MCDR's default `vanilla` handler and the `!!` prefix; without
  MCDR the rule does nothing useful.
- `chatHeldItem` has the server write the item name into the message with the vanilla `show_item`
  hover; a client with this mod swaps that text for the 16×16 icon (drawn by the client, the
  server only sends text), while a client without the mod keeps the light purple item name. The
  icon uses a font shipped by the mod, so a client without Fabric API falls back to the text.
- Singleplayer works too: put this mod and fabric-carpet in the client's `mods` folder and the
  integrated server runs the same logic. `mcdrCommandBridge` is the exception, since there is no
  MCDR in singleplayer.
- Without cheats enabled in singleplayer the commands are never sent to the client, so
  `skipCommandConfirm` cannot remove the "unrecognised command" prompt there.

## Building

```bash
./gradlew build
```

Carpet is a compile-time dependency resolved from `libs/`. Put the Carpet jar there as
`libs/fabric-carpet.jar`, or pass it explicitly:

```bash
./gradlew build -Pcarpet_jar=/path/to/fabric-carpet-26.3.jar
```

## License

MIT
