# Message Templates - User Guide

**Developed by [@overspend1](https://github.com/overspend1)**

## What are Message Templates?

Message Templates let you save frequently used messages and quickly insert them into any chat with support for dynamic variables. Perfect for businesses, customer support, or anyone who sends similar messages regularly.

---

## Features

### ✅ Core Features
- **Unlimited Templates** - Save as many templates as you need
- **Variable Support** - Use placeholders that auto-fill with dynamic data
- **Categories** - Organize templates into categories (Work, Personal, Business, etc.)
- **Favorites** - Mark frequently used templates for quick access
- **Quick Access** - Insert templates directly from the message input
- **Search** - Find templates quickly by title or content
- **Share Templates** - Share templates with friends or team members
- **Import/Export** - Backup and restore your templates

### 🎯 Built-in Variables

Templates support these automatic variables:

| Variable | Description | Example |
|----------|-------------|---------|
| `{name}` | Contact's full name | "John Doe" |
| `{firstname}` | Contact's first name | "John" |
| `{lastname}` | Contact's last name | "Doe" |
| `{date}` | Current date | "2025-01-15" |
| `{time}` | Current time | "14:30" |
| `{day}` | Day of week | "Monday" |
| `{datetime}` | Date and time | "2025-01-15 14:30" |

Plus unlimited custom variables for your specific needs!

---

## Creating Templates

### Method 1: From Template Manager

1. Go to **Settings → Message Templates**
2. Tap the **+** button
3. Fill in:
   - **Title**: Short name for the template
   - **Content**: Your message with variables
   - **Category**: Choose or create a category
   - **Favorite**: Mark as favorite (optional)
4. Use the variable buttons to insert placeholders
5. Tap **Save**

### Method 2: Quick Create

1. Long press any message in a chat
2. Select **"Save as Template"**
3. Edit and save

---

## Using Templates

### Quick Insert from Chat

1. In any chat, tap the **📝 Template** button (next to message input)
2. Select your template from the quick selector
3. If it has variables, fill them in
4. The message is inserted into your input field

### From Template Manager

1. Open **Template Manager**
2. Browse or search for your template
3. Tap the template
4. Fill in any required variables
5. Template is copied to clipboard or sent directly

---

## Template Examples

### Example 1: Simple Greeting
```
Title: Morning Greeting
Content: Good morning {name}! Hope you have a great day! ☀️
```

### Example 2: Meeting Request
```
Title: Meeting Request
Content: Hi {name},

Can we schedule a meeting on {date} at {time}?

Looking forward to hearing from you!
```

### Example 3: Order Confirmation
```
Title: Order Confirmation
Content: Hi {name},

Your order #{orderId} has been confirmed!

Order Date: {date}
Total: ${total}

Expected delivery: {deliveryDate}

Thank you for your business!
```

### Example 4: Custom Variables
```
Title: Project Update
Content: Hi {name},

Quick update on {project}:

Status: {status}
Deadline: {deadline}
Progress: {progress}%

Let me know if you have any questions!
```

---

## Managing Templates

### Editing Templates
1. Long press a template
2. Select **Edit**
3. Make your changes
4. Save

### Organizing with Categories

**Default Categories:**
- 📁 General
- 💼 Work
- 💬 Personal
- 📊 Business
- 👋 Greetings
- 💭 Responses

**Create Custom Categories:**
1. When creating/editing a template
2. Type a new category name in the dropdown
3. It's automatically created

### Marking Favorites
- Tap the ⭐ icon on any template
- Access favorites quickly from the "Favorites" tab
- Favorites appear first in quick selector

---

## Advanced Features

### Template Variables

**Auto-filled variables** (no input needed):
- `{date}`, `{time}`, `{day}`, `{datetime}`

**Contact-based variables** (auto-filled from chat):
- `{name}`, `{firstname}`, `{lastname}`

**Custom variables** (you'll be prompted):
- Any other variable like `{company}`, `{project}`, etc.

### Search and Filters

**Search templates:**
- By title
- By content
- By category
- By favorite status

**Sort options:**
- Most used
- Recently used
- Alphabetically
- By category

### Statistics

Each template tracks:
- **Use count** - How many times you've used it
- **Created date** - When it was created
- **Last updated** - When it was last modified

---

## Import/Export

### Export Templates
1. Open Template Manager
2. Tap menu → **Export**
3. Choose export format:
   - JSON (for backup)
   - Share with others
4. Save or share

### Import Templates
1. Open Template Manager
2. Tap menu → **Import**
3. Select the JSON file
4. Templates are merged (duplicates are skipped)

---

## Tips and Tricks

### 1. Use Categories Effectively
Organize templates by purpose:
- **Work**: Professional messages
- **Personal**: Friends and family
- **Support**: Customer service responses
- **Sales**: Sales pitches and follow-ups

### 2. Combine Multiple Variables
```
Hi {name},

Meeting confirmed for {date} at {time} in {location}.

Topic: {topic}

See you there!
```

### 3. Create Template Chains
Save related templates in the same category for workflows:
1. Initial contact
2. Follow-up
3. Closing

### 4. Use Favorites Wisely
Only mark your top 5-10 most used templates as favorites for quick access.

### 5. Regular Cleanup
Review and delete unused templates monthly to keep your library organized.

---

## Keyboard Shortcuts

| Action | Shortcut |
|--------|----------|
| Open Template Manager | *Settings → Templates* |
| Quick Insert | Tap 📝 in message input |
| Create New | + button in Template Manager |
| Search Templates | 🔍 in Template Manager |
| Toggle Favorite | ⭐ on template |

---

## Use Cases

### 1. **Customer Support**
Save common responses to FAQs:
```
Hi {name},

Thank you for contacting us about {issue}.

[Your solution here]

Reference: #{ticketId}
Date: {date}

Best regards,
{agentName}
```

### 2. **Sales Teams**
Quick follow-ups:
```
Hi {name},

Following up on our conversation about {product}.

Special offer: {discount}% off until {expiryDate}

Reply to claim your discount!
```

### 3. **Project Management**
Status updates:
```
Project: {project}
Status: {status}
Progress: {progress}%
Next Milestone: {milestone}
Due: {dueDate}
```

### 4. **Personal Use**
Birthday wishes:
```
Happy Birthday {name}! 🎉

Wishing you an amazing year ahead!

Love,
{yourname}
```

---

## FAQ

**Q: How many templates can I create?**
A: Unlimited! There's no limit on the number of templates.

**Q: Can I share templates with others?**
A: Yes! Use the Export/Share feature to send templates to friends or team members.

**Q: Do templates sync across devices?**
A: Yes, if you use OvergramSync, your templates sync automatically.

**Q: Can I use templates in groups?**
A: Yes! Templates work in all chats - private, groups, and channels.

**Q: What happens if I don't fill a custom variable?**
A: You'll be prompted to fill it before the template is inserted.

**Q: Can I edit a template after creating it?**
A: Yes! Long press → Edit anytime.

**Q: How do I delete a template?**
A: Long press → Delete

**Q: Can templates include emojis?**
A: Yes! Use any emojis, formatting, or special characters.

---

## Troubleshooting

**Templates not showing up:**
- Refresh the template list
- Check if you're in the right category
- Search by name

**Variables not working:**
- Make sure you use correct syntax: `{variable}`
- Check for typos in variable names
- Some variables auto-fill based on context

**Can't save template:**
- Title and content are required
- Maximum content length: 4000 characters
- Check for special characters in title

---

## Technical Details

**Storage:**
- Templates are stored in a local SQLite database
- Database location: `overgram_templates.db`
- Automatic backups with OvergramSync

**Performance:**
- Instant search across thousands of templates
- Indexed by category, favorites, and use count
- Optimized for fast lookup

**Privacy:**
- All templates are stored locally
- Only synced if you enable OvergramSync
- No data sent to external servers

---

## Future Features (Coming Soon)

- 🔄 Template versioning
- 📊 Usage analytics
- 🤖 AI-suggested templates
- 🌐 Cloud template library
- 👥 Team template sharing
- 📱 Template widgets
- 🎨 Rich formatting support

---

**Developed by [@overspend1](https://github.com/overspend1)**

Need help? Join our [Telegram chat](https://t.me/overgramchat)!
