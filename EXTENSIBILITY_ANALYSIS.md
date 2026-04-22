# AMM Extensibility Analysis: Adding Capabilities Without Building an APK

**Date:** 2026-04-22  
**Scope:** Third-party developers, power users, integrators  
**Question:** *How can people extend AMM's capabilities without forking the Android codebase and building an APK?*

---

## 1. The Core Problem

AMM is a native Android app (Kotlin + C++). Traditionally, adding a feature means:
1. Forking the repo
2. Writing Kotlin/C++ code
3. Building with Android Studio / Gradle
4. Signing and distributing an APK

This is a high barrier for web developers, data scientists, and hobbyists. The goal is to expose **extension points** that let third parties ship capabilities as **data, not code**.

---

## 2. Extension Points Already in AMM

### 2.1 HTTP API (Zero-Code Extensions)

**What it is:** AMM runs NanoHTTPD on `127.0.0.1:8765`. Any app — PWA, Python script, Tasker automation, or another Android app — can call it.

**Current endpoints:**
```
GET  /health   → {"status":"ok"}
GET  /status   → {"vision_model_loaded": true, "model_name": "..."}
POST /vision   → multipart(image + prompt) → JSON response
```

**How to extend without APK:**
- Write a PWA in HTML/JS that calls `fetch('http://127.0.0.1:8765/vision')`
- Write a Python script on a laptop that proxies to AMM over ADB reverse port forwarding
- Build a Home Assistant integration that hits the HTTP API

**Limitation:** The HTTP API surface is small today. New endpoints require an APK update (or the PWA Store FDR above).

---

### 2.2 GGUF Model Sideloading (Data-Only Extensions)

**What it is:** Users can import any `.gguf` file into AMM. The model's metadata (chat template, context size) is read automatically.

**How to extend without APK:**
- Download a new vision model from HuggingFace (text + mmproj)
- Import it via AMM's "Add New Model" flow
- The model is immediately available for chat or vision inference

**Limitation:** The model must be in GGUF format. You cannot ship a custom tokenizer, preprocessing pipeline, or post-processing logic.

---

### 2.3 JavaScript Bridge (WebView-First Extensions)

**What it is:** `BrowserActivity` exposes `window.ammAndroid` to any page loaded in its WebView.

**Current API:**
```javascript
ammAndroid.isEmbedded();      // true
ammAndroid.getAmmVersion();   // "1.0.2"
```

**How to extend without APK:**
- Build a web app that detects `window.ammAndroid`
- Call bridge methods to query AMM state
- Use `fetch()` to the localhost HTTP service for inference

**Proposed expansion (see FDR-060):**
```javascript
ammAndroid.getCapabilities();     // {vision: true, chat: true}
ammAndroid.getModelStatus();      // {loaded: true, name: "qwen2.5-vl-3b"}
ammAndroid.shareText("...");      // Android share sheet
ammAndroid.loadModel("model-id"); // Switch models programmatically
```

---

## 3. Extension Points That Require Design Work

### 3.1 Plugin Manifest (JSON Extensions)

**Concept:** AMM reads a `plugin.json` file from a well-known directory (e.g., `/sdcard/AMM/plugins/my-plugin/`).

```json
{
  "name": "Calorie Scanner",
  "version": "1.0.0",
  "entrypoint": "index.html",
  "permissions": ["vision", "share"],
  "icon": "icon.png",
  "models": ["qwen2.5-vl-3b"],
  "prompts": {
    "scan": "Estimate calories in this food photo. Return JSON."
  }
}
```

**How it works:**
- User drops a folder into `AMM/plugins/`
- AMM scans on startup and registers the plugin as a tile
- Launching it opens `BrowserActivity` pointing to the local `index.html`
- The JS bridge grants only the declared permissions

**Effort:** Medium. Requires:
- File-system watcher or scan-on-launch logic
- Permission model in the bridge
- Plugin UI in Vision Hub

---

### 3.2 Prompt Packs (Template Extensions)

**Concept:** Ship structured prompt templates that appear as "Tasks" in AMM's chat UI.

```json
{
  "name": "Medical Symptom Checker",
  "system_prompt": "You are a triage assistant. Ask clarifying questions.",
  "user_prompt_template": "Patient reports: {{symptoms}}",
  "model_filter": "medical",
  "icon": "stethoscope.png"
}
```

**How it works:**
- Drop `.task.json` files into `AMM/tasks/`
- AMM imports them as pre-built Task entries
- User selects the task from the chat drawer

**Effort:** Low. AMM already has a `Task` entity. Just need an import scanner.

---

### 3.3 Custom Model Hubs (Configuration Extensions)

**Concept:** Instead of hardcoding the HuggingFace API, let users add custom model registries.

