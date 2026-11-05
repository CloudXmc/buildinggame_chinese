# Building Game <a href="https://discord.gg/5AJEGXG"><img align="right" src="https://img.shields.io/discord/389784128700153868?logo=Discord&logoColor=white&label=Discord" alt="Discord guild"></a>

围绕随机主题限时搭建、再互相打分的竞技小游戏：先投票选出主题 → 限时建造 → 逐一参观所有人的作品并打分 → 分数最高者获胜。

> 详细配置文档见官方 [Wiki](https://github.com/stefvanschie/buildinggame/wiki)（英文）。

> **简体中文汉化版** — 本仓库的全面汉化由 **Lzasn** 完成。
> 原始项目：<https://github.com/stefvanschie/buildinggame>，许可证 **Unlicense**（公有领域，可自由修改与再分发）。

## 支持的服务器版本

适用于 **Bukkit / Spigot / Paper / Purpur** 等服务端（纯原版不行），支持以下版本：

| Minecraft 版本 | NMS 实现模块 |
| --- | --- |
| 1.20 / 1.20.1 | `v1_20_0_1` |
| 1.20.2 | `v1_20_2` |
| 1.20.3 | `v1_20_3` |
| 1.20.4 | `v1_20_4` |
| 1.20.5 | `v1_20_5` |
| 1.20.6 | `v1_20_6` |
| 1.21 / 1.21.1 | `v1_21_0_1` |
| 1.21.2 / 1.21.3 | `v1_21_2_3` |
| 1.21.4 | `v1_21_4` |
| 1.21.5 | `v1_21_5` |
| 1.21.6 / 1.21.7 / 1.21.8 | `v1_21_6_8` |
| 1.21.9 / 1.21.10 | `v1_21_9_10` |
| 1.21.11 | `v1_21_11` |
| 26.1 / 26.2 / 26.3 | `v26` |

共 20 个版本号、15 套 NMS 实现，全部打包在同一个 jar 内。范围外的版本会在启动时抛出「当前服务器版本不受支持」。

## 安装

1. 把 `BuildingGame-14.3.0-zhCN.jar` 放进服务端 `plugins/` 目录。
2. 启动服务器，插件会在 `plugins/BuildingGame/` 生成中文配置文件。
3. 若服务器上已有旧版英文配置，**请先删除 `plugins/BuildingGame/messages.yml`** 再启动，否则插件会沿用旧的英文副本。

## 快速上手

搭建场地（需 OP，按顺序执行）：

```
/bg createarena <竞技场名>          创建竞技场
/bg setlobby <竞技场名>             站在大厅位置执行
/bg setspawn <竞技场名>             站在地块出生点执行，每执行一次生成一块地
/bg setbounds <竞技场名> <地块ID>    手持魔杖点击地块两个对角，设定边界
/bg setfloor <竞技场名> <地块ID>     同上，设定地板
/bg setmainspawn                    设置服务器主出生点
```

时间与人数：

```
/bg settimer <竞技场> 300      建造时间（秒）
/bg setvotetimer <竞技场> 60   投票时间
/bg setwintimer <竞技场> 15    结算展示时间
/bg setlobbytimer <竞技场> 30  大厅等待时间
/bg setminplayers <竞技场> 2   最少开局人数
/bg setmatches <竞技场> 3      比赛场数
/bg setgamemode <竞技场> solo  游戏模式（solo / team）
```

玩家：

```
/bg join <竞技场>    加入
/bg leave            离开
/bg vote <1-10>      参观时打分
/bg stats            查看战绩
/bg spectate <玩家>   旁观
```

游戏内直接输入 `/bg` 可看到全部命令，说明文字均为中文。

## 汉化范围

**本次汉化由 Lzasn 完成，覆盖全部玩家可见文本。**

- **`messages.yml`**：4680 行、2386 条文本全量翻译，涵盖游戏内提示、计分板、告示牌、菜单物品名。键名、`%player%` 等占位符与 `&` 颜色码全部原样保留。
- **Java 硬编码约 280 处**：60 多个生物编辑菜单（猫的毛色、熊猫性格、狼的变种、铁傀儡破损度、马纹样、鹦鹉类型、村民职业…）、`/bg` 全部命令说明与反馈提示、举报菜单、竞技场与地块提示、控制台启动日志。
- **`config.yml`**：建造主题列表（`subjects`）译为中文，并补充中文注释。
- **`plugin.yml`**：中文描述与仓库信息。
- **`gui/confirmationmenu.xml`**：确认框的接受 / 拒绝按钮。
- **`addon` / `bungeecord` 模块**：提示与日志。

其中约 20 处为跨行字符串拼接（如 `"Activated " + name + " for " + sec + " seconds"`），因中英文语序不同整段重写过，而非逐词替换。

**有意保留英文的部分**：SQL 语句、配置键名、材质枚举、反射方法名、`@Contract` 注解字符串、品牌名（GameCube / Instagram / YouTube 等），以及 PlaceholderAPI 返回的 `true` / `false`（改了会导致占位符解析失败）。

## 从源码构建

不同模块位于不同目录：主插件在 `/buildinggame`，BungeeCord 部分在 `/bungeecord`，附加组件在 `/addon`。

### BungeeCord / Addon

进入对应目录后直接执行：

```bash
mvn clean package
```

### Building Game 主插件

需要先初始化 NMS 模块（1.20.1 ~ 1.20.4 走 paper-nms 插件，会自动从 Paper 拉取并重映射）：

```bash
mvn paper-nms:init -pl buildinggame/nms/1_20_0-1
mvn paper-nms:init -pl buildinggame/nms/1_20_2
mvn paper-nms:init -pl buildinggame/nms/1_20_3
mvn paper-nms:init -pl buildinggame/nms/1_20_4
```

1.20.5 ~ 26.2 的 NMS 依赖需要用 Spigot 官方 **BuildTools** 预编译（每个版本约几分钟到十几分钟）：

```bash
wget https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar -O BuildTools.jar

java -jar BuildTools.jar --rev 1.20.6 --remapped --disable-java-check
java -jar BuildTools.jar --rev 1.21.1 --remapped --disable-java-check
java -jar BuildTools.jar --rev 1.21.3 --remapped --disable-java-check
java -jar BuildTools.jar --rev 1.21.4 --remapped --disable-java-check
java -jar BuildTools.jar --rev 1.21.5 --remapped --disable-java-check
java -jar BuildTools.jar --rev 1.21.8 --remapped --disable-java-check
java -jar BuildTools.jar --rev 1.21.10 --remapped --disable-java-check
java -jar BuildTools.jar --rev 1.21.11 --remapped --disable-java-check
java -jar BuildTools.jar --rev 26.2 --remapped --disable-java-check
```

完成后构建整个项目：

```bash
mvn clean package
```

> 汉化版已在 **JDK 25 + Maven 3.9** 下完整编译通过（16 个模块全部 SUCCESS），产物位于 `dist/`。
> 另有一条不需要 BuildTools 的捷径：从 CodeMC 镜像拉取 `remapped-obf` jar 与 Mojang 映射表，用 SpecialSource 自行正向重映射后装入本地仓库（详见开发笔记）。

## 许可

遵循原项目的 **Unlicense**（公有领域）。可自由使用、修改、商用，无需署名；汉化部分同样不作任何担保。

---

非官方 Minecraft 产品，未获 Mojang 或 Microsoft 批准，亦与之无关联。
