# FDR-060: PWA Store Tab in Vision Hub

**Status:** Draft  
**Date:** 2026-04-22  
**Author:** AMM Engineering  
**Related:** `BrowserActivity`, `VisionHubActivity`, `HttpService`, bp-app PWA

---

## 1. Executive Summary

AMM currently hosts two ways to interact with AI models:
1. **Chat** — text-only conversations via SmolLM
2. **Vision Hub** — on-device VLM inference + HTTP service for external apps

The only external app that can reach AMM today is **bp-app** (a browser-hosted PWA). Users must open Chrome, navigate to the PWA URL, and hope AMM's HTTP service is running. There is no first-class way to discover, install, or launch PWAs from inside AMM itself.

This FDR proposes a **"Apps" tab inside Vision Hub** that acts as a lightweight PWA store / launcher. PWAs are registered in AMM (not the Android OS), cached locally, and launched in an embedded WebView with full access to the AMM HTTP API.

---

## 2. Goals

| Goal | Priority |
|------|----------|
| Let users add third-party PWAs to AMM without sideloading APKs | P0 |
| Provide a discoverable "Apps" tab in Vision Hub | P0 |
| Cache PWA assets offline so they work without internet | P1 |
| Expose a stable JavaScript bridge so PWAs can call AMM capabilities | P1 |
| Allow third-party developers to ship capabilities as URLs, not APKs | P1 |

---

## 3. User Stories

### 3.1 End User
> "I installed AMM for the blood-pressure OCR. Now I want to add a medication-reminder PWA that also uses AMM's vision API. I tap 'Apps' in Vision Hub, paste the PWA URL, and it appears as a tile. I can launch it even when offline."

### 3.2 Developer
> "I built a calorie-scanner PWA that calls `POST /vision` on AMM. Instead of telling users to open Chrome and bookmark my site, I tell them to paste `https://myapp.github.io/calscan/` into AMM's Apps tab. AMM handles caching, offline access, and the AMM bridge automatically."

---

## 4. Proposed Architecture

```
┌─────────────────────────────────────────────┐
│  VisionHubActivity                          │
│  ┌─────────┬─────────┬─────────┐           │
│  │ Status  │  Load   │  Apps   │  ← NEW    │
│  │ (svc)   │ (model) │ (PWA)   │           │
│  └─────────┴─────────┴─────────┘           │
│                                             │
│  [Apps Tab UI]                              │
│  ┌─────────────────────────────────────┐   │
│  │  + Add PWA (URL input)              │   │
│  │                                     │   │
│  │  ┌─────────┐ ┌─────────┐           │   │
│  │  │ bp-app  │ │ calscan │  tiles    │   │
│  │  │ [icon]  │ │ [icon]  │           │   │
│  │  └─────────┘ └─────────┘           │   │
│  └─────────────────────────────────────┘   │
│                     │                       │
│                     ▼                       │
│         ┌──────────────────┐               │
│         │ BrowserActivity  │               │
│         │ (embedded WV)    │               │
│         │ with AMM bridge  │               │
│         └────────┬─────────┘               │
│                  │                          │
│     ┌────────────┼────────────┐            │
│     ▼            ▼            ▼            │
│  /health     /vision      /status          │
│  (HttpService on 127.0.0.1:8765)           │
└─────────────────────────────────────────────┘
```

### 4.1 Data Model

New Room entity: `InstalledPWA`

```kotlin
@Entity(tableName = "InstalledPWA")
data class InstalledPWA(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val url: String = "",              // start_url from manifest
    val manifestUrl: String = "",
    val iconBase64: String = "",       // cached icon
    val scope: String = "",            // manifest scope
    val dateAdded: Long = System.currentTimeMillis(),
    val lastOpened: Long = System.currentTimeMillis(),
)
```

### 4.2 UI Flow

