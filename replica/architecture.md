# Architecture: a layers-and-keyframes motion editor for Android (a rebuild of Alight Motion's core loop)

Personal use. Local-first, no server, no accounts, no network calls. The skill's default web stack (Next.js, Supabase, Stripe, Resend, Vercel) does not apply, so none of it is used. Everything below is for a native Android app built by GitHub Actions.

## Stack

| layer | choice | why |
| --- | --- | --- |
| language | Java 17 (Kotlin allowed later, it interoperates) | the language you already ship in, no new tooling to learn |
| app structure | two Gradle modules: `:engine` (pure Java, no Android imports) and `:app` | the engine can be unit tested in CI with no device or emulator |
| UI | classic Android Views plus custom Views for the timeline and the curve editor | both are custom drawing and touch handling, and Jetpack Compose needs Kotlin |
| preview | OpenGL ES 3.0 on a `GLSurfaceView` | compositing many layers with transforms and opacity at playback speed needs the GPU |
| export | the same GL renderer drawing into a `MediaCodec` encoder input Surface, muxed with `MediaMuxer` | one renderer for preview and export, so what you see is what you export |
| video and audio decode | `MediaExtractor` plus `MediaCodec` (milestone 2) | built in, no dependency |
| database | SQLite through Room (schema in `schema.sql`) | structured project data, transactions, no server needed |
| media files | copied into app-private storage per project on import | the picker's temporary access disappears, a copy never breaks |
| picking files | Android Photo Picker and the system file picker | no storage permission prompts |
| saving exports | `MediaStore` (Movies folder) | no permission needed on Android 10 and later |
| background work | a foreground service for export | export must survive the screen turning off |
| auth, payments, email, hosting | none | not needed for a personal local app |
| build and CI | GitHub Actions: `assembleDebug`, engine unit tests, APK as a downloadable artifact | you have no PC and build from the phone, so CI is the build machine |
| min SDK | 29 (Android 10) | no legacy storage permissions, hardware encoders are more reliable. Raise or lower later if needed. |

Rejected on purpose: a cross-platform framework (Expo, Flutter) because real-time GL compositing and `MediaCodec` access are native work anyway. Media3 Transformer was also considered. It is the faster route to a basic video editor, but it is not built around per-layer keyframes with custom easing curves (check its current docs before ruling it out for good), so this plan has the clone own its renderer.

## Schema

Tables: 9 (`project`, `media`, `layer`, `effect_instance`, `mask`, `track`, `keyframe`, `bookmark`, `export_job`). Full SQL in `replica/schema.sql`, tested against SQLite 3.45 (loads clean, bad inserts are rejected).

Access rules: none. It is one user's data in the app's private storage. The skill's owner column and row level security do not apply. Integrity is enforced in the database with foreign keys, check constraints and unique indexes.

Differences from the recon data model, and why:
- A `Curve` is not its own table. The easing for the motion between two keyframes is stored on the first keyframe (`curve_type`, four Bezier numbers `c1x c1y c2x c2y`, and `curve_params`). Copying a curve is copying those columns.
- Every animatable value is one numeric `track` with a `path` (`position.x`, `opacity`, `fill.r`...). Vectors and colours are split into scalar channels so one keyframe table handles everything. A track belongs to exactly one layer, effect or mask.
- An Element is a `project` row with `kind = 'element'`. A layer of type `element` points at it through `nested_project_id`, so linked editing falls out for free: edit the element and every project that uses it changes.
- Times are integer microseconds from the project start, so 30 and 60 fps frame times are exact.
- Timestamps are epoch milliseconds in UTC. SQLite has no `timestamptz`.

Rules the database cannot enforce, so the engine does and tests:
- No cycles in the parent chain (only the self-parent case is blocked in SQL).
- A parent and its child must be in the same project.
- Element nesting cannot loop (an element inside itself).

Interpolation, so the engine and the curve editor agree: between keyframes A and B, `u = (t - A.time) / (B.time - A.time)`, `e = curve_A(u)`, `value = A.value + (B.value - A.value) * e`. Bezier curves solve x for time and return y. A y outside 0..1 is overshoot.

## API

This app has no web routes. The equivalent is the set of actions the UI calls on the engine and the services. One row per action.

| action | does | who | input | output | flow |
| --- | --- | --- | --- | --- | --- |
| `ProjectRepo.list` | projects newest first | S01 | none | project summaries | F01 |
| `ProjectRepo.create` | new project | S03 | name, width, height, fps | project id | F01 |
| `ProjectRepo.duplicate / delete` | copy or remove a project and its media | S01 | project id | none | F01 |
| `MediaImporter.importMedia` | copy a picked file into project storage and read its size and duration | S05 | uri, kind | media row | F01 |
| `LayerOps.add` | add a layer of a type | S05 | type, content, media id | layer id | F01 |
| `LayerOps.moveTrim` | change start, end and order on the timeline | S04 | layer id, start, end, z | none | F01 |
| `LayerOps.setParent` | link a layer to a parent or to none, rejecting cycles | S07 | child id, parent id or null | ok or error | F05 |
| `TrackOps.setValue` | change a value, creating or updating a keyframe when keyframing is on | S06 | layer id, path, value, time | none | F02 |
| `TrackOps.toggleKeyframe` | add or remove a keyframe at the playhead | S06 | track id, time | none | F02 |
| `KeyframeOps.setCurve` | set the easing from a keyframe to the next | S08 | keyframe id, curve | none | F03 |
| `KeyframeOps.copyCurve / pasteCurve / pasteToAll` | reuse a curve | S08 | keyframe ids | none | F03 |
| `EffectOps.add / remove / reorder / setEnabled` | manage effects on a layer | S09, S10 | layer id, effect type | effect id | F04 |
| `Playback.play / pause / seek` | move the playhead | S04 | time | none | F02, F03 |
| `Renderer.renderFrame` | evaluate the scene at a time and draw it | S04, export | project, time, target surface | pixels | F02, F03, F06 |
| `PreviewView.setZoom / pan` | zoom and pan the preview | S11 | scale, offset | none | F07 |
| `UndoStack.undo / redo` | step through edit history | S04 | none | none | all |
| `ExportService.start` | render and encode in a foreground service | S12 | project id, settings | job id | F06 |
| `ExportService.cancel` | stop a running export | S13 | job id | none | F06 |
| `ElementOps.saveAsElement / insert` | turn a group into a reusable element, or place one | S02, S05 | group id or element id | element id | F08 |

