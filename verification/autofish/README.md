# AutoFish real-handler regression

The unchanged 33-case stable-fishing suite executes AutoFishHandler from the selected reobfuscated JAR. Boundary stubs are compiled in a separate test JVM; they are never part of Gradle source sets, production classes or a Minecraft launch.

```powershell
.\verification\autofish\run-tests.ps1 -ModJar .\build\libs\ceqol-1.1.0.jar -JavaHome <JDK8-directory>
```

Checks cover genuine bite sounds, ignored splash/motion signals, single reel, ten-tick recast delay, hook acknowledgments, timeouts, cancellation, repeated cycles and absence of worker threads. Source and test provenance: local AutoFish stablefix2, based on NorthWestWind's AutoFish 1.0.4.
