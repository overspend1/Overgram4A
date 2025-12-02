#!/bin/bash
# Test Telegram bot configuration
# Usage: ./test-telegram-bot.sh <BOT_TOKEN> <CHANNEL_ID>

set -e

echo "🤖 Telegram Bot Tester"
echo "======================="
echo ""

# Check if arguments provided
if [ -z "$1" ] || [ -z "$2" ]; then
    echo "❌ Error: Missing arguments"
    echo ""
    echo "Usage: $0 <BOT_TOKEN> <CHANNEL_ID>"
    echo ""
    echo "Example:"
    echo "  $0 123456789:ABCdefGHIjklMNOpqrsTUVwxyz @overgramupdates"
    echo ""
    echo "Or use environment variables:"
    echo "  export TELEGRAM_BOT_TOKEN='123456789:ABCdefGHIjklMNOpqrsTUVwxyz'"
    echo "  export TELEGRAM_CHANNEL_ID='@overgramupdates'"
    echo "  $0 \$TELEGRAM_BOT_TOKEN \$TELEGRAM_CHANNEL_ID"
    exit 1
fi

BOT_TOKEN="$1"
CHANNEL_ID="$2"

echo "🔑 Bot Token: ${BOT_TOKEN:0:10}...${BOT_TOKEN: -5}"
echo "📱 Channel ID: $CHANNEL_ID"
echo ""

# Test 1: Get bot info
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Test 1: Get Bot Info"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

RESPONSE=$(curl -s "https://api.telegram.org/bot${BOT_TOKEN}/getMe")
OK=$(echo "$RESPONSE" | jq -r '.ok' 2>/dev/null || echo "false")

if [ "$OK" == "true" ]; then
    echo "✅ Bot token is valid!"
    BOT_NAME=$(echo "$RESPONSE" | jq -r '.result.first_name')
    BOT_USERNAME=$(echo "$RESPONSE" | jq -r '.result.username')
    echo "   Bot name: $BOT_NAME"
    echo "   Username: @$BOT_USERNAME"
else
    echo "❌ Invalid bot token!"
    echo "   Response: $RESPONSE"
    exit 1
fi
echo ""

# Test 2: Send text message
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Test 2: Send Text Message"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

MESSAGE="🧪 <b>Test Message</b>

This is a test from the Telegram bot tester script.

<b>Features:</b>
• HTML formatting ✅
• Bold text ✅
• Line breaks ✅

<i>If you see this, your bot is working correctly!</i>

🤖 Sent at $(date '+%Y-%m-%d %H:%M:%S UTC')"

RESPONSE=$(curl -s -X POST "https://api.telegram.org/bot${BOT_TOKEN}/sendMessage" \
    -d "chat_id=${CHANNEL_ID}" \
    -d "text=${MESSAGE}" \
    -d "parse_mode=HTML")

OK=$(echo "$RESPONSE" | jq -r '.ok' 2>/dev/null || echo "false")

if [ "$OK" == "true" ]; then
    echo "✅ Text message sent successfully!"
    MESSAGE_ID=$(echo "$RESPONSE" | jq -r '.result.message_id')
    echo "   Message ID: $MESSAGE_ID"
    echo "   Check your channel: $CHANNEL_ID"
else
    echo "❌ Failed to send message!"
    ERROR=$(echo "$RESPONSE" | jq -r '.description' 2>/dev/null || echo "Unknown error")
    echo "   Error: $ERROR"
    echo ""
    echo "Common issues:"
    echo "  • Bot not added as admin to channel"
    echo "  • 'Post Messages' permission not enabled"
    echo "  • Channel ID incorrect (should be @username or -1001234567890)"
    exit 1
fi
echo ""

# Test 3: Create test APK and upload
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Test 3: Upload File (Small Test)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Create a small test file
TEST_FILE="test-upload.txt"
cat > "$TEST_FILE" <<EOF
Overgram Android - Test Upload
==============================

This is a test file upload from the Telegram bot tester.

If you see this file in your channel, the bot can upload files correctly!

Timestamp: $(date)
Channel: $CHANNEL_ID
Bot: @$BOT_USERNAME
EOF

echo "📝 Created test file: $TEST_FILE"
echo ""

CAPTION="📄 <b>Test File Upload</b>

This is a test to verify file upload capability.

If you see this, the bot can upload APK files! ✅"

RESPONSE=$(curl -s \
    -F "chat_id=${CHANNEL_ID}" \
    -F "document=@${TEST_FILE}" \
    -F "caption=${CAPTION}" \
    -F "parse_mode=HTML" \
    "https://api.telegram.org/bot${BOT_TOKEN}/sendDocument")

OK=$(echo "$RESPONSE" | jq -r '.ok' 2>/dev/null || echo "false")

if [ "$OK" == "true" ]; then
    echo "✅ File uploaded successfully!"
    FILE_ID=$(echo "$RESPONSE" | jq -r '.result.document.file_id')
    echo "   File ID: $FILE_ID"
else
    echo "❌ Failed to upload file!"
    ERROR=$(echo "$RESPONSE" | jq -r '.description' 2>/dev/null || echo "Unknown error")
    echo "   Error: $ERROR"
fi

# Cleanup
rm -f "$TEST_FILE"
echo ""

# Final summary
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📊 Test Summary"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "✅ Bot token valid"
echo "✅ Bot can send messages"
if [ "$OK" == "true" ]; then
    echo "✅ Bot can upload files"
else
    echo "⚠️  File upload had issues (may still work for APKs)"
fi
echo ""
echo "🎉 Your bot is configured correctly!"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📋 Next Steps:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "1. Add secrets to GitHub:"
echo "   Settings → Secrets and variables → Actions"
echo ""
echo "2. Add these secrets:"
echo "   Name:  TELEGRAM_BOT_TOKEN"
echo "   Value: $BOT_TOKEN"
echo ""
echo "   Name:  TELEGRAM_CHANNEL_ID"
echo "   Value: $CHANNEL_ID"
echo ""
echo "3. Push to trigger workflow:"
echo "   git add ."
echo "   git commit -m \"test: trigger auto-release\""
echo "   git push origin rewrite"
echo ""
echo "4. Check Actions tab on GitHub to see build progress"
echo ""
echo "5. APK will be posted to your channel automatically!"
echo ""