Total: 19 rows (a few rows cover two or three closely related calls).

Webhooks in and out: none.

Jobs:
- Autosave: write changes to SQLite in one transaction about a second after the last edit, and again on pause.
- Export: foreground service, so it survives the screen turning off. On Android 15 and later use the media processing foreground service type (check the current Android docs when you build it).
- Thumbnails: regenerate a project's thumbnail when it is closed.
- Cleanup: on launch, delete media files that no row points to.

## The parts that bite

- Preview speed: this is the problem the original's reviewers complain about (lag, crashes). Preview renders at reduced resolution, export at full. The scene is evaluated into a flat draw list per frame so no allocations happen while playing.
- One renderer, two targets: preview and export must share the draw code. Two code paths drift apart and the export stops matching the preview.
- Texture memory: shape and text layers are drawn to bitmaps and uploaded as textures. Keep an LRU cache with a memory cap, re-rasterize a layer only when its content changes or it scales past the cached size.
- GL context loss: the context is lost when the app is backgrounded. Everything on the GPU must be rebuildable from the model.
- Video decoding limits (milestone 2): phones decode only a few videos at once in hardware. Seeking lands on keyframes, so scrubbing needs a policy. Decide that before building video layers.
- Export on real devices: H.264 encoders often require even width and height and limit resolution and profile. HEVC support varies. Check capabilities with `MediaCodecList` before offering a setting. MP4 has no alpha channel.
- Audio: decode, mix and re-encode to AAC in sync with video time. Easy to get a drifting offset.
- Time: microseconds everywhere internally, and snap to frames only at the edges (UI and export).
- Undo: one drag is one undo step. Record commands, not snapshots. Rebuild the engine state from the database after a crash.
- Gesture conflicts: two-finger pan and zoom only when no layer is selected, otherwise the same gestures edit the layer. The input handler needs an explicit mode, not guesses.
- Process death: a long export must resume or fail cleanly. Mark the job `failed` on service restart rather than leaving it `running`.
- Heat: long exports throttle the phone. Lower the default export bitrate and expose the quality slider.
- Not needed: time zones, money, idempotency, multi-tenancy, deliverability, GDPR deletion, rate limits. There is no server.

Performance targets, to tune on your own phone (they are goals, not measured):
- Preview holds 30 fps with 10 simple layers at half resolution.
- A 60 second 1080p30 export of shapes and images finishes without running out of memory.
- Opening a project with 50 layers takes under 2 seconds.

## Build order

0. Setup. A repo with the two modules, a GitHub Actions workflow that builds a debug APK and runs the engine unit tests, and an empty app that installs. No screens.
1. Vertical slice (proves the stack). S01, a minimal S03 (fixed 1080 by 1920 at 30 fps, no choices yet) and S04. Tables: `project`, `layer`, `track`, `keyframe`. Actions: `ProjectRepo.create`, `LayerOps.add` (rectangle only), `TrackOps.setValue` for position, `Playback`, `Renderer.renderFrame` and a bare `ExportService.start`. The test: add a rectangle, set two position keyframes, play it, export an MP4 and watch it.
2. Must-haves (features.csv): create-project options (S03), project thumbnails, text and image layers, then video and audio layers (`media`, `MediaImporter`), transform on the canvas, trim and move on the timeline, opacity, keyframes on scale, rotation and opacity, export settings (S12) and the result (S13), `UndoStack`. Engine unit tests grow with each one.
3. Should-haves: curve editor and easing presets (S08, `KeyframeOps`), colour keyframes, solid and gradient fills, stroke and shadow (`effect_instance`), groups, masks (`mask`), parenting (S07), copy and paste of layers and curves, preview pan and zoom (S11), custom fonts, a few basic effects (S09, S10).
4. Could-haves: overshoot and the advanced easing types, null objects and parenting helper modes, bookmarks, extract audio, elements (S02, S14, `ElementOps`), extra export formats, cameras, motion blur. Rigging and the full effects library are skipped.
5. Fixes from replica-entrepreneur: optional, since this is not for sale. The one place the review complaints (crashes, lag, steep learning curve) matter is the performance targets above, which already guide steps 1 to 3.

Riskiest three parts: (1) real-time GL preview with many layers, (2) `MediaCodec` export behaving across devices, (3) the touch timeline and its gesture rules.

Next: `/replica-design`.
