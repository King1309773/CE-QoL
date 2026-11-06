# 合并来源

## 1.4.4：附带 Just Enough Resources

- 原作者为 way2muchnoise。使用 [CurseForge 正式版 1.12.2-0.9.2.60](https://www.curseforge.com/minecraft/mc-mods/just-enough-resources-jer/files/2728585)，对应上游 [`1ca8376`](https://github.com/way2muchnoise/JustEnoughResources/tree/1ca8376)。
- 合并包保留上游的原始类、资源、Forge 模组身份、JEI 插件和访问转换器。JER 作为独立组件与 CE QoL 同包安装。上游许可原文随包提供，位于 `META-INF/licenses/jer/LICENSE.md`。

## 1.2.0：本地增强

2026-10-31，在 1.1.1 基线上增加按键搜索索引、重铸文本缓存和验证式保存。AutoFish 处理器增加本地低耐久保护和只读状态查询，另加独立选项、状态 HUD 与设置页；默认剩余耐久阈值为 10。此版本处理器不再只是标识符映射，收抛状态机仍由原 33 项回归验证，保护和状态有新增用例。原作归属与许可保持，均非上游官方发布。

## 1.1.0：加入 AutoFish

2026-10-28，用户要求加入当前使用的自动钓鱼修复版。

- 原作：NorthWestWind，项目 https://github.com/North-West-Wind/AutoFish ，历史来源提交 `a6a57e150b484353d913a46219b28351215e4183`。
- 集成基线：本地 AutoFish 1.0.4-stablefix.2，SHA-256 `119BF5B68DE35564779BFD6A84C4ABE165A4D37610243A870DC4F8CD72CFB179`；稳定钓鱼补丁形成于 2026-10-19。
- 稳定处理器只进行 20 个经 stable39 对照的 SRG→MCP 标识符替换，流程全文一致。重混淆后继续运行原 33 项真实处理器回归。
- 保留原类命名空间、格式化按键说明（包括末尾格式重置）、默认键码 43 和分类，避免丢失已有绑定。仅加入处理器、按键和受保护的初始化辅助类，没有合入旧独立入口、代理或计时线程。
- 上游当前提供 GPLv3；历史指定提交和原 JAR 没有附许可证文件。交付保留官方许可证原文及准确归属说明，路径为 `src/main/resources/META-INF/licenses/autofish/`，AutoFish 对应源码随本项目源码一起提供。此记录不把 AutoFish 原作归于 KingJJ。

## 1.0.0：原四项功能

2026-10-28，用户选定四项自制客户端功能；原项目不被改写。

- Reforging Quality Display 1.2.1：本地原始源码，没有独立 Git 仓库。
- Visual Controls 1.1.0：原项目 `King1309773/VisualControls`，源提交 `da4266a4420ff97f5ba07fdf706435245d36d9a7`。
- SimpleDifficulty Temperature HUD 1.6.1：原项目 `King1309773/SDTemperatureHUD`，源提交 `7e417da`。
- Thirst Preview 1.1.4：原项目 `King1309773/ThirstPreview`，源提交 `c95d3152ce22a9b34c261783e350ab13c3a522f2`。

原功能类和资源保留原包名/命名空间；四个独立 `@Mod` 入口移除，统一由 CEQoL 分派生命周期。温度自动配置与配置事件归属变为 `ceqol`，文件名仍为 `sdtemperaturehud`；补水显式读取原 `thirstpreview.cfg`。原品质量化、图标/横条计算、温度采样与按键绑定逻辑保留。

新加入统一配置入口，链接已有温度和按键界面，并提供原补水开关。原四个 JAR 和第三方依赖均不作为嵌套 JAR 或第三方类打包。

补水设置保存会保留其他键的值，并恢复三个自有开关的说明文字；不承诺逐字保留 Forge 配置解析器无法往返的任意原始注释。

温度模块的十项旧独立发行身份/恢复基线测试退役，新增一项统一配置归属和旧文件名检查，净减九项。功能行为测试保留；统一发行身份改由最终 JAR 检查和实际 Forge 启动验证。
