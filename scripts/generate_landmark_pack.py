#!/usr/bin/env python3
"""Generate the bundled offline hero pack from a curated landmark list.

For each country we have 3-5 curated representative landmarks (natural + man-made)
in `scripts/country_landmarks.json`. This fetches one high-quality image per
landmark (rotating up to IMAGES_PER_COUNTRY in the app) from open Wikimedia
sources, so the hero is an iconic, recognisable sight and the app never needs an
image API at runtime.

    pip install Pillow
    python scripts/generate_landmark_pack.py            # all trackable countries
    python scripts/generate_landmark_pack.py --limit 12 # first 12 (for review)

Per landmark the image is resolved as:
  1. Wikidata entity whose country (P17) is the country *or its sovereign state*,
     using its representative image (P18) - the canonical, disambiguated photo.
  2. Fallback: Commons search "<landmark> <country>", first large landscape JPEG
     that is not an off-topic subject (planes, stamps, maps, wildlife macros, ...).

Output:
  app/src/main/assets/country_landscape_photos/<iso2>_<k>.webp
  app/src/main/assets/country_landscape_photos/attribution.json
    ( iso2 -> [ { file, landmark, author, license, licenseUrl, source }, ... ] )

Resumable: a country whose <iso2>_0.webp exists is skipped.
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
LANDMARKS_JSON = REPO_ROOT / "scripts/country_landmarks.json"
PACK_DIR = REPO_ROOT / "app/src/main/assets/country_landscape_photos"
ATTRIBUTION_JSON = PACK_DIR / "attribution.json"

USER_AGENT = "AtlasTravelApp/1.0 (Atlas country hero pack generator; nilpomaroltur@gmail.com)"
WD_API = "https://www.wikidata.org/w/api.php"
COMMONS_API = "https://commons.wikimedia.org/w/api.php"

IMAGES_PER_COUNTRY = 4
MIN_SOURCE_WIDTH = 1000
SOURCE_WIDTH = 1600
TARGET_WIDTH = 1080
WEBP_QUALITY = 70
MIN_ENCODED_BYTES = 15 * 1024
THROTTLE_SECONDS = 0.2

# Off-topic subjects that Commons text search sometimes ranks highly for a landmark.
JUNK = re.compile(
    r"plane|aircraft|airport|airplane|\bjet\b|helicopter|military|navy|warship|"
    r"\bstamp\b|coin|banknote|\blogo\b|\bflag\b|\bmap\b|diagram|model|"
    r"damselfly|dragonfly|\bbird\b|butterfly|insect|beetle|moth|spider|"
    r"portrait|\bstatue of\b",
    re.IGNORECASE,
)
_TAGS = re.compile(r"<[^>]+>")


def http_json(url: str) -> dict:
    req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT, "Accept": "application/json"})
    with urllib.request.urlopen(req, timeout=40) as resp:
        return json.loads(resp.read().decode("utf-8"))


def wd(params: dict) -> dict:
    return http_json(WD_API + "?" + urllib.parse.urlencode(params))


def commons(params: dict) -> dict:
    return http_json(COMMONS_API + "?" + urllib.parse.urlencode(params))


def iso_to_qid() -> dict[str, str]:
    """{ ISO2(upper): Wikidata QID } via one SPARQL query on P297."""
    query = "SELECT ?iso ?c WHERE { ?c wdt:P297 ?iso . }"
    url = "https://query.wikidata.org/sparql?format=json&query=" + urllib.parse.quote(query)
    data = http_json(url)
    out = {}
    for b in data["results"]["bindings"]:
        out.setdefault(b["iso"]["value"].upper(), b["c"]["value"].rsplit("/", 1)[-1])
    return out


def name_variants(name: str) -> list[str]:
    out = [name]
    for sep in ("/", " and "):
        if sep in name:
            out.append(name.split(sep)[0].strip())
    seen, uniq = set(), []
    for v in out:
        if v and v not in seen:
            seen.add(v)
            uniq.append(v)
    return uniq


def wikidata_image(name: str, country_qids: set[str]) -> str | None:
    """P18 of the first matching entity located in the country / its sovereign."""
    for cand in name_variants(name):
        try:
            search = wd({"action": "wbsearchentities", "format": "json", "search": cand,
                         "language": "en", "uselang": "en", "limit": "7", "type": "item"})
            ids = [c["id"] for c in search.get("search", [])]
            if not ids:
                continue
            ents = wd({"action": "wbgetentities", "format": "json", "ids": "|".join(ids), "props": "claims"})
        except Exception:
            continue
        for qid in ids:  # preserve search-rank order
            claims = ents.get("entities", {}).get(qid, {}).get("claims", {})
            p17 = {c["mainsnak"].get("datavalue", {}).get("value", {}).get("id") for c in claims.get("P17", [])}
            if p17 & country_qids and "P18" in claims:
                val = claims["P18"][0]["mainsnak"].get("datavalue", {}).get("value")
                if val:
                    return val
    return None


def commons_search_image(name: str, country_name: str) -> str | None:
    try:
        data = commons({"action": "query", "format": "json", "generator": "search",
                        "gsrnamespace": "6", "gsrsearch": f"{name} {country_name}", "gsrlimit": "20",
                        "prop": "imageinfo", "iiprop": "size|mime"})
    except Exception:
        return None
    pages = sorted(((data.get("query") or {}).get("pages") or {}).values(), key=lambda p: p.get("index", 999))
    for p in pages:
        ii = (p.get("imageinfo") or [{}])[0]
        w, h, mime, title = ii.get("width", 0), ii.get("height", 0), ii.get("mime", ""), p["title"][5:]
        if mime == "image/jpeg" and w > h and w >= MIN_SOURCE_WIDTH and not JUNK.search(title):
            return title
    return None


def commons_attribution(filename: str, landmark: str) -> dict:
    try:
        data = commons({"action": "query", "format": "json", "prop": "imageinfo",
                        "iiprop": "extmetadata", "titles": f"File:{filename}"})
        page = next(iter((data.get("query") or {}).get("pages", {}).values()))
        meta = (page.get("imageinfo") or [{}])[0].get("extmetadata", {})
    except Exception:
        meta = {}

    def val(k: str) -> str | None:
        v = meta.get(k, {}).get("value")
        return _TAGS.sub("", v).strip() if isinstance(v, str) else None

    return {"file": filename, "landmark": landmark, "author": val("Artist"),
            "license": val("LicenseShortName"), "licenseUrl": val("LicenseUrl"),
            "source": "Wikimedia Commons"}


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


def load_countries() -> list[dict]:
    data = json.loads(COUNTRIES_JSON.read_text(encoding="utf-8"))
    rows = data if isinstance(data, list) else next(v for v in data.values() if isinstance(v, list))
    return [r for r in rows if r.get("is_trackable")]


def main() -> int:
    parser = argparse.ArgumentParser(description="Generate the landmark hero pack from Wikimedia.")
    parser.add_argument("--limit", type=int, default=0, help="Stop after N countries (0 = no limit).")
    args = parser.parse_args()
    for stream in (sys.stdout, sys.stderr):
        try:
            stream.reconfigure(encoding="utf-8", errors="replace")
        except Exception:
            pass

    PACK_DIR.mkdir(parents=True, exist_ok=True)
    landmarks = json.loads(LANDMARKS_JSON.read_text(encoding="utf-8"))["countries"]
    attribution = {}
    if ATTRIBUTION_JSON.exists():
        attribution = json.loads(ATTRIBUTION_JSON.read_text(encoding="utf-8")).get("photos", {})

    countries = load_countries()
    print(f"{len(countries)} countries. Fetching ISO->Wikidata map ...")
    qids = iso_to_qid()
    print(f"Mapped {len(qids)} ISO codes. Target {IMAGES_PER_COUNTRY}/country. Pack: {PACK_DIR}")

    processed = 0
    for row in countries:
        iso_u = row["iso2"].upper()
        iso = row["iso2"].lower()
        if (PACK_DIR / f"{iso}_0.webp").exists():
            continue
        entry = landmarks.get(iso_u)
        if not entry:
            print(f"[{iso}] no landmark list")
            continue

        country_name = row.get("name_en") or entry.get("name_ca")
        country_qids = {qids.get(iso_u)}
        parent = row.get("parent_iso2")
        if parent:
            country_qids.add(qids.get(parent.upper()))
        country_qids.discard(None)

        saved, records = 0, []
        for lm in entry["landmarks"]:
            if saved >= IMAGES_PER_COUNTRY:
                break
            lm_name = lm["name"]
            fname = wikidata_image(lm_name, country_qids) or commons_search_image(lm_name, country_name)
            if not fname:
                continue
            dest = PACK_DIR / f"{iso}_{saved}.webp"
            if download_webp(fname, dest):
                if dest.stat().st_size >= MIN_ENCODED_BYTES:
                    records.append(commons_attribution(fname, lm_name))
                    saved += 1
                else:
                    dest.unlink()
            time.sleep(THROTTLE_SECONDS)

        if records:
            attribution[iso] = records
            ATTRIBUTION_JSON.write_text(
                json.dumps({"photos": dict(sorted(attribution.items()))}, ensure_ascii=False, indent=2),
                encoding="utf-8")
            print(f"[{iso}] {country_name}: {saved} -> " + " | ".join(r["landmark"] for r in records))
        else:
            print(f"[{iso}] {country_name}: NO IMAGES")

        processed += 1
        if args.limit and processed >= args.limit:
            print(f"Reached limit ({args.limit}).")
            break

    files = list(PACK_DIR.glob("*.webp"))
    total = sum(f.stat().st_size for f in files)
    print(f"Done. {len(files)} images across "
          f"{len({f.stem.rsplit('_',1)[0] for f in files})} countries, {total/1024/1024:.1f} MB.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
