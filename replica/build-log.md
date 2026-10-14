# Build log

Skipped: /replica-design (user chose to skip). The build skill's web steps (HTML, 390 and 1440px, screenshots) do not apply to a native Android app, so they were replaced by engine unit tests.

| item | date | status | missing | notes |
| --- | --- | --- | --- | --- |
| M0 repo, CI | 2026-10-14 | partial | Android app module, APK build in CI | `gradle :engine:test` workflow written. Gradle compile verified offline here. The workflow itself has not run, and JUnit could not be downloaded here. |
| Engine: curves, keyframes, tracks | 2026-10-14 | done | advanced easing types (bounce, elastic, cyclic, random, steps) | Bezier solved with Newton and bisection fallback. Overshoot works. |
| Engine: layers, project, parenting | 2026-10-14 | done | groups, masks, effects, elements | Parent loops rejected. Removing a parent clears its children's link. |
| Engine: evaluator (draw list per time) | 2026-10-14 | done | camera objects, group transforms | Parent convention is After Effects style (child is relative to parent's position, rotation and scale, not its anchor). To verify against the original on your own account. |
| Undo and redo | not started | | | next |
| S01 to S14 (all screens) | not started | | | needs the Android app module |

Tests: 43 passed (Curve 8, Track 12, Project 8, Evaluator 15) via a throwaway JUnit shim. Two deliberate bugs (flipped rotation sign, removed loop check) were each caught by the tests.

features.csv left unchanged: no feature exists in the app yet, only in the engine. Rows turn to `yes` or `partial` once a screen uses them.

Harder than expected: nothing so far. Bigger than it looked: the Android shell (GL preview, then `MediaCodec` export) is the real risk, and it cannot be compiled or run in this sandbox, so those steps will need your phone-to-GitHub loop for feedback.
