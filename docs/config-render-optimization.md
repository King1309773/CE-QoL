# CE QoL 1.1.1 configuration and render optimization

Date: 2026-10-31 (Asia/Shanghai).

The user requested improvements to both configuration experience and performance/compatibility. Keep the existing configuration paths, key identities, hydration icon-only presentation, and all five modules.

Use a working copy for the hydration settings screen. Toggling controls affects only that copy; Save commits once and returns, Cancel/Escape discards it. Reinitializing the screen after a resize retains the copy. Keep the three existing toggle IDs; add Cancel on the same footer row. Explain each control with localized hover text. Do not automatically save on screen closure. The temperature editor already has a working copy and retains its behavior.

Saving unchanged hydration properties must not rewrite the file. Reload before saving to retain unrelated values; retain owned property descriptions. Continue using thirstpreview.cfg rather than migrating to a new file.

Only resolve held hydration for the horizontal HUD after the optional adapter has captured an eligible frame. Within one held-item selection, do not resolve the same object twice after a non-hydrating active item; preserve active/main/offhand precedence. Reuse the client listener's decimal formatter. Do not cache hydration values across frames, alter SimpleDifficulty rules, or modify AutoFish timings.

## Implementation and validation

- [x] Add failing regression cases for unchanged-file saves, draft apply/discard, optional-frame readiness, and duplicate held-object resolution.
- [x] Implement the working copy, footer/hover translations, conditional property writes, and render guards.
- [x] Run focused regression RED (29 cases, 7 expected failures), then full Java 8 test/build GREEN (437 cases) with actual pack rules and GraveStone fingerprint.
- [x] Run the 33 AutoFish regressions against the final reobfuscated JAR (33 passed).
- [x] Reuse an isolated Forge client fixture to check Save/Cancel/Escape/resize and unchanged existing container tooltips; no user saves loaded (17 captures).
- [x] Update release/verification notes, package sources and screenshots, back up and replace only the installed CE QoL JAR after verifying hashes. Preserve actual configs/options.

Compare results to 1.1.0; make no FPS claim without a gameplay benchmark. No new GitHub publication is included.
