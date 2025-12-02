#!/bin/bash
# Final cleanup script to remove all remaining AyuGram branding

set -e

echo "🧹 Overgram Branding Cleanup"
echo "=============================="
echo ""

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 1: Update string resources"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Update all string resource files
find TMessagesProj/src/main/res/values* -name "over.xml" -type f | while read file; do
    echo "  Processing: $file"

    # Replace AyuGram with Overgram
    sed -i.bak \
        -e 's/>AyuGram</>Overgram</g' \
        -e 's/"AyuGram"/"Overgram"/g' \
        -e 's/AyuGram Preferences/Overgram Preferences/g' \
        -e 's/AyuGram Push Service/Overgram Push Service/g' \
        -e 's/AyuGram database/Overgram database/g' \
        -e 's/Clear Ayu Database/Clear Overgram Database/g' \
        "$file"

    # Replace Radolyn Labs with @overspend1
    sed -i.bak \
        -e 's/Radolyn Labs/@overspend1/g' \
        -e 's/developed and maintained by/developed by/g' \
        "$file"

    # Update description
    sed -i.bak \
        -e 's/Just an exteraGram based client with ToS breaking features in mind\./A beautiful Telegram client with Liquid Glass design and powerful privacy features\./g' \
        "$file"
done

echo -e "${GREEN}✅ Updated string resources${NC}"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 2: Rename drawable files"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Rename ayu_ghost.xml to over_ghost.xml
if [ -f "TMessagesProj/src/main/res/drawable/ayu_ghost.xml" ]; then
    mv "TMessagesProj/src/main/res/drawable/ayu_ghost.xml" "TMessagesProj/src/main/res/drawable/over_ghost.xml"
    echo -e "  ${GREEN}✅ Renamed ayu_ghost.xml → over_ghost.xml${NC}"
else
    echo -e "  ${YELLOW}⚠️  ayu_ghost.xml not found (may already be renamed)${NC}"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 3: Update drawable references in code"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Update references to ayu_ghost in Java files
find TMessagesProj/src/main/java -name "*.java" -type f -exec sed -i.bak \
    's/R\.drawable\.ayu_ghost/R.drawable.over_ghost/g' {} \;

# Update references in XML files
find TMessagesProj/src/main/res -name "*.xml" -type f -exec sed -i.bak \
    's/@drawable\/ayu_ghost/@drawable\/over_ghost/g' {} \;

echo -e "${GREEN}✅ Updated drawable references${NC}"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 4: Update preference keys and identifiers"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Update preference key references (but NOT the actual keys to preserve settings)
# We'll keep the internal keys as "ayu_*" for backwards compatibility
# But update display strings

find TMessagesProj/src/main/res/xml -name "*.xml" -type f -exec sed -i.bak \
    -e 's/AyuGram/Overgram/g' \
    -e 's/title="Ayu/title="Over/g' \
    {} \;

echo -e "${GREEN}✅ Updated preference displays${NC}"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 5: Update comments and documentation"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Update comments in Java files (but not in com.overspend1.overgram package, already done)
find TMessagesProj/src/main/java/org/telegram -name "*.java" -type f -exec sed -i.bak \
    -e 's/AyuGram/Overgram/g' \
    -e 's/ayugram/overgram/g' \
    {} \;

find TMessagesProj/src/main/java/com/exteragram -name "*.java" -type f -exec sed -i.bak \
    -e 's/AyuGram/Overgram/g' \
    -e 's/ayugram/overgram/g' \
    {} \;

echo -e "${GREEN}✅ Updated comments${NC}"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 6: Clean up backup files"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

find . -name "*.bak" -type f -delete
echo -e "${GREEN}✅ Cleaned up backup files${NC}"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ Cleanup Complete!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo -e "${GREEN}Summary of changes:${NC}"
echo "  ✅ String resources updated (all languages)"
echo "  ✅ Drawable files renamed"
echo "  ✅ Drawable references updated"
echo "  ✅ Preference displays updated"
echo "  ✅ Comments updated"
echo ""
echo -e "${BLUE}📋 Changes made:${NC}"
echo "  • AyuGram → Overgram"
echo "  • Radolyn Labs → @overspend1"
echo "  • ayu_ghost.xml → over_ghost.xml"
echo "  • Updated descriptions"
echo ""
echo -e "${YELLOW}⚠️  Note:${NC}"
echo "  Internal preference keys kept as 'ayu_*' for backwards compatibility"
echo "  This preserves existing user settings"
echo ""
echo -e "${BLUE}Next steps:${NC}"
echo "  1. Review changes: git diff"
echo "  2. Test build: ./gradlew assembleAfatDebug"
echo "  3. Commit: git add . && git commit"
echo ""
