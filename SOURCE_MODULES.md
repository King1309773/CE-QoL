# 合并来源

2026-10-28，用户选定四项自制客户端功能；原项目不被改写。

- Reforging Quality Display 1.2.1：本地原始源码，没有独立 Git 仓库。
- Visual Controls 1.1.0：原项目 `King1309773/VisualControls`，源提交 `da4266a4420ff97f5ba07fdf706435245d36d9a7`。
- SimpleDifficulty Temperature HUD 1.6.1：原项目 `King1309773/SDTemperatureHUD`，源提交 `7e417da`。
- Thirst Preview 1.1.4：原项目 `King1309773/ThirstPreview`，源提交 `c95d3152ce22a9b34c261783e350ab13c3a522f2`。

原功能类和资源保留原包名/命名空间；四个独立 `@Mod` 入口移除，统一由 CEQoL 分派生命周期。温度自动配置与配置事件归属变为 `ceqol`，文件名仍为 `sdtemperaturehud`；补水显式读取原 `thirstpreview.cfg`。原品质量化、图标/横条计算、温度采样与按键绑定逻辑保留。

新加入统一配置入口，链接已有温度和按键界面，并提供原补水开关。原四个 JAR 和第三方依赖均不作为嵌套 JAR 或第三方类打包。

补水设置保存会保留其他键的值，并恢复三个自有开关的说明文字；不承诺逐字保留 Forge 配置解析器无法往返的任意原始注释。

温度模块的十项旧独立发行身份/恢复基线测试退役，新增一项统一配置归属和旧文件名检查，净减九项。功能行为测试保留；统一发行身份改由最终 JAR 检查和实际 Forge 启动验证。
