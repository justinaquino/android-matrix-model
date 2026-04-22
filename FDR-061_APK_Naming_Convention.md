# FDR-061: APK Naming Convention for GitHub Releases

**Status:** Approved  
**Date:** 2026-04-22  
**Scope:** CI/CD, release management, sideload distribution  

---

## 1. Problem

The default Android Gradle build produces APKs named `app-debug.apk` or `app-release-unsigned.apk`. These names:
- Carry no version information
- Are indistinguishable across releases
- Force users to rename files manually after download
- Make support tickets harder («I have app-debug.apk from somewhere»)

---

## 2. Decision

All APKs attached to GitHub Releases **must** follow the template:

```
AMM_v{VERSION}_{VARIANT}.apk
```

| Segment | Meaning | Example |
|---------|---------|---------|
| `AMM` | Product name (Android Matrix Models) | fixed |
| `v{VERSION}` | Semantic version from `CHANGELOG.md` / git tag | `v1.1.0` |
| `{VARIANT}` | Build type | `debug` or `release` |
| `.apk` | Android package extension | fixed |

### Examples

| Git Tag | Build Command | Release Asset Name |
|---------|--------------|-------------------|
| `v1.1.0-amm` | `./gradlew assembleDebug` | `AMM_v1.1.0_debug.apk` |
| `v1.1.0-amm` | `./gradlew assembleRelease` | `AMM_v1.1.0_release.apk` |
| `v1.2.0-beta-amm` | `./gradlew assembleDebug` | `AMM_v1.2.0-beta_debug.apk` |

---

## 3. Implementation Options

### Option A — Gradle `applicationVariants` (Recommended)

Add to `app/build.gradle.kts`:

```kotlin
android {
    applicationVariants.all {
        val variant = this
        variant.outputs.all {
            val versionName = variant.versionName
            val buildType = variant.buildType.name
            outputFileName = "AMM_v${versionName}_${buildType}.apk"
        }
    }
}
```

**Pros:**
- Automatic — zero manual steps
- Works for local builds, CI builds, and Android Studio
- Version name is pulled from `build.gradle.kts` automatically

**Cons:**
- Requires a one-time Gradle change

### Option B — Shell rename before upload (Interim)

In the release script / manual workflow:

```bash
./gradlew assembleDebug
mv app/build/outputs/apk/debug/app-debug.apk \
   app/build/outputs/apk/debug/AMM_v$(cat VERSION.txt)_debug.apk
gh release upload $TAG AMM_v*_debug.apk
```

**Pros:**
- No Gradle changes
- Works today

**Cons:**
- Manual or script-dependent; easy to forget
- Local builds still produce `app-debug.apk`

### Option C — GitHub Actions CI (Future)

When CI is added:

```yaml
- name: Build APK
  run: ./gradlew assembleDebug

- name: Rename APK
  run: |
    VERSION=${GITHUB_REF_NAME#v}
    VERSION=${VERSION%-amm}
    mv app/build/outputs/apk/debug/app-debug.apk \
       "AMM_v${VERSION}_debug.apk"

- name: Upload Release Asset
  run: gh release upload "$GITHUB_REF_NAME" AMM_v*.apk
```

---

## 4. Chosen Approach

**Immediate:** Use **Option B** (shell rename) for manual releases until Gradle is updated.  
**Next sprint:** Implement **Option A** in `app/build.gradle.kts` so the name is correct at build time.

---

## 5. Verification

After implementing Option A, verify:

```bash
./gradlew assembleDebug
ls app/build/outputs/apk/debug/
# Expected: AMM_v1.1.0_debug.apk
```

---

## 6. Related Files

| File | Action |
|------|--------|
| `app/build.gradle.kts` | Add `outputFileName` template (Option A) |
| `.github/workflows/release.yml` | Add rename step (Option C, future) |
| `CHANGELOG.md` | Document naming convention for users |
