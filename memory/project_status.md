---
name: project-status
description: Current Atlas app development phase, last completed work, and next items
metadata:
  type: project
---

v3.2 is complete (per-stop photos, cover photos, DB v21). Navbar root-nav fix done.

Stats screen Mapa tab ✅ complete (2026-06-09):
- No scroll conflict: Map tab fills screen, other tabs remain scrollable
- `StatsMapCanvas.kt` — fullscreen canvas with projection-based zoom (no graphicsLayer), fixed-size markers, pan/pinch up to 20×, tooltip dismisses on move
- 50m Natural Earth GeoJSON in assets — higher-quality country outlines
- Six filter-toggleable layers: country state fills, solid completed flight arcs, dashed planned arcs, airport dots, trip stop markers, excursion stop markers
- Tap-to-identify (priority: trip stops → excursion stops → airports → route midpoints → country polygon ray-cast)
- Starting position: centered on living country at 5× zoom via LaunchedEffect; falls back to world view
- Reset button: top-right Refresh icon restores the personalised initial position
- `MapFilterOverlay`: collapsible 2×3 grid of layer toggles (bottom-right)
- `GeoProjection` gained `projectZoomed()` and `unproject()`
- `StatsFlightMapRoute` gained `isPlanned`, `fromCode`, `toCode`; `StatsMapMarker` added to ViewModel

**Why:** pre-v4.0 stats polish pass. Resum + Cronologia + Mapa tabs all complete.

Next pre-v4.0 item: auto-status update for trips and flights (background coroutine on app start).
After that: v4.0 country depth / stats dataset.
