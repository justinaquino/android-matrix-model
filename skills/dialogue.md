---
name: dialogue
description: Manage multi-party project logs — consolidating individual contributors' raw notes into one running project Log, and using that Log to keep roadmaps and near-term goals current. Trigger this whenever the user says "let's start a dialogue" (in a project, on a topic, with a team), asks to consolidate notes/logs, mentions a file named like "[date code] [Project] Log.txt", drops a quick dated snippet (e.g. "260730 Justin: ...") into chat, or asks to update a roadmap/milestones from scattered notes. Also use it proactively whenever multiple people's notes for the same project need to be merged into a single source of truth, or when a commit/push is about to happen and per-person logs haven't been folded into the main Log yet.
---

> **Author:** Justin N. Aquino (skill content), saved by Kimi Code CLI (agent session)
> **Date:** 2026-07-30
> **Canonical location:** `tools/IT-knowledge/skills/dialogue/SKILL.md`
> **Shared to (all `lae` repos, widened 2026-08-03):** `work/comfac-opencode`, `work/csama`, `work/comfac-2b`, `work/comfac-local-models`, `work/comfac-openwebui`, `work/comfac-research-tools`, `work/comfac-s2s`, `work/comfac-synopsys` — each at `skills/dialogue.md`. Sync from this file; never edit a copy.
> **Also the canonical Dialogue Method:** `tools/IT-knowledge/ai-instructions/dialogue-method.md` (identical body) — this is what per-project `DIALOGUE.md` files reference. Projects with an established backlog + dated `logs/` convention keep it; match each project's existing filenames rather than imposing this scheme.

# Dialogue

Dialogue is a lightweight protocol for letting several people drop notes on a project asynchronously — from a desk, from a phone, mid-chat — and having those notes folded into one running Log that stays useful for both the long view (roadmap) and the immediate view (what's next).

The core idea: individual notes are cheap and messy on purpose. The Log is where they get organized. You, the agent, are the thing that does that organizing.

## The two kinds of file

**Party notes** — one file per contributor, containing that person's raw, unedited pours. Never rewritten, never deleted, never reorganized. This is the append-only source material. Naming: `[DATECODE] [Person] [Project].txt` (e.g. `260730 Edmund Synopsis.txt`). If a project already has an established date code, reuse it for new party files rather than restamping with today's date — the code identifies the project/thread, not the day of this particular note.

**The Log** — the single consolidated file per project, the thing people actually read. Naming: `[DATECODE] [Project] Log.txt` (e.g. `260730 Synopsis Log.txt`). The date code here should be the date the project started, if that's known or discoverable from the earliest party notes; if it can't be determined, default to the date you're creating the Log. Once set, don't change the Log's date code on later consolidations — it identifies the project, not the edit.

If you're working somewhere these conventions already differ slightly (existing filenames, an established pattern in the folder), match what's already there rather than imposing this scheme.

## When to consolidate

Fold party notes into the Log at three points, treated as transactions:

- **On update** — new content has landed in one or more party files.
- **On commit** — the user is about to commit.
- **On push** — the user is about to push.

Each of these is a discrete transaction, not a bigger or smaller one than the others. That distinction matters for how you read the notes themselves: if a person's raw notes repeat the same point three times across three sittings, that repetition is not itself a signal that the point is three times as important — it's three separate transactions touching the same idea. When you consolidate, dedupe the repetition in the Log's prose, but let the *fact* that it kept coming back across multiple sessions inform your read of momentum (something people keep returning to unprompted is worth flagging), not your read of textual emphasis (bolding or restating something once doesn't outweigh something said plainly once). In short: weigh how often someone brought a topic back up over time, not how many words they spent on it in one sitting.

## "Let's start a dialogue"

When the user says this — about a project, a topic, or with a specific team — it means two things happen together:

