#!/usr/bin/env python3
"""Generate the bundled offline country landscape pack from Wikimedia (open, no key).

The country-detail hero shows a landscape photo per country, and the app rotates
through a few per country (one a day). So the app never depends on a paid/limited
image API at runtime, the pack is generated once from open, CC/public-domain
Wikimedia Commons sources and committed as bundled assets.

    pip install Pillow
    python scripts/generate_landscape_pack.py

No API key is required (Wikimedia only asks for a descriptive User-Agent and
reasonable request rates, both handled here).

Per country it collects up to N landscape photos, preferring Commons' curated
"Quality images of <country>" set, then topping up from "Landscapes of <country>";
if a country has neither category it falls back to the single Wikidata country /
capital image. Files are re-encoded to compact WebP:

    app/src/main/assets/country_landscape_photos/<iso2>_<k>.webp   (k = 0..N-1)
    app/src/main/assets/country_landscape_photos/attribution.json  (list per iso2)

Resumable: a country whose <iso2>_0.webp already exists is skipped.
"""
from __future__ import annotations

import argparse
import io
import json
import re
import sys
import time
import urllib.parse
import urllib.request
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[1]
COUNTRIES_JSON = REPO_ROOT / "app/src/main/assets/data/countries.json"
PACK_DIR = REPO_ROOT / "app/src/main/assets/country_landscape_photos"
ATTRIBUTION_JSON = PACK_DIR / "attribution.json"

USER_AGENT = "AtlasTravelApp/1.0 (Atlas country landscape pack generator; https://github.com/Nilpomarol/Atlas)"
IMAGES_PER_COUNTRY = 4
CANDIDATE_POOL = 12       # gather extra candidates so low-detail ones can be skipped
MIN_SOURCE_WIDTH = 1200   # require a reasonably high-res original
SOURCE_WIDTH = 1600       # width requested from Commons before local downscale
TARGET_WIDTH = 1080       # final bundled width (near-native on modern phones)
WEBP_QUALITY = 70
MIN_ENCODED_BYTES = 15 * 1024  # below this the source is low-detail/low-quality -> skip
THROTTLE_SECONDS = 0.2

# Category prefixes to draw from, in preference order. "Landscapes of" comes first so
# heroes are actual scenery; the curated "Quality/Featured" sets (which include any
# subject: wildlife, portraits, ...) only fill in for countries with a thin landscapes set.
CATEGORY_PREFIXES = ["Landscapes of", "Quality images of", "Featured pictures of"]

BAD_COUNTRY_IMAGE = re.compile(
    r"\.svg$|location|locator|satellite|cloud[-\s]?free|unocha|orthographic|"
    r"globe|\bmap\b|\bblank\b|projection",
    re.IGNORECASE,
)
_TAGS = re.compile(r"<[^>]+>")


def http_json(url: str) -> dict:
    req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT, "Accept": "application/json"})
    with urllib.request.urlopen(req, timeout=40) as resp:
        return json.loads(resp.read().decode("utf-8"))


def commons_api(params: dict) -> dict:
    return http_json("https://commons.wikimedia.org/w/api.php?" + urllib.parse.urlencode(params))


def filename_from_filepath(value: str) -> str:
    return urllib.parse.unquote(value.split("Special:FilePath/")[-1])


# ---- Candidate collection -------------------------------------------------

def category_landscape_files(category: str) -> list[tuple[str, int]]:
    """Landscape-oriented JPEG files in a Commons category, largest first."""
    try:
        data = commons_api({
            "action": "query", "format": "json", "generator": "categorymembers",
            "gcmtitle": f"Category:{category}", "gcmnamespace": "6", "gcmtype": "file",
            "gcmlimit": "100", "prop": "imageinfo", "iiprop": "size|mime",
        })
    except Exception:
        return []
    out = []
    for page in ((data.get("query") or {}).get("pages") or {}).values():
        ii = (page.get("imageinfo") or [{}])[0]
        w, h, mime = ii.get("width", 0), ii.get("height", 0), ii.get("mime", "")
        if mime == "image/jpeg" and w > h and w >= MIN_SOURCE_WIDTH:
            out.append((page["title"][5:], w))  # strip "File:"
    out.sort(key=lambda t: -t[1])
    return out


def gather_candidates(name_en: str) -> list[str]:
    """Ordered, de-duplicated file titles from the preferred categories."""
    titles: list[str] = []
    seen: set[str] = set()
    for prefix in CATEGORY_PREFIXES:
        files: list[tuple[str, int]] = []
        for variant in (name_en, f"the {name_en}"):
            files = category_landscape_files(f"{prefix} {variant}")
            if files:
                break
        for title, _ in files:
            if title not in seen:
                seen.add(title)
                titles.append(title)
    return titles


def fetch_wikidata_fallback() -> dict[str, str]:
    """{ ISO2(upper): filename } single-image fallback (country image, else capital)."""
    query = (
        "SELECT ?iso ?image ?capImage WHERE {"
        "  ?c wdt:P297 ?iso ."
        "  OPTIONAL { ?c wdt:P18 ?image . }"
        "  OPTIONAL { ?c wdt:P36 ?cap . ?cap wdt:P18 ?capImage . }"
        "}"
    )
    url = "https://query.wikidata.org/sparql?format=json&query=" + urllib.parse.quote(query)
    data = http_json(url)
    slots: dict[str, dict[str, str]] = {}
    for b in data["results"]["bindings"]:
        iso = b["iso"]["value"].upper()
        slot = slots.setdefault(iso, {})
        if "image" in b and "country" not in slot:
            slot["country"] = filename_from_filepath(b["image"]["value"])
        if "capImage" in b and "capital" not in slot:
            slot["capital"] = filename_from_filepath(b["capImage"]["value"])
    out = {}
    for iso, slot in slots.items():
        country = slot.get("country")
        out[iso] = (country if country and not BAD_COUNTRY_IMAGE.search(country)
                    else slot.get("capital") or country)
    return {k: v for k, v in out.items() if v}


