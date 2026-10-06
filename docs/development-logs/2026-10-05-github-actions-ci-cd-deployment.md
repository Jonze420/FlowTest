# Development Log — 2026-10-05: GitHub Actions CI/CD Deployment & Publication

**Date:** 2026-10-05  
**Author:** StompFlow Engineering Team  
**Status:** Completed  
**Milestone:** Production CI/CD, Automated Android Signing & Play Store Publication  

---

## 1. Objective

Implement robust, industry-standard GitHub Actions workflows to automate:
1. **Production Deployment**: Compile, package, sign (APK & AAB), verify SHA-256 integrity, and publish GitHub Releases with assets.
2. **Google Play Store Publication**: Deploy production Android App Bundles (`.aab`) to Google Play tracks (Internal, Alpha, Beta, Production) using the Google Play Developer API.
3. **Continuous Integration**: Continuous verification on pull requests and branch pushes, building debug artifacts and running unit tests.
4. **Documentation**: Provide a detailed guide for secret management, release keystores, and runner orchestration.

---

## 2. Architecture & Workflow Design

### 2.1 Workflow Matrix

1. **`deploy-production.yml`**
   - **Triggers**: Tag push (`v*.*.*`), manual dispatch (`workflow_dispatch`).
   - **Environment**: Ubuntu-latest runner, Temurin JDK 17, Gradle 8.11.1 (via `gradle/actions/setup-gradle@v4`).
   - **Build Tasks**:
     - `gradle testDebugUnitTest`
     - `gradle :app:assembleRelease`
     - `gradle :app:bundleRelease`
   - **Signing**: Integrates `r0adkll/sign-android-release@v1` using repository secrets (`SIGNING_KEY`, `KEY_STORE_PASSWORD`, `ALIAS`, `KEY_PASSWORD`). Includes safe fallback warnings when running in development forks without secrets.
   - **Integrity**: Computes SHA-256 hash digests (`SHA256SUMS.txt`) across distribution files.
   - **Release Delivery**: Uploads release artifacts to GitHub Actions retention storage and drafts/publishes a GitHub Release using `softprops/action-gh-release@v2`.

2. **`publish-google-play.yml`**
   - **Triggers**: Manual dispatch with dynamic input parameters (`track`, `status`, `user_fraction`, `release_name`, `whats_new_text`).
   - **Packaging**: Generates and signs optimized Android App Bundle (`.aab`).
   - **Publishing**: Uses `r0adkll/upload-google-play@v1` with GCP service account authentication (`PLAY_CONFIG_JSON` / `SERVICE_ACCOUNT_JSON`).
   - **Metadata**: Generates localized What's New notes (`distribution/whatsnew/whatsnew-en-US`) and uploads ProGuard/R8 mapping files (`mapping.txt`) for stack trace deobfuscation.

3. **`ci.yml`**
   - **Triggers**: Push and pull requests to `main`, `master`, and `develop`.
   - **Validation**: Compiles debug and release configurations, runs test suites, and archives debug APKs for quick developer testing.

---

## 3. Security & Compliance Highlights

- **Secrets Isolation**: Release keystore binaries and passwords are never checked into git. All signing is executed ephemerally within the GitHub Actions runner.
- **Play Policy Compliance**: Follows zero-permission photo picker guidelines, API level 35 targeting, and scoped storage practices.
- **Integrity Verification**: Automatic generation of `SHA256SUMS.txt` prevents tampering and guarantees artifact provenance.

---

## 4. Verification & Testing

- Verified release compilation locally using `gradle :app:assembleRelease` and `gradle :app:bundleRelease`.
- Confirmed zero build errors and correct R8 resource handling.
- Validated YAML schema syntax for all GitHub Actions workflows.
