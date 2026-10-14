# Build log

Skipped: /replica-design (user chose to skip). The build skill's web steps (HTML, 390 and 1440px, screenshots) do not apply to a native Android app, so they were replaced by engine unit tests.

| item | date | status | missing | notes |
| --- | --- | --- | --- | --- |
| M0 repo, CI | 2026-10-14 | done | | Both jobs (engine tests, debug APK) green on GitHub, run 38547907773. APK artifact is `motionlab-debug-apk`. |
| Engine: curves, keyframes, tracks | 2026-10-14 | done | advanced easing types (bounce, elastic, cyclic, random, steps) | Bezier solved with Newton and bisection fallback. Overshoot works. |
| Engine: layers, project, parenting | 2026-10-14 | done | groups, masks, effects, elements | Parent loops rejected. Removing a parent clears its children's link. |
| Engine: evaluator (draw list per time) | 2026-10-14 | done | camera objects, group transforms | Parent convention is After Effects style (child is relative to parent's position, rotation and scale, not its anchor). To verify against the original on your own account. |
| Undo and redo | not started | | | next |
| Render math (QuadTransform) | 2026-10-14 | done | | Layer matrix to GL clip space, 5 tests. |
| App shell + GL preview of a built-in demo project (S04 preview only) | 2026-10-14 | compiles in CI, not yet seen on a device | project is hard coded, no editing, no timeline | Play/pause, time slider, loops. CI compiled it successfully. Whether it draws correctly on a real phone is still unconfirmed. |
| S01 to S14 (the real screens) | not started | | | next: S01 project list, S03, timeline |

Tests: 48 passed (Curve 8, Track 12, Project 8, Evaluator 15, QuadTransform 5) via a throwaway JUnit shim. Two deliberate bugs (flipped rotation sign, removed loop check) were each caught by the tests.

features.csv left unchanged: no feature exists in the app yet, only in the engine. Rows turn to `yes` or `partial` once a screen uses them.

Harder than expected: nothing so far. Bigger than it looked: the Android shell (GL preview, then `MediaCodec` export) is the real risk, and it cannot be compiled or run in this sandbox, so those steps will need your phone-to-GitHub loop for feedback.
