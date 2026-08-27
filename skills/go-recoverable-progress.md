---
name: go-recoverable-progress
description: Run work in small increments that each end with log → commit → push → proceed, so no session loss can destroy more than one increment. Trigger this whenever Justin says "go-rp" or "proceed-rp" (his standing authorization to run the cycle autonomously), when starting any non-trivial body of work in a repo, when a session is long-running or about to pause/end with unpushed changes, or when deciding whether a piece of work is "done" — it is not done until it is pushed.
---

> **Author:** Justin, 2026-08-14 · skill filed by Kimi Code CLI (agent session), 2026-08-18
> **Canonical location:** `IT-knowledge/skills/go-recoverable-progress/SKILL.md`
> **Shared to (all `lae` repos):** `work/comfac-opencode`, `work/csama`, `work/comfac-2b`, `work/comfac-local-models`, `work/comfac-openwebui`, `work/comfac-research-tools`, `work/comfac-s2s`, `work/comfac-synopsys`, `work/android-matrix-model`, `work/open-agents`, `work/local-ai-progressive-web-apps` — each at `skills/go-recoverable-progress.md`. Sync from this file; never edit a copy.
> **Canonical method write-up:** `IT-knowledge/ai-instructions/recoverable-progress-rp.md` · registered in `agents-justin.md` §go-rp · group-log record `PORTFOLIO-lae-260803.md` (2026-08-14 entry).

# Go-Recoverable-Progress (`go-rp` / `proceed-rp`)

Work in increments small enough that each one ends with the full cycle:

1. **log** — append to the per-repo log (newest-first, dated lines; never rewrite existing entries). One or two lines plus a pointer to the detail file.
2. **commit** — every repo the increment touched. Nested repos are committed and pushed by hand; superproject gitlink bumps are the human's step unless the repo's AGENTS.md says otherwise.
3. **push** — immediately, in the same breath as the commit.
4. **proceed** — start the next increment without asking.

**No increment is done until it is pushed. Unpushed work is unrecoverable work.** This estate has four data-loss RCAs (`IT-knowledge/rca/`, table in `agents-justin.md` §SST); three of them destroyed or orphaned unpushed state.

## What `go-rp` means when Justin says it

Standing authorization to run the cycle autonomously — log, commit, push, proceed, repeatedly — interrupting him only at the decision points the project's own plan reserves for him.

It is **not** a license to skip the log ("silent work is invisible work" — the RLC rule in every repo's AGENTS.md), and it is **not** a license for destructive git operations — `reset` / `rebase` / `force-push` still need explicit per-action approval.

## Why increments, not sessions

An increment is the largest unit of work whose loss you can shrug off. Context compaction, a crashed session, a Nextcloud sync incident — all of these truncate mid-session. If the last push was twenty minutes ago, the truncate costs twenty minutes. If it was yesterday, it costs the day.
