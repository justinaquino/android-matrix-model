# CSAMA Mobile: Distilling the Companion

> **AMM** = Android Matrix Models = the pocket *kasama*  
> **Goal:** Prove that CSAMA's curated training data makes sub-3B models genuinely useful on Android phones.

---

## The Core Hypothesis

CSAMA trains on **8 GB VRAM desktops** with 7B-parameter models. The output is:
- Domain-specific LoRAs (bash, python, npm, web)
- A scored, validated Vector DB of proven solutions
- A flywheel of student challenges → validated entries → training pairs

**AMM asks:** Can we take that same curated data and make a **1.7B–3B model** on a phone actually helpful?

We believe yes — because:
1. The VDB carries the **syntax and facts** the small model forgets
2. The LoRAs encode **workflow patterns** that generalize across scales
3. The validation layer guarantees every training example is **high-signal**, which matters more for small models than big ones

---

## Model Roadmap

### Phase 1: Baseline (now)
- **Model:** Generic Qwen2.5-Coder-3B-Instruct Q4_K_M
- **Runtime:** llama.cpp via AMM
- **Test:** Can it follow a simple bash one-liner with in-context VDB examples?
- **Pass criteria:** ≥ 70 % accuracy on 50 CSAMA bash challenges

### Phase 2: Distilled LoRA (Month 1)
- **Base:** Qwen2.5-Coder-3B-Instruct
- **Training data:** CSAMA bash/python/npm VDB entries (top 500 per domain)
- **Method:** QLoRA rank 16, 1 epoch, Unsloth
- **Output:** `csama-bash-3b-lora`, `csama-python-3b-lora`, `csama-npm-3b-lora`
- **Merge:** Merge LoRA into base → GGUF Q4_K_M for AMM
- **Test:** Same 50 challenges, now without few-shot context (model must remember the pattern)
- **Pass criteria:** ≥ 80 % accuracy

### Phase 3: On-Device RAG (Month 2)
- **VDB subset:** Top 1,000 entries per domain, embedded with `all-MiniLM-L6-v2`
- **Storage:** SQLite + `smolvectordb` stub module (already in AMM)
- **Retrieval:** Embed query → fetch top-3 → inject into prompt
- **Context budget:** 1,024 tokens total (retrieval + user query + response)
- **Test:** Open-ended student challenges from the field
- **Pass criteria:** Model escapes error loops using VDB failure→recovery pairs

### Phase 4: Slim Specialist (Month 3)
- **Model:** SmolLM2-1.7B or Qwen2.5-Coder-1.5B (if released)
- **Training:** Full SFT on CSAMA corpus (5,000+ validated entries)
- **Target:** Run smoothly on 4 GB RAM devices (entry-level Android)
- **Trade-off:** More token/s, less depth. Best for retrieval-heavy tasks.
- **Pass criteria:** Usable latency (< 3s to first token) + ≥ 75 % accuracy

---

## Training Data Pipeline

```
CSAMA Desktop Forge
    ├── VDB entries (validated, scored)
    ├── LoRA adapters (rank 32–64, 7B base)
    └── DPO preference pairs (success vs. failure)
            ↓
    Distillation Script (runs on desktop)
    ├── Extract high-scoring entries per domain
    ├── Generate synthetic conversations for 3B scale
    ├── Run through 7B teacher + VDB for "best answer"
    └── Package as instruction-completion JSONL
            ↓
    Mobile Training (Unsloth, 8 GB VRAM)
    ├── QLoRA rank 16 on Qwen2.5-Coder-3B
    ├── 1 epoch, batch 1, grad accum 4
    └── Export merged GGUF Q4_K_M
            ↓
    AMM On-Device
    ├── Load GGUF into llama.cpp
    ├── Load domain-specific system prompts
    └── Sync VDB subset via Syncthing / Forgejo release
```

---

## Mobile Constraints vs. Desktop

