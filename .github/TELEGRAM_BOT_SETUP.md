# 🤖 Telegram Bot Setup Guide

Complete step-by-step guide to set up automatic APK uploads to Telegram.

## 📋 Prerequisites

- Telegram account
- GitHub repository access
- 10 minutes of your time

---

## Step 1: Create Telegram Bot

### 1.1 Open BotFather

1. Open Telegram
2. Search for `@BotFather`
3. Start chat with `/start`

### 1.2 Create New Bot

Send this command:
```
/newbot
```

BotFather will ask for details:

**Bot name:** (Display name, can be anything)
```
Overgram Release Bot
```

**Bot username:** (Must end with 'bot')
```
overgram_release_bot
```

### 1.3 Save Your Token

BotFather will reply with:
```
Done! Congratulations on your new bot...

Use this token to access the HTTP API:
123456789:ABCdefGHIjklMNOpqrsTUVwxyz1234567
```

**🔴 IMPORTANT: Copy this token!** You'll need it for GitHub secrets.

**⚠️ Keep it secret!** Anyone with this token can control your bot.

---

## Step 2: Create/Configure Telegram Channel

### Option A: Create New Channel

1. Open Telegram
2. Click **New Channel**
3. **Channel name:** `Overgram Updates`
4. **Description:**
   ```
   Official releases and updates for Overgram Android

   🌐 overgram.one
   💬 @overgramchat
   👨‍💻 @overspend1
   ```
5. Choose **Public Channel**
6. **Link:** `overgramupdates` (or your preferred name)
7. Click **Create**

### Option B: Use Existing Channel

Just note your channel username (e.g., `@overgramupdates`)

---

## Step 3: Add Bot to Channel

### 3.1 Open Channel Settings

1. Open your channel
2. Click channel name at top
3. Click **Administrators**

### 3.2 Add Administrator

1. Click **Add Administrator**
2. Search for your bot username (e.g., `@overgram_release_bot`)
3. Click on the bot

### 3.3 Set Permissions

**Enable only:**
- ✅ **Post Messages**

**Disable everything else:**
- ❌ Edit Messages
- ❌ Delete Messages
- ❌ Add Members
- ❌ Manage Chat
- etc.

Click **Save** / **Done**

---

## Step 4: Test Bot Access

### 4.1 Get Bot Info

Open terminal and run:
```bash
curl "https://api.telegram.org/bot123456789:ABCdefGHIjklMNOpqrsTUVwxyz1234567/getMe"
```

Replace `123456789:ABCdefGHIjklMNOpqrsTUVwxyz1234567` with your actual token.

**Expected response:**
```json
{
  "ok": true,
  "result": {
    "id": 123456789,
    "is_bot": true,
    "first_name": "Overgram Release Bot",
    "username": "overgram_release_bot"
  }
}
```

### 4.2 Test Sending Message

```bash
curl -X POST "https://api.telegram.org/bot123456789:ABCdefGHIjklMNOpqrsTUVwxyz1234567/sendMessage" \
  -d "chat_id=@overgramupdates" \
  -d "text=Test message from bot" \
  -d "parse_mode=HTML"
```

**Replace:**
- Token with your bot token
- `@overgramupdates` with your channel username

**Expected:** Message appears in your channel

**If it fails:**
- ✅ Check bot is admin in channel
- ✅ Check channel username is correct
- ✅ Verify "Post Messages" permission is enabled

---

## Step 5: Configure GitHub Secrets

### 5.1 Open Repository Settings

1. Go to your GitHub repository
2. Click **Settings** tab
3. Click **Secrets and variables** → **Actions**

### 5.2 Add Telegram Secrets

Click **New repository secret** for each:

#### Secret 1: TELEGRAM_BOT_TOKEN
- **Name:** `TELEGRAM_BOT_TOKEN`
- **Value:** Your bot token (e.g., `123456789:ABCdefGHIjklMNOpqrsTUVwxyz1234567`)

#### Secret 2: TELEGRAM_CHANNEL_ID
- **Name:** `TELEGRAM_CHANNEL_ID`
- **Value:** Your channel username with @ (e.g., `@overgramupdates`)

**Alternatively, use channel ID:**
```bash
# Get channel ID (numeric)
curl "https://api.telegram.org/bot<TOKEN>/sendMessage?chat_id=@overgramupdates&text=test" | jq '.result.chat.id'
```
Then use the numeric ID (e.g., `-1001234567890`)

---

## Step 6: (Optional) Configure APK Signing

### 6.1 Prepare Keystore

If you have a keystore file (e.g., `overgram-release.jks`):

```bash
# Encode to base64
base64 -w 0 overgram-release.jks > keystore-base64.txt

# On macOS:
base64 -i overgram-release.jks -o keystore-base64.txt
```

### 6.2 Add Signing Secrets

Add these secrets in GitHub:

#### Secret 3: KEYSTORE_BASE64
- **Name:** `KEYSTORE_BASE64`
- **Value:** Contents of `keystore-base64.txt` file

#### Secret 4: KEYSTORE_PASSWORD
- **Name:** `KEYSTORE_PASSWORD`
- **Value:** Your keystore password

#### Secret 5: KEY_ALIAS
- **Name:** `KEY_ALIAS`
- **Value:** Your key alias

#### Secret 6: KEY_PASSWORD
- **Name:** `KEY_PASSWORD`
- **Value:** Your key password

