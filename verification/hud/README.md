# Isolated Forge HUD integration harness

`TooltipSmoke.java` is a test-only Forge mod, excluded from the normal Gradle source sets and production JAR. It extends the existing tooltip/config/preset integration checks with actual unified editor input, failed-save retry, scoped persistence, cache and restart checks. Do not install this test mod into a real pack instance: it deliberately writes sentinel configurations and automatically exits the isolated client.

Compile the normal project first, then run `compile-harness.ps1` with JDK 8, a fresh fixture directory, mapped Forge 1.12.2-14.23.5.2860, the actual runtime classpath, generated MCP-to-SRG mappings and SpecialSource 1.8.3. Launch that fixture using the normal Forge launch wrapper with an offline identity, asset/native paths and only CE QoL, SimpleDifficulty, QualityTools, Refined Storage and their required libraries. Do not copy accounts, real saves or real user options into the fixture.

The fixture baseline uses temperature Scale 1.25/BackgroundAlpha 173, hydration tooltip=true/HUD=false/details=false and fishing reserve=10. Its test consumption rules provide apple=1, melon=3, bread=0, carrot=-3 and potato=40. The initial language is selected through the isolated `options.txt`. Supply `-Dsmoke.captures=<existing absolute folder>` for PNG evidence.

Successful full runs finish with:

```
SMOKE_HUD_CANCEL_RESIZE_PASS
SMOKE_HUD_SAVE_PASS
SMOKE_HUD_CACHE_PASS
SMOKE_HUD_LIFECYCLE_PASS
SMOKE_COMPLETE ceqol 1.4.0 HUD cache and existing client integration
```

Run a second full client process with the same isolated settings plus `-Dsmoke.restartCheck=true`; it compares every captured preset setting against `hud-expected.json` and finishes with `SMOKE_HUD_RESTART_PASS exactAllSettings=true`. Repeat full and restart runs separately for `en_us` and `zh_cn`.

The intentional blocked save logs an error before retry; the success markers and absence of `SMOKE_FAILED` distinguish it from an unexpected failure. Neither these checks nor cached presentation identity assertions measure pack FPS or exercise real-world continuous fishing.
