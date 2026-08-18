---
name: backup-basics
description: The replaceable-vs-irreplaceable file classification that decides what gets backed up, committed, or must never be deleted. Trigger this when deciding whether a file/dir belongs in git, whether something needs to be in a backup tier, whether a path is safe to clean up, when a backup destination is growing suspiciously large, or whenever the terms "irrf", "irreplaceable", "reproducible bulk", or "workfiles-classify.conf" come up.
---

> **Author:** Justin · skill filed by Kimi Code CLI (agent session), 2026-08-18
> **Canonical location:** `IT-knowledge/skills/backup-basics/SKILL.md`
> **Shared to (all `lae` repos):** `work/comfac-opencode`, `work/csama`, `work/comfac-2b`, `work/comfac-local-models`, `work/comfac-openwebui`, `work/comfac-research-tools`, `work/comfac-s2s`, `work/comfac-synopsys`, `work/android-matrix-model`, `work/open-agents`, `work/local-ai-progressive-web-apps` — each at `skills/backup-basics.md`. Sync from this file; never edit a copy.
> **Doctrine + tier table:** `work/CGG-Hardening/backup-strategy/README.md` → Data classification · registered in `agents-justin.md` §irrf.

# Backup basics — replaceable vs irreplaceable

Every file in this estate falls into one of two classes, and the class decides how it is protected.

## Irreplaceable (`irrf`)

**Data created here that exists nowhere else** — if it is lost, it is gone:

- authored docs, manuscripts, notes, logs, plans
- scripts, configs, Modelfiles, skill definitions
- DB dumps, calibration JSONs, extracted training data
- `.brc` / `.ssh` / `.gnupg` credential and key material

The machine-readable set is `scripts/backup/workfiles-classify.conf` (the `IRREPLACEABLE|` rows).

**Rule: an irrf original is not safe until the whole chain verifies** — Forgejo (tier 1, git repos) + a drive-D hardlinked generation (tier 2) + Synology (tier 3) — each checked **from the destination** (file count + bytes), never from the sender's exit code. Deleting or "cleaning up" an irrf original before the chain verifies is the failure mode behind all four data-loss RCAs (`agents-justin.md` §SST).

## Replaceable (reproducible bulk)

**Bytes that can be re-downloaded or regenerated** — model weights/GGUFs, game libraries, installers, re-downloadable media, pip/npm/uv caches, build outputs:

- **Never enter git** ("big binaries never enter git" — models, weights, media, backups).
- **Excluded from backup tiers by design** — this is why the tier-2/tier-3 payload is tens of GB, not the hundreds of GB on the drive roots.
- The *knowledge* about them is irreplaceable even when the bytes are not: the config, the Modelfile, the manifest of where the bytes came from (Justin, 2026-08-15: "reproducible = the *bytes* that can be re-downloaded, never the knowledge"). Track those; skip the bulk.
- Every ≥50 MB gitignored payload should have a re-fetch manifest — audited by `gen-reproducible-manifest.sh --check` in the EOD run.

## Symptom to watch

A backup destination growing toward source-drive size means reproducible bulk leaked into the classified set — re-check `workfiles-classify.conf` before assuming the growth is real.

## The three tiers

| Tier | Destination | Carries | Deletes at destination? |
|---|---|---|---|
| 1 | Forgejo (`git.gi7b.org`, `git.comfac-it.net`) | git repos | N/A (git history) |
| 2 | drive-D (`x555qaHDD`), dated hardlinked generations | consolidated irrf | Never (`--delete` refused in code) |
| 3 | Synology NAS (`nas240404`), off-machine | `opencode260220` irrf subtree | Never (`--delete` stripped + asserted) |

Nextcloud sync is **never** a backup — a sync propagates deletions.
