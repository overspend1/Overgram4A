# Android App Signing Guide

Complete guide for signing Overgram for Android releases.

## Quick Start

### 1. Generate a Keystore

```bash
# Generate a new keystore
keytool -genkey -v -keystore overgram-release.keystore \
  -alias overgram \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000

# You'll be prompted for:
# - Keystore password (remember this!)
# - Key password (remember this!)
# - Your name, organization, city, state, country
```

**Important**: Store this keystore file and passwords securely! You'll need them for every release. If you lose them, you cannot update the app on Google Play Store.

### 2. Create keystore.properties

```bash
# Copy the example file
cp keystore.properties.example keystore.properties

# Edit with your values
nano keystore.properties
```

Fill in your actual values:
```properties
storeFile=../overgram-release.keystore
storePassword=YourSecurePassword123
keyAlias=overgram
keyPassword=YourSecurePassword123
```

### 3. Build Signed APK/AAB

```bash
# Build signed release APK
./gradlew assembleAfatRelease

# Build signed AAB for Play Store
./gradlew bundleAfatRelease
```

The signed files will be in:
- **APK**: `TMessagesProj/build/outputs/apk/afat/release/`
- **AAB**: `TMessagesProj/build/outputs/bundle/afatRelease/`

## Detailed Setup

### Understanding Android App Signing

Android requires all APKs to be digitally signed before installation. There are two types of keys:

1. **App Signing Key**: The ultimate key Google uses to sign your APK (managed by Google Play)
2. **Upload Key**: The key you use to sign APKs before uploading to Play Console

### Setting Up Play App Signing

When publishing to Google Play Store for the first time:

1. **Let Google Create the App Signing Key** (Recommended)
   - Google generates and securely stores the app signing key
   - You create and use an upload key for all uploads
   - Google re-signs your app with the app signing key

2. **Manual Setup**:
   - Generate app signing key yourself
   - Upload it to Google Play Console
   - Create a separate upload key

### Creating Multiple Keys

For enhanced security, use separate keys:

```bash
# App signing key (keep this VERY secure, you may never need to use it directly)
keytool -genkey -v -keystore overgram-app-signing.keystore \
  -alias overgram-app \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000

# Upload key (use this for daily builds)
keytool -genkey -v -keystore overgram-upload.keystore \
  -alias overgram-upload \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

Update `keystore.properties`:
```properties
# App signing key
storeFile=../overgram-app-signing.keystore
storePassword=YourAppSigningPassword
keyAlias=overgram-app
keyPassword=YourAppSigningPassword

# Upload key
uploadStoreFile=../overgram-upload.keystore
uploadStorePassword=YourUploadPassword
uploadKeyAlias=overgram-upload
uploadKeyPassword=YourUploadPassword
```

## Gradle Configuration

The build.gradle already includes signing configuration. Here's how it works:

```gradle
// In TMessagesProj/build.gradle

