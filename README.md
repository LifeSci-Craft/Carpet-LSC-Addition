# Carpet LSC Addition

> Fabric Carpet 拓展模组：不死图腾扳手、精准放置协议、树叶快速腐烂等 7 条规则。

[![Minecraft](https://img.shields.io/badge/minecraft-26.1--26.3-blue)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/fabric-%3E%3D0.19.5-blue)](https://fabricmc.net/)
[![Carpet](https://img.shields.io/badge/carpet-%3E%3D26.1-blue)](https://github.com/gnembon/fabric-carpet)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

**简体中文**（本页） | [English](README_en.md)

服务端模组，玩家无需安装。7 条规则默认全部关闭，用 `/carpet <规则> <true|false>` 逐个开启，
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

### `daytimeSleep`

玩家可以在白天上床睡觉，时间跳到夜晚（`night` 时间标记，13000 刻）。

| 项目 | 行为 |
| --- | --- |
| `players_sleeping_percentage` | 与原版一致；为 `0` 时 1 名玩家睡觉即可。 |
| 夜晚睡觉 | 与原版一致，所有人醒于次日清晨。 |
| 无昼夜循环的维度 | 不受影响。 |

## 安装

1. 安装 Fabric Loader 0.19.5 或更高版本，以及与 Minecraft 版本对应的 Fabric API。
2. 把 `fabric-carpet` 与本模组放入服务端的 `mods` 目录。

## 命令

| 命令 | 说明 |
| --- | --- |
| `/ping` | 查看自己到服务器的延迟。需要 `commandPing`。 |

## 配置

规则通过 `/carpet` 与 `config/carpet-lsc-addition.conf` 设置，选项见上方「规则」表。

## 注意

- 本模组只在服务端生效，玩家客户端无需安装，原版客户端可正常进入服务器。
- `accurateBlockPlacement` 需要客户端安装 Litematica 或 Tweakeroo 并开启对应功能；其他规则无客户端要求。
- `totemWrench` 的方块转动不触发方块更新，与 `flippinCactus` 一致。

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
