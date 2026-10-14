# Recon map: Alight Motion (Android)

Scope: core loop only. Layers, timeline, keyframes and easing, preview, MP4 export. Effects are a thin slice. Cameras, rigging, the effects library, membership and cloud sharing are out.
For: personal use (the user's own app, not for sale)
Date: 2026-10-14

> Evidence note: this map is built from public pages only. No click-through of a real account was done, so the screen list, the project-creation screen and click counts are partly inferred. Rows marked "to verify" should be confirmed on your own Alight Motion account (Step 2 of "Next steps" below). Terms of service were not reviewed in detail. Nothing from a logged-in session was used.

## Sources

| # | source | URL | notes |
| --- | --- | --- | --- |
| 1 | Google Play listing | https://play.google.com/store/apps/details?id=com.alightcreative.motion | feature list, 4.0 stars from about 1.25M reviews, 100M+ downloads, contains ads, min 1.5GB RAM |
| 2 | App Store listing | https://apps.apple.com/us/app/alight-motion/id1459833443 | same feature list, subscription tiers |
| 3 | marketing site | https://alightmotion.com | pitch, export formats (H.264/HEVC MP4, GIF, PNG sequence), project sharing |
| 4 | help center home | https://support.alightmotion.com | six categories |
| 5 | Animation Easing Curves | https://support.alightmotion.com/hc/en-us/articles/10536934703889-Animation-Easing-Curves | curve editor, presets, advanced types, overshoot, copy/paste curve |
| 6 | Layer Parenting and Null Objects | https://support.alightmotion.com/hc/en-us/articles/10536997444369-Layer-Parenting-and-Null-Objects | parent button, inheritance, null objects, Parenting Helper |
| 7 | Elements: The Complete Guide | https://support.alightmotion.com/hc/en-us/articles/10536791122449-Elements-The-Complete-Guide | saved reusable groups, linked editing, sharing |
| 8 | Preview Pan and Zoom | https://support.alightmotion.com/hc/en-us/articles/10536990235409-Preview-Pan-and-Zoom | gestures, View Options panel, Active Camera View |
| 9 | Export troubleshooting | https://support.alightmotion.com/hc/en-us/articles/10537514944785-I-can-t-export-my-project-What-should-I-do | export entry points and settings |
| 10 | Quick Start Guide | https://support.alightmotion.com/hc/en-us/articles/10536777320337-Alight-Motion-Quick-Start-Guide | landing page only: add layers, adjust, animate |
| 11 | Effects & Feature Guides category | https://support.alightmotion.com/hc/en-us/categories/10536777021713-Effects-Feature-Guides | index only. 19 effect articles, not read |
| 12 | Getting Started category | https://support.alightmotion.com/hc/en-us/categories/13224816269969-Getting-Started | index only |

Source conflict: Google Play and App Store say 160+ effect building blocks, the marketing site says 100+. The Play and App Store figure is probably newer.

Not used: "mod APK" and "pro download" sites that showed up in search. They are piracy sites and not a reliable source.

## Core loop

Stack layers (shapes, text, images, video, audio) on a timeline, animate any property with keyframes and easing, preview it, and export a video.

## Screens

| ID | screen | route / how to reach | purpose | key components | states seen |
| --- | --- | --- | --- | --- | --- |
| S01 | Project list (home) | app launch, Projects tab | open, create, export, manage projects | project thumbnails, (+) button, long-press selection, Export button (bottom-left on long-press) | empty (to verify), filled |
| S02 | Elements tab | Projects, Elements sub-tab | manage saved reusable groups | element thumbnails, (+) button, long-press select/delete/duplicate | empty (to verify), filled |
| S03 | New project settings | S01 (+) button | set aspect ratio, resolution, frame rate | to verify. Inferred from element settings (resolution, frame rate) | to verify |
| S04 | Editor | open a project | main workspace: preview above, timeline below | preview canvas, playhead, play/pause, layer bars, time ruler, toolbar | empty project, filled, playing, layer selected |
| S05 | Add menu | S04 Add Layer | create layers or insert elements | Object tab (Null, shapes), text, media, audio, Elements list | to verify |
| S06 | Layer properties | S04 select a layer | edit transform, fill, stroke, opacity, etc. | numeric fields, spinners, keyframe toggles per property, Layer Parent button | to verify |
| S07 | Layer Parent picker | S06 Layer Parent button | choose the parent from a dropdown | dropdown with the current layer greyed out, "none" | per help article |
| S08 | Curve Editor | S06 keyframed property | edit easing between two keyframes | curve canvas, white drag handles, preset buttons on the right, overflow menu with Enable Overshoot, Copy Curve, Paste Curve, Paste Curve to All Keyframes | needs at least two keyframes |
| S09 | Effect Browser | S04 or S06 add effect | pick an effect | effect list with tags such as "Layer Parenting", Guide option on the right | to verify |
| S10 | Effect settings | S09 select an effect | tune one effect's settings, each keyframeable | per-effect controls, Guide button | to verify |
| S11 | View Options panel | S04 | preview zoom control, Active Camera View toggle | Preview Zoom control, magnifier icon for Pan and Zoom Mode | per help article |
| S12 | Export settings | S01 long-press, Export, arrow next to Video | choose resolution, frame rate, bitrate, quality | Resolution, Frame rate, Bitrate, Quality slider | per help article |
| S13 | Export progress and result | S12 export | render and finish | progress, share/save (to verify) | rendering, done, failed (see help article) |
| S14 | Element properties | S04 select an element-backed layer | edit element text, open original, convert to group | edit text, open original, convert to independent group, gear icon for settings | per help article (could-have) |

Not mapped: sign-in and account screens, membership paywall, settings, language, premium effect pages, sample projects, cloud sync. These are outside the slice.

## Flows

```
F01 Create a project and add a first layer
    S01 -> S03 -> S04 -> S05 -> S04
    happy path clicks: to measure on own account
    edge: no storage, unsupported media, large video import

F02 Animate a property with keyframes
    S04 -> S06 -> (toggle keyframe on a property, move playhead, change value) -> S04
    happy path clicks: to measure on own account
    edge: only one keyframe, keyframes moved or deleted, multiple properties at once

F03 Ease the motion between two keyframes
    S04 -> S06 -> S08 -> S04
    happy path clicks: to measure on own account (preset path is a single tap on a preset button)
    edge: fewer than two keyframes so no curve shows, overshoot enabled, curve copied to another pair, three keyframes giving two curves

F04 Add an effect to a layer
    S04 -> S09 -> S10 -> S04
    happy path clicks: to measure on own account
    edge: stacking effects, copy and paste effects, keyframing effect settings

F05 Parent a layer to another layer or a null
    S04 -> S05 (Object, Null) -> S06 -> S07 -> S04
    happy path clicks: to measure on own account
    edge: layer cannot be its own parent, chained parents, parent deleted, one parent per child

F06 Export a video
    S01 (long-press project) -> Export -> S12 -> S13
    happy path clicks: about 4 plus the export tap (long-press, Export, arrow next to Video, adjust, export)
    edge: export fails on a heavy project, low memory, other apps using resources, lower quality needed

F07 Zoom and pan the preview
    S04 -> S11 (magnifier icon) -> two-finger gestures
    happy path clicks: 1 tap then gestures
    edge: gestures edit the layer if one is selected, two-finger double tap resets to 100%

F08 Save a group as an Element and reuse it (could-have)
    S04 -> (three-dot menu, save as element) -> S02 -> S05 -> S04
    happy path clicks: to measure on own account
    edge: linked edits update every project, text elements customized per project

Gaps to verify with the user's own account: how a project is created (aspect ratio, resolution, fps), how the timeline is trimmed and moved, how layers are reordered, undo and redo, and what the export result screen offers.
```

## Components

| component | variants | states | used on |
| --- | --- | --- | --- |
| Project thumbnail card | project, element | default, selected (long-press), exporting | S01, S02 |
| (+) button | new project, new element | default | S01, S02 |
| Timeline layer bar | shape, text, image, video, audio, null, group, camera | default, selected, trimmed | S04 |
| Playhead | single | default, dragging | S04, S08 |
| Keyframe marker | per property | default, selected | S04, S06 |
| Numeric spinner | transform fields, weights | default, editing, keyframed | S06, S10 |
| Curve canvas with handles | preset, custom, advanced types | default, handle dragged, overshoot on | S08 |
| Preset button | ease in, ease out, ease in-out, others | default, applied | S08 |
| Overflow menu (three dots) | curve, element | closed, open | S08, S04 |
| Dropdown picker | layer parent | default, option greyed out | S07 |
| Effect list item | with tags | default, selected | S09 |
| Slider | quality | default | S12 |
| Toggle or mode icon | pan and zoom, active camera view | on, off | S11 |

## Inferred data model

```
Project      id, name, width, height, frame_rate, duration, created_at, modified_at, thumbnail
             evidence: S01, S03, export article (resolution, frame rate)
             confidence: medium (fields are inferred, not seen)

Layer        id, project_id, type (shape | text | image | video | audio | null | camera | group | element),
             parent_layer_id (nullable), order, start_time, end_time, transform (position, scale, rotation, opacity),
             fill (solid | gradient | media), stroke, shadow, border
             evidence: S04, S06, parenting article, Play listing
             confidence: high for types and parenting, medium for fields

Keyframe     id, layer_id, property_path, time, value, curve_id (to the next keyframe)
             evidence: easing article ("each adjacent pair gets its own curve")
             confidence: high

Curve        id, type (preset | custom | bounce | elastic | cyclic | random | steps | elastic_steps),
             control_points, overshoot_enabled
             evidence: easing article, S08
             confidence: high

EffectInstance  id, layer_id, effect_type, order, settings (keyframeable)
                evidence: effects articles index, S09, S10
                confidence: medium

Element      id, name, content (group of layers), resolution, frame_rate, retime_mode
             evidence: Elements guide
             confidence: high (could-have feature)

Bookmark     id, project_id, time, label
             evidence: Play listing ("bookmarks for editing")
             confidence: guess (details unknown)

ExportJob    id, project_id, format, resolution, frame_rate, bitrate, quality
             evidence: S12, marketing site (H.264/HEVC MP4, GIF, PNG sequence)
             confidence: medium
```

Relationships: Project 1-n Layer, Layer 1-n Keyframe, Layer 1-n EffectInstance, Layer n-1 Layer (parent, one parent only, many children), Keyframe n-1 Curve, Project n-n Element (linked), Project 1-n ExportJob.

Storage is local. The marketing site and the Elements guide describe sharing as a link plus QR code or a project package, which suggests a hosted service (to verify, out of scope).

## Feature matrix

See `features.csv`. Must: 13, should: 13, could: 12, skip: 5 (43 rows, 38 counted in the parity score).

## Out of scope (cannot or should not be cloned)

- Membership tiers, watermark logic and the paywall (not needed for personal use)
- The premium effects library and the "160+ effect building blocks" in full (large content set, each effect needs its own shader). A handful of effects is as far as the slice goes.
- Alight's hosted sharing, the Alight link and QR code, premade elements and sample projects (their service and content)
- Ads
- Alight's own logo, name, icons, copy and presets
- Character joint rigging and cameras (could-haves outside this slice, listed in the CSV)

## Size

Screens 14 (11 inside the slice), flows 8, entities 8. Hard parts:
1. Real-time preview that composites many layers at playback speed (OpenGL ES or a similar rendering path)
2. Keyframe and easing engine, including the curve editor UI and overshoot
3. Video export encoding on a phone, without crashing on heavy projects (users report crashes and lag in the original)
4. Touch-first timeline and layer editing (gestures, selection conflicts such as the pan and zoom rule)
5. Effects, since each one is custom rendering work

Size: L for the slice as listed. A thin MVP is M (shape, text and image layers, position/scale/rotation/opacity keyframes, three easing presets, preview, MP4 export with no effects). The full app is XL and should not be attempted.

## Next steps

1. Run through the app once on your own account and fill the "to verify" cells: new project settings, timeline gestures, the Add menu, undo and redo, export result screen. Save any screenshots in `replica/screens/` for your own reference only. They are never shipped.
2. Run `/replica-architect` to pick the stack and plan the build order.
