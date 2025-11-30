# Telegram Bot Setup for Buildkite

Complete guide for automatic APK distribution via Telegram using Buildkite CI/CD.

**Perfect for limited GitHub accounts!** 🚀

## 🎯 What This Does

When you push code to Buildkite:
1. ✅ Builds signed APK automatically
2. ✅ **Uploads APK directly to @overgramupdates**
3. ✅ Users can download from Telegram (no GitHub required!)
4. ✅ Beautiful formatted message with features
5. ✅ Zero friction for users

## 📱 Example Telegram Post

When a build completes, your channel will receive:

```
🚀 Overgram Android v1.0.0

Build #42 • 2025-11-30

✨ Features:
• Liquid Glass design system
• Full ghost mode
• Message history database
• Material You theming

📱 Installation:
1. Download APK above
2. Enable Unknown Sources
3. Install and enjoy!

👨‍💻 By @overspend1 • 💬 @overgramchat
```

**Plus the APK file attached!** Users tap to download directly.

## 🔧 Setup Steps

### 1. Create Telegram Bot

Talk to [@BotFather](https://t.me/botfather):

```
/newbot

Bot name: Overgram CI/CD Bot
Username: @overgram_builds_bot
```

Copy the token:
```
123456789:ABCdefGHIjklMNOpqrsTUVwxyz
```

### 2. Create Telegram Channel

Create channel: **Overgram Updates** (@overgramupdates)

Settings:
- Type: Public Channel
- Username: `overgramupdates`
- Description: Official updates for Overgram

### 3. Add Bot to Channel

1. Go to channel settings
2. Administrators → Add Administrator
3. Search for `@overgram_builds_bot`
4. Add with "Post Messages" permission

### 4. Configure Buildkite

Go to Buildkite → Your Pipeline → Settings → Environment Variables

Add these:

```bash
TELEGRAM_BOT_TOKEN = 123456789:ABCdefGHIjklMNOpqrsTUVwxyz
TELEGRAM_CHANNEL_ID = @overgramupdates
```

**That's it!** ✅

## 🚀 How to Trigger

### Automatic (Every Push)

```bash
git add .
git commit -m "feat: add new feature"
git push origin rewrite
```

Buildkite will:
1. Build APK
2. Sign it
3. Upload to @overgramupdates
4. Send notification

### Manual Trigger

1. Go to Buildkite dashboard
2. Click "New Build"
3. Select branch
4. Click "Create Build"

## 📦 What Gets Uploaded

**Priority:**
1. **Signed APK** (if signing configured) - Recommended
2. **Unsigned APK** (if signing failed) - Fallback

**APK naming:**
- `overgram-android-signed.apk`
- `overgram-android-release.apk`

## 🎨 Message Format

The bot sends messages with:

### Text Message (HTML formatted):
- 🚀 Version number
- 📅 Build date and number
- ✨ Feature highlights
- 📱 Installation instructions
- 👨‍💻 Developer attribution
- 💬 Chat link

### File Attachment:
- APK file (ready to download)
- File size shown
- One-tap download

## 🔒 Security Notes

### Bot Token Safety
- ✅ Store in Buildkite environment variables (encrypted)
- ✅ Never commit to repository
- ✅ Rotate if compromised
- ❌ Don't share publicly

### Channel Security
- Make bot admin with minimal permissions
- Only "Post Messages" permission needed
- Can revoke anytime

## 🐛 Troubleshooting

### APK not uploading

**Check Buildkite logs:**
```bash
# Look for:
✅ APK uploaded to Telegram!
# Or:
⚠️ No APK file or no Telegram token - skipping upload
```

**Common fixes:**
1. Verify `TELEGRAM_BOT_TOKEN` is set
2. Verify `TELEGRAM_CHANNEL_ID` is `@overgramupdates`
3. Check bot is admin in channel
4. Ensure APK build succeeded

### Bot can't post

**Test bot token:**
```bash
curl "https://api.telegram.org/bot<YOUR_TOKEN>/getMe"
```

Should return bot info.

**Test posting:**
```bash
curl -X POST "https://api.telegram.org/bot<YOUR_TOKEN>/sendMessage" \
  -d "chat_id=@overgramupdates" \
  -d "text=Test message"
```

**Fix:**
- Ensure bot is admin in channel
- Check channel username is correct (`@overgramupdates`)
- Verify token is not expired

### File too large

Telegram limits:
- **Bot API**: 50 MB per file
- **Regular users**: 2 GB per file

If APK > 50 MB:
- Enable ProGuard/R8 (minification)
- Remove unnecessary resources
- Use app bundles (AAB) for Play Store

**Current APK size check:**
```bash
ls -lh artifacts/*.apk
```

## 📊 Alternative: Buildkite Artifact Links

If APK upload fails, the backup notification includes:
- Link to Buildkite artifacts page
- Users must have Buildkite access
- Not ideal for public releases

## 🎯 Best Practices

### For Public Releases

1. **Use signed APKs** - More trustworthy
2. **Include changelog** - Update message template
3. **Test locally first** - Don't spam channel with failed builds
4. **Version tags** - Use semantic versioning

### For Development Builds

1. **Separate channel** - Create @overgrambeta for testing
2. **Minimal messages** - Less noise
3. **Mark as beta** - Clear communication

## 🔄 Advanced: Multiple Channels

You can post to different channels based on branch:

```yaml
# In .buildkite/pipeline.yml
- |
  if [ "$BUILDKITE_BRANCH" == "main" ]; then
    CHANNEL="@overgramupdates"
  else
    CHANNEL="@overgrambeta"
  fi

  curl -F "chat_id=${CHANNEL}" ...
```

## 📝 Customizing Messages

Edit `.buildkite/pipeline.yml` line ~206:

```bash
CAPTION="🚀 <b>Overgram Android v${VERSION}</b>%0A%0A"
CAPTION+="Your custom message here%0A"
```

**HTML formatting supported:**
- `<b>bold</b>`
- `<i>italic</i>`
- `<a href="url">link</a>`
- `<code>code</code>`

## 🌟 Benefits Over GitHub Releases

| Feature | GitHub Releases | Telegram Bot |
|---------|----------------|--------------|
| No account needed | ❌ Need GitHub | ✅ Just Telegram |
| Direct download | ❌ Web interface | ✅ One tap |
| Notifications | ❌ Email only | ✅ Push notification |
| Comments | ❌ No | ✅ Channel comments |
| Sharing | 🟡 Link only | ✅ Forward message |
| Mobile friendly | 🟡 Okay | ✅ Perfect |

## 📈 Analytics

Track downloads using:
- Telegram channel stats (if enabled)
- Bot analytics (views, forwards)
- Buildkite artifact downloads

## 🎉 Success Checklist

After first successful build:

- [ ] Message appears in @overgramupdates
- [ ] APK file is attached
- [ ] Caption is formatted correctly
- [ ] APK downloads and installs
- [ ] File size is reasonable (< 50 MB)
- [ ] Users can download without login
- [ ] Channel subscribers get notification

## 💡 Pro Tips

1. **Pin important releases** - Pin message in channel
2. **Delete old builds** - Keep channel clean
3. **Use release notes** - Update caption template with changes
4. **Emoji indicators** - 🚀 Release, 🔨 Nightly, 🐛 Bugfix
5. **Cross-post to chat** - Share in @overgramchat too

## 🔗 Resources

- [Telegram Bot API Docs](https://core.telegram.org/bots/api)
- [Buildkite Docs](https://buildkite.com/docs)
- [APK Signing Guide](../SIGNING_GUIDE.md)

---

**Perfect for limited GitHub accounts!** Everything goes through Buildkite and Telegram. Your users get APKs with zero friction! 🚀

**Developed by [@overspend1](https://github.com/overspend1)**
