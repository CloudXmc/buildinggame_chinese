## 简介

感谢你愿意为这个项目出一份力，我们很感谢你抽时间来帮助改进本项目。
为了让贡献过程尽量简单、愉快，请先阅读以下指引。

## 基本规则

在做出贡献之前，请确保你的贡献符合以下基本规则：

### 关于 Issue 的规则

* 确认你使用的是最新版本，很可能问题已经被修复了。
* 提交新的 issue 之前，先检查是否已经有与你相同的 issue。
* 不要把你的 issue 发到别人的 issue 里，请保持条理清晰，单独开一个新 issue。
* 不要为已过时的 Minecraft 版本寻求支持，我们不会为它们添加适配。
* Issues 区只用于反馈实际的 bug 或代码问题，不要用作求助区。需要支持请前往
[SpigotMC](https://www.spigotmc.org/threads/building-game.83557/) 和 [DevBukkit](https://dev.bukkit.org/projects/buildinggame#comments)。

### 关于 Pull Request 的规则

* 确保你的 Pull Request 对所有用户都有价值，而不只对你自己有用。如果你想添加只服务于你或你服务器的功能，
请考虑维护自己的 fork，或使用[插件 API](https://github.com/stefvanschie/buildinggame/wiki/API)。
* 确保没有人在你之前提交过相同的 Pull Request。
* 如果你的 Pull Request 是针对某个 issue 的，请同时附上该 issue 的链接。
* 遵循本仓库中 `style_guide` 文件所列的代码规范。
* 提交前请检查并修复 IDE 给出的所有警告和错误。

## 新手指引

如果你想为项目做贡献，但不知道从哪里入手，可以看看
[当前已打开的 issue](https://github.com/stefvanschie/buildinggame/issues)。总会有人需要你的帮助。

如果你想提交涉及配置项的 Pull Request（`config.yml` 或 `messages.yml`），强烈建议先看看
[这个类](https://github.com/stefvanschie/buildinggame/blob/master/buildinggame/src/com/gmail/stefvanschiedev/buildinggame/managers/files/SettingsManager.java)。
简而言之，所有配置项都由它统一管理：不需要再折腾 `config#isSet` 或 `config#addDefault`，这些都已经替你处理好了。
你只需要把新配置项加进默认的 `config.yml` 或 `messages.yml`，然后就可以继续写代码了。

## Pull Request 入门

想修复某个 issue 时，请先确认该 issue 尚未指派给别人；如果已指派，对方会来处理。另外，当你对项目已经比较熟悉之后，
请把带有 "good first issue" 标签的 issue 留给经验较少的贡献者，除非确实没有其他 issue 可选。

带 "good first issue" 标签并不代表它必须是你第一个处理的 issue。如果你觉得自己对项目还不够熟悉，同样可以选择这类 issue。

想修复某个 issue 时，请先留言说明你要处理它，以便我们把它指派给你。这样可以避免多人重复修复同一个问题。

如果你的 Pull Request 超过几行（比如超过 10 行），请先 fork 本仓库，在 fork 中完成改动。较小的改动
（文档修改、拼写/语法修正、添加注释、常量取值错误、补充日志等）可以直接提交，无需另行 fork。

## 提交 Issue

提交 issue 时会展示一个模板。请不要忽略它，而是按要求填写相关信息。模板底部留有空间，供你补充其他必要信息。
该模板面向提交 bug 的用户；如果你不是报告 bug，而是建议改进代码、指出拼写错误等，可以忽略模板。