**Add PWA Dialog**
1. User taps "+ Add PWA"
2. Dialog asks for URL
3. AMM fetches the page, extracts `link[rel="manifest"]`
4. AMM parses `manifest.json` for `name`, `short_name`, `start_url`, `icons`, `scope`
5. AMM downloads the best icon and stores it as base64 in Room
6. AMM inserts `InstalledPWA` row
7. Tile appears in Apps grid

**Launch PWA**
1. User taps a tile
2. `BrowserActivity` opens with `EXTRA_URL = pwa.startUrl`
3. JavaScript bridge `window.ammAndroid` is injected automatically
4. PWA can call `ammAndroid.getAmmVersion()`, `ammAndroid.isEmbedded()`, etc.
5. PWA can `fetch('http://127.0.0.1:8765/vision', ...)` as normal

**Delete PWA**
1. Long-press tile → "Remove"
2. Deletes Room row and cached icon
3. Clears WebView cache for that scope (optional)

### 4.3 JavaScript Bridge Expansion

Current bridge (`AmmBridge` in BrowserActivity):
```kotlin
@JavascriptInterface fun isEmbedded(): Boolean = true
@JavascriptInterface fun getAmmVersion(): String = "1.0.2"
```

Proposed additions:
```kotlin
@JavascriptInterface fun getCapabilities(): String =
    // returns JSON: { "vision": true, "chat": false, "speech2text": false }

@JavascriptInterface fun getModelStatus(): String =
    // returns JSON: { "vision_model_loaded": true, "model_name": "qwen2.5-vl-3b" }

@JavascriptInterface fun loadVisionModel(modelId: String): String =
    // tells AMM to load a specific vision model by name

@JavascriptInterface fun shareText(text: String) =
    // opens Android share sheet
```

### 4.4 Offline Caching

Use WebView's standard cache (`cacheMode = LOAD_CACHE_ELSE_NETWORK`) plus a custom `WebViewClient` that intercepts requests and stores responses in a local disk cache scoped to each PWA. For MVP, rely on the WebView default cache; advanced offline can come later.

### 4.5 Pre-installed PWAs

Ship AMM with a default list:

| Name | URL | Description |
|------|-----|-------------|
| bp-app | `https://comfac-global-group.github.io/bp-app/` | Blood pressure OCR (existing) |

Users can delete pre-installed PWAs if desired.

---

## 5. Files to Create / Modify

| File | Action | Notes |
|------|--------|-------|
| `data/InstalledPWA.kt` | Create | Room entity + DAO |
| `data/AppDB.kt` | Modify | Add `InstalledPWA` to entities, migration |
| `ui/screens/vision_hub/VisionHubActivity.kt` | Modify | Add "Apps" tab with LazyVerticalGrid |
| `ui/screens/vision_hub/PwaStoreScreen.kt` | Create | Add-PWA dialog, tile grid, delete logic |
| `ui/screens/browser/BrowserActivity.kt` | Modify | Expand `AmmBridge` JS interface |
| `res/values/strings.xml` | Modify | New strings for Apps tab |

---

## 6. Security Considerations

| Risk | Mitigation |
|------|------------|
| Malicious PWA calls HTTP endpoints | Only `127.0.0.1:8765` is exposed; no filesystem access via bridge |
| PWA phishing | Show AMM toolbar with URL when launching; scope enforcement |
| Cache poisoning | Scope-based cache isolation; clear cache on delete |
| Mixed content | Already handled by `MIXED_CONTENT_ALWAYS_ALLOW` in BrowserActivity |

---

## 7. Success Criteria

- [ ] User can add a PWA by URL and see it as a tile in Vision Hub
- [ ] User can launch the PWA inside AMM's WebView
- [ ] PWA can call `fetch('http://127.0.0.1:8765/vision')` successfully
- [ ] Added PWA persists across app restarts
- [ ] User can remove a PWA
- [ ] bp-app ships pre-installed and works out of the box

---

## 8. Out of Scope

- PWA service worker support ( rely on WebView default )
- Push notifications from PWAs
- Background sync
- PWA "store" with search/discovery (just direct URL input for MVP)
- Sandboxing beyond WebView standard security model
