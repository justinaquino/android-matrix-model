# `android-csama` — charter for the restructure of AMM

> **Author:** Claude Opus 5 (1M context)
> **Date:** 2026-08-05
> **Methodology:** Records Justin's ruling and reconciles it against what this repo already contains
> (README read, roster measured from PC00 `/api/tags`). **No rename or remote change performed** — §5
> explains why that is Justin's action, not an agent's.
> **Status:** 🟠 CHARTER — the goal is set; the restructure is not done.

**Justin's ruling, 2026-08-05:**

> *"We will have android-csama — this one is the android-matrix-model restructured to have the goal like
> csama but for android devices."*

---

## 1. What changes, in one line

**AMM stops being `csama`'s *runtime* and becomes `csama`'s *phone tier*** — with the same three
deliverables as the desktop programme, scoped to Android hardware.

| | Before (AMM as written) | After (`android-csama`) |
|---|---|---|
| Self-description | *"the **Android execution layer** for CSAMA"* — a runtime that proves the desktop training data works on phones | **a programme with the same goal as `csama`, for Android devices** |
| Owns | the app: a SmolChat fork, APKs, on-device chat | **benchmarks · optimisations · tools for running** — on phones. Plus the app |
| Relationship to `csama` | downstream consumer | **sibling tier.** Same goal, same discipline, different hardware class |

`csama` owns *"benchmarks and optimizations and tools for running — laptops/desktops"* (Justin,
2026-08-05). **`android-csama` is that sentence with "phones" substituted.** The app is the delivery
vehicle, not the mission.

## 2. What the repo already gets right — keep it

The existing `README.md` is closer to this than the rename suggests. It already:

- names itself the mobile side of CSAMA and cites *kasama* correctly;
- carries a **desktop-vs-mobile comparison table** — the right instinct, and the skeleton of the
  benchmark this charter asks for;
- states mobile inference as **2–8 tok/s (CPU)** against desktop 10–30 tok/s (GPU);
- ships real artifacts — `AMM_v1.1.2` … `v1.1.5` APKs, an `app/` tree, `260422-errors/`, and
  `BP-APP-DETECTION-RCA.md`, i.e. it already has a defect record;
- names candidate models in the right class: **Qwen2.5-Coder-3B Q4 or SmolLM-1.7B**.

**Do not restart this repo.** The restructure is a re-aiming, not a rewrite.

## 3. What is stale and must be corrected

| Claim in `README.md` | Status | Correction |
|---|---|---|
| *"Desktop (CSAMA): Qwen2.5-Coder-7B Q4 + domain LoRAs"* | ✅ **still right, and now better supported** — measured at **4.68 GB**, inside the new ≤5 GB band | keep; cite the measured size |
| *"VRAM/RAM: 8 GB **dedicated**"* for desktop csama | 🔴 **stale.** `csama` is explicitly the **no-dedicated-GPU** tier — 31% of surveyed interns have no dedicated GPU and 18% do not know | rewrite as **≤5 GB to run, CPU / integrated graphics** |
| *"Mobile: 4–8 GB shared with OS"* | 🟡 unverified — no measurement on record | keep as a target; mark it as a target until measured |
| *"2–8 tok/s (CPU)"* mobile | 🟡 **provenance unknown** | either cite the measurement or mark it an estimate. §4.8 of the CPU brief proposes **≥10 tok/s** as the desktop floor; the phone floor needs its own number |
| Model tiers implying csama is 7B-only | 🟡 | the band admits **3 in-band candidates**: `qwen2.5-coder:7b-instruct-q4_K_M` (4.68 GB), `qwen2.5:7b` (4.68 GB), `llama3.2:3b` (2.02 GB) |

**Band note.** `android-csama` inherits the ≤5 GB band on paper, but **a phone's real ceiling is lower and
is set by RAM shared with the OS, thermals and the app's memory budget** — not by disk size. The honest
statement is *"≤5 GB by band, and whatever the device actually sustains"*, and the second half needs
measuring. Canonical band table: `agents-justin.md` §`csama` → *The size bands*.

## 4. What `android-csama` must produce that AMM does not yet

Mirroring `csama`'s three responsibilities:

### 4.1 Benchmarks — the phone-class harness

`csama` has an executed context-capacity sweep and an inherited eval suite (`shmodels`, ITK branch
`synology-directory-log`). **Android has neither.** Needed, in order:

1. **A device inventory** — the actual phones available for testing: SoC, RAM, Android version. *This does
   not exist and nothing can be measured without it.*
2. **Prefill vs decode, separately** — the same CPU cliff as the desktop brief §4.1, and worse on a phone:
   thermal throttling turns a sustained prefill into a different machine after two minutes.
3. **A sustained-session trace** — tok/s over 20 minutes, with temperature if obtainable. **On a phone this
   is the headline metric, not a footnote**: a model that is fast for one prompt and throttled by the
   tenth has not shipped.
