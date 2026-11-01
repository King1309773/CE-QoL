# CE QoL 1.4.0 HUD editing and presentation cache

**Goal:** Reduce repeated temperature/fishing text measurement and provide one editor for both temperature panels and fishing status, retaining existing config and preset formats.

**Architecture:** Keep sampled temperature data and AutoFish processing unchanged. Cache measured presentations by immutable values, settings and a font/language/resource generation context; draw warning animations each frame. A unified draft editor reuses the existing temperature layout model and a shared fishing position model. Commit changed layout fields through the existing preset transaction, capturing other settings at save time.

**Scope:** Temperature compact/analysis panels and fishing status. Hydration preview remains anchored to its existing thirst bar. Fishing font scale remains unchanged. No new automation, preset management or configuration migration.

- [x] Add failing tests for value-equal temperature snapshots and presentation cache invalidation, including cached nulls and context/settings changes.
- [x] Implement and integrate bounded caches in actual temperature measurement and fishing text generation; invalidate on font/language/Unicode/bidi/resource reload changes.
- [x] Add failing tests for shared fishing position constraints, snap geometry and scoped layout commits preserving unrelated current settings.
- [x] Implement unified editor with drag, selection, temperature wheel scaling, alignment guides/snapping, selected reset, explicit Save/Cancel, Esc discard and resize retention. Keep legacy editor API for existing callers; route all visible entries to unified editor.
- [x] Add main configuration entry, route existing temperature editor launcher, suppress duplicate temperature overlay and discard drafts on world/screen lifecycle changes. Add Chinese/English help.
- [x] Run complete build, exact production AutoFish regression, real isolated Forge GUI/cache/save/cancel/failure/resize/resource reload checks in both languages. Visually inspect captures.
- [x] Complete independent code review, document actual verification, package artifacts, replace the installed v涛.3 JAR with backup and unchanged user settings, and commit.

Save merges only explicitly changed panel positions/scales into current settings. A fish-only move does not rewrite temperature; a temperature-only move does not rewrite fishing. Failed save keeps the editor/draft and existing transaction backups. Closing/replacing/disconnecting never auto-saves. No cache stores worlds, players, item stacks or unbounded history.