```json
{
  "hubs": [
    {
      "name": "My Company Models",
      "search_url": "https://models.mycompany.com/api/search?q={query}",
      "download_url": "https://models.mycompany.com/download/{model_id}"
    }
  ]
}
```

**How it works:**
- AMM reads `hubs.json` from internal storage
- HF search screen queries all registered hubs
- Results are merged and shown uniformly

**Effort:** Medium. Requires abstracting the current `HFModelsAPI` into a pluggable interface.

---

### 3.4 WASM / WASI Runtime (Code Extensions Without APK)

**Concept:** Embed a WebAssembly runtime (e.g., WasmEdge, Wasmer) inside AMM. Users ship `.wasm` modules.

**Use cases:**
- Custom image preprocessing filters (resize, denoise, rotate)
- Custom text post-processing (regex extraction, JSON validation)
- Lightweight adapters for non-GGUF models

**How it works:**
- AMM exposes a WASM host with imports for file I/O and HTTP
- User drops `my-preprocessor.wasm` into `AMM/wasm/`
- AMM calls it before/after inference

**Effort:** High. Requires:
- WASM runtime native library (~5-10 MB)
- Host function bindings (JNI)
- Security sandboxing

---

### 3.5 Lua / Python Scripting (Script Extensions)

**Concept:** Embed a lightweight scripting engine (e.g., LuaJ, Chaquopy) for user scripts.

**Lua example:**
```lua
-- AMM calls this before every vision request
function preprocess_image(image_bytes)
    -- rotate 90 degrees, resize to 640x480
    return transform.rotate(image_bytes, 90)
end
```

**Effort:** Medium-High. LuaJ is small (~200 KB) and pure Java. Python via Chaquopy is heavier but more familiar to ML developers.

---

## 4. Comparison Matrix

| Extension Type | Skill Required | Distribution | Sandboxing | Effort to Implement |
|----------------|--------------|--------------|------------|---------------------|
| HTTP API client | Web dev | URL / PWA | Strong (localhost only) | ✅ Already exists |
| GGUF model | ML hobbyist | File download | N/A (data only) | ✅ Already exists |
| JS Bridge PWA | Web dev | URL / zip | Strong (WebView) | 🟡 FDR-060 |
| Plugin Manifest | Web dev | Folder / zip | Medium (permission model) | 🟡 Medium |
| Prompt Packs | Power user | JSON file | N/A (data only) | 🟢 Low |
| Custom Model Hubs | DevOps / admin | JSON config | N/A (config only) | 🟡 Medium |
| WASM modules | Rust/C++ dev | `.wasm` file | Strong (WASM sandbox) | 🔴 High |
| Lua scripts | Scripter | `.lua` file | Medium (capability model) | 🟡 Medium |

---

## 5. Recommended Roadmap

### Phase 1: PWA Store (Immediate)
Implement **FDR-060**. This gives the biggest bang for buck:
- Any web developer can extend AMM
- No new native dependencies
- Works with existing HTTP API and JS bridge
- Distribution is just a URL

### Phase 2: Prompt Packs (Easy Win)
Add a file-system scanner for `.task.json` files in `AMM/tasks/`.
- Zero security risk (just prompts)
- Power users can share task templates
- Community can build prompt libraries

### Phase 3: Expanded JS Bridge + Plugin Manifest
- Add `ammAndroid.*` methods for model switching, sharing, clipboard
- Support offline plugin folders with `plugin.json`
- Permission model: user must approve plugin permissions at install time

### Phase 4: WASM or Lua (Advanced)
Only if Phase 1-3 prove insufficient. WASM gives maximum power but maximum complexity.

---

## 6. Security Principles for All Extensions

1. **No filesystem access from scripts/PWAs** — only via explicit bridge calls
2. **No network access beyond localhost** — PWAs can fetch the internet, but AMM endpoints are localhost-only
3. **User consent for every permission** — camera, microphone, model loading require explicit approval
4. **Extension isolation** — each PWA/plugin gets its own WebView cache and storage scope
5. **No silent model downloads** — any model fetch > 50 MB requires Wi-Fi + user confirmation

---

## 7. Conclusion

AMM does **not** require an APK rebuild for most extensions today. The HTTP API and GGUF sideloading already enable:
- New AI models (drop in a GGUF)
- New frontends (build a PWA that calls localhost)

With **FDR-060 (PWA Store)** and **Prompt Packs**, AMM becomes a platform where:
- Web developers ship features as URLs
- Power users ship workflows as JSON
- Only core engine improvements (new model architectures, GPU backends) require native code changes

The long-term goal is to make AMM feel like **"VS Code for on-device AI"** — a stable native host with a rich ecosystem of extensions that ship as data, not binaries.
