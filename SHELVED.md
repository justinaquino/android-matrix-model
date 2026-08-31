# SHELVED — 2026-08-31

**Status: shelved.** Not in active development. Justin's call, 2026-08-31.

Tier-1 backup is current: `citfj/main` and `origin/main` (GitHub) both at the
same commit as local `HEAD`, verified from the destination after fetch.

## What was removed when shelving

Reproducible build output only. All of it was already `.gitignore`d, so the
repo's tracked set (246 files) is unchanged and the working tree is clean.

| Removed | Size |
|---|---|
| `app/build/` | 6.5 GB |
| `smollm/build/` | 4.1 GB |
| `smollm/.cxx/` | 1.5 GB |
| `.gradle/`, `.kotlin/` | 13 MB |

Repo went 14 GB → 2.6 GB. Staged (not deleted) at
`For Deletion/260831-build-output/android-matrix-model/`.

## What was deliberately kept

- **The release APK history at repo root** — `AMM_v1.1.2` … `AMM_v1.1.6`.
  `v1.1.2/3/4` are all 132,628,379 bytes but have **three different md5s**, so
  they are distinct builds, not duplicates — none was discarded on a size match.
- **`AMM_v1.1.6.apk` was rescued before the sweep.** It existed *only* inside
  `app/build/outputs/apk/release/`, so removing build output would have destroyed
  the newest release. Copied to the root with md5 verified
  (`b45f24802087181a212540bed13ede68`) before `app/build` was moved.
- `llama.cpp` and `smollm` sources, `smolvectordb`, `metadata`, gradle configs —
  everything needed to rebuild.

## To rebuild

Standard Android/Gradle build; the native side needs the NDK + CMake for
`smollm`/`llama.cpp`. Nothing beyond the tracked configs is required.

## Known issue to fix before building again

The `llama.cpp` submodule shows **115 modified files that are mode-only changes
(100755 → 100644)** — the executable bit stripped from upstream `.sh`/`.py`
scripts, with zero content changes (`git diff --stat` = 0 insertions, 0
deletions). This is filesystem/sync damage, not authored work, so it was **not**
committed. Build scripts may fail as non-executable until the modes are
restored:

```bash
git -C llama.cpp checkout .   # discards the mode changes — verify nothing else is pending first
```
