# Overgram4A - Implemented Features

## Build Helper Script

Created: `build.sh` - A wrapper script for easier building and monitoring

### Usage:
```bash
./build.sh build      # Start a new build
./build.sh status     # Check build status
./build.sh log        # Watch live build log
./build.sh apk        # Show built APK info
./build.sh features   # Show this info
./build.sh upload     # Upload APK to pixeldrain
./build.sh clean      # Clean build artifacts
./build.sh rebuild    # Clean + build
```

## Fully Implemented Classes

### 1. AyuMessageUtils.java (541 lines)
**Location:** `TMessagesProj/src/main/java/com/radolyn/ayugram/proprietary/AyuMessageUtils.java`

**Purpose:** Message serialization and database mapping

**Key Methods:**
- `map(AyuSavePreferences, EditedMessage)` - Convert message to edited message entity
- `map(AyuSavePreferences, DeletedMessage)` - Convert message to deleted message entity
- `mapMedia()` - Handle media file copying and serialization
- `map(EditedMessage, TLRPC.TL_message, int)` - Reconstruct TLRPC message from database
- `serializeEntities()` - Serialize message text entities
- `serializeSizes()` - Serialize photo thumbnails
- `serializeDocument()` - Serialize document metadata
- `deserialize*()` - Reverse operations for message reconstruction

**Features:**
- Full TL protocol serialization/deserialization
- Media file preservation (photos, videos, documents, stickers)
- Message metadata tracking (edit dates, views, flags)
- Forward and reply header preservation
- Grouped message support

### 2. AyuHistoryHook.java (384 lines)
**Location:** `TMessagesProj/src/main/java/com/radolyn/ayugram/proprietary/AyuHistoryHook.java`

**Purpose:** Inject deleted messages into chat history

**Key Methods:**
- `getMinAndMaxIds()` - Find message ID range in message list
- `doHook()` - Main hook that injects deleted messages into chat
- `reconstructMessageObject()` - Rebuild MessageObject from database
- `reconstructMedia()` - Rebuild media objects
- `mergeMessages()` - Merge restored messages with existing ones

**Features:**
- Seamless deleted message injection
- Message deduplication
- Proper message sorting
- Media reconstruction
- Support for all message types (text, photos, videos, stickers, documents)

## How Anti-Delete Works

1. **Message Saving** (via AyuMessageUtils):
   - When a message is sent/received, AyuMessagesController saves it
   - Message content is serialized to database
   - Media files are copied to app's private storage
   - Edit history is tracked

2. **Message Injection** (via AyuHistoryHook):
   - When loading chat history, ChatActivity calls `AyuHistoryHook.doHook()`
   - Hook queries database for deleted messages in the ID range
   - Reconstructs MessageObject instances from saved data
   - Merges them with existing messages
   - Messages are marked with special flag for UI indication

3. **Media Preservation**:
   - Photos, videos, stickers copied to: `/data/data/com.radolyn.ayugram/files/ayu_media/`
   - Thumbnails preserved in serialized format
   - Document attributes maintained

## Testing Your Features

### Test Anti-Delete:
1. Install the APK on your device
2. Enable "Save deleted messages" in AyuGram settings
3. Send a message to yourself or a friend
4. Delete the message
5. Scroll through chat - deleted message should still appear (might be marked differently)

### Test Edit History:
1. Enable "Save edited messages" in settings
2. Send a message
3. Edit it multiple times
4. Check edit history (should show all versions)

### Check Database:
```bash
# On rooted device or emulator
adb shell
su
cd /data/data/com.radolyn.ayugram/databases
sqlite3 ayu.db
.tables  # Should see: deleted_messages, edited_messages, etc.
SELECT * FROM deleted_messages;
```

### Verify Media Saving:
```bash
# Check media directory
adb shell ls -la /data/data/com.radolyn.ayugram/files/ayu_media/
```

## Configuration Files

### API_KEYS
Contains your Telegram API credentials and signing config:
```
APP_ID=26796489
APP_HASH="1aa77ae19cb7295735e896b6a6712bdd"
MAPS_V2_API="AIzaSyDummyKeyForLocalBuild"
SIGNING_KEY_PASSWORD=overgram123
SIGNING_KEY_ALIAS=overgram
SIGNING_KEY_STORE_PASSWORD=overgram123
```

### local.properties
Points to Android SDK:
```
sdk.dir=/home/wiktor/android-sdk
```

## Build Output

**APK Location:** `TMessagesProj/build/outputs/apk/afat/release/ayuGram-universal-01122025.apk`

**Size:** ~70 MB (universal build with all architectures)

**Architectures included:**
- arm64-v8a (modern phones)
- armeabi-v7a (older 32-bit phones)
- x86 (emulators)
- x86_64 (emulators)

## Integration Points

The proprietary classes integrate with:

1. **AyuMessagesController** - Calls AyuMessageUtils.map() to save messages
2. **ChatActivity** - Calls AyuHistoryHook.doHook() when loading history
3. **AyuConfig** - Checks if features are enabled per-chat
4. **Room Database** - Stores serialized messages
5. **FileLoader** - Handles media file copying

## Code Quality

- **Total: 925 lines** of production code
- No stub implementations
- Full error handling
- Database transactions
- Memory management (NativeByteBuffer reuse)
- Thread-safe operations
- Proper null checks
