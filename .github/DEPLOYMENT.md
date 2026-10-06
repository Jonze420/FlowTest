# StompFlow CI/CD Deployment & Publication Guide

This document describes the automated deployment and publishing pipelines configured for **StompFlow** using GitHub Actions.

---

## Workflows Overview

| Workflow File | Purpose | Triggers | Artifacts Generated |
| :--- | :--- | :--- | :--- |
| **`deploy-production.yml`** | Production release builds, APK & AAB signing, GitHub Releases creation | Tag pushes (`v*.*.*`), manual `workflow_dispatch` | Release APK, Release AAB, SHA-256 checksums, GitHub Release |
| **`publish-google-play.yml`** | Direct publication to Google Play Console | Manual `workflow_dispatch` with track selection (`internal`, `alpha`, `beta`, `production`) | Published AAB, release notes, Play Console deployment |
| **`ci.yml`** | Continuous integration testing & build integrity | Push & PR to `main`, `master`, `develop` | Debug APK test build, unit test logs |

---

## Required GitHub Repository Secrets

Configure these secrets in your repository settings under **Settings > Secrets and variables > Actions > Repository secrets**:

### 1. Android Release Signing Secrets (Required for Production & Publication)

| Secret Name | Description | Example / How to generate |
| :--- | :--- | :--- |
| `SIGNING_KEY` *(or `KEYSTORE_BASE64`)* | Base64-encoded `.jks` or `.keystore` release file | `base64 -w 0 my-release-key.jks > key.base64` |
| `KEY_STORE_PASSWORD` | Password for your keystore file | e.g., `SuperSecretPass123` |
| `ALIAS` | Key alias name inside the keystore | e.g., `stompflow` or `release_key` |
| `KEY_PASSWORD` | Password for the key alias | e.g., `SuperSecretPass123` |

#### How to Generate a Production Keystore

```bash
keytool -genkeypair \
  -v \
  -keystore stompflow-release.jks \
  -alias stompflow \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storepass YOUR_KEYSTORE_PASSWORD \
  -keypass YOUR_KEY_PASSWORD \
  -dname "CN=StompFlow, OU=Audio, O=Studio, L=San Francisco, ST=CA, C=US"
```

To encode it for the GitHub Secret:

```bash
# On Linux/macOS
base64 -w 0 stompflow-release.jks | pbcopy # or clip.exe / xclip
```

### 2. Google Play Store Deployment Secrets (Required for `publish-google-play.yml`)

| Secret Name | Description | Source |
| :--- | :--- | :--- |
| `PLAY_CONFIG_JSON` *(or `SERVICE_ACCOUNT_JSON`)* | JSON private key credentials for Google Play Developer API | Google Cloud Console & Google Play Console |

#### How to Set Up Google Play API Access

1. Log in to the [Google Play Console](https://play.google.com/console).
2. Go to **Setup > API access**.
3. Link or select your Google Cloud project.
4. Click **Create Service Account** and follow the link to Google Cloud Console IAM.
5. Create a service account with the **Service Account User** role.
6. Generate a **JSON key** for that service account and download the file.
7. Return to the Google Play Console API access page, grant the service account permissions:
   - "Release apps to testing tracks" (for internal / alpha / beta)
   - "Release to production, exclude devices, and use Play App Signing" (for production)
8. Copy the entire contents of the downloaded JSON file into GitHub Secret `PLAY_CONFIG_JSON`.

---

## Running Deployments

### A. Publishing a Production Release via Git Tag

1. Update `versionCode` and `versionName` in `app/build.gradle.kts` if needed.
2. Commit and push a tag matching semantic versioning:
   ```bash
   git tag v1.2.1
   git push origin v1.2.1
   ```
3. The `deploy-production.yml` workflow will automatically run:
   - Verify code and execute unit tests.
   - Build `assembleRelease` and `bundleRelease`.
   - Sign both the APK and AAB.
   - Calculate SHA-256 integrity checksums.
   - Publish a new GitHub Release with the APK, AAB, and release notes attached.

### B. Triggering a Production Build Manually

1. In GitHub, navigate to **Actions**.
2. Select **Production Release & Deployment**.
3. Click **Run workflow**.
4. Optionally customize:
   - Release tag (e.g., `v1.2.1`)
   - Release title
   - Draft / Pre-release flags
5. Click **Run workflow**.

### C. Deploying to Google Play Console Tracks

1. In GitHub, navigate to **Actions**.
2. Select **Publish to Google Play Store**.
3. Click **Run workflow**.
4. Choose target track:
   - `internal`: Internal app sharing / internal test track (fastest validation)
   - `alpha`: Closed testing track
   - `beta`: Open testing track
   - `production`: Full or staged rollout to production
5. Select status (`completed`, `draft`, `inProgress`) and rollout percentage.
6. Enter release notes text.
7. Click **Run workflow**.
