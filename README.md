# CE QoL / 便捷增强

Minecraft Forge 1.12.2 客户端模组，当前版本 1.1.0 将以下五项功能整合到一个模组入口和一个安装文件：

| 功能 | 合并来源 | 使用方式 |
|---|---|---|
| 重铸台品质显示 | Reforging Quality Display 1.2.1 | 打开 Quality Tools 重铸台，品质和效果在右侧常驻显示 |
| 可视化按键 | Visual Controls 1.1.0 | 控制设置中的可视化按键入口，或 CE QoL 配置页 |
| 温度 HUD | SD Temperature HUD 1.6.1 | 显示体温、分析、预警，配置页调整布局和外观 |
| 补水预览 | Thirst Preview 1.1.4 | 物品提示只显示整滴/半滴，手持时预览口渴条恢复 |
| 自动钓鱼 | AutoFish 1.0.4 本地 stablefix2 | 持竿按原快捷键启停，手动抛第一杆后自动收竿、再抛竿 |

补水提示没有点数、倍率、“补水：”前缀或数字回退。保留 RS 底部信息、文字列表提示、已核验 GraveStone 横条和原生水滴条的适配。

## 安装

环境为 Minecraft 1.12.2、Forge 14.23.5.2860、Java 8。保留 SimpleDifficulty 0.3.9、QualityTools 1.0.7 及它们原有的依赖。

从 CE QoL 1.0.0 升级：退出游戏，备份并移出旧 CE QoL 和独立的 `forgeautofish` JAR，再安装 `ceqol-1.1.0.jar`。

首次从独立版切换：将 `reforgingquality`、`visualcontrols`、`sdtemperaturehud`、`thirstpreview`、`forgeautofish` 五个 JAR 移出 `mods` 并备份，再安装合并版。不能同时保留被合并的独立版或多个 CE QoL 版本。只需客户端安装。

继续使用现有的 `config/sdtemperaturehud.cfg`、`config/thirstpreview.cfg`，无需重新设置温度布局、颜色和补水开关。按键绑定继续使用原版设置文件，包括 AutoFish 的原按键名称和绑定。模组列表中只显示一个 CE QoL 条目，点击配置可进入统一设置页。

品质面板沿用原容器、槽位和重铸处理，不改品质概率、材料消耗或服务端规则。DebrisClient 自动重铸仍为可选适配；其开关由 DebrisClient 自身管理。

卸载时移出合并版，恢复备份的旧合并版与 AutoFish，或恢复五个独立 JAR。配置文件可保留。温度 HUD 的地图集成继续按实际安装的 Xaero 模组启用，编译依赖没有打入合并版。

## 自动钓鱼

默认使用反斜杠键启停，已有自定义绑定继续沿用。统一设置页的“自动钓鱼快捷键”会直接筛选出对应按键，方便修改；它不是启动钓鱼的按钮。

先手动抛第一杆。只在自己的活鱼漂附近收到原版真正咬钩声后收竿，至少等待十次客户端更新并确认旧鱼漂消失，再抛一次。普通入水和鱼漂下沉不会触发；没有新鱼漂时不连续重试。

保留 stablefix2 的关闭、换物品、死亡、断线和会话变更时的清理逻辑；不使用后台延迟线程。依赖原版咬钩声音事件，替换该事件的模组仍可能影响识别。AutoFish 原作者为 NorthWestWind，本地稳定补丁及整合另行注明；来源和上游许可原文见 SOURCE_MODULES.md 与 `META-INF/licenses/autofish/`。

## 补水图标范围

2 点对应一滴，1 点对应半滴。当前目标整合包全部 133 条消费规则为 1～20 点，可完整显示；自定义绝对值超过 20 点时最多十滴，零值为空滴，负值为红色整/半轮廓。图标行无法安全容纳时跳过，不显示数字。

完全绕过 Forge 物品提示生成/绘制事件的界面、任务名称标签和流体条目仍需另行适配。联机客户端和服务端应使用相同的补水规则。

## 构建

使用 JDK 8。将构建引用放入 `libs/`：SimpleDifficulty 0.3.9、QualityTools 1.0.7、XaeroLib 1.7.3、Xaero Minimap 26.6.0、Xaero World Map 1.47.0，文件名与 `build.gradle` 一致。它们只用于编译引用，不会合入成品。

```powershell
.\gradlew.bat test build --console=plain
```

可通过 `-PpackRules=<consumableThirst.json绝对路径>` 和 `-PgravestoneJar=<GraveStone-1.0.0.jar绝对路径>` 指定目标规则与适配指纹测试。没有可选指纹文件时，不能将跳过视为已验证对应环境。

自动钓鱼的 33 项真实处理器回归单独运行 `verification/autofish/run-tests.ps1`，直接验证重混淆后的成品；边界测试桩不会进入正常 Gradle 类路径或生产 JAR。

来源与合并调整见 `SOURCE_MODULES.md`，实际验证范围见 `VERIFICATION.md`。