# ---- Attribution + download ----------------------------------------------

def commons_attribution(filename: str) -> dict:
    try:
        data = commons_api({
            "action": "query", "format": "json", "prop": "imageinfo",
            "iiprop": "extmetadata", "titles": f"File:{filename}",
        })
        page = next(iter((data.get("query") or {}).get("pages", {}).values()))
        meta = (page.get("imageinfo") or [{}])[0].get("extmetadata", {})
    except Exception:
        meta = {}

    def val(k: str) -> str | None:
        v = meta.get(k, {}).get("value")
        return _TAGS.sub("", v).strip() if isinstance(v, str) else None

    return {
        "file": filename,
        "author": val("Artist"),
        "license": val("LicenseShortName"),
        "licenseUrl": val("LicenseUrl"),
        "source": "Wikimedia Commons",
    }


def download_webp(filename: str, dest: Path) -> bool:
    from PIL import Image
    url = ("https://commons.wikimedia.org/wiki/Special:FilePath/"
           + urllib.parse.quote(filename) + f"?width={SOURCE_WIDTH}")
    tmp = dest.with_suffix(dest.suffix + ".tmp")
    try:
        with urllib.request.urlopen(urllib.request.Request(url, headers={"User-Agent": USER_AGENT}), timeout=60) as resp:
            raw = resp.read()
        if not raw:
            return False
        im = Image.open(io.BytesIO(raw)).convert("RGB")
        im.thumbnail((TARGET_WIDTH, TARGET_WIDTH * 2))
        im.save(tmp, "WEBP", quality=WEBP_QUALITY, method=6)
        if tmp.stat().st_size > 0:
            tmp.replace(dest)
            return True
    except Exception as exc:
        print(f"    download/encode failed: {exc}")
    if tmp.exists():
        tmp.unlink()
    return False


def load_countries(only_trackable: bool) -> list[dict]:
    data = json.loads(COUNTRIES_JSON.read_text(encoding="utf-8"))
    rows = data if isinstance(data, list) else next(v for v in data.values() if isinstance(v, list))
    return [r for r in rows if r.get("is_trackable")] if only_trackable else rows


def main() -> int:
    parser = argparse.ArgumentParser(description="Generate the rotating offline landscape pack from Wikimedia.")
    parser.add_argument("--all", action="store_true", help="Include non-trackable territories too.")
    parser.add_argument("--limit", type=int, default=0, help="Stop after N countries (0 = no limit).")
    args = parser.parse_args()

    for stream in (sys.stdout, sys.stderr):
        try:
            stream.reconfigure(encoding="utf-8", errors="replace")
        except Exception:
            pass

    PACK_DIR.mkdir(parents=True, exist_ok=True)
    attribution: dict = {}
    if ATTRIBUTION_JSON.exists():
        attribution = json.loads(ATTRIBUTION_JSON.read_text(encoding="utf-8")).get("photos", {})

    countries = load_countries(only_trackable=not args.all)
    print(f"{len(countries)} countries. Fetching Wikidata fallback images ...")
    fallback = fetch_wikidata_fallback()
    print(f"Fallback covers {len(fallback)} ISO codes. Target {IMAGES_PER_COUNTRY}/country. Pack: {PACK_DIR}")

    processed = 0
    for row in countries:
        iso2 = row["iso2"].lower()
        if (PACK_DIR / f"{iso2}_0.webp").exists():
            continue
        name = row.get("name_en") or row.get("name_ca")

        titles = gather_candidates(name)[:CANDIDATE_POOL]
        fb = fallback.get(row["iso2"].upper())
        if not titles and fb:
            titles = [fb]
        if not titles:
            print(f"[{iso2}] {name}: no image found")
            continue

        entries = []
        saved = 0
        for title in titles:
            if saved >= IMAGES_PER_COUNTRY:
                break
            dest = PACK_DIR / f"{iso2}_{saved}.webp"
            if download_webp(title, dest):
                # Drop over-compressed / low-detail results in favour of the next candidate.
                if dest.stat().st_size >= MIN_ENCODED_BYTES:
                    entries.append(commons_attribution(title))
                    saved += 1
                else:
                    dest.unlink()
            time.sleep(THROTTLE_SECONDS)
        # Guarantee at least one image: if the filter rejected everything, keep the best.
        if saved == 0:
            best = titles[0] if not fb else fb
            if download_webp(best, PACK_DIR / f"{iso2}_0.webp"):
                entries.append(commons_attribution(best))
        if entries:
            attribution[iso2] = entries
            ATTRIBUTION_JSON.write_text(
                json.dumps({"photos": dict(sorted(attribution.items()))}, ensure_ascii=False, indent=2),
                encoding="utf-8",
            )
            print(f"[{iso2}] {name}: {len(entries)} image(s)")

        processed += 1
        if args.limit and processed >= args.limit:
            print(f"Reached limit ({args.limit}).")
            break

    files = list(PACK_DIR.glob("*.webp"))
    total = sum(f.stat().st_size for f in files)
    print(f"Done. Pack holds {len(files)} images across "
          f"{len({f.stem.rsplit('_', 1)[0] for f in files})} countries, {total/1024/1024:.1f} MB.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