1. Do a full consolidation pass right now: read every party file for that project, fold everything into the Log (creating it if it doesn't exist), and note what changed.
2. Make it clear the floor is open — the other parties can now add their own notes, in their own files, whenever they have something. Don't wait for a formal check-in structure; the point is that people can pour in a sentence between meetings and it'll get picked up.

## Opportunistic snippets in chat

Someone may drop a quick, dated, named snippet straight into a conversation instead of writing to a file — typically because they're on a phone. Recognize the pattern: a date code, a name, and a short idea, e.g. "260730 Justin: worried the M3 sensor budget is tight." Treat this exactly like a party-file entry: append it to that person's party file under that project (creating the file if needed), and fold it into the Log at the next transaction point. Don't make the user do anything more formal than that to get a thought captured.

## Reading and writing the Log

The Log is a working document, not an archive dump. When consolidating:

- Organize by theme or workstream, not by "who said what when" — the party files already preserve that chronology if anyone needs to go back to it.
- Attribute ideas to people where it matters (decisions, ownership, open questions), but don't force every sentence to carry a name tag.
- Surface conflicts or open questions explicitly rather than silently picking one version — Dialogue is meant to catch places where two people have different pictures of the same project.
- Keep a clear split between the long view and the immediate view. A workable shape is:
  - **Snapshot** — where the project stands right now, one or two lines.
  - **Roadmap** — the milestones and longer-arc direction, updated as notes shift it.
  - **Next up** — the immediate, near-term goals people are actually working on.
  - **Open questions / conflicts** — anything unresolved that needs a person to weigh in.
  - **Notes log** — the folded, deduped, organized version of what came in, still loosely chronological so people can see the project's recent pulse.

Adjust this shape to fit what the project actually needs — the point of the structure is that anyone can open the Log and get both "where is this headed" and "what do I do this week" without reading every party file.

## Updating roadmaps and milestones

Consolidation isn't just filing — it's the occasion to ask whether the roadmap still holds. When you fold in new notes, check whether anything shifts a milestone, closes one out, or adds a new one, and update the Roadmap section accordingly rather than just appending new text underneath the old. Flag anything you're not confident about changing unilaterally (e.g. a milestone date that only one person mentioned) as an open question rather than silently editing it in.

## Where the Log goes next — the three tiers (added 2026-08-03)

A project Log does not stand alone. Since 2026-08-03 the logging system has three tiers, and
consolidation writes **upward** through them in one work block:

| Tier | File | Holds |
|---|---|---|
| 1. **Project log** | `<project>/logs/YYMMDD-<repo>-log.md` (+ the `DIALOGUE.md` backlog) | everything — the detail, evidence, RCAs, dead ends |
| 2. **Project-group log** (`pgl`) | `work/comfac-operations/PORTFOLIO-<name>-YYMMDD.md` | the cross-project picture: path + outcome + what it changes for the sibling projects |
| 3. **Master log** | `work/comfac-operations/260209_comfac_log.md` (work) · `personal/260601-justin-personal-log.md` (personal) | one short line per group: that it moved, and where to read |

**Why the middle tier exists.** Projects that affect each other — FreeCAD's PCB, CFD, usability and
file-conversion tracks; the `lae` AI projects; sensors/energy — have separate logs but one shared
reality. A decision in one lands on the others, and neither the per-project detail nor the
one-line master summary carries that. The group file does: newest entry at top, every project named
with a path to its own log and the outcome, plus the line that says what it changes for the siblings.

**What this means when you consolidate:** after folding party notes into a project's Log, check
whether that project belongs to a group. If it does, add or update today's entry in the group file
in the same work block — then the master-log line cites the *group*, not each project separately.

**Groups can nest.** A group may itself be a member of another group when it inherits the parent's
platform but pursues goals the parent does not have — `pcb` sits under `freecad` because KiCad is a
FreeCAD workbench, while its own goals are the skills and the production ladder. Entries file in the
sub-group; the parent carries one member row and never restates them.

Full rules, file shape and entry format: `tools/IT-knowledge/ai-instructions/project-group-logs.md`.

## Pruning: a running log plus dated archives (added 2026-08-04)

A log that only ever grows eventually cannot be read, and — worse — cannot be safely *written*, because
every session that holds it in context is holding a bigger stale copy. Both master logs are now split:

- **The running log** keeps its original filename and holds **the present back to a cut point**:
  `work/comfac-operations/260209_comfac_log.md`, `personal/260601-justin-personal-log.md`.
- **Archives** carry the range they cover in the filename — `<stem> <start> to <end>.md`, e.g.
  `260209_comfac_log 260718 to 260731.md`, `260601-justin-personal-log 260530 to 260731.md`. Datecodes
  are `YYMMDD`, matching the older `… Work Log 190801 to 191022.docx` convention.
- **An archive is closed.** Never append to one. New entries always go to the running log; the running
  log's header carries the index of archives and the range each covers.
- The same shape applies to any project log that outgrows one file — same naming, same rule.

**Prune with the tool, not by hand:**

```bash
# healthy log — keep the present, archive the rest
python3 tools/IT-knowledge/scripts/prune_master_log.py --log personal/260601-justin-personal-log.md \
    --title "Justin personal log" --from-rev HEAD --cut 2026-08-01 --apply

# damaged log — union an older revision with every entry added in later commits, then prune
python3 tools/IT-knowledge/scripts/prune_master_log.py --log work/comfac-operations/260209_comfac_log.md \
    --from-rev 953b5c1 --cut 2026-08-01 --apply
```

It is a dry run unless `--apply`, and it refuses to write unless the base reassembles byte-for-byte,
the entry and line counts are conserved, and every entry lands in exactly one output file.

### 🔴 The failure this exists to prevent

On **2026-08-03** the Comfac master log went from **15,828 lines to double digits in one day**, then
oscillated between 41 and 248 across commits. Several parallel sessions each held the file in context,
appended their own entry, and wrote the whole file back — every write silently dropping the others'
entries. It was rebuilt on 2026-08-04 by unioning an older git revision with the entries added in the
21 commits after it: **295 entries recovered, 28 of which no single revision still held.**

**So, on any shared newest-at-top log:**

1. **Re-read the file immediately before you write it** — not at the start of your session.
2. **Prepend only your own block** (`entry + current`). Never reconstruct the file from memory of it.
3. **`git pull` / `git log` before committing a shared log**; rebase before writing, not after.
4. **Never use a whole-file write** on one. Read-modify-write, or a targeted replace.
5. If your own earlier entry has vanished, **recover it from git** (`git log -S "<your text>" -- <file>`)
   rather than retyping, and say in the restored text that it was restored — so the pattern stays visible.

Two parsing traps the tool encodes, worth knowing if you ever regroup a log by hand: these files mix real
entries with **sub-sections that use `##` instead of `###`** (`Commits`, `Done today`, `Specs &
Constraints`) — a `##` heading is an entry only if it carries a date, everything else belongs to the entry
above it; and **within-day order in the source is arbitrary**, so order by explicit `HH:MM` first, then by
the `(later)` / `(later, N)` markers.
