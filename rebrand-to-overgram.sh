#!/bin/bash
# Rebrand from AyuGram to Overgram
# This script renames packages, classes, and references throughout the codebase

set -e

echo "🎨 Overgram Rebranding Script"
echo "=============================="
echo ""
echo "This will rebrand AyuGram to Overgram throughout the codebase"
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
OLD_PACKAGE="com.radolyn.ayugram"
NEW_PACKAGE="com.overspend1.overgram"
OLD_PACKAGE_PATH="com/radolyn/ayugram"
NEW_PACKAGE_PATH="com/overspend1/overgram"

echo -e "${YELLOW}⚠️  WARNING: This will make extensive changes!${NC}"
echo ""
echo "Changes:"
echo "  • Package: $OLD_PACKAGE → $NEW_PACKAGE"
echo "  • Class prefix: Ayu* → Over*"
echo "  • String resources: ayu_* → over_*"
echo "  • File names and directories"
echo ""
read -p "Continue? (y/N) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Aborted."
    exit 1
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 1: Update gradle.properties"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if [ -f "gradle.properties" ]; then
    sed -i.bak 's/APP_PACKAGE=com\.radolyn\.ayugram/APP_PACKAGE=com.overspend1.overgram/' gradle.properties
    echo -e "${GREEN}✅ Updated APP_PACKAGE in gradle.properties${NC}"
else
    echo -e "${RED}❌ gradle.properties not found${NC}"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 2: Rename Java package directory"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

SRC_DIR="TMessagesProj/src/main/java"
OLD_DIR="$SRC_DIR/$OLD_PACKAGE_PATH"
NEW_DIR="$SRC_DIR/$NEW_PACKAGE_PATH"

if [ -d "$OLD_DIR" ]; then
    echo "Creating new package structure..."
    mkdir -p "$(dirname "$NEW_DIR")"

    echo "Moving files from $OLD_PACKAGE_PATH to $NEW_PACKAGE_PATH..."
    mv "$OLD_DIR" "$NEW_DIR"

    echo -e "${GREEN}✅ Package directory renamed${NC}"

    # Clean up old empty directories
    rm -rf "$SRC_DIR/com/radolyn" 2>/dev/null || true
else
    echo -e "${YELLOW}⚠️  Old package directory not found (may already be renamed)${NC}"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 3: Update package declarations in Java files"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

echo "Updating package declarations..."
find "$NEW_DIR" -name "*.java" -type f -exec sed -i.bak \
    "s/package com\.radolyn\.ayugram/package com.overspend1.overgram/g" {} \;

echo -e "${GREEN}✅ Updated package declarations${NC}"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 4: Update imports throughout codebase"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

echo "Updating import statements..."
find "$SRC_DIR" -name "*.java" -type f -exec sed -i.bak \
    "s/import com\.radolyn\.ayugram/import com.overspend1.overgram/g" {} \;

echo -e "${GREEN}✅ Updated imports${NC}"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 5: Rename class prefixes (Ayu → Over)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

echo "This will rename:"
echo "  • AyuConfig → OverConfig"
echo "  • AyuUtils → OverUtils"
echo "  • etc."
echo ""

# First, rename the actual files
cd "$NEW_DIR"
for file in $(find . -name "Ayu*.java"); do
    newfile=$(echo "$file" | sed 's/Ayu/Over/g')
    if [ "$file" != "$newfile" ]; then
        echo "  Renaming: $file → $newfile"
        mv "$file" "$newfile"
    fi
done
cd - > /dev/null

# Update class names in file contents
find "$SRC_DIR" -name "*.java" -type f -exec sed -i.bak \
    -e 's/\bAyuConfig\b/OverConfig/g' \
    -e 's/\bAyuConstants\b/OverConstants/g' \
    -e 's/\bAyuUtils\b/OverUtils/g' \
    -e 's/\bAyuFilter\b/OverFilter/g' \
    -e 's/\bAyuForwarder\b/OverForwarder/g' \
    -e 's/\bAyuCustomHandlers\b/OverCustomHandlers/g' \
    -e 's/\bAyuState\b/OverState/g' \
    -e 's/\bAyuStateVariable\b/OverStateVariable/g' \
    -e 's/\bAyuDatabase\b/OverDatabase/g' \
    -e 's/\bAyuData\b/OverData/g' \
    -e 's/\bAyuFileLocation\b/OverFileLocation/g' \
    -e 's/\bAyuGhostUtils\b/OverGhostUtils/g' \
    -e 's/\bAyuEasyUtils\b/OverEasyUtils/g' \
    -e 's/\bAyuMessageBase\b/OverMessageBase/g' \
    -e 's/\bAyuMessagesController\b/OverMessagesController/g' \
    -e 's/\bAyuSavePreferences\b/OverSavePreferences/g' \
    -e 's/\bAyuMessageCell\b/OverMessageCell/g' \
    -e 's/\bAyuMessageHistory\b/OverMessageHistory/g' \
    -e 's/\bAyuUi\b/OverUi/g' \
    -e 's/\bAyuGramPreferencesActivity\b/OvergramPreferencesActivity/g' \
    -e 's/\bAyuSyncPreferencesActivity\b/OverSyncPreferencesActivity/g' \
    -e 's/\bAyuSyncConfig\b/OverSyncConfig/g' \
    -e 's/\bAyuSyncController\b/OverSyncController/g' \
    -e 's/\bAyuSyncControllerEmpty\b/OverSyncControllerEmpty/g' \
    -e 's/\bAyuSyncState\b/OverSyncState/g' \
    -e 's/\bAyuSyncConnectionState\b/OverSyncConnectionState/g' \
    -e 's/\bAyuSyncWebSocketClient\b/OverSyncWebSocketClient/g' \
    -e 's/\bAyuInterceptor\b/OverInterceptor/g' \
    -e 's/\bAyuUser\b/OverUser/g' \
    -e 's/\bAyuPrivacyException\b/OverPrivacyException/g' \
    -e 's/\bAyuMessageUtils\b/OverMessageUtils/g' \
    -e 's/\bAyuHistoryHook\b/OverHistoryHook/g' \
    {} \;

