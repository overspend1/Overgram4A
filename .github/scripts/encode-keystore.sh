#!/bin/bash
# Encode keystore to base64 for GitHub Secrets
# Usage: ./encode-keystore.sh path/to/keystore.jks

set -e

echo "🔑 Keystore Encoder for GitHub Actions"
echo "========================================"
echo ""

# Check if keystore file is provided
if [ -z "$1" ]; then
    echo "❌ Error: No keystore file specified"
    echo ""
    echo "Usage: $0 path/to/keystore.jks"
    echo ""
    echo "Example:"
    echo "  $0 overgram-release.jks"
    exit 1
fi

KEYSTORE_FILE="$1"

# Check if file exists
if [ ! -f "$KEYSTORE_FILE" ]; then
    echo "❌ Error: Keystore file not found: $KEYSTORE_FILE"
    exit 1
fi

echo "📂 Keystore file: $KEYSTORE_FILE"
echo ""

# Get file info
FILE_SIZE=$(ls -lh "$KEYSTORE_FILE" | awk '{print $5}')
echo "📊 File size: $FILE_SIZE"
echo ""

# Encode to base64
echo "🔄 Encoding to base64..."
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    base64 -i "$KEYSTORE_FILE" -o keystore-base64.txt
else
    # Linux
    base64 -w 0 "$KEYSTORE_FILE" > keystore-base64.txt
fi

if [ -f "keystore-base64.txt" ]; then
    echo "✅ Encoded successfully!"
    echo ""
    echo "📄 Output file: keystore-base64.txt"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "📋 Next Steps:"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "1. Go to GitHub repository settings:"
    echo "   Settings → Secrets and variables → Actions"
    echo ""
    echo "2. Click 'New repository secret'"
    echo ""
    echo "3. Add secret KEYSTORE_BASE64:"
    echo "   Name:  KEYSTORE_BASE64"
    echo "   Value: (paste contents of keystore-base64.txt)"
    echo ""
    echo "4. Copy the contents:"
    if command -v xclip &> /dev/null; then
        cat keystore-base64.txt | xclip -selection clipboard
        echo "   ✅ Copied to clipboard!"
    elif command -v pbcopy &> /dev/null; then
        cat keystore-base64.txt | pbcopy
        echo "   ✅ Copied to clipboard!"
    else
        echo "   Run: cat keystore-base64.txt"
    fi
    echo ""
    echo "5. Also add these secrets:"
    echo "   KEYSTORE_PASSWORD - Your keystore password"
    echo "   KEY_ALIAS         - Your key alias"
    echo "   KEY_PASSWORD      - Your key password"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "⚠️  IMPORTANT: Delete keystore-base64.txt after copying!"
    echo "   Run: rm keystore-base64.txt"
    echo ""
else
    echo "❌ Error: Failed to create encoded file"
    exit 1
fi
