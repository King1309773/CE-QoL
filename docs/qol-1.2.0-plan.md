# CE QoL 1.2.0 plan (2026-10-31)

User approved the three recommended optimizations plus fishing status and durability protection.

Keep legacy config paths and key identities; hydration tooltips stay icons-only. Implement on feature/qol-1.2.0, retaining 1.1.1 artifacts and installation until final checks pass.

- [x] Separate visual-controls filtering from registry/conflict rebuilding. Cache translated search text until registry refresh; preserve Forge context/modifier conflict semantics.
- [x] Cache quality text and fitted layout for an unchanged slot snapshot and context (player/world/language/font/size); invalidate on GUI/resource reload and NBT changes. Retain original container and reforge handler.
- [x] Add a verified configuration writer: stage next to target, preserve other values, validate serialization, back up previous bytes, replace; no-op preserves timestamp. Update live hydration settings only after success; keep draft on failure and show a retry/error state.
- [x] Add AutoFish settings in ceqol.cfg: HUD visibility, low-durability guard (default on), remaining durability reserve (default 10, 1-100), HUD offsets. Preserve existing temp/thirst files. Add a settings panel, keeping entry 5 for the legacy filtered binding editor and adding entry 6 for fishing settings.
- [x] Show one small, movable-by-offset fishing status line while enabled or stopped by low durability, respecting F1, menus and spectator/creative visibility. Derive status from handler state. All packets stay on CLIENT END ticks. Check remaining durability before any automated reel/recast; disable, clear pending actions and notify once on low durability. Do not automatically retrieve or switch rods. First cast remains manual.
- [x] RED/GREEN unit tests, final Java 8 build, original 33 fishing regressions plus boundary/damage-during-delay protection cases, isolated client checks and screenshots. No user saves or real accounts.
- [x] Review, document limits, package 1.2.0, back up and replace installed 1.1.1 only after artifact/config fingerprint checks. No GitHub publishing requested.

Durability protection reads the item's exposed vanilla durability; unbreakable/undamageable rods bypass it. Mod-specific hidden consumption cannot be predicted. No FPS percentage claims without measurement.
