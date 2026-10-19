# Carpet LSC Addition

> Fabric Carpet 拓展模组：不死图腾扳手、精准放置协议、树叶快速腐烂等 11 条规则。

[![Minecraft](https://img.shields.io/badge/minecraft-26.1--26.3-blue)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/fabric-%3E%3D0.19.5-blue)](https://fabricmc.net/)
[![Carpet](https://img.shields.io/badge/carpet-%3E%3D26.1-blue)](https://github.com/gnembon/fabric-carpet)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

**简体中文**（本页） | [English](README_en.md)

服务端模组，玩家无需安装。11 条规则默认全部关闭，用 `/carpet <规则> <true|false>` 逐个开启，
在 `/carpet` 里归入 `LSC` 分类。

## 基本信息

| 项目 | 值 |
| --- | --- |
| Minecraft | 26.1 – 26.3 |
| Fabric Loader | >= 0.19.5 |
| fabric-carpet | >= 26.1 |
| Java | >= 25 |
| 运行环境 | 服务端 |
| 语言 | 简体中文 / English，跟随 `/carpet language` 切换 |

## 规则

| 选项 | 默认值 | 说明 |
| --- | --- | --- |
| `totemWrench` | `false` | 把不死图腾当作扳手使用。 |
| `inventoryTotem` | `false` | 放在背包里的不死图腾也能防止死亡。 |
| `accurateBlockPlacement` | `false` | 精准放置协议，移植自 Carpet Extra。 |
| `commandPing` | `false` | 启用 `/ping` 指令。 |
| `fastLeafDecay` | `false` | 砍倒树木后，该树的树叶在数秒内逐层腐烂。 |
| `emeraldTemptsVillagers` | `false` | 手持绿宝石的玩家会吸引村民。 |
| `daytimeSleep` | `false` | 玩家可以在白天睡觉，时间跳到夜晚。 |
| `quickPaintingSwap` | `false` | 潜行右键已放置的画，切换成同尺寸的其他画作。 |
| `mcdrCommandBridge` | `false` | 启用 `/!!` 把 MCDR 指令交给 MCDR 处理。 |
| `chatHeldItem` | `false` | 聊天中的 `#` 替换成手持物品。 |
| `skipCommandConfirm` | `false` | 需要更高权限的指令不再弹确认窗。 |

### `totemWrench`

手持不死图腾操作方块，作用范围与 Carpet 的 `flippinCactus` 相同（活塞、观察者、发射器、
中继器、楼梯、台阶、带釉陶瓦、原木柱、铁轨、漏斗等带朝向的方块）。

| 持有图腾的手 | 效果 |
| --- | --- |
| 主手 | 右键点击方块，转动该方块。 |
| 副手 | 反转即将放置的方块朝向。 |

与 `flippinCactus` 相互独立。

### `inventoryTotem`

| 图腾位置 | 效果 |
| --- | --- |
| 背包任意位置 | 玩家即将死亡时消耗该图腾。 |
| 主手 / 副手 | 与原版一致。 |

### `accurateBlockPlacement`

客户端（Litematica 简易放置、Tweakeroo 灵活放置）把目标方块状态编码进命中向量，服务端按编码结果放置。

| 支持的属性 | 取值 |
| --- | --- |
| 朝向 | 六个方向 |
| 轴 | `x`、`y`、`z` |
| 上半 / 下半 | `top`、`bottom` |
| 半砖类型 | `top`、`bottom` |
| 中继器延迟 | `1` – `4` |
| 比较器模式 | `compare`、`subtract` |

### `fastLeafDecay`

破坏原木后，该树的树叶会在数秒内腐烂，而不是一瞬间整片消失。

| 项目 | 行为 |
| --- | --- |
| 起始 | 紧挨着树桩的树叶约 0.2 秒后开始掉落。 |
| 扩散 | 由内向外逐层推进，每远一格约延迟 0.25 秒，整棵通常 1 – 3 秒完成。 |
| 上限 | 单次最多 6 秒；树冠超过 4096 块树叶时整棵交给原版处理，避免半途中断留下残缺树冠。 |
| 掉落物 | 走原版树叶随机刻，掉落物、工具加成、统计与原版一致。 |

其他树木的树叶不受影响，玩家放置的持久树叶不会被清除。规则中途关闭会立即停止尚未处理的腐烂。

### `emeraldTemptsVillagers`

村民会被手持绿宝石的玩家吸引，类似手持小麦吸引牛羊。规则可随时切换，立即生效。

跟随速度约为被小麦吸引的牛的 1.2 倍（实测 3.2 方块/秒对 2.8 方块/秒）。

村民被攻击后会优先逃跑，受伤后 5 秒内不再被绿宝石吸引。

### `daytimeSleep`

玩家可以在白天上床睡觉，时间跳到夜晚（`night` 时间标记，13000 刻）。

| 项目 | 行为 |
| --- | --- |
| `players_sleeping_percentage` | 与原版一致；为 `0` 时 1 名玩家睡觉即可。 |
| 夜晚睡觉 | 与原版一致，所有人醒于次日清晨。 |
| 无昼夜循环的维度 | 不受影响。 |

### `quickPaintingSwap`

潜行时右键已放置的画，按画作 ID 顺序切换到下一幅**同尺寸**的画。

| 项目 | 行为 |
| --- | --- |
| 触发 | 潜行 + 主手右键已放置的画。 |
| 可切换范围 | 只切换宽高完全相同的画，因此画的体积与位置都不变。 |
| 顺序 | 按画作 ID 排序后循环。 |
| 未潜行 | 不潜行时右键不会改变画作。 |

### `mcdrCommandBridge`

启用 `/!!` 指令，把参数交给 MCDReforged 处理，避免以聊天方式输入 `!!` 指令时指令内容出现在聊天栏。

| 项目 | 行为 |
| --- | --- |
| 用法 | `/!! <MCDR 指令>`，例如 `/!! ig check`。 |
| 转发方式 | 在服务端日志打出一条 `<玩家名> !!<指令>`，由 MCDR 按玩家聊天解析并派发。 |
| 前置 | MCDR 使用默认 `vanilla` 处理器，且 `command_prefix` 为 `!!`。 |
| 未装 MCDR | 指令能执行，但不会有任何响应。 |

### `chatHeldItem`

聊天内容中的 `#` 会替换成当前手持物品，悬停可查看完整物品信息。

| 项目 | 行为 |
| --- | --- |
| 触发字符 | `#`，一条消息可出现多次。 |
| 显示 | `[物品名]`，整体使用亮紫色以区别于普通聊天。 |
| 悬停 | 原版 `show_item`，显示完整物品提示框。 |
| 空手 | 显示 `[空手]`，同样为亮紫色。 |
| 取值 | 优先主手，主手为空时取副手。 |

### `skipCommandConfirm`

执行需要更高权限的指令时，客户端不再弹出「确认执行指令」窗口，直接执行。

| 项目 | 行为 |
| --- | --- |
| 生效范围 | 需要额外权限的指令（例如管理员指令）。 |
| 不改变的 | 权限本身；无权限时仍会被服务端拒绝。 |
| 不影响 | 指令拼写错误时的「无法识别指令」提示。 |

## 安装

1. 安装 Fabric Loader 0.19.5 或更高版本，以及与 Minecraft 版本对应的 Fabric API。
2. 把 `fabric-carpet` 与本模组放入服务端的 `mods` 目录。

## 命令

| 命令 | 说明 |
| --- | --- |
| `/ping` | 查看自己到服务器的延迟。需要 `commandPing`。 |
| `/!! <指令>` | 把 MCDR 指令转发给 MCDR。需要 `mcdrCommandBridge`。 |

## 配置

规则通过 `/carpet` 与 `config/carpet-lsc-addition.conf` 设置，选项见上方「规则」表。

## 注意

- 本模组只在服务端生效，玩家客户端无需安装，原版客户端可正常进入服务器。
- `accurateBlockPlacement` 需要客户端安装 Litematica 或 Tweakeroo 并开启对应功能；其他规则无客户端要求。
- `totemWrench` 的方块转动不触发方块更新，与 `flippinCactus` 一致。
- `mcdrCommandBridge` 依赖 MCDR 的默认 `vanilla` 处理器与 `!!` 前缀；未装 MCDR 时该规则无实际作用。
- `chatHeldItem` 用原版 `show_item` 呈现物品，客户端无需安装模组，但不会显示物品图标——原版聊天文本只能渲染字体里的字形，内联物品图标必须由客户端模组绘制。
- 单机游戏中同样生效：把本模组与 fabric-carpet 放进客户端 `mods` 目录即可，集成服务端会执行相同的逻辑。`mcdrCommandBridge` 例外，单机没有 MCDR，该规则无作用。
- 单机未开启作弊时指令不会下发给客户端，`skipCommandConfirm` 无法消除「无法识别指令」提示。

## 构建

```bash
./gradlew build
```

Carpet 为编译期依赖，从 `libs/` 解析。把 Carpet 的 jar 放为 `libs/fabric-carpet.jar`，或显式指定：

```bash
./gradlew build -Pcarpet_jar=/path/to/fabric-carpet-26.3.jar
```

## License

MIT
