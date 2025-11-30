# Buildkite CI/CD Setup for Overgram Android

This guide explains how to set up and use Buildkite CI/CD for automated builds, testing, and releases.

## 🚀 Quick Start

### 1. Create Buildkite Account

1. Go to [buildkite.com](https://buildkite.com) and sign up
2. Create a new organization (e.g., "Overgram")
3. Connect your GitHub account

### 2. Create Pipeline

1. Click "New Pipeline"
2. Name: `Overgram Android`
3. Repository: `https://github.com/overspend1/Overgram4A`
4. Configure pipeline settings:
   - **Default Branch**: `rewrite` (or your main branch)
   - **Steps**: Upload `.buildkite/pipeline.yml`
   - **Build Skipping**: Enable for `[skip ci]` in commit messages

### 3. Set Up Build Agents

You need at least one agent to run builds. You have two options:

#### Option A: Use Buildkite Hosted Agents (Easiest)
- Buildkite provides hosted agents
- No setup required
- May have limited Android SDK support
- **Best for**: Testing and quick setups

#### Option B: Self-Hosted Agent (Recommended for Android)
```bash
# Install Buildkite agent on Linux/macOS
# See: https://buildkite.com/docs/agent/v3/installation

# On Ubuntu/Debian:
sudo sh -c 'echo deb https://apt.buildkite.com/buildkite-agent stable main > /etc/apt/sources.list.d/buildkite-agent.list'
sudo apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 32A37959C2FA5C3C99EFBC32A79206696452D198
sudo apt-get update && sudo apt-get install -y buildkite-agent

# Configure agent
sudo sed -i "s/xxx/YOUR_AGENT_TOKEN/g" /etc/buildkite-agent/buildkite-agent.cfg

# Add queue tag
echo 'tags="queue=android,os=linux"' | sudo tee -a /etc/buildkite-agent/buildkite-agent.cfg

# Install Android SDK
sudo apt-get install -y openjdk-17-jdk
mkdir -p ~/android-sdk
cd ~/android-sdk
wget https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip
unzip commandlinetools-linux-9477386_latest.zip
mkdir -p cmdline-tools/latest
mv cmdline-tools/* cmdline-tools/latest/
yes | cmdline-tools/latest/bin/sdkmanager --licenses
cmdline-tools/latest/bin/sdkmanager "platform-tools" "platforms;android-33" "build-tools;33.0.2" "ndk;25.2.9519653"

# Set environment
echo 'export ANDROID_HOME=/home/buildkite-agent/android-sdk' | sudo tee -a /etc/buildkite-agent/buildkite-agent.cfg
echo 'export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools' | sudo tee -a /etc/buildkite-agent/buildkite-agent.cfg

# Start agent
sudo systemctl enable buildkite-agent && sudo systemctl start buildkite-agent
```

### 4. Configure Environment Variables

In Buildkite pipeline settings → Environment Variables, add:

#### Required for Signing:
```bash
KEYSTORE_FILE=<base64 encoded keystore>
KEYSTORE_PASSWORD=your_keystore_password
KEY_ALIAS=overgram
KEY_PASSWORD=your_key_password
```

To encode your keystore:
```bash
base64 -w 0 overgram-release.keystore > keystore.base64
# Copy the contents and paste as KEYSTORE_FILE
```

#### Required for GitHub Releases:
```bash
GITHUB_TOKEN=ghp_xxxxxxxxxxxxxxxxxxxx
```

Create token at: https://github.com/settings/tokens
- Permissions needed: `repo` (full access)

#### Optional for Telegram Notifications:
```bash
TELEGRAM_BOT_TOKEN=123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11
TELEGRAM_CHANNEL_ID=@overgramreleases
```

Create bot with [@BotFather](https://t.me/botfather)

## 📋 Pipeline Steps

### 1. **Build Android Debug** (`:android:`)
- Builds debug APK for testing
- Runs on `android` queue
- Outputs: `overgram-android-debug.apk`
- **Duration**: ~5-10 minutes

### 2. **Build Android Release** (`:android:`)
- Builds release APK
- Signs with keystore if provided
- Optimized and minified
- Outputs:
  - `overgram-android-release.apk` (unsigned)
  - `overgram-android-signed.apk` (signed)
- **Duration**: ~8-15 minutes

### 3. **Run Tests** (`:test_tube:`)
- Waits for debug build
- Runs unit tests (`./gradlew test`)
- Runs lint checks (`./gradlew lint`)
- **Duration**: ~3-5 minutes

### 4. **Build AAB Bundle** (`:package:`)
- Creates Android App Bundle for Play Store
- Smaller download size
- Supports dynamic delivery
- Output: `overgram-android-release.aab`
- **Duration**: ~8-12 minutes

### 5. **Create Release** (`:rocket:`)
- **Manual approval** required (block step)
- Click to approve release creation
- Can cancel if tests fail

### 6. **Create GitHub Release** (`:github:`)
- Uploads all build artifacts to GitHub
- Creates draft release
- Extracts version from `gradle.properties`
- **Requires**: `GITHUB_TOKEN`

### 7. **Notify Telegram Channel** (`:telegram:`)
- Sends build notification to Telegram
- **Optional**: Soft fail (won't block pipeline)
- **Requires**: `TELEGRAM_BOT_TOKEN`, `TELEGRAM_CHANNEL_ID`

### 8. **Upload to Play Store** (`:play_store:`)
- **Optional**: Disabled by default
- Uploads AAB to Google Play Internal track
- **Requires**: Service account JSON

## 🔧 Configuration

### Modify Pipeline

Edit `.buildkite/pipeline.yml` to customize:

**Change Android SDK version:**
```yaml
env:
  ANDROID_HOME: "/opt/android-sdk"
  ANDROID_COMPILE_SDK: "33"  # Add this
```

**Add more test steps:**
```yaml
- label: ":test_tube: Integration Tests"
  commands:
    - ./gradlew connectedAndroidTest
```

**Change branch filter:**
```yaml
branches: "main rewrite release/*"
```

### Triggering Builds

Builds trigger automatically on:
- ✅ Push to repository
- ✅ Pull request creation/update
- ✅ Manual trigger from Buildkite UI

Skip builds by including `[skip ci]` in commit message:
```bash
git commit -m "docs: update README [skip ci]"
```

### Manual Trigger

1. Go to Buildkite dashboard
2. Select "Overgram Android" pipeline
3. Click "New Build"
4. Choose branch and commit
5. Click "Create Build"

## 📊 Build Status Badge

Add to your README:

```markdown
[![Build status](https://badge.buildkite.com/YOUR_BADGE_TOKEN.svg)](https://buildkite.com/YOUR_ORG/overgram-android)
```

Get badge token from: Pipeline Settings → Badges

## 🐛 Troubleshooting

### Build fails with "SDK not found"
**Solution**: Ensure `ANDROID_HOME` is set correctly on agent
```bash
echo $ANDROID_HOME  # Should output /opt/android-sdk or similar
```

### Signing fails
**Solution**: Verify environment variables
```bash
# Check if KEYSTORE_FILE is set
buildkite-agent meta-data exists "env:KEYSTORE_FILE"

# Test decoding
echo "$KEYSTORE_FILE" | base64 -d > test.keystore
file test.keystore  # Should show "Java KeyStore"
```

### Tests fail
**Solution**: Run locally first
```bash
./gradlew test
./gradlew lint
```

### GitHub release creation fails
**Solution**: Check token permissions
- Token needs `repo` scope
- Token must not be expired
- Repository must exist

### Telegram notification fails
**Solution**: Verify bot setup
```bash
# Test bot token
curl "https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/getMe"

# Test sending message
curl -X POST "https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage" \
  -d "chat_id=@overgramreleases" \
  -d "text=Test message"
```

### Agent goes offline
**Solution**: Check agent status
```bash
# On agent machine
sudo systemctl status buildkite-agent
sudo journalctl -u buildkite-agent -f

# Restart if needed
sudo systemctl restart buildkite-agent
```

## 🔒 Security Best Practices

### 1. Protect Secrets
- ✅ Use Buildkite's secret management
- ✅ Never commit secrets to repository
- ✅ Rotate tokens regularly
- ❌ Don't print secrets in build logs

### 2. Limit Access
- Only give pipeline access to trusted developers
- Use separate keystores for debug/release
- Restrict agent access to necessary queues

### 3. Secure Agents
```bash
# Run agent as dedicated user
sudo useradd -m -s /bin/bash buildkite-agent

# Limit permissions
sudo chown -R buildkite-agent:buildkite-agent /home/buildkite-agent

# Enable firewall
sudo ufw allow ssh
sudo ufw enable
```

## 📈 Performance Optimization

### Cache Dependencies
Add to pipeline:
```yaml
plugins:
  - gradle-cache#v1:
      key: "v1-{{ checksum 'build.gradle' }}"
```

### Parallel Builds
```yaml
env:
  GRADLE_OPTS: "-Dorg.gradle.parallel=true -Dorg.gradle.workers.max=4"
```

### Use Docker
```yaml
agents:
  queue: "docker"
plugins:
  - docker#v5.0.0:
      image: "thyrlian/android-sdk:latest"
```

## 📖 Resources

- [Buildkite Docs](https://buildkite.com/docs)
- [Android CI/CD Guide](https://buildkite.com/docs/guides/android)
- [Agent Installation](https://buildkite.com/docs/agent/v3/installation)
- [Pipeline Reference](https://buildkite.com/docs/pipelines/defining-steps)

## 💡 Tips

1. **Start small**: Begin with just debug builds, add features incrementally
2. **Test locally**: Always test `./gradlew` commands locally before adding to pipeline
3. **Monitor builds**: Check build times and optimize slow steps
4. **Use artifacts**: Download built APKs from Buildkite artifacts tab
5. **Parallel steps**: Run independent steps in parallel for faster builds

## 🎯 Next Steps

After setup:
1. ✅ Push a commit to trigger first build
2. ✅ Verify build succeeds
3. ✅ Add build status badge to README
4. ✅ Configure Telegram notifications
5. ✅ Set up Play Store upload (optional)

---

**Questions?** Check the [Buildkite Community](https://forum.buildkite.com/) or [GitHub Issues](https://github.com/overspend1/Overgram4A/issues)

**Developed by [@overspend1](https://github.com/overspend1)**