4. **Cold start and RAM high-water** — Android will kill the app; measure where.
5. **The same task classes as the desktop tier**, so the two are comparable: error-fixing first (the
   56%-named problem), then retrieval/scaffolding, which the README already identifies as mobile's role.

### 4.2 Optimisations — the Android-specific levers

Research questions, to be answered with numbers and sources (the desktop brief's §4 is the template):

- **NPU / DSP delegates** — Qualcomm QNN / Hexagon, Google Tensor, MediaTek APU: reachable at all from a
  llama.cpp-derived stack, or GPU/CPU only in practice?
- **GPU offload on mobile** — OpenCL / Vulkan on Adreno and Mali; whether it beats the big cores or just
  drains battery faster.
- **Quant formats for ARM** — **`Q4_0` repacking (`Q4_0_4_4` / `4_8` / `8_8`) is ARM-targeted**, so it is
  more likely to matter here than on x86. Measure it here first, and share the finding with the desktop
  tier.
- **big.LITTLE scheduling** — thread count and core affinity; pinning to performance cores vs letting the
  scheduler migrate.
- **Memory-mapped weights and storage speed** — cold start is dominated by I/O on a phone.
- **Battery per 1k tokens** — a metric the desktop tier does not need and the phone tier cannot ship
  without.

### 4.3 Tools for running

- an installer/first-run path that **detects the device's own capability** and picks a model. The desktop
  brief flags the same need for a different reason: **18% of surveyed interns could not say whether their
  machine has a GPU.** On Android, asking is even less viable.
- model download/verify with a size and sha check;
- the VDB subset sync (README already specifies *"embedded top-1k subset"*) and LoRA sync via
  Forgejo + Syncthing, per `csama`'s deliverable line.

## 5. The restructure itself — what I did not do, and why

**Not performed:** repo rename, remote rename, app package/ID change, or any edit to `README.md`.

- A Forgejo repo rename is **outward-facing** — it breaks clones, remotes and any link in
  `agents-justin.md`, `PORTFOLIO-lae-260803.md` and this repo's own docs. That is your call.
- The app's **package identifier** is a shipping-artifact decision: changing it makes existing installs of
  `AMM_v1.1.5` a different app, with no upgrade path. It may be right to keep the package ID and change
  only the display name and the docs.
- **The name in the ruling is `android-csama`.** Recorded as written. Note the shorthand `AMM` is already
  in `agents-justin.md`'s abbreviation table and in the portfolio roster, so a rename means editing both,
  and `amcs`/`acsama` would need choosing as the abbreviation.

**Proposed sequence, for your approval:**

| # | Step | Owner |
|---|---|---|
| 1 | Confirm the name and pick the abbreviation | **Justin** |
| 2 | Decide: rename the repo, or keep the path and re-aim the docs? | **Justin** |
| 3 | Decide: change the app package ID, or display name only? | **Justin** |
| 4 | Rewrite `README.md` §CSAMA to the sibling-tier framing; fix the stale rows in §3 above | agent |
| 5 | **Device inventory** — the blocking prerequisite for every benchmark in §4.1 | **Justin** (hardware) |
| 6 | Update `agents-justin.md` roster + abbreviation table, and `PORTFOLIO-lae-260803.md` | agent |
| 7 | Port the desktop brief's §4.8 "define acceptably before measuring" to phone floors | agent + **Justin** |

## 6. The one thing that blocks everything

**There is no device inventory.** `csama` at least has PC00 and a documented gap (no Windows laptop
without a dedicated GPU). `android-csama` has **APKs and no recorded test device** — no SoC, no RAM figure,
no Android version anywhere in the repo. Every benchmark in §4.1 and every optimisation in §4.2 needs one
real phone on the record first.

**This is the same shape as the desktop tier's blocker** (*"get one real Windows laptop with no dedicated
GPU into the loop"*), and it has the same fix: name a device, write down its specs, measure one thing.

---

## Cross-links

- Canonical size bands: `agents-justin.md` §`csama` → *The size bands*
- Desktop CPU brief, the template for §4.2: `work/csama/docs/260804-cpu-model-slate-and-research-brief.md`
- Requirements basis: `work/csama/docs/Student_AI_Needs_Survey_Report.pdf` (n=39; **coding 85%**, **no
  dedicated GPU 31% + unsure 18%**, **56% name error-fixing as the #1 problem**)
- Existing eval suite to inherit rather than rebuild: ITK branch `synology-directory-log`,
  `skills/shmodels/` — and **RCA-015** on why it looked absent
- Portfolio roster: `work/comfac-operations/PORTFOLIO-lae-260803.md`

---

*Written 2026-08-05. Nothing renamed, nothing measured. The goal is set; the device is missing.*
