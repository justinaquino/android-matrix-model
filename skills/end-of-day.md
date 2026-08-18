---
name: end-of-day
description: Run the End-of-Day (EOD) routine — log the day, commit + push every repo, and back up the irreplaceable set to all tiers with verification from each destination. Trigger this at the end of a working day or session block, when Justin says "EOD" / "end of day" / "wrap up", when several repos have accumulated unpushed work, or before the workstation will be left idle/off. Also use it whenever a backup run must be *verified* rather than merely *reported*.
---

> **Author:** Justin · skill filed by Kimi Code CLI (agent session), 2026-08-18
> **Canonical location:** `IT-knowledge/skills/end-of-day/SKILL.md`
> **Shared to (all `lae` repos):** `work/comfac-opencode`, `work/csama`, `work/comfac-2b`, `work/comfac-local-models`, `work/comfac-openwebui`, `work/comfac-research-tools`, `work/comfac-s2s`, `work/comfac-synopsys`, `work/android-matrix-model`, `work/open-agents`, `work/local-ai-progressive-web-apps` — each at `skills/end-of-day.md`. Sync from this file; never edit a copy.
> **Full protocol:** `IT-knowledge/system-admin/EOD_Protocol.md` · short version `agents-justin.md` §2 · backup detail `work/CGG-Hardening/backup-strategy/260810-eod-backup-protocol.md`.

# End-of-Day (EOD) routine

**Purpose:** log the day's work, commit + push every git repo, and back up the irreplaceable set to every tier — verified **from each destination**, never trusted from a script's own exit code (doctrine 4).

Run the steps in order from `$OCROOT` (resolve it via `scripts/backup/lib.sh` — never hardcode the path):

```bash
# 1. Log the day (1–3 lines + a pointer to the per-repo log — keep it short)
#    → append to personal/260621-justin-personal.md

# 2. Tier 1 — commit + push every repo to Forgejo
bash "$OCROOT/scripts/backup/backup-forgejo.sh"

# 3. Tier 2 — consolidate irreplaceable content into a hardlinked view,
#    then push a dated generation to drive-D. Never deletes; --link-dest
#    hardlinks unchanged files against yesterday's generation.
bash "$OCROOT/scripts/backup/consolidate-workfiles.sh"
bash "$OCROOT/scripts/backup/backup-workfiles-driveD.sh"

# 4. Tier 3 (Synology) + Nextcloud health + reproducible-manifest check,
#    all in one call; writes the daily report.
#    (Kingston tier DISCONTINUED 2026-08-14 — chronically full.)
bash "$OCROOT/scripts/backup/backup-status.sh" --force
```

## Verify before calling it done

A script printing `OK` is not evidence a file arrived — on 2026-08-09 a backup reported `OK` with `FILES=0` after 92 minutes. Confirm from the actual destination:

- **drive-D:** count files in today's generation (`find <gen> -type f | wc -l`).
- **Synology:** count at the destination over SSH, not the sender's log. Synology's own counters undercount on any run that needed a retry (known, logged bug).

Exact verification queries: `work/CGG-Hardening/backup-strategy/260810-eod-backup-protocol.md`.

## Rules

- Keep EOD entries in `personal/260621-justin-personal.md` to 1–3 lines plus a pointer; the full narrative belongs in the relevant per-repo log.
- **Never add `--delete` to any rsync on OCROOT** — see `agents-justin.md` §CRITICAL.
- An empty or timed-out verification is a **FAILURE**, not a pass.
