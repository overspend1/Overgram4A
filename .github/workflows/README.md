# GitHub Workflows Documentation

This directory contains GitHub Actions workflows for building, testing, and releasing Overgram Android.

## 🚀 Auto Release Workflow (`auto-release.yml`)

**The main workflow for automatic releases with Telegram distribution.**

### What It Does

1. **Automatic Builds** - Triggers on push to `main`, `rewrite`, or `develop` branches
2. **Smart Versioning** - Auto-generates version tags based on branch:
   - `main` → `v2024.12.02` (stable release)
   - `rewrite`/`develop` → `beta-20241202-1430` (beta build)
   - Tags `v*` → Uses tag version
3. **GitHub Releases** - Automatically creates releases with detailed notes
4. **Telegram Upload** - Uploads APK directly to your Telegram channel
5. **APK Signing** - Signs APKs if keystore secrets are configured

### Automatic Tagging Logic

| Branch/Trigger | Version Format | Tag Example | Release Type |
|----------------|----------------|-------------|--------------|
| `main` | `YYYY.MM.DD` | `v2024.12.02` | Stable Release |
| `rewrite` | `beta-YYYYMMDD-HHMM` | `beta-20241202-1430` | Beta/Prerelease |
| `develop` | `beta-YYYYMMDD-HHMM` | `beta-20241202-1430` | Beta/Prerelease |
| Tag `v1.2.3` | Uses tag | `v1.2.3` | Stable Release |

### Setup Requirements

#### Required Secrets

Configure these in **Settings → Secrets → Actions**:

1. **Telegram (Required for uploads)**
   ```
   TELEGRAM_BOT_TOKEN=123456789:ABCdefGHIjklMNOpqrsTUVwxyz
   TELEGRAM_CHANNEL_ID=@overgramupdates
   ```

2. **APK Signing (Optional but recommended)**
   ```
   KEYSTORE_BASE64=<base64-encoded keystore>
   KEYSTORE_PASSWORD=your_store_password
   KEY_ALIAS=your_key_alias
   KEY_PASSWORD=your_key_password
   ```

#### How to Get Telegram Bot Token

