#!/bin/bash
# Overgram Project Setup Script
# Sets up missing files and configurations for building

set -e

echo "🚀 Overgram Project Setup"
echo "========================="
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check if API_KEYS exists
echo "📋 Step 1: Check API_KEYS file"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if [ -f "API_KEYS" ]; then
    echo -e "${GREEN}✅ API_KEYS file exists${NC}"
else
    echo -e "${YELLOW}⚠️  API_KEYS file missing${NC}"
    echo ""
    echo "Creating API_KEYS template..."

    cat > API_KEYS <<'EOF'
# Overgram API Configuration
# Get your API credentials at: https://my.telegram.org/apps

APP_ID = 6
APP_HASH = "eb06d4abfb49dc3eeb1aeb98ae0f581e"

# Google Maps API Key (optional)
# Get it at: https://console.cloud.google.com/apis/credentials
MAPS_V2_API = ""

# APK Signing Configuration (optional, for local builds)
# These are only used if you don't have keystore.properties
SIGNING_KEY_PASSWORD = ""
SIGNING_KEY_ALIAS = ""
SIGNING_KEY_STORE_PASSWORD = ""
EOF

    echo -e "${GREEN}✅ Created API_KEYS template${NC}"
    echo ""
    echo -e "${BLUE}📝 IMPORTANT: Edit API_KEYS file with your credentials:${NC}"
    echo "   1. Get API credentials at: https://my.telegram.org/apps"
    echo "   2. Replace APP_ID and APP_HASH with your values"
    echo "   3. (Optional) Add Google Maps API key"
    echo ""
fi

# Check proprietary classes
echo ""
echo "📋 Step 2: Check proprietary classes"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

AYUGRAM_DIR="TMessagesProj/src/main/java/com/radolyn/ayugram/proprietary"

if [ -d "$AYUGRAM_DIR" ] && [ -f "$AYUGRAM_DIR/AyuMessageUtils.java" ]; then
    echo -e "${GREEN}✅ Proprietary classes exist${NC}"
else
    echo -e "${YELLOW}⚠️  Proprietary classes missing${NC}"
    echo ""
    echo "Creating stub implementations..."

    mkdir -p "$AYUGRAM_DIR"

    # Create AyuMessageUtils stub
    cat > "$AYUGRAM_DIR/AyuMessageUtils.java" <<'EOF'
package com.radolyn.ayugram.proprietary;

import org.telegram.tgnet.TLRPC;

/**
 * Stub implementation of AyuMessageUtils
 * Replace this with the actual implementation if you have access to it.
 */
public class AyuMessageUtils {
    public static void processMessage(TLRPC.Message message) {
        // Stub: Do nothing
    }

    public static void saveMessageHistory(TLRPC.Message message) {
        // Stub: Do nothing
    }
}
EOF

    # Create AyuHistoryHook stub
    cat > "$AYUGRAM_DIR/AyuHistoryHook.java" <<'EOF'
package com.radolyn.ayugram.proprietary;

import org.telegram.tgnet.TLRPC;

/**
 * Stub implementation of AyuHistoryHook
 * Replace this with the actual implementation if you have access to it.
 */
public class AyuHistoryHook {
    public static void onMessageReceived(TLRPC.Message message) {
        // Stub: Do nothing
    }

    public static void onMessageEdited(TLRPC.Message oldMessage, TLRPC.Message newMessage) {
        // Stub: Do nothing
    }

    public static void onMessageDeleted(TLRPC.Message message) {
        // Stub: Do nothing
    }
}
EOF

    echo -e "${GREEN}✅ Created stub implementations${NC}"
    echo ""
    echo -e "${YELLOW}⚠️  NOTE: These are stub implementations.${NC}"
    echo "   If you have the actual implementations, replace these files."
    echo "   The app will build but history features won't work properly."
    echo ""
fi

# Check keystore
echo ""
echo "📋 Step 3: Check APK signing configuration"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if [ -f "keystore.properties" ]; then
    echo -e "${GREEN}✅ keystore.properties exists${NC}"
else
    if [ -f "TMessagesProj/config/extera.jks" ]; then
        echo -e "${YELLOW}⚠️  keystore.properties missing but extera.jks found${NC}"
        echo ""
        echo "Creating keystore.properties for extera.jks..."

        cat > keystore.properties <<'EOF'
# Using existing extera.jks keystore
storeFile=TMessagesProj/config/extera.jks
storePassword=radolyn
keyAlias=extera
keyPassword=radolyn
EOF

        echo -e "${GREEN}✅ Created keystore.properties (using extera.jks)${NC}"
        echo ""
        echo -e "${BLUE}📝 NOTE: Using default extera.jks keystore${NC}"
        echo "   For production builds, create your own keystore:"
        echo "   See SIGNING_GUIDE.md for instructions"
        echo ""
    else
        echo -e "${YELLOW}⚠️  No keystore found${NC}"
        echo ""
        echo "Creating keystore.properties template..."

        cp keystore.properties.example keystore.properties

        echo -e "${GREEN}✅ Created keystore.properties template${NC}"
        echo ""
        echo -e "${BLUE}📝 To create a keystore:${NC}"
        echo "   1. See SIGNING_GUIDE.md for detailed instructions"
        echo "   2. Or use the GitHub Actions workflow (no local keystore needed)"
        echo ""
    fi
fi

# Check app branding
echo ""
echo "📋 Step 4: App branding"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

echo -e "${BLUE}Current configuration:${NC}"
echo "  Package: com.radolyn.ayugram (from gradle.properties)"
echo "  App Name: (check AndroidManifest.xml)"
echo "  Icons: TMessagesProj/src/main/res/mipmap-*/"
echo ""
echo -e "${YELLOW}⚠️  To customize:${NC}"
echo "  1. Package name: Edit gradle.properties (APP_PACKAGE)"
echo "  2. App name: Edit TMessagesProj/src/main/res/values/strings.xml"
echo "  3. Icons: Replace files in TMessagesProj/src/main/res/mipmap-*/"
echo "  4. See BRANDING_GUIDE.md (if exists) or create one"
echo ""

# Summary
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ Setup Complete!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo -e "${GREEN}✅ API_KEYS${NC} - Created/exists"
echo -e "${GREEN}✅ Proprietary classes${NC} - Stubs created"
echo -e "${GREEN}✅ Keystore config${NC} - Created/exists"
echo ""
echo -e "${BLUE}📋 Next Steps:${NC}"
echo ""
echo "1. Edit API_KEYS with your Telegram API credentials"
echo "   Get them at: https://my.telegram.org/apps"
echo ""
echo "2. (Optional) Customize branding:"
echo "   - Package name in gradle.properties"
echo "   - App name in strings.xml"
echo "   - App icons in mipmap folders"
echo ""
echo "3. Test build:"
echo "   ./gradlew assembleAfatDebug"
echo ""
echo "4. Or build release:"
echo "   ./gradlew assembleAfatRelease"
echo ""
echo "5. Push to GitHub to trigger CI/CD:"
echo "   git add ."
echo "   git commit -m \"chore: setup project for build\""
echo "   git push origin rewrite"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "🎉 Ready to build!"
echo ""