| Factor | Desktop CSAMA | Mobile AMM |
|--------|---------------|------------|
| **Weight budget** | 8 GB VRAM (6 GB model + 2 GB context) | 4–8 GB RAM shared with OS |
| **Model size** | 7B Q4 (~4 GB) | 3B Q4 (~1.5 GB) or 1.7B Q4 (~900 MB) |
| **Context** | 2k–4k tokens | 1k tokens max comfortable |
| **Speed** | 10–30 tok/s (GPU) | 2–8 tok/s (CPU NEON) |
| **Battery** | Unlimited | 5–15 min sustained before thermal throttling |
| **Storage** | Terabytes | 2–4 GB free for models |
| **Offline** | Always | Always — this is the point |

**Design implication:** The mobile companion is **retrieval-first, generation-second**. It fetches a proven snippet from the VDB, validates it, and presents it. Generation is reserved for when no match exists.

---

## AMM-Specific Integration Points

### 1. Model Download from Forgejo
AMM currently pulls from HuggingFace Hub. We will add a **CSAMA model registry**:
```
GET https://git.comfac-it.net/api/v1/repos/justin/csama/releases/latest
→ Download `csama-bash-3b-q4.gguf`
```

### 2. System Prompt per Domain
AMM's task mode already supports ephemeral inference. We will add CSAMA domain presets:
- **Bash companion:** "You are a Bash specialist for Ubuntu 22.04..."
- **Python companion:** "You are a pandas helper for data cleaning..."
- **NPM companion:** "You are a Node.js deployment assistant..."

### 3. VDB Sync
`smolvectordb` module (stub, not yet wired) will:
- Store embeddings locally in SQLite
- Sync delta updates from CSAMA desktop via Syncthing
- Allow retrieval-augmented chat without network

### 4. HTTP API for External Apps
Already works (`127.0.0.1:8765`). CSAMA skills will expose:
```bash
POST /csama/chat
  { "domain": "bash", "query": "rename all .txt to .bak" }
→ { "solution": "for f in *.txt; do mv ...", "confidence": 0.94, "source": "vdb" }
```

---

## Evaluation: What "Useful" Means on a Phone

We do not expect the 3B phone model to match the 7B desktop model. We expect it to be **reliably helpful for narrow tasks**:

| Task | 7B Desktop | 3B Mobile | 1.7B Mobile |
|------|------------|-----------|-------------|
| Bash one-liner | ✅ 95 % | ✅ 85 % | ⚠️ 70 % (with VDB) |
| pandas 5-liner | ✅ 90 % | ✅ 75 % | ⚠️ 60 % |
| npm scaffold | ✅ 92 % | ✅ 80 % | ⚠️ 65 % |
| HTML skeleton | ✅ 95 % | ✅ 88 % | ✅ 75 % |
| Debug webpack | ⚠️ 50 % | ❌ 20 % | ❌ 10 % |
| Refactor 3 files | ⚠️ 60 % | ❌ 25 % | ❌ 15 % |

**Green = phone can do it. Yellow = needs VDB help. Red = escalate to desktop.**

---

## Success Metrics

After 3 months:
1. **Model zoo:** At least 3 domain-specific GGUFs (bash, python, npm) running on AMM
2. **Accuracy:** ≥ 80 % on CSAMA validation suite (with VDB retrieval)
3. **Latency:** First token < 3s on mid-range Android (Snapdragon 7-series)
4. **Adoption:** 2+ Comfac teams using AMM as field reference tool
5. **Feedback loop:** 50+ new mobile-generated challenges feeding back into CSAMA VDB

---

## Related Documents

- `repoanalysis.md` — Full technical analysis of AMM codebase
- `vision-models-fdr.md` — Vision model integration plan (bp-app OCR)
- `../csama/docs/PORTABLE_COMPANION.md` — CSAMA desktop/laptop/mobile sync strategy
- `../csama/docs/ARCHITECTURE.md` — VDB + LoRA architecture
- `../csama/training/pipeline/lora_training.md` — Training procedures

---

*The pocket kasama does not need to be a genius. It needs to be a reliable partner that remembers what the team proved works — and fits in your pocket.*
