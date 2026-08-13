# Atlas — Offline Country Landscape Photo Pack

## Goal

The country-detail hero shows a landscape photo per country. Historically these were
fetched at runtime from the **Unsplash Search API**, which is a paid / rate-limited
feature (the free "demo" tier is 50 requests/hour). The objective is that the app
**never depends on an image API at runtime**.

This is achieved by shipping a few curated **landmark** images per country as **bundled
assets**, generated once from **open, CC/public-domain Wikimedia** sources (from a
hand-curated list of iconic sights). The app rotates through a country's images by date
(about one a day). The hero image is resolved in this priority order:

1. **User cache** — a photo already downloaded to
   `filesDir/country_landscape_photos/<filename>` (from an optional key-gated refresh).
2. **Bundled pack** — `assets/country_landscape_photos/<iso2>_<k>.webp`, rotated daily.
3. **Gradient fallback** — a paper/ink gradient when neither exists.

The resolution lives in `rememberLandscapeHeroModel(...)` in
`ui/rework/screens/countries/ReworkCountriesScreen.kt`. The Unsplash client remains only
as an **optional** enhancement (a fresh set when a key is configured); it is no longer
required for images to appear.

## Pack location and naming

```
app/src/main/assets/country_landscape_photos/
    <iso2>_<k>.webp       # up to 4 per country (k = 0..3), e.g. de_0.webp, de_1.webp
    attribution.json      # provenance: iso2 -> [ { file, author, license, licenseUrl, source }, ... ]
```

- `<iso2>` is the lowercase ISO-3166 alpha-2 code, matching `Country.iso2`.
- The resolver rotates by date across the contiguous `_0.._k` variants (up to
  `MAX_HERO_VARIANTS`), and also accepts a legacy single `<iso2>.webp`/`.jpg`.

## Generating / refreshing the pack

The pack is built from a **curated landmark list** — `scripts/country_landmarks.json`,
3–5 iconic sights per country (natural + man-made) — so each hero is a recognisable
landmark (Germany→Neuschwanstein, Japan→Mount Fuji, Egypt→Pyramids) rather than a random
scenery photo.

```bash
pip install Pillow                          # one-time: re-encode to compact WebP
python scripts/generate_landmark_pack.py    # all trackable countries (resumable)
python scripts/generate_landmark_pack.py --limit 12   # first 12, to review style
```

- **No API key.** Wikimedia (Wikidata + Commons) only asks for a descriptive User-Agent
  (set by the script) and reasonable request rates (throttled). No hourly cap.
- For each landmark, the image is resolved as:
  1. the **Wikidata** entity whose country (`P17`) is the country *or its sovereign
     state*, using its representative image (`P18`) — the canonical, disambiguated photo
     (this rejects e.g. the Quebec "Mont-Blanc" or a "Nile Valley Sunbird" bird);
  2. fallback: **Commons search** `"<landmark> <country>"`, first large landscape JPEG
     that is not an off-topic subject (planes, stamps, maps, wildlife macros — see `JUNK`).
- It keeps the first `IMAGES_PER_COUNTRY` (4) landmarks whose re-encoded image clears
  `MIN_ENCODED_BYTES`; each country has 5 landmarks, so a weak one is simply skipped.
- Each image is downloaded from Commons at ~1600px and **re-encoded to WebP** at
  `TARGET_WIDTH` (1080px) / q70 — near-native sharpness. The full 4×~244 pack is roughly
  **~90 MB**; lower `WEBP_QUALITY`/`TARGET_WIDTH` or `IMAGES_PER_COUNTRY` to shrink it.
- Author + license + the **landmark name** are recorded per image in `attribution.json`.
- **Resumable**: a country whose `<iso2>_0.webp` exists is skipped. `--limit N` stops
  after N countries.

`scripts/generate_landscape_pack.py` is a retained alternative that instead pulls generic
scenery from Commons "Landscapes of / Quality images of / Featured pictures of" categories;
the landmark generator above is the current source of the pack.

### Editing the landmark list

`scripts/country_landmarks.json` is the editable source of truth
(`{ "countries": { "DE": { "name_ca": "Alemanya", "landmarks": [ { "name": "...",
"type": "M" }, ... ] } } }`). Add/replace a landmark name, delete that country's
`<iso2>_*.webp` files, and re-run the generator to refresh just that country.

## Licensing / attribution

Images come from **Wikimedia Commons** under CC-BY / CC-BY-SA / public-domain licenses.
These permit redistribution (including bundling in the app) **with attribution**. The
required credit — author + license — is captured per image in `attribution.json` and
should be surfaced in the UI (e.g. a small "© author · license" credit on the hero, or an
about/credits screen) before release.

## Runtime independence

At runtime the app reads only the bundled asset (or a previously cached file). No network
call is made to show a hero image. The Unsplash integration is now strictly optional: if a
user configures a key, the app may refresh a country's photo into the local cache, which
then takes priority over the bundled asset — but nothing breaks without it.

## Size / tuning

`TARGET_WIDTH` and `WEBP_QUALITY` in the script control the size/quality tradeoff. Lower
them for a smaller APK; raise them for crisper heroes. To bundle a narrower set, filter the
country list in the script (e.g. UN members only) and let the rest use the gradient
fallback or the optional API refresh.
