# ⚡ Quick Start - Auto Release Workflow

Get your automatic APK releases working in 5 minutes!

## 🚀 Setup in 3 Steps

### Step 1: Create Telegram Bot (2 min)

```
1. Open Telegram → Search @BotFather
2. Send: /newbot
3. Name: Overgram Release Bot
4. Username: overgram_release_bot
5. Copy token: 123456789:ABCdefGHIjklMNOpqrsTUVwxyz
```

### Step 2: Add Bot to Channel (1 min)

```
1. Open your channel (@overgramupdates)
2. Settings → Administrators → Add Administrator
3. Search for your bot
4. Enable ONLY "Post Messages" permission
5. Save
```

### Step 3: Add Secrets to GitHub (2 min)

```
1. GitHub repo → Settings → Secrets → Actions
2. Click "New repository secret"

Add these:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Name:  TELEGRAM_BOT_TOKEN
Value: 123456789:ABCdefGHIjklMNOpqrsTUVwxyz
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Name:  TELEGRAM_CHANNEL_ID
Value: @overgramupdates
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

## ✅ Test It

```bash
# Test bot (optional)
./.github/scripts/test-telegram-bot.sh YOUR_TOKEN @overgramupdates

# Trigger build
git add .
git commit -m "test: trigger auto-release"
git push origin rewrite

# Watch it work
# 1. Go to Actions tab on GitHub
# 2. Wait ~5-10 minutes
# 3. Check your Telegram channel
# 4. APK will be posted automatically!
```

## 📦 What Happens Automatically

```
Push to branch
      ↓
GitHub Actions builds APK
      ↓
Creates GitHub Release
      ↓
Uploads APK to Telegram
      ↓
Posts beautiful message
      ↓
✅ DONE!
```

## 🏷️ Auto Versioning

| Branch | Version Format | Example | Type |
|--------|---------------|---------|------|
| `main` | `v2024.12.02` | `v2024.12.02` | Release |
| `rewrite` | `beta-YYYYMMDD-HHMM` | `beta-20241202-1430` | Beta |
| `develop` | `beta-YYYYMMDD-HHMM` | `beta-20241202-1520` | Beta |
| Tag `v1.2.3` | Uses tag | `v1.2.3` | Release |

## 🔒 Optional: APK Signing

Want signed APKs? Add these secrets:

```bash
# Encode keystore
./.github/scripts/encode-keystore.sh your-keystore.jks

# Add to GitHub secrets:
KEYSTORE_BASE64     = (contents of keystore-base64.txt)
KEYSTORE_PASSWORD   = your_password
KEY_ALIAS           = your_alias
KEY_PASSWORD        = your_key_password
```

## 📱 What Gets Posted

```
🚀 Overgram Android v2024.12.02

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

**Plus the APK file attached! 📦**

## 🛠️ Helper Scripts

```bash
# Test Telegram bot setup
./.github/scripts/test-telegram-bot.sh <TOKEN> <CHANNEL_ID>

# Encode keystore for signing
./.github/scripts/encode-keystore.sh path/to/keystore.jks
```

## 📚 Full Documentation

- **Setup Guide:** [TELEGRAM_BOT_SETUP.md](.github/TELEGRAM_BOT_SETUP.md)
- **Workflow Details:** [workflows/README.md](.github/workflows/README.md)
- **Signing Guide:** [SIGNING_GUIDE.md](../SIGNING_GUIDE.md)

## 🐛 Common Issues

### Bot can't post
```
✅ Check bot is admin in channel
✅ Check "Post Messages" permission enabled
✅ Check channel ID is correct (@overgramupdates)
```

### Workflow doesn't run
```
✅ Check workflow file in .github/workflows/
✅ Check branch name (rewrite, main, develop)
✅ Check Actions are enabled in repo settings
```

### APK not uploading
```
✅ Check secrets are set correctly
✅ Check secret names match exactly
✅ Check APK size < 50MB
```

## 💡 Pro Tips

1. **Delete old betas** - Keep channel clean
2. **Pin releases** - Pin important versions
3. **Customize message** - Edit workflow caption
4. **Multiple channels** - Beta to one, release to another
5. **Monitor logs** - Check Actions tab for issues

## 🎉 That's It!

You're all set! Just push code and watch the magic happen.

**Questions?** See full docs or ask in [@overgramchat](https://t.me/overgramchat)

---

**Developed by [@overspend1](https://github.com/overspend1)**