echo -e "${GREEN}✅ Renamed class prefixes${NC}"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 6: Update string resources"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Rename ayu.xml to over.xml in all values directories
for ayuxml in $(find TMessagesProj/src/main/res -name "ayu.xml"); do
    overxml=$(dirname "$ayuxml")/over.xml
    echo "  Renaming: $ayuxml → $overxml"
    mv "$ayuxml" "$overxml"
done

# Update string resource names (ayu_ → over_)
find TMessagesProj/src/main/res -name "*.xml" -type f -exec sed -i.bak \
    -e 's/name="ayu_/name="over_/g' \
    -e 's/@string\/ayu_/@string\/over_/g' \
    -e 's/R\.string\.ayu_/R.string.over_/g' \
    {} \;

# Update Java files to use new string resource names
find "$SRC_DIR" -name "*.java" -type f -exec sed -i.bak \
    's/R\.string\.ayu_/R.string.over_/g' {} \;

echo -e "${GREEN}✅ Updated string resources${NC}"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 7: Update text references (AyuGram → Overgram)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Update visible strings (but preserve database/preference keys)
find TMessagesProj/src/main/res -name "*.xml" -type f -exec sed -i.bak \
    -e 's/>AyuGram</>Overgram</g' \
    -e 's/>AyuSync</>OvergramSync</g' \
    -e 's/"AyuGram"/"Overgram"/g' \
    {} \;

# Update comments and documentation
find "$SRC_DIR" -name "*.java" -type f -exec sed -i.bak \
    -e 's/\bAyuGram\b/Overgram/g' \
    -e 's/\bAyuSync\b/OvergramSync/g' \
    {} \;

echo -e "${GREEN}✅ Updated text references${NC}"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 8: Update build.gradle output file names"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if [ -f "TMessagesProj/build.gradle" ]; then
    sed -i.bak 's/ayuGram/overgram/g' TMessagesProj/build.gradle
    echo -e "${GREEN}✅ Updated build.gradle${NC}"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 9: Clean up backup files"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

echo "Removing .bak files..."
find . -name "*.bak" -type f -delete
echo -e "${GREEN}✅ Cleaned up backup files${NC}"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ Rebranding Complete!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo -e "${GREEN}Summary of changes:${NC}"
echo "  ✅ Package renamed: $OLD_PACKAGE → $NEW_PACKAGE"
echo "  ✅ Class prefixes: Ayu* → Over*"
echo "  ✅ String resources: ayu_* → over_*"
echo "  ✅ Text references: AyuGram → Overgram"
echo ""
echo -e "${YELLOW}⚠️  IMPORTANT: Manual checks needed:${NC}"
echo ""
echo "1. Database table/column names"
echo "   • Old data may be lost if names changed"
echo "   • Check migration code if needed"
echo ""
echo "2. SharedPreferences keys"
echo "   • Old settings may be lost"
echo "   • Consider migration code"
echo ""
echo "3. Test build:"
echo "   ./gradlew clean assembleAfatDebug"
echo ""
echo "4. Review changes:"
echo "   git status"
echo "   git diff"
echo ""
echo "5. Commit if everything looks good:"
echo "   git add ."
echo "   git commit -m \"refactor: rebrand from AyuGram to Overgram\""
echo ""
echo -e "${BLUE}📋 Next steps:${NC}"
echo "  1. Review the changes carefully"
echo "  2. Test build the project"
echo "  3. Fix any compilation errors"
echo "  4. Test the app on a device"
echo "  5. Commit and push"
echo ""