**⚠️ If you don't have a keystore:**
- Workflow will build unsigned APK
- See [SIGNING_GUIDE.md](../SIGNING_GUIDE.md) to create one

---

## Step 7: Test the Workflow

### 7.1 Trigger Build

```bash
# Make a small change
echo "# Test" >> README.md

# Commit and push
git add README.md
git commit -m "test: trigger auto-release workflow"
git push origin rewrite
```

### 7.2 Monitor Progress

1. Go to GitHub repository
2. Click **Actions** tab
3. Click on the running workflow
4. Watch the build progress

### 7.3 Check Results

After workflow completes (~5-10 minutes):

**✅ GitHub Release**
- Go to **Releases** tab
- Should see new release (e.g., `beta-20241202-1430`)
- APK attached for download

**✅ Telegram Channel**
- Check your Telegram channel
- Should see new post with APK file
- Beautiful formatted message
- Direct download link

---

## 🎉 Success Checklist

- [ ] Bot created via @BotFather
- [ ] Bot token saved
- [ ] Channel created/configured
- [ ] Bot added as admin to channel
- [ ] "Post Messages" permission enabled
- [ ] Bot test successful (Step 4)
- [ ] GitHub secrets configured
- [ ] Test workflow triggered
- [ ] GitHub release created
- [ ] APK uploaded to Telegram
- [ ] Message formatting looks good

---

## 🐛 Troubleshooting

### Bot can't post to channel

**Error:** `Chat not found` or `Bot was kicked`

**Solution:**
1. Remove bot from channel
2. Re-add as administrator
3. Enable "Post Messages" permission
4. Try again

### File upload fails

**Error:** `File too large`

**Telegram limit:** 50 MB for bot API

**Solutions:**
1. Enable ProGuard/R8 minification in `build.gradle`
2. Remove unused resources
3. Split APKs by architecture
4. Workflow will send GitHub link instead

### Workflow doesn't trigger

**Check:**
1. Workflow file is in `.github/workflows/` directory
2. Branch name matches trigger (e.g., `rewrite`, `main`, `develop`)
3. Workflow file has no syntax errors
4. Actions are enabled in repository settings

### Secrets not working

**Check:**
1. Secret names match exactly (case-sensitive)
2. No extra spaces in secret values
3. Secrets are in repository settings (not environment)
4. Re-save secrets if unsure

---

## 🔒 Security Best Practices

### Bot Token Security

✅ **DO:**
- Store in GitHub Secrets only
- Never commit to repository
- Rotate if leaked
- Use for one purpose only

❌ **DON'T:**
- Share publicly
- Commit to code
- Use in multiple projects
- Log in plaintext

### Channel Security

✅ **DO:**
- Minimal bot permissions
- Regular permission audits
- Monitor bot activity

❌ **DON'T:**
- Give unnecessary permissions
- Share admin access widely

---

## 📊 Monitoring

### Check Upload Success

After each build, verify:

1. **GitHub Actions Log**
   - Look for: `✅ APK uploaded successfully to Telegram!`
   - Or: `❌ Failed to upload APK`

2. **Telegram Channel**
   - New post with APK
   - Correct version number
   - Proper formatting

3. **Release Page**
   - GitHub release created
   - APK attached
   - Release notes generated

### Telegram Upload Logs

In workflow logs, look for:
```
📱 Uploading APK to Telegram channel...
File size: 87M
✅ APK uploaded successfully to Telegram!
```

---

## 🎨 Customization

### Change Message Format

Edit `.github/workflows/auto-release.yml` around line 250:

```yaml
CAPTION="${EMOJI} <b>Overgram Android ${VERSION}</b>

Your custom message here

👨‍💻 By @overspend1 • 💬 @overgramchat"
```

### Change Version Format

Edit version logic in workflow:

```yaml
# Stable releases (main branch)
VERSION=$(date +"%Y.%m.%d")  # Change format

# Beta builds
VERSION="beta-$(date +"%Y%m%d-%H%M")"  # Change format
```

### Multiple Channels

Send to different channels based on branch:

```yaml
if [ "$GITHUB_REF" == "refs/heads/main" ]; then
  CHANNEL="@overgramupdates"
else
  CHANNEL="@overgrambeta"
fi
```

---

## 💡 Pro Tips

1. **Pin Important Releases** - Pin message in channel for visibility
2. **Delete Old Betas** - Keep channel clean by removing old beta builds
3. **Use Emojis** - 🚀 for releases, 🧪 for betas, 🐛 for hotfixes
4. **Cross-Post** - Forward to chat group for discussion
5. **Custom Thumbnails** - Upload logo with `-F thumb=@logo.png`
6. **Changelog** - Update caption with actual changes from commits

---

## 🔗 Useful Links

- [Telegram Bot API Docs](https://core.telegram.org/bots/api)
- [GitHub Actions Docs](https://docs.github.com/en/actions)
- [Workflow README](.github/workflows/README.md)
- [Signing Guide](../SIGNING_GUIDE.md)

---

## ❓ Need Help?

- 📱 Telegram: [@overgramchat](https://t.me/overgramchat)
- 🐛 Issues: [GitHub Issues](https://github.com/overspend1/Overgram4A/issues)
- 📧 Developer: [@overspend1](https://github.com/overspend1)

---

**Developed by [@overspend1](https://github.com/overspend1)**
