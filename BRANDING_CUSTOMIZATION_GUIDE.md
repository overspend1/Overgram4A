# 🎨 Branding Customization Guide

Complete guide to customize Overgram with your own branding.

## 📋 What Can Be Customized

1. **App Name** - Display name shown on home screen
2. **Package Name** - Unique identifier (com.your.app)
3. **App Icon** - Icon shown on home screen
4. **App Logo** - Logo in About screen
5. **Colors & Theme** - Primary/accent colors
6. **Splash Screen** - Launch screen
7. **About Information** - Version, developer name, links

---

## 1. 📱 Change App Name

### Edit app name in strings.xml:

```bash
# Open the file
nano TMessagesProj/src/main/res/values/strings.xml
```

Find and edit:
```xml
<string name="AppName">Overgram</string>
<string name="AppNameBeta">Overgram Beta</string>
```

Change to your app name:
```xml
<string name="AppName">YourApp</string>
<string name="AppNameBeta">YourApp Beta</string>
```

---

## 2. 📦 Change Package Name

### Edit gradle.properties:

```bash
nano gradle.properties
```

Find:
```properties
APP_PACKAGE=com.radolyn.ayugram
```

Change to your package:
```properties
APP_PACKAGE=com.yourname.yourapp
```

### Update AndroidManifest.xml:

```bash
nano TMessagesProj/config/release/AndroidManifest.xml
```

Replace all occurrences of `com.radolyn.ayugram` with your package name.

**⚠️ IMPORTANT:** Package name changes require:
1. Fresh install (uninstall old version first)
2. Updated Firebase project (google-services.json)
3. New signing keystore

---

## 3. 🎨 Replace App Icons

### Icon locations:

```
TMessagesProj/src/main/res/
├── mipmap-mdpi/ic_launcher.png       (48x48px)
├── mipmap-hdpi/ic_launcher.png       (72x72px)
├── mipmap-xhdpi/ic_launcher.png      (96x96px)
├── mipmap-xxhdpi/ic_launcher.png     (144x144px)
└── mipmap-xxxhdpi/ic_launcher.png    (192x192px)
```

### Quick replace:

```bash
# Create icons from a single image using ImageMagick
convert your-icon.png -resize 48x48 TMessagesProj/src/main/res/mipmap-mdpi/ic_launcher.png
convert your-icon.png -resize 72x72 TMessagesProj/src/main/res/mipmap-hdpi/ic_launcher.png
convert your-icon.png -resize 96x96 TMessagesProj/src/main/res/mipmap-xhdpi/ic_launcher.png
convert your-icon.png -resize 144x144 TMessagesProj/src/main/res/mipmap-xxhdpi/ic_launcher.png
convert your-icon.png -resize 192x192 TMessagesProj/src/main/res/mipmap-xxxhdpi/ic_launcher.png
```

### Adaptive icons (Android 8+):

```
mipmap-anydpi-v26/ic_launcher.xml
mipmap-anydpi-v26/ic_launcher_round.xml
```

For adaptive icons, create:
```
drawable-v24/ic_launcher_foreground.xml
drawable/ic_launcher_background.xml
```

### Online tools:

