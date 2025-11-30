# CI/CD Quick Start Guide

Quick reference for setting up continuous integration and deployment for Overgram Android.

**Developed by [@overspend1](https://github.com/overspend1)**

## 🚀 Choose Your CI/CD Platform

### Option 1: GitHub Actions (Recommended for Beginners)

✅ **Pros:**
- Free for public repositories
- No setup required
- Integrated with GitHub
- Easy to configure

❌ **Cons:**
- Limited build minutes for private repos
- Slower than self-hosted

**Setup:**
1. Already configured! (`.github/workflows/android-build.yml`)
2. Add secrets to GitHub repository:
   - Go to Settings → Secrets and variables → Actions
   - Add the following secrets:

   ```
   KEYSTORE_BASE64      = <base64 encoded keystore>
   KEYSTORE_PASSWORD    = <your keystore password>
   KEY_ALIAS            = overgram
   KEY_PASSWORD         = <your key password>
   TELEGRAM_BOT_TOKEN   = <optional, for notifications>
   TELEGRAM_CHANNEL_ID  = @overgramreleases
   ```

3. Push code to trigger build
4. Check Actions tab on GitHub

**Get keystore base64:**
```bash
base64 -w 0 overgram-release.keystore
```

---

### Option 2: Buildkite (Recommended for Advanced Users)

✅ **Pros:**
- Self-hosted agents (full control)
- Unlimited build minutes
- Faster builds
- Advanced features

❌ **Cons:**
- Requires agent setup
- Paid plans for teams

**Quick Setup:**
```bash
# 1. Clone repository on your build server
git clone https://github.com/overspend1/Overgram4A.git
cd Overgram4A

# 2. Run setup script
chmod +x .buildkite/setup-agent.sh
./.buildkite/setup-agent.sh

# 3. Go to buildkite.com and create pipeline
# 4. Point it to .buildkite/pipeline.yml
# 5. Add environment secrets
```

See [.buildkite/README.md](.buildkite/README.md) for detailed instructions.

---

## 🔐 Signing Configuration

### Generate Keystore

```bash
keytool -genkey -v \
  -keystore overgram-release.keystore \
  -alias overgram \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000

# Enter password when prompted
# Fill in your information
```

### For GitHub Actions

```bash
# Encode keystore to base64
base64 -w 0 overgram-release.keystore

# Copy output and add as KEYSTORE_BASE64 secret in GitHub
```

### For Buildkite

```bash
# Encode keystore
base64 -w 0 overgram-release.keystore > keystore.base64

# Add to Buildkite environment variables:
# KEYSTORE_FILE = <contents of keystore.base64>
# KEYSTORE_PASSWORD = <your password>
# KEY_ALIAS = overgram
# KEY_PASSWORD = <your password>
```

---

## 📱 Telegram Notifications

### 1. Create Bot

Talk to [@BotFather](https://t.me/botfather):
```
/newbot
Name: Overgram Builds Bot
Username: overgram_builds_bot
```

Copy the token (e.g., `123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11`)

### 2. Add Bot to Channel

1. Add bot to your channel as administrator
2. Get channel ID:
   ```bash
   curl https://api.telegram.org/bot<TOKEN>/getUpdates
   ```

### 3. Add to CI/CD

**GitHub Actions:**
- Add `TELEGRAM_BOT_TOKEN` secret
- Add `TELEGRAM_CHANNEL_ID` secret (e.g., `@overgramreleases`)

**Buildkite:**
- Add environment variables:
  - `TELEGRAM_BOT_TOKEN`
  - `TELEGRAM_CHANNEL_ID`

---

## 🎯 Build Triggers

### GitHub Actions

Builds trigger on:
- ✅ Push to `rewrite`, `main`, or `develop`
- ✅ Pull requests
- ✅ Manual trigger (Actions tab → Run workflow)

Skip build:
```bash
git commit -m "docs: update README [skip ci]"
```

### Buildkite

Builds trigger on:
- ✅ Every push
- ✅ Pull requests
- ✅ Manual trigger (Buildkite UI → New Build)

---

## 📦 Build Artifacts

### GitHub Actions

Download from:
- Actions tab → Click build → Artifacts section
- Or from Releases (if tagged)

### Buildkite

Download from:
- Build page → Artifacts tab
- Direct link: `https://buildkite.com/.../artifacts`

---

## 🧪 Testing Locally

Before pushing, test locally:

```bash
# Debug build
./gradlew assembleAfatDebug

# Release build
./gradlew assembleAfatRelease

# Run tests
./gradlew test

# Run lint
./gradlew lint

# Build everything
./gradlew build
```

---

## 🐛 Common Issues

### Build fails with "SDK not found"
```bash
export ANDROID_HOME=/path/to/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
```

### Signing fails
- Check keystore password is correct
- Verify base64 encoding is correct
- Ensure secrets are set in CI/CD platform

### Tests fail
- Run locally first: `./gradlew test`
- Check test reports in `TMessagesProj/build/reports/tests/`

### Out of memory
Add to `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=512m
```

---

## 📊 Build Status Badges

### GitHub Actions

Add to README:
```markdown
![Build](https://github.com/overspend1/Overgram4A/workflows/Android%20CI%2FCD/badge.svg)
```

### Buildkite

Add to README:
```markdown
[![Build status](https://badge.buildkite.com/YOUR_TOKEN.svg)](https://buildkite.com/YOUR_ORG/overgram-android)
```

Get token from: Buildkite → Pipeline Settings → Badges

---

## 🎨 Build Flavors

Current flavor: `afat`

Change in commands:
```bash
# Debug
./gradlew assembleAfatDebug

# Release
./gradlew assembleAfatRelease

# Bundle (AAB)
./gradlew bundleAfatRelease
```

---

## 📈 Performance Tips

1. **Enable Gradle caching** (already configured)
2. **Use parallel builds** (already enabled)
3. **Limit workers** if low RAM:
   ```properties
   org.gradle.workers.max=2
   ```
4. **Cache dependencies** in CI/CD (already configured)

---

## 🔗 Useful Links

- [Buildkite Setup Guide](.buildkite/README.md)
- [Signing Guide](SIGNING_GUIDE.md)
- [GitHub Actions Docs](https://docs.github.com/en/actions)
- [Buildkite Docs](https://buildkite.com/docs)
- [Android Build Guide](docs/building-android.md)

---

## ✅ Checklist

Before first build:

- [ ] Generate keystore
- [ ] Encode keystore to base64
- [ ] Add secrets to CI/CD platform
- [ ] (Optional) Set up Telegram bot
- [ ] Push code to trigger first build
- [ ] Verify build succeeds
- [ ] Download and test APK
- [ ] Add build badge to README

---

## 💡 Pro Tips

1. **Test signing locally** before pushing:
   ```bash
   ./gradlew assembleAfatRelease
   jarsigner -verify -verbose -certs TMessagesProj/build/outputs/apk/afat/release/*.apk
   ```

2. **Parallel builds** - Push to multiple branches simultaneously

3. **Nightly builds** - Set up scheduled builds in GitHub Actions:
   ```yaml
   on:
     schedule:
       - cron: '0 2 * * *'  # 2 AM daily
   ```

4. **Release automation** - Tag commits for automatic releases:
   ```bash
   git tag -a v1.0.0 -m "Release 1.0.0"
   git push origin v1.0.0
   ```

---

**Questions?** Open an issue on [GitHub](https://github.com/overspend1/Overgram4A/issues)

**Developed by [@overspend1](https://github.com/overspend1)** 🚀
