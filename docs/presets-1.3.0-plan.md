# CE QoL 1.3.0 settings presets (2026-10-31)

User requested the previously proposed named settings presets, import/export and per-feature defaults. Preserve the installed 1.2.0 until validation completes.

Scope: temperature layout/appearance/visibility/reminders/map display, hydration's three display switches, fishing HUD/protection preferences. Exclude key bindings, active fishing state, gameplay rules and other mods. A preset may include all three groups or one group. Capture existing legacy temperature layout representation without forcing migration.

- [x] Add strict schema/JSON codec, complete included groups, supported version, typed/ranged finite numbers, enums/colors, duplicate-key rejection and size/depth limits.
- [x] Named local presets with Chinese names, validated path names, atomic export into exports/, import from imports/ that only stores validated JSON, explicit overwrite confirmation.
- [x] Apply selected preset with confirmation; preserve unknown cfg values and current settings on failure. Back up raw affected cfg files and settings before applying, support one-step undo. Refresh Forge's cached temperature Configuration after writes and verify persistence after later GUI saves.
- [x] Feature-scoped defaults (temperature/hydration/fishing/all) through the same application path; no key reset or activation of fishing. Add a unified presets/settings screen with clear selection/scope/status and confirmation dialogs.
- [x] RED/GREEN tests for roundtrip, types/ranges/unknown/missing/duplicate keys, import non-application, per-group reset, overwrite/path rejection, failed apply rollback and undo.
- [x] Java 8 full build and original fishing regressions, independent review, isolated Forge client UI/apply/undo/restart persistence checks, Chinese and English captures, no user saves.
- [x] Update docs, package source/JAR, back up installed 1.2.0 and configs, install only the tested 1.3.0 artifact. No GitHub publishing.

Storage: config/ceqol/{presets,imports,exports,preset-backups}. No automatic profile application at startup. Import is separate from application. Cross-file commits support rollback on detected failure; no claim of power-loss atomicity across files.