android {
    signingConfigs {
        release {
            // Load keystore.properties
            def keystorePropertiesFile = rootProject.file("keystore.properties")
            if (keystorePropertiesFile.exists()) {
                def keystoreProperties = new Properties()
                keystoreProperties.load(new FileInputStream(keystorePropertiesFile))

                storeFile file(keystoreProperties['storeFile'])
                storePassword keystoreProperties['storePassword']
                keyAlias keystoreProperties['keyAlias']
                keyPassword keystoreProperties['keyPassword']
            }
        }
    }

    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

## CI/CD Signing (Buildkite)

For automated builds, store keystore and credentials securely:

### 1. Encode Keystore to Base64

```bash
# Encode keystore
base64 overgram-release.keystore > keystore.base64

# For Windows PowerShell:
[Convert]::ToBase64String([IO.File]::ReadAllBytes("overgram-release.keystore")) | Out-File keystore.base64
```

### 2. Set Buildkite Environment Variables

In Buildkite settings, add these encrypted environment variables:

```bash
KEYSTORE_BASE64=<contents of keystore.base64>
KEYSTORE_PASSWORD=YourPassword
KEY_ALIAS=overgram
KEY_PASSWORD=YourPassword
```

### 3. Update .buildkite/pipeline.yml

The pipeline already includes signing setup:

```yaml
- label: "📱 Build Android Release"
  commands:
    # Decode keystore
    - echo "$KEYSTORE_BASE64" | base64 -d > overgram-release.keystore

    # Create keystore.properties
    - |
      cat > keystore.properties <<EOF
      storeFile=../overgram-release.keystore
      storePassword=$KEYSTORE_PASSWORD
      keyAlias=$KEY_ALIAS
      keyPassword=$KEY_PASSWORD
      EOF

    # Build signed APK
    - ./gradlew assembleAfatRelease

    # Build signed AAB
    - ./gradlew bundleAfatRelease
```

## Verifying Signatures

### Check APK Signature

```bash
# Using apksigner (Android SDK build tools)
apksigner verify --verbose app-release.apk

# Using jarsigner
jarsigner -verify -verbose -certs app-release.apk
```

### View Certificate Details

```bash
# Extract certificate from APK
keytool -printcert -jarfile app-release.apk

# View keystore certificate
keytool -list -v -keystore overgram-release.keystore -alias overgram
```

### Compare Signatures

```bash
# Get SHA-256 fingerprint from keystore
keytool -list -v -keystore overgram-release.keystore -alias overgram | grep SHA256

# Get SHA-256 from APK
apksigner verify --print-certs app-release.apk | grep SHA-256
```

They should match!

## Security Best Practices

### 1. Keystore Storage

✅ **DO**:
- Store keystore in a secure, encrypted location
- Keep multiple backups (encrypted cloud storage, physical USB drive)
- Use a password manager for passwords
- Never commit keystore to git

❌ **DON'T**:
- Store keystore in the project directory
- Share keystore via email or chat
- Use weak passwords
- Reuse passwords from other services

### 2. Password Management

```bash
# Use strong, unique passwords
# Example generation:
openssl rand -base64 32

# Store in password manager (1Password, Bitwarden, etc.)
```

### 3. Access Control

- Limit who has access to the keystore
- Use separate upload keys for different developers
- Rotate upload keys periodically
- Revoke access when team members leave

### 4. Git Ignore

Ensure these are in `.gitignore`:

```gitignore
# Signing
*.keystore
*.jks
keystore.properties
*.base64

# Passwords
secrets.properties
```

## Troubleshooting

### Error: "keystore.properties not found"

**Solution**:
```bash
# Create from example
cp keystore.properties.example keystore.properties
# Edit with your values
```

### Error: "keystore cannot be found"

**Solution**:
```bash
# Verify file exists
ls -la ../overgram-release.keystore

# Use absolute path in keystore.properties
storeFile=/full/path/to/overgram-release.keystore
```

### Error: "Incorrect keystore password"

**Solution**:
```bash
# Verify password is correct
keytool -list -keystore overgram-release.keystore

# If forgotten, you must create a new keystore
# (cannot publish updates to existing Play Store app!)
```

### Error: "Key was created with errors"

**Solution**:
```bash
# Ensure you're using the correct key algorithm
keytool -genkey -v -keystore overgram-release.keystore \
  -alias overgram \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

## Play Store Upload

### First Time Setup

1. **Create App in Play Console**
   - Go to https://play.google.com/console
   - Create new app: "Overgram"
   - Fill in store listing, content rating, etc.

2. **Enable Play App Signing**
   - Go to Release > Setup > App Integrity
   - Enroll in Play App Signing
   - Either let Google generate the key or upload yours

3. **Upload AAB**
   ```bash
   # Build AAB
   ./gradlew bundleAfatRelease

   # Upload via Play Console or use Google Play Developer API
   ```

4. **Complete Release**
   - Add release notes
   - Set rollout percentage
   - Submit for review

### Subsequent Releases

```bash
# 1. Update version in build.gradle
versionCode = 2
versionName = "1.1.0"

# 2. Build signed AAB
./gradlew bundleAfatRelease

# 3. Upload to Play Console
# (use same upload key)
```

## Key Rotation

If you need to rotate your upload key (security breach, key compromise):

### 1. Generate New Upload Key

```bash
keytool -genkey -v -keystore overgram-upload-new.keystore \
  -alias overgram-upload-new \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

### 2. Create PEM Certificate

```bash
keytool -export -rfc \
  -keystore overgram-upload-new.keystore \
  -alias overgram-upload-new \
  -file upload_cert_new.pem
```

### 3. Upload to Play Console

1. Go to Play Console > Release > Setup > App Integrity
2. Click "Request upload key reset"
3. Upload `upload_cert_new.pem`
4. Google will approve within 24-48 hours

### 4. Update Configuration

Update `keystore.properties` with new key information.

## Backup Checklist

Before releasing your first version, ensure you have:

- [ ] Keystore file backed up to encrypted cloud storage
- [ ] Keystore file backed up to physical USB drive
- [ ] Passwords stored in password manager
- [ ] Keystore fingerprint documented
- [ ] Team members know backup location
- [ ] Recovery procedure documented

## Emergency Recovery

If you lose your keystore:

### For Apps Not Yet Published:
- Generate a new keystore
- Continue development with new key

### For Published Apps:
- **Cannot update the app** without the original key
- Options:
  1. If using Play App Signing, contact Google Support
  2. Publish as a completely new app (users must reinstall)
  3. Try to recover from backups

This is why **backups are critical**!

## Additional Resources

- [Android Signing Docs](https://developer.android.com/studio/publish/app-signing)
- [Play App Signing](https://support.google.com/googleplay/android-developer/answer/9842756)
- [Keystore Best Practices](https://developer.android.com/training/articles/keystore)

---

**Remember**: Your keystore is the key to your app's identity. Protect it like your most valuable asset!
