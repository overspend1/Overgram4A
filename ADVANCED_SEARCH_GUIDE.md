# Advanced Search - User Guide

**Developed by [@overspend1](https://github.com/overspend1)**

## What is Advanced Search?

Advanced Search is the most powerful search feature in any Telegram client. Go beyond simple text search with comprehensive filters, regular expressions, and saved queries. Find exactly what you're looking for across millions of messages.

---

## Features

### 🔍 Search Capabilities
- **Text Search** - Search by keywords or phrases
- **Regular Expressions** - Use regex for complex patterns
- **Case Sensitive** - Toggle case-sensitive matching
- **Media Filters** - Search by media type (photos, videos, files, etc.)
- **Date Range** - Filter by date range
- **Sender Filters** - Find messages from specific users
- **File Filters** - Search by file size and extension
- **Content Filters** - Find links, hashtags, mentions
- **Status Filters** - Edited, forwarded, replied messages
- **Saved Searches** - Save complex queries for reuse

### 🎯 Filter Types

#### 1. Text Filters
- **Query**: Your search term
- **Regex Mode**: Enable regular expressions
- **Case Sensitive**: Match exact case
- **Min/Max Length**: Filter by message length

#### 2. Media Filters
- **Media Type**: Photo, Video, Audio, Voice, File, GIF, Sticker, Location, Contact
- **Has Media**: Only with media / Only without media
- **File Size**: Min and max file size
- **File Extension**: Search by file type (.pdf, .jpg, .zip, etc.)

#### 3. Date Filters
- **Date From**: Start date
- **Date To**: End date
- **Presets**: Today, Yesterday, Last 7 days, Last 30 days, This month

#### 4. Sender Filters
- **From User**: Specific user ID
- **From Chat**: Specific chat ID

#### 5. Message Type Filters
- **Message Type**: Text, Media, Service, Poll, Quiz
- **Only Edited**: Only edited messages
- **Only Deleted**: Only deleted messages (if history saved)
- **Only Forwarded**: Only forwarded messages
- **Only Replies**: Only reply messages
- **Only Pinned**: Only pinned messages

#### 6. Content Filters
- **Has Links**: Messages with URLs
- **Has Hashtags**: Messages with #hashtags
- **Has Mentions**: Messages with @mentions

---

## Basic Search

### Simple Text Search

1. Open **Advanced Search** (Settings → Advanced Search)
2. Enter your search term
3. Tap **Search**

**Example:**
```
Query: "project update"
Results: All messages containing "project update"
```

### Case Sensitive Search

1. Enter your search term
2. Enable **Case Sensitive**
3. Search

**Example:**
```
Query: "API"
Case Sensitive: ON
Results: Only "API", not "api" or "Api"
```

---

## Regular Expression Search

### What are Regular Expressions?

Regular expressions (regex) are powerful patterns for complex searches.

### Basic Regex Patterns

| Pattern | Meaning | Example |
|---------|---------|---------|
| `.` | Any character | `a.c` matches "abc", "a1c" |
| `*` | Zero or more | `ab*c` matches "ac", "abc", "abbc" |
| `+` | One or more | `ab+c` matches "abc", "abbc" |
| `?` | Optional | `colou?r` matches "color", "colour" |
| `^` | Start of line | `^Hello` matches "Hello world" |
| `$` | End of line | `world$` matches "Hello world" |
| `\d` | Any digit | `\d{3}` matches "123" |
| `\w` | Any word char | `\w+` matches "hello" |
| `[abc]` | Any of a, b, c | `[0-9]` matches any digit |
| `(a\|b)` | a or b | `(cat\|dog)` matches "cat" or "dog" |

### Regex Examples

**1. Find phone numbers:**
```regex
\d{3}-\d{3}-\d{4}
Matches: 555-123-4567
```

**2. Find emails:**
```regex
\w+@\w+\.\w+
Matches: john@example.com
```

**3. Find URLs:**
```regex
https?://\S+
Matches: https://example.com
```

**4. Find prices:**
```regex
\$\d+(\.\d{2})?
Matches: $19.99
```

**5. Find dates:**
```regex
\d{4}-\d{2}-\d{2}
Matches: 2025-01-15
```

**6. Find hashtags:**
```regex
#\w+
Matches: #telegram #overgram
```

---

## Advanced Filters

### Search by Media Type

**Find all photos from last week:**
```
Media Type: Photo
Date From: 7 days ago
Date To: Today
```

**Find videos larger than 50MB:**
```
Media Type: Video
Min File Size: 50 MB
```

**Find PDF files:**
```
Media Type: File
File Extension: .pdf
```

### Search by Date Range

**Find messages from January 2024:**
```
Date From: 2024-01-01
Date To: 2024-01-31
```

**Find messages from last 24 hours:**
```
Date From: Yesterday
Date To: Today
```

### Search by Content

**Find all messages with links:**
```
Has Links: Yes
```

**Find all hashtags:**
```
Has Hashtags: Yes
```

**Find all mentions:**
```
Has Mentions: Yes
```

### Search by Status

**Find all edited messages:**
```
Only Edited: Yes
```

**Find all forwarded messages:**
```
Only Forwarded: Yes
```

**Find all replies:**
```
Only Replies: Yes
```

---

## Combining Filters

### Example 1: Find Important Work Files

```
Query: "report"
Media Type: File
File Extension: .pdf
Date From: Last 30 days
From User: Your boss
```

### Example 2: Find Meeting Notes

```
Query: "meeting notes"
Has Links: Yes
Date From: This month
Only Edited: No
```

### Example 3: Find Shared Photos

```
Media Type: Photo
Only Forwarded: Yes
Date From: Last 7 days
```

### Example 4: Find Customer Complaints

```
Query: "(problem|issue|bug)"
Use Regex: Yes
Has Mentions: Yes
Date From: Last 30 days
```

---

## Saved Searches

### Creating Saved Searches

1. Set up your filters
2. Tap menu → **Save Search**
3. Enter a name
4. Tap **Save**

### Using Saved Searches

1. Tap menu → **Saved Searches**
2. Select a saved search
3. Results load automatically

### Managing Saved Searches

- **Edit**: Long press → Edit
- **Delete**: Long press → Delete
- **Duplicate**: Long press → Duplicate
- **Share**: Long press → Share

### Saved Search Examples

**1. "Unread Documents"**
```
Media Type: File
Date From: Last 7 days
```

**2. "Boss Messages"**
```
From User: Boss ID
Date From: Today
```

**3. "Project Updates"**
```
Query: "project"
Has Links: Yes
Date From: Last 30 days
```

**4. "Urgent Messages"**
```
Query: "(urgent|asap|important)"
Use Regex: Yes
Only Pinned: Yes
```

---

## Search Results

### Understanding Results

Each result shows:
- **Message Text** - With highlighted matches
- **Chat Name** - Where the message was sent
- **Date** - When it was sent
- **Relevance Score** - Color-coded indicator

### Relevance Scoring

Results are ranked by:
1. **Match Quality** - Exact vs partial matches
2. **Recency** - Newer messages rank higher
3. **Match Count** - More matches = higher score

### Result Actions

**Tap result** → Jump to message in chat
**Long press** → Show options:
- Forward
- Copy
- Delete
- Share
- Save

---

## Export Results

### Export Options

1. Complete search
2. Tap menu → **Export**
3. Choose format:
   - **CSV** - For spreadsheets
   - **JSON** - For data processing
   - **Text** - Simple text file
   - **HTML** - Web page

### Export Fields

Exported data includes:
- Message ID
- Chat ID
- Sender ID
- Date
- Message text
- Media info (if any)
- Match positions

---

## Performance Tips

### For Faster Searches

1. **Use Date Filters** - Narrow the time range
2. **Specify Media Type** - Reduce search scope
3. **Limit to One Chat** - Search in specific dialog
4. **Use Specific Terms** - Avoid common words
5. **Save Complex Queries** - Reuse instead of rebuilding

### Search Limits

- **Max Results**: 1000 per search
- **Max Characters**: 4000 in query
- **Regex Timeout**: 5 seconds per pattern
- **Cache Duration**: 5 minutes

---

## Use Cases

### 1. **Find Important Files**
```
Media Type: File
File Extension: .pdf
Query: "(contract|agreement|invoice)"
Use Regex: Yes
Date From: Last 90 days
```

### 2. **Track Project Progress**
```
Query: "status update"
Has Links: Yes
From Chat: Project Group
Date Range: This month
```

### 3. **Find Customer Feedback**
```
Query: "(feedback|review|complaint)"
Use Regex: Yes
Has Mentions: Yes
Date From: Last 30 days
```

### 4. **Locate Shared Media**
```
Media Type: Photo
Only Forwarded: Yes
Date From: Last 7 days
File Size: > 1 MB
```

### 5. **Find Technical Discussions**
```
Query: "(bug|error|fix)"
Use Regex: Yes
Has Links: Yes
From Chat: Dev Team
```

---

## Regular Expression Cookbook

### Common Patterns

**Phone Numbers (US):**
```regex
\(?\d{3}\)?[-.\s]?\d{3}[-.\s]?\d{4}
```

**Email Addresses:**
```regex
[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}
```

**URLs:**
```regex
https?://(?:www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b(?:[-a-zA-Z0-9()@:%_\+.~#?&/=]*)
```

**IPv4 Addresses:**
```regex
\b(?:[0-9]{1,3}\.){3}[0-9]{1,3}\b
```

**Dates (YYYY-MM-DD):**
```regex
\d{4}-\d{2}-\d{2}
```

**Times (HH:MM):**
```regex
\d{2}:\d{2}
```

**Currency (USD):**
```regex
\$\d+(?:,\d{3})*(?:\.\d{2})?
```

**Credit Card Numbers:**
```regex
\d{4}[-\s]?\d{4}[-\s]?\d{4}[-\s]?\d{4}
```

**Hashtags:**
```regex
#\w+
```

**Mentions:**
```regex
@\w+
```

---

## Keyboard Shortcuts

| Action | Shortcut |
|--------|----------|
| Open Advanced Search | *Settings → Advanced Search* |
| Toggle Regex | Checkbox in search form |
| Clear Filters | Clear button |
| Save Search | Menu → Save |
| Export Results | Menu → Export |

---

## FAQ

**Q: What's the difference from regular search?**
A: Advanced Search offers comprehensive filters (date, media, sender, etc.), regex support, and saved queries.

**Q: Can I search across all chats?**
A: Yes! Leave the chat filter empty to search globally.

**Q: How fast is Advanced Search?**
A: Optimized for speed - typically processes 1000+ messages per second.

**Q: Do saved searches sync?**
A: Yes, with OvergramSync enabled, saved searches sync across devices.

**Q: Can I use regex in saved searches?**
A: Yes! Regex patterns are preserved in saved searches.

**Q: What's the maximum search depth?**
A: All messages in your local database (unlimited).

**Q: Can I search deleted messages?**
A: Yes, if you have message history enabled in Overgram settings.

**Q: How do I search for special characters?**
A: Use regex mode and escape special chars: `\$`, `\?`, `\*`, etc.

---

## Troubleshooting

**No results found:**
- Check your filters aren't too restrictive
- Verify date range includes the period you want
- Try broader search terms
- Disable regex if not needed

**Regex not working:**
- Validate your pattern at regex101.com
- Escape special characters
- Check for syntax errors
- Test with simpler patterns first

**Search is slow:**
- Add date filters to narrow scope
- Search in specific chat instead of globally
- Simplify complex regex patterns
- Clear app cache

**Results incomplete:**
- Increase result limit in settings
- Check if messages are in local database
- Sync with Telegram servers

---

## Technical Details

**Search Algorithm:**
- **Text Search**: Boyer-Moore string matching
- **Regex**: Java Pattern compiled regex
- **Scoring**: TF-IDF + recency weighting
- **Performance**: Asynchronous with progress callbacks

**Database Queries:**
- Indexed searches for speed
- Batch processing (100 messages per batch)
- Optimized SQL queries
- Result caching (5-minute TTL)

**Supported Regex:**
- Java regex syntax
- Full Unicode support
- Multiline mode available
- Case-insensitive flag support

---

## Future Features (Coming Soon)

- 🤖 AI-powered semantic search
- 📊 Search analytics dashboard
- 🔄 Auto-save recent searches
- 🎨 Custom result highlighting
- 📱 Search widgets
- 🌐 Cloud search across devices
- 💾 Offline search cache
- 🔔 Search alerts/notifications

---

**Developed by [@overspend1](https://github.com/overspend1)**

Need help? Join our [Telegram chat](https://t.me/overgramchat)!
