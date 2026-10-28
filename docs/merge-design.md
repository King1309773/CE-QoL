# CE QoL 合并设计

用户明确选择四项功能：重铸台品质显示、按键可视化、温度 HUD、补水显示。目标是一份客户端 JAR 和一个 Forge 模组入口。

- 基于 Reforging Quality Display 1.2.1、Visual Controls 1.1.0、SD Temperature HUD 1.6.1 与 Thirst Preview 1.1.4 的本地源码整合，不嵌套原 JAR。
- 统一身份 `ceqol`，保留各模块资源命名空间，保留 Quality Tools 的原容器和重铸处理，不改变消费规则。
- 温度配置归属迁移到统一身份，文件名继续为 `sdtemperaturehud.cfg`；补水继续读取 `thirstpreview.cfg`；按键继续保存到原版设置。
- 统一配置入口链接温度配置、可视化按键、补水开关；品质面板在重铸台自动生效。
- 补水提示保持仅水滴图标、无前缀、无数字回退，保留 RS 兼容与手持预览。维持 postInit 的 HUD 监听顺序。
- 依赖仍为 SimpleDifficulty 0.3.9 与 QualityTools 1.0.7。地图集成依赖只编译引用，不打包第三方模组。
- 验证单独安装四个旧模组时统一入口断言失败，再验证合并版唯一入口、旧配置、各界面入口和水滴绘制。完整构建回归已有行为测试。退出游戏后备份原四个 JAR，再安装同一已验证成品。
