#!/bin/bash

# Overgram4A Build Helper Script
# Usage: ./build.sh [command]

PROJECT_DIR="/home/wiktor/Overgram4A"
LOG_FILE="/tmp/overgram_build.log"
APK_DIR="$PROJECT_DIR/TMessagesProj/build/outputs/apk"

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

show_help() {
    echo -e "${BLUE}Overgram4A Build Helper${NC}"
    echo ""
    echo "Usage: ./build.sh [command]"
    echo ""
    echo "Commands:"
    echo "  build           - Start a new build (afat release)"
    echo "  status          - Show current build status"
    echo "  clean           - Clean build artifacts"
    echo "  rebuild         - Clean and build"
    echo "  log             - Show build log (tail -f)"
    echo "  apk             - Show built APK location and info"
    echo "  features        - Show where implemented features are located"
    echo "  upload          - Upload latest APK to pixeldrain"
    echo "  help            - Show this help message"
    echo ""
}

build_project() {
    echo -e "${YELLOW}Starting build...${NC}"
    cd "$PROJECT_DIR"
    ./gradlew assembleAfatRelease 2>&1 | tee "$LOG_FILE"

    if [ ${PIPESTATUS[0]} -eq 0 ]; then
        echo -e "${GREEN}Build successful!${NC}"
        show_apk_info
    else
        echo -e "${RED}Build failed! Check log: $LOG_FILE${NC}"
        exit 1
    fi
}

show_status() {
    echo -e "${BLUE}Build Status:${NC}"
    echo ""

    # Check if gradle is running
    if pgrep -f "gradle" > /dev/null; then
        echo -e "${YELLOW}Status: Build is currently running${NC}"
        echo ""
        echo -e "Recent log output:"
        tail -20 "$LOG_FILE" 2>/dev/null || echo "No log file yet"
    else
        echo -e "${GREEN}Status: No build currently running${NC}"

        # Check if APK exists
        if [ -d "$APK_DIR" ]; then
            echo ""
            echo "Latest builds:"
            find "$APK_DIR" -name "*.apk" -type f -exec ls -lh {} \; 2>/dev/null | tail -5
        fi
    fi
}

clean_build() {
    echo -e "${YELLOW}Cleaning build artifacts...${NC}"
    cd "$PROJECT_DIR"
    ./gradlew clean
    echo -e "${GREEN}Clean complete!${NC}"
}

show_log() {
    if [ -f "$LOG_FILE" ]; then
        echo -e "${BLUE}Showing live build log (Ctrl+C to exit)${NC}"
        tail -f "$LOG_FILE"
    else
        echo -e "${RED}No log file found at $LOG_FILE${NC}"
    fi
}

show_apk_info() {
    echo -e "${BLUE}Built APK Information:${NC}"
    echo ""

    # Find the latest APK
    LATEST_APK=$(find "$APK_DIR" -name "*.apk" -type f -printf '%T@ %p\n' 2>/dev/null | sort -n | tail -1 | cut -f2- -d" ")

    if [ -n "$LATEST_APK" ]; then
        echo -e "${GREEN}Latest APK:${NC} $LATEST_APK"
        ls -lh "$LATEST_APK"
        echo ""
        echo -e "${GREEN}SHA256:${NC}"
        sha256sum "$LATEST_APK"
        echo ""
        echo -e "To install: adb install \"$LATEST_APK\""
    else
        echo -e "${RED}No APK files found${NC}"
    fi
}

show_features() {
    echo -e "${BLUE}Implemented Features Location:${NC}"
    echo ""

    echo -e "${GREEN}Proprietary Classes (Fully Implemented):${NC}"
    echo "  1. AyuMessageUtils.java"
    echo "     Location: TMessagesProj/src/main/java/com/radolyn/ayugram/proprietary/AyuMessageUtils.java"
    echo "     Features: Message serialization, media handling, database mapping"
    echo ""
    echo "  2. AyuHistoryHook.java"
    echo "     Location: TMessagesProj/src/main/java/com/radolyn/ayugram/proprietary/AyuHistoryHook.java"
    echo "     Features: Deleted message injection, message reconstruction, history merging"
    echo ""

    echo -e "${GREEN}Configuration Files:${NC}"
    echo "  - API_KEYS (Telegram API credentials and signing config)"
    echo "  - local.properties (Android SDK location)"
    echo ""

    echo -e "${GREEN}Key Features:${NC}"
    echo "  ✓ Anti-delete messages (saves deleted messages to database)"
    echo "  ✓ Message history injection (shows deleted messages in chat)"
    echo "  ✓ Media file preservation (copies photos, videos, stickers)"
    echo "  ✓ Edit history tracking (saves message edits)"
    echo "  ✓ Full TLRPC serialization/deserialization"
    echo ""

    echo -e "${YELLOW}To verify implementation:${NC}"
    echo "  cat TMessagesProj/src/main/java/com/radolyn/ayugram/proprietary/AyuMessageUtils.java | wc -l"
    echo "  cat TMessagesProj/src/main/java/com/radolyn/ayugram/proprietary/AyuHistoryHook.java | wc -l"
}

upload_apk() {
    LATEST_APK=$(find "$APK_DIR" -name "*.apk" -type f -printf '%T@ %p\n' 2>/dev/null | sort -n | tail -1 | cut -f2- -d" ")

    if [ -z "$LATEST_APK" ]; then
        echo -e "${RED}No APK found to upload${NC}"
        exit 1
    fi

    echo -e "${YELLOW}Uploading to pixeldrain...${NC}"
    echo "File: $LATEST_APK"

    RESPONSE=$(curl -T "$LATEST_APK" -u :dce76fe0-c905-4fba-8813-fb90cb4c1960 https://pixeldrain.com/api/file/ 2>/dev/null)
    FILE_ID=$(echo "$RESPONSE" | grep -o '"id":"[^"]*"' | cut -d'"' -f4)

    if [ -n "$FILE_ID" ]; then
        echo -e "${GREEN}Upload successful!${NC}"
        echo -e "${GREEN}Download link:${NC} https://pixeldrain.com/u/$FILE_ID"
    else
        echo -e "${RED}Upload failed${NC}"
        echo "$RESPONSE"
    fi
}

# Main command handler
case "$1" in
    build)
        build_project
        ;;
    status)
        show_status
        ;;
    clean)
        clean_build
        ;;
    rebuild)
        clean_build
        build_project
        ;;
    log)
        show_log
        ;;
    apk)
        show_apk_info
        ;;
    features)
        show_features
        ;;
    upload)
        upload_apk
        ;;
    help|"")
        show_help
        ;;
    *)
        echo -e "${RED}Unknown command: $1${NC}"
        echo ""
        show_help
        exit 1
        ;;
esac