1. Message [@BotFather](https://t.me/botfather) on Telegram
2. Send `/newbot` and follow prompts
3. Copy the token (e.g., `123456789:ABCdefGHIjklMNOpqrsTUVwxyz`)
4. Add your bot as admin to your channel with "Post Messages" permission

#### How to Encode Keystore

```bash
# Encode your keystore to base64
base64 -w 0 your-keystore.jks > keystore.txt
# Copy contents of keystore.txt to KEYSTORE_BASE64 secret
```

### Usage

#### Automatic Trigger (Recommended)

Just push to your branches:

```bash
# Beta build from develop/rewrite
git push origin rewrite

# Stable release from main
git push origin main
```

**The workflow will automatically:**
- ✅ Build the APK
- ✅ Sign it (if secrets configured)
- ✅ Create a GitHub release
- ✅ Upload APK to Telegram
- ✅ Send beautiful formatted message

#### Manual Trigger

1. Go to **Actions** tab
2. Select "Auto Release & Telegram Upload"
3. Click "Run workflow"
4. Choose branch
5. Click "Run workflow"

### Workflow Outputs

After successful run, you'll have:

1. **GitHub Release** - Created at `https://github.com/USER/REPO/releases`
   - Tagged with auto-generated version
   - Includes detailed release notes
   - APK attached for download

2. **Telegram Post** - Sent to your channel
   - APK file directly uploaded (if < 50MB)
   - Beautiful HTML-formatted caption
   - Installation instructions
   - Links to GitHub and website

3. **Artifacts** - Stored for 30 days
   - Downloadable from workflow run page

### What Gets Posted to Telegram

#### Beta Builds

```
🧪 Overgram Android beta-20241202-1430

Type: Beta Build
Build: #42 • 2024-12-02 14:30 UTC
Size: 87M

✨ Features:
• Liquid Glass design system
• Full ghost mode
• Message history database
• Material You theming
• Local encryption & biometric lock

📱 Installation:
1. Download APK above
2. Enable Unknown Sources
3. Install and enjoy!

🔗 Links:
• Website: overgram.one
• GitHub: github.com/overspend1/Overgram4A

👨‍💻 By @overspend1 • 💬 @overgramchat
```

#### Stable Releases

```
🚀 Overgram Android 2024.12.02

Type: Release
Build: #42 • 2024-12-02 14:30 UTC
Size: 87M

✨ Features:
• Liquid Glass design system
• Full ghost mode
• Message history database
• Material You theming
• Local encryption & biometric lock

📱 Installation:
1. Download APK above
2. Enable Unknown Sources
3. Install and enjoy!

🔗 Links:
• Website: overgram.one
• GitHub: github.com/overspend1/Overgram4A

👨‍💻 By @overspend1 • 💬 @overgramchat
```

### Customization

#### Change Version Format

Edit the version logic in the workflow:

```yaml
# For main branch
VERSION=$(date +"%Y.%m.%d")  # Change format here

# For beta builds
VERSION="beta-$(date +"%Y%m%d-%H%M")"  # Change format here
```

#### Customize Telegram Message

Edit the caption in the "Upload APK to Telegram" step (~line 250):

```yaml
CAPTION="${EMOJI} <b>Overgram Android ${VERSION}</b>

Your custom message here..."
```

#### Change Branch Triggers

Edit the workflow trigger:

```yaml
on:
  push:
    branches:
      - main
      - your-branch-name
```

### Troubleshooting

#### APK not uploading to Telegram

**Check:**
1. ✅ `TELEGRAM_BOT_TOKEN` secret is set
2. ✅ `TELEGRAM_CHANNEL_ID` is correct (e.g., `@overgramupdates`)
3. ✅ Bot is admin in channel with "Post Messages" permission
4. ✅ APK size is < 50MB (Telegram limit)

**Test bot manually:**
```bash
curl "https://api.telegram.org/bot<TOKEN>/getMe"
```

#### Build failing

**Common issues:**
- Missing submodules: Run `git submodule update --init --recursive`
- Gradle cache issues: Clear cache in Actions settings
- JDK version: Ensure JDK 17 is used

#### Release not creating

**Check:**
- Workflow has `contents: write` permission (default for most repos)
- No existing release with same tag
- GitHub token is valid

### File Size Limits

| Platform | Limit | Action |
|----------|-------|--------|
| Telegram Bot API | 50 MB | Direct upload |
| Telegram (user) | 2 GB | Manual share |
| GitHub Artifacts | 2 GB | Always works |
| GitHub Releases | 2 GB | Always works |

If APK > 50MB, the workflow will:
1. Create GitHub release normally
2. Send Telegram message with GitHub download link (not APK file)

**To reduce APK size:**
- Enable R8/ProGuard minification
- Remove unused resources
- Use app bundles (AAB) for Play Store

---

## 📋 Other Workflows

### `android-build.yml`

Legacy CI/CD workflow with:
- Debug & Release builds
- Unit tests & lint
- AAB bundle creation
- Manual release creation
- Text-only Telegram notifications

**When to use:** If you need more control over release process

### `release.yml`

Self-hosted runner workflow with:
- Ccache optimization
- Space cleanup
- Custom build environment

**When to use:** If you have self-hosted runners

---

## 🔄 Migration Guide

### From Buildkite to GitHub Actions

The new `auto-release.yml` workflow provides feature parity with Buildkite:

| Feature | Buildkite | GitHub Actions |
|---------|-----------|----------------|
| Auto build | ✅ | ✅ |
| APK signing | ✅ | ✅ |
| Telegram upload | ✅ | ✅ |
| Auto versioning | Manual | ✅ Automatic |
| Release creation | Manual block | ✅ Automatic |
| Beta/Release tagging | Manual | ✅ Automatic |

**Advantages of GitHub Actions:**
1. ⚡ Faster (no manual approval blocks)
2. 🤖 Fully automatic versioning
3. 📊 Better integration with GitHub
4. 💰 Free for public repos
5. 🔄 No infrastructure to maintain

### Setting Up

1. **Add secrets** to GitHub (see Setup Requirements above)
2. **Push to branch** to trigger first build
3. **Check Actions tab** for build status
4. **Verify Telegram post** in your channel
5. **Done!** Future pushes will auto-release

---

## 📚 Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Telegram Bot API](https://core.telegram.org/bots/api)
- [Android Signing Guide](../../SIGNING_GUIDE.md)
- [Overgram Documentation](https://docs.overgram.one)

---

**Developed by [@overspend1](https://github.com/overspend1)**
