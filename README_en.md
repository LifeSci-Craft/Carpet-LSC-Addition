# Carpet LSC Addition

> A Fabric Carpet extension: totem wrench, accurate block placement, fast leaf decay and 4 more rules.

[![Minecraft](https://img.shields.io/badge/minecraft-26.1--26.3-blue)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/fabric-%3E%3D0.19.5-blue)](https://fabricmc.net/)
[![Carpet](https://img.shields.io/badge/carpet-%3E%3D26.1-blue)](https://github.com/gnembon/fabric-carpet)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

[简体中文](README.md) | **English** (this page)

A server-side mod; players do not need to install it. All 7 rules are off by default and are
enabled one at a time with `/carpet <rule> <true|false>`. In `/carpet` they are grouped under the
`LSC` category.

## Basic information

| Item | Value |
| --- | --- |
| Minecraft | 26.1 – 26.3 |
| Fabric Loader | >= 0.19.5 |
| fabric-carpet | >= 26.1 |
| Java | >= 25 |
| Environment | Server-side |

## Rules

| Option | Default | Description |
| --- | --- | --- |
| `totemWrench` | `false` | Use a totem of undying as a wrench. |
| `inventoryTotem` | `false` | A totem of undying in the inventory prevents death. |
| `accurateBlockPlacement` | `false` | Accurate block placement protocol, from Carpet Extra. |
| `commandPing` | `false` | Enables the `/ping` command. |
| `fastLeafDecay` | `false` | Leaves decay quickly once their tree is chopped down. |
| `emeraldTemptsVillagers` | `false` | Villagers follow players holding an emerald. |
| `daytimeSleep` | `false` | Players can sleep during the day, skipping to nightfall. |

### `totemWrench`

Hold a totem of undying to operate on blocks. It applies to the same blocks as Carpet's
`flippinCactus` (pistons, observers, dispensers, repeaters, stairs, slabs, glazed terracotta,
pillars, rails, hoppers and other directional blocks).

| Hand holding the totem | Effect |
| --- | --- |
| Main hand | Right-clicking a block rotates that block in place. |
| Off hand | The facing of the block being placed is inverted. |

Independent of `flippinCactus`.

### `inventoryTotem`

| Totem position | Effect |
| --- | --- |
| Anywhere in the inventory | Consumed when the player would die. |
| Main hand / off hand | Same as vanilla. |

### `accurateBlockPlacement`

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

### `fastLeafDecay`

Breaking a log decays the leaves of that tree immediately. Leaves of other trees are unaffected,
and player-placed persistent leaves are never destroyed.

### `emeraldTemptsVillagers`

Villagers follow a player holding an emerald, in the same way cows and sheep follow wheat. The
rule can be toggled at any time and takes effect immediately.

### `daytimeSleep`

Players can get into a bed during the day and skip time to nightfall (the `night` time marker,
13000 ticks).

| Item | Behaviour |
| --- | --- |
| `players_sleeping_percentage` | Unchanged from vanilla; `0` means one sleeping player is enough. |
| Sleeping at night | Unchanged; everyone wakes up in the morning. |
| Dimensions without a day/night cycle | Unaffected. |

## Installation

1. Install Fabric Loader 0.19.5 or newer, plus Fabric API for your Minecraft version.
2. Put `fabric-carpet` and this mod in the server's `mods` directory.

## Commands

| Command | Description |
| --- | --- |
| `/ping` | Show your latency to the server. Requires `commandPing`. |

## Configuration

Rules are set with `/carpet` and `config/carpet-lsc-addition.conf`; see the Rules table above.

## Notes

- The mod is server-side only. Clients do not need it, and vanilla clients can join normally.
- `accurateBlockPlacement` needs Litematica or Tweakeroo on the client; the other rules have no
  client requirement.
- `totemWrench` rotating does not cause block updates, matching `flippinCactus`.

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
