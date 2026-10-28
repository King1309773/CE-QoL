# CE QoL / 便捷增强

Minecraft Forge 1.12.2 客户端模组，将以下四项功能整合到一个模组入口和一个安装文件：

| 功能 | 合并来源 | 使用方式 |
|---|---|---|
| 重铸台品质显示 | Reforging Quality Display 1.2.1 | 打开 Quality Tools 重铸台，品质和效果在右侧常驻显示 |
| 可视化按键 | Visual Controls 1.1.0 | 控制设置中的可视化按键入口，或 CE QoL 配置页 |
| 温度 HUD | SD Temperature HUD 1.6.1 | 显示体温、分析、预警，配置页调整布局和外观 |
| 补水预览 | Thirst Preview 1.1.4 | 物品提示只显示整滴/半滴，手持时预览口渴条恢复 |

补水提示没有点数、倍率、“补水：”前缀或数字回退。保留 RS 底部信息、文字列表提示、已核验 GraveStone 横条和原生水滴条的适配。

## 安装

环境为 Minecraft 1.12.2、Forge 14.23.5.2860、Java 8。保留 SimpleDifficulty 0.3.9、QualityTools 1.0.7 及它们原有的依赖。

退出游戏，将原来的 `reforgingquality`、`visualcontrols`、`sdtemperaturehud`、`thirstpreview` 四个 JAR 移出 `mods` 并备份，再安装 `ceqol-1.0.0.jar`。合并版和这四个独立版不能同时安装。只需客户端安装。

继续使用现有的 `config/sdtemperaturehud.cfg`、`config/thirstpreview.cfg`，无需重新设置温度布局、颜色和补水开关。按键绑定继续使用原版设置文件。模组列表中只显示一个 CE QoL 条目，点击配置可进入统一设置页。

品质面板沿用原容器、槽位和重铸处理，不改品质概率、材料消耗或服务端规则。DebrisClient 自动重铸仍为可选适配；其开关由 DebrisClient 自身管理。

卸载时移出合并版，恢复备份的四个独立 JAR 即可。配置文件可保留。温度 HUD 的地图集成继续按实际安装的 Xaero 模组启用，第三方依赖没有打入合并版。

## 补水图标范围

2 点对应一滴，1 点对应半滴。当前目标整合包全部 133 条消费规则为 1～20 点，可完整显示；自定义绝对值超过 20 点时最多十滴，零值为空滴，负值为红色整/半轮廓。图标行无法安全容纳时跳过，不显示数字。

完全绕过 Forge 物品提示生成/绘制事件的界面、任务名称标签和流体条目仍需另行适配。联机客户端和服务端应使用相同的补水规则。

## 构建

使用 JDK 8。将构建引用放入 `libs/`：SimpleDifficulty 0.3.9、QualityTools 1.0.7、XaeroLib 1.7.3、Xaero Minimap 26.6.0、Xaero World Map 1.47.0，文件名与 `build.gradle` 一致。它们只用于编译引用，不会合入成品。

```powershell
.\gradlew.bat test build --console=plain
```

可通过 `-PpackRules=<consumableThirst.json绝对路径>` 和 `-PgravestoneJar=<GraveStone-1.0.0.jar绝对路径>` 指定目标规则与适配指纹测试。没有可选指纹文件时，不能将跳过视为已验证对应环境。

来源与合并调整见 `SOURCE_MODULES.md`，实际验证范围见 `VERIFICATION.md`。