- [Android Asset Studio](https://romannurik.github.io/AndroidAssetStudio/)
- [App Icon Generator](https://appicon.co/)

---

## 4. 🖼️ Change App Logo

### In-app logo (About screen):

```bash
# Replace logo in drawable folders
cp your-logo.png TMessagesProj/src/main/res/drawable-xxhdpi/intro_tg_plane.png
cp your-logo.png TMessagesProj/src/main/res/drawable-xhdpi/intro_tg_plane.png
```

---

## 5. 🎨 Customize Colors

### Primary colors:

```bash
nano TMessagesProj/src/main/res/values/colors.xml
```

Edit:
```xml
<color name="brand">#0088cc</color>
<color name="brand_light">#00aaff</color>
<color name="brand_dark">#006699</color>
```

### Material You colors (Android 12+):

Already implemented in Overgram! Colors adapt to user's wallpaper.

---

## 6. 🚀 Customize Splash Screen

### Edit splash layout:

```bash
nano TMessagesProj/src/main/res/drawable/telegram_logo.xml
```

Or replace image:
```bash
cp your-splash-logo.png TMessagesProj/src/main/res/drawable-xxhdpi/telegram_logo.png
```

---

## 7. 📝 Update About Information

### Developer name and links:

Find and edit in source files:
```bash
# Search for references to "overspend1", "radolyn", etc.
grep -r "overspend1" TMessagesProj/src/main/java/ --include="*.java"
grep -r "radolyn" TMessagesProj/src/main/java/ --include="*.java"
```

Common files to update:
- `TMessagesProj/src/main/java/*/ui/AboutActivity.java`
- `TMessagesProj/src/main/res/values/strings.xml`

### Update version info:

```bash
nano gradle.properties
```

```properties
APP_VERSION_NAME=1.0.0
APP_VERSION_CODE=100
```

---

## 8. 🔗 Update URLs and Links

### Website, social media, etc:

Search and replace in source:
```bash
# Find all URLs
grep -r "overgram.one" TMessagesProj/src/
grep -r "overgramupdates" TMessagesProj/src/
grep -r "overgramchat" TMessagesProj/src/
```

Update to your links:
- Website: `yourapp.com`
- Telegram channel: `@yourappupdates`
- Telegram chat: `@yourappchat`
- GitHub: `github.com/yourname/yourapp`

---

## 9. 🔑 Update API Keys

### Telegram API:

```bash
nano API_KEYS
```

Get your own keys at: https://my.telegram.org/apps

```properties
APP_ID = YOUR_APP_ID
APP_HASH = "YOUR_APP_HASH"
```

### Google Maps (Optional):

```properties
MAPS_V2_API = "YOUR_MAPS_API_KEY"
```

Get at: https://console.cloud.google.com/apis/credentials

---

## 10. 🔒 Create Your Own Keystore

### Generate keystore:

```bash
keytool -genkey -v \
  -keystore yourapp-release.keystore \
  -alias yourapp \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storepass your_store_password \
  -keypass your_key_password \
  -dname "CN=Your Name, OU=YourApp, O=Your Company, L=City, ST=State, C=US"
```

### Update keystore.properties:

```properties
storeFile=../yourapp-release.keystore
storePassword=your_store_password
keyAlias=yourapp
keyPassword=your_key_password
```

**⚠️ IMPORTANT:**
- Keep keystore safe! You can't publish updates without it.
- Never commit keystore to git
- Back it up securely

---

## 11. 🎯 Branding Checklist

Use this checklist to ensure complete rebranding:

- [ ] App name in strings.xml
- [ ] Package name in gradle.properties
- [ ] Package name in AndroidManifest.xml
- [ ] App icons (5 sizes)
- [ ] Adaptive icon (if using)
- [ ] App logo in About screen
- [ ] Primary colors
- [ ] Splash screen logo
- [ ] Developer name references
- [ ] Website URL
- [ ] Telegram channel/chat links
- [ ] GitHub repository URL
- [ ] Telegram API credentials (APP_ID, APP_HASH)
- [ ] Google Maps API key (if using)
- [ ] New signing keystore
- [ ] Firebase project (google-services.json)
- [ ] Version name and code

---

## 12. 🧪 Testing

### Test your branding:

```bash
# Build debug APK
./gradlew assembleAfatDebug

# Install on device
adb install TMessagesProj/build/outputs/apk/afat/debug/*.apk
```

### Check:

- [ ] App name on home screen
- [ ] App icon on home screen
- [ ] Package name (in Settings → Apps)
- [ ] About screen information
- [ ] Colors and theme
- [ ] Splash screen
- [ ] All links work correctly

---

## 13. 🚀 Build & Release

### Build release APK:

```bash
./gradlew assembleAfatRelease
```

### Or push to GitHub:

```bash
git add .
git commit -m "feat: custom branding"
git push origin rewrite
```

GitHub Actions will automatically:
- Build signed APK
- Create release
- Upload to Telegram

---

## 📦 Complete Example

Here's a complete rebranding example from "Overgram" to "MyTelegram":

### 1. gradle.properties:
```properties
APP_VERSION_NAME=1.0.0
APP_VERSION_CODE=100
APP_PACKAGE=com.myname.mytelegram
```

### 2. strings.xml:
```xml
<string name="AppName">MyTelegram</string>
```

### 3. API_KEYS:
```properties
APP_ID = 12345
APP_HASH = "your_hash_here"
```

### 4. Icons:
```bash
# Generate all icon sizes from source
convert mytelegram-icon.png -resize 48x48 ...
```

### 5. Keystore:
```bash
keytool -genkey -v -keystore mytelegram.keystore -alias mytelegram ...
```

### 6. Update all references:
```bash
grep -rl "overgram" TMessagesProj/src/ | xargs sed -i 's/overgram/mytelegram/gi'
grep -rl "radolyn" TMessagesProj/src/ | xargs sed -i 's/radolyn/myname/gi'
```

---

## 🐛 Common Issues

### Package name conflicts:

**Error:** App already installed with different signature

**Solution:** Uninstall old app first
```bash
adb uninstall com.radolyn.ayugram
adb install your-new-app.apk
```

### Icon not updating:

**Solution:** Clear launcher cache or reboot device

### Colors not changing:

**Solution:** Clean and rebuild
```bash
./gradlew clean
./gradlew assembleAfatDebug
```

---

## 💡 Pro Tips

1. **Keep a brand guide** - Document your colors, fonts, etc.
2. **Use version control** - Commit each branding change separately
3. **Test on multiple devices** - Icons look different on various launchers
4. **Backup keystore** - Store it securely (encrypted backup, password manager)
5. **Consistent naming** - Use same name across all platforms
6. **Update README** - Reflect your branding in documentation

---

## 🔗 Resources

- [Android App Bundle](https://developer.android.com/guide/app-bundle)
- [Material Design Colors](https://material.io/design/color)
- [Icon Guidelines](https://developer.android.com/guide/practices/ui_guidelines/icon_design_adaptive)
- [Android Asset Studio](https://romannurik.github.io/AndroidAssetStudio/)

---

**Good luck with your rebranding!** 🎨✨
