# Overgram Branding Assets

This document describes where to place branding assets for Overgram Android.

## Logo Files

### Main App Logo

**Location**: `.github/Overgram.png`
- **Size**: 1024x1024px recommended
- **Format**: PNG with transparency
- **Usage**: README header, documentation
- **Current**: The blue gradient logo with glass effect and paper plane icon

### App Icons (Android)

App icons should be placed in the following locations with appropriate sizes:

#### Launcher Icons
```
TMessagesProj/src/main/res/
├── mipmap-mdpi/ic_launcher.png          (48x48px)
├── mipmap-hdpi/ic_launcher.png          (72x72px)
├── mipmap-xhdpi/ic_launcher.png         (96x96px)
├── mipmap-xxhdpi/ic_launcher.png        (144x144px)
├── mipmap-xxxhdpi/ic_launcher.png       (192x192px)
└── mipmap-anydpi-v26/ic_launcher.xml    (Adaptive icon)
```

#### Adaptive Icons (Android 8.0+)
```
TMessagesProj/src/main/res/
├── mipmap-anydpi-v26/
│   ├── ic_launcher.xml
│   └── ic_launcher_round.xml
└── drawable/
    ├── ic_launcher_background.xml
    └── ic_launcher_foreground.xml
```

### Notification Icon

**Location**: `TMessagesProj/src/main/res/drawable/notification.png`
- **Size**: 24x24dp with appropriate padding
- **Format**: PNG, white silhouette on transparent background
- **Usage**: Status bar notifications

### Splash Screen

**Location**: `TMessagesProj/src/main/res/drawable/`
- `launch_screen.xml` - Vector or image drawable
- Should use the Overgram logo with appropriate background

## Color Scheme

Based on the logo, the primary colors are:

```kotlin
// Primary gradient colors
val primaryLight = Color(0xFF5CCEF5)  // Light cyan/blue
val primaryDark = Color(0xFF2E5A9D)   // Deep blue

// Accent colors
val accentGlass = Color(0xFF4DB8E8)   // Glass blue
val backgroundDark = Color(0xFF0A1628) // Dark navy background

// Material You (Android 12+)
// These will adapt to user's wallpaper
```

## Logo Design Specifications

### Current Logo Features:
- **Style**: Modern glassmorphism
- **Shape**: Rounded square with glass effect
- **Icon**: Telegram paper plane (3D rendered)
- **Gradient**: Cyan to blue (#5CCEF5 → #2E5A9D)
- **Effects**:
  - Subtle inner shadow for depth
  - Soft glow at bottom
  - Glass reflection on icon
  - Dark center square for contrast

### Typography:
- **Wordmark**: "Overgram"
- **Font Style**: Modern sans-serif, bold weight
- **Colors**:
  - "Over" part: Blue gradient (#5CCEF5)
  - "gram" part: White/Light gray (#E0E0E0)

## Screenshot Guidelines

Demo screenshots should be placed in `.github/demos/` directory:

```
.github/demos/
├── demo1.png  (Chat list view)
├── demo2.png  (Chat conversation)
├── demo3.png  (Settings screen)
├── demo4.png  (Ghost mode features)
├── demo5.png  (Liquid glass settings)
└── demo6.png  (Message history)
```

**Specifications**:
- **Size**: 1080x2340px (standard Android resolution)
- **Format**: PNG
- **Content**: Show key features with Overgram branding visible

## Generating Android Icons

### Using Android Studio:
1. Right-click `res` folder → New → Image Asset
2. Select "Launcher Icons (Adaptive and Legacy)"
3. Choose your logo as foreground layer
4. Set background color: `#0A1628` (dark navy)
5. Generate all densities

### Using Command Line (ImageMagick):
```bash
# Install ImageMagick first
# For mipmap densities:
convert Overgram-Icon.png -resize 48x48 res/mipmap-mdpi/ic_launcher.png
convert Overgram-Icon.png -resize 72x72 res/mipmap-hdpi/ic_launcher.png
convert Overgram-Icon.png -resize 96x96 res/mipmap-xhdpi/ic_launcher.png
convert Overgram-Icon.png -resize 144x144 res/mipmap-xxhdpi/ic_launcher.png
convert Overgram-Icon.png -resize 192x192 res/mipmap-xxxhdpi/ic_launcher.png
```

### Using Online Tools:
- [Icon Kitchen](https://icon.kitchen/) - Android adaptive icons
- [App Icon Generator](https://appicon.co/) - Multi-platform icons
- [Android Asset Studio](https://romannurik.github.io/AndroidAssetStudio/) - Official tool

## Play Store Assets

When publishing to Google Play Store, you'll need:

### App Icon
- **Size**: 512x512px
- **Format**: PNG (32-bit)
- **No transparency**, no rounded corners (Google handles this)

### Feature Graphic
- **Size**: 1024x500px
- **Format**: PNG or JPEG
- **Content**: Overgram logo + tagline (e.g., "Make Telegram Your Own")

### Screenshots (Required)
- **Minimum**: 2 screenshots
- **Maximum**: 8 screenshots
- **Size**: 16:9 or 9:16 aspect ratio
- **Dimensions**:
  - Phone: 1080x1920px or 1080x2340px
  - Tablet (optional): 1536x2048px

### Promo Graphics (Optional)
- **Promo Graphic**: 180x120px
- **TV Banner**: 1280x720px

## Branding Checklist

Before releasing Overgram, ensure:

- [ ] Main logo saved as `.github/Overgram.png` (1024x1024px)
- [ ] All launcher icon densities generated
- [ ] Adaptive icon created for Android 8.0+
- [ ] Notification icon updated
- [ ] Splash screen uses Overgram branding
- [ ] Screenshots showcase Liquid Glass feature
- [ ] Play Store assets prepared (512x512 icon, feature graphic)
- [ ] Old AyuGram logos removed from repository
- [ ] README references correct logo path
- [ ] App name in strings.xml updated to "Overgram"

## Logo Source Files

**Recommended**: Keep source files (AI, PSD, SVG, Figma) in a separate design repository or cloud storage:

```
Overgram-Design/
├── Logo/
│   ├── Overgram-Logo.ai         (Adobe Illustrator)
│   ├── Overgram-Logo.svg        (Vector)
│   ├── Overgram-Logo.psd        (Photoshop)
│   └── Overgram-Logo@4x.png     (High-res raster)
├── Icons/
│   ├── App-Icon-Source.psd
│   └── Notification-Icon.svg
├── Screenshots/
│   └── [Original uncompressed screenshots]
└── Play-Store/
    ├── Feature-Graphic.psd
    └── Promo-Graphics.psd
```

**Do NOT** commit large source files to the main repository. Keep them in:
- Private design repository
- Google Drive / Dropbox
- Figma workspace

## Current Status

✅ Logo design created with glass effect
⏳ Need to save logo to `.github/Overgram.png`
⏳ Need to generate Android launcher icons
⏳ Need to update app icons in res/mipmap
⏳ Need to create Play Store assets
⏳ Need to capture screenshots with Liquid Glass enabled
⏳ Need to remove old AyuGram branding

## References

- [Android Icon Design Guidelines](https://developer.android.com/google-play/resources/icon-design-specifications)
- [Material Design - Product Icons](https://material.io/design/iconography/product-icons.html)
- [Adaptive Icons](https://developer.android.com/guide/practices/ui_guidelines/icon_design_adaptive)

---

**Overgram** - Beautiful, Modern, Yours ✨
