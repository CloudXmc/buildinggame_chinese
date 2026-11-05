# BuildingGame v14.3.0 简体中文汉化版

基于上游 [stefvanschie/buildinggame](https://github.com/stefvanschie/buildinggame) `V 14.3.0` 的全面汉化。

## 下载

| 文件 | 说明 |
| --- | --- |
| `BuildingGame-14.3.0-zhCN.jar` | 主插件，放进服务端 `plugins/` 目录 |
| `BuildingGame-Addon-14.3.0-zhCN.jar` | BungeeCord 跨服附加组件 |
| `BuildingGame-BungeeCord-14.3.0-zhCN.jar` | BungeeCord 端插件 |

## 汉化范围

- `messages.yml`：4680 行、2386 条文本全量翻译（提示、计分板、告示牌、菜单物品名），键名与占位符原样保留
- Java 硬编码约 280 处：生物编辑菜单、`/bg` 命令说明与反馈、举报菜单、控制台日志
- `config.yml` 建造主题译为中文；`plugin.yml` 中文描述；确认框按钮

## 支持的服务器版本

Bukkit / Spigot / Paper / Purpur，**1.20 ~ 26.3**（1.20 / 1.20.1 / 1.20.2 / 1.20.3 / 1.20.4 / 1.20.5 / 1.20.6 / 1.21 ~ 1.21.11 / 26.1 / 26.2 / 26.3）。

## 注意事项

- 服务器上已有旧版英文配置时，请先删除 `plugins/BuildingGame/messages.yml` 再启动，否则不会应用中文。
- 主插件需要 Java 17+；各版本服务端按其自身要求选择 Java 版本。
