/*
 * This is the source code of Overgram for Android.
 *
 * We do not and cannot prevent the use of our code,
 * but be respectful and credit the original author.
 *
 * Copyright @overspend1, 2024
 * Based on original AyuGram code by @Radolyn, 2023
 */

package com.overspend1.overgram.proprietary;

import android.util.Pair;
import com.google.android.exoplayer2.util.Log;
import com.overspend1.overgram.OverConfig;
import com.overspend1.overgram.OverConstants;
import com.overspend1.overgram.database.entities.DeletedMessageFull;
import com.overspend1.overgram.messages.OverMessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Hook for loading and injecting deleted messages into chat history
 */
public class OverHistoryHook {

    /**
     * Get minimum and maximum message IDs from a list of messages
     */
    public static Pair<Integer, Integer> getMinAndMaxIds(ArrayList<MessageObject> messages) {
        if (messages == null || messages.isEmpty()) {
            return new Pair<>(0, 0);
        }

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for (MessageObject msg : messages) {
            if (msg == null || msg.messageOwner == null) {
                continue;
            }

            int id = msg.messageOwner.id;
            if (id < min) {
                min = id;
            }
            if (id > max) {
                max = id;
            }
        }

        return new Pair<>(min == Integer.MAX_VALUE ? 0 : min, max == Integer.MIN_VALUE ? 0 : max);
    }

    /**
     * Main hook: Load deleted messages from database and inject them into the messages list
     */
    public static void doHook(
            int currentAccount,
            ArrayList<MessageObject> messages,
            HashMap<Integer, MessageObject> messagesDict,
            int startId,
            int endId,
            long dialogId,
            int limit,
            long topicId,
            boolean isSecretChat
    ) {
        if (!OverConfig.saveDeletedMessages) {
            return;
        }

        if (isSecretChat) {
            // Secret chats are not supported for message history
            return;
        }

        try {
            doHookInner(currentAccount, messages, messagesDict, startId, endId, dialogId, limit, topicId);
        } catch (Exception e) {
            Log.e("Overgram", "Error in doHook", e);
            FileLog.e("doHook", e);
        }
    }

    private static void doHookInner(
            int currentAccount,
            ArrayList<MessageObject> messages,
            HashMap<Integer, MessageObject> messagesDict,
            int startId,
            int endId,
            long dialogId,
            int limit,
            long topicId
    ) {
        var messagesController = OverMessagesController.getInstance();
        var userId = UserConfig.getInstance(currentAccount).getCurrentUser().id;

        // Fetch deleted messages from database
        var deletedMessages = messagesController.getMessages(userId, dialogId, topicId, startId, endId, limit);

        if (deletedMessages == null || deletedMessages.isEmpty()) {
            Log.d("Overgram", "No deleted messages found for dialog " + dialogId + " between " + startId + " and " + endId);
            return;
        }

        Log.d("Overgram", "Found " + deletedMessages.size() + " deleted messages for dialog " + dialogId);

        int injectedCount = 0;

        for (DeletedMessageFull deletedFull : deletedMessages) {
            var deleted = deletedFull.message;

            // Skip if message already exists in the list
            if (messagesDict.containsKey(deleted.messageId)) {
                continue;
            }

            // Create TLRPC message from deleted message
            var msg = createMessageFromDeleted(deletedFull, currentAccount);
            if (msg == null) {
                continue;
            }

            // Create MessageObject
            var messageObject = new MessageObject(currentAccount, msg, true, true);
            messageObject.deleted = true; // Mark as deleted for UI

            // Add to lists
            messages.add(messageObject);
            messagesDict.put(deleted.messageId, messageObject);

            injectedCount++;
        }

        if (injectedCount > 0) {
            Log.d("Overgram", "Injected " + injectedCount + " deleted messages into chat");

            // Sort messages by ID (newest first)
            Collections.sort(messages, (a, b) -> {
                if (a == null || a.messageOwner == null) return 1;
                if (b == null || b.messageOwner == null) return -1;
                return Integer.compare(b.messageOwner.id, a.messageOwner.id);
            });
        }
    }

    /**
     * Create a TLRPC.Message from a deleted message entity
     */
    private static TLRPC.Message createMessageFromDeleted(DeletedMessageFull deletedFull, int accountId) {
        try {
            var deleted = deletedFull.message;
            var msg = new TLRPC.TL_message();

            // Basic message info
            msg.id = deleted.messageId;
            msg.dialog_id = deleted.dialogId;
            msg.date = deleted.date;
            msg.flags = deleted.flags;
            msg.edit_date = deleted.editDate;
            msg.views = deleted.views;

            if (deleted.groupedId != 0) {
                msg.grouped_id = deleted.groupedId;
            }

            // Peer info
            if (deleted.peerId != 0) {
                msg.peer_id = MessagesController.getInstance(accountId).getPeer(deleted.peerId);
            }
            if (deleted.fromId != 0) {
                msg.from_id = MessagesController.getInstance(accountId).getPeer(deleted.fromId);
            }

            // Forward info
            if (deleted.fwdFlags != 0) {
                var fwd = new TLRPC.TL_messageFwdHeader();
                fwd.flags = deleted.fwdFlags;
                if (deleted.fwdFromId != 0) {
                    fwd.from_id = MessagesController.getInstance(accountId).getPeer(deleted.fwdFromId);
                }
                fwd.from_name = deleted.fwdName;
                fwd.date = deleted.fwdDate;
                fwd.post_author = deleted.fwdPostAuthor;
                msg.fwd_from = fwd;
            }

            // Reply info
            if (deleted.replyFlags != 0) {
                var reply = new TLRPC.TL_messageReplyHeader();
                reply.flags = deleted.replyFlags;
                reply.reply_to_msg_id = deleted.replyMessageId;
                if (deleted.replyPeerId != 0) {
                    reply.reply_to_peer_id = MessagesController.getInstance(accountId).getPeer(deleted.replyPeerId);
                }
                reply.reply_to_top_id = deleted.replyTopId;
                reply.forum_topic = deleted.replyForumTopic;
                msg.reply_to = reply;
            }

            // Message text
            msg.message = deleted.text != null ? deleted.text : "";

            // Entities (deserialize)
            if (deleted.textEntities != null) {
                msg.entities = OverMessageUtils.deserializeTL(deleted.textEntities);
            }

            // Media
            if (deleted.mediaPath != null && !deleted.mediaPath.isEmpty()) {
                restoreMedia(deleted, msg);
            }

            // Mark as deleted
            msg.flags |= 0x20000000; // Custom flag for deleted messages

            // Add reactions if available
            if (deletedFull.reactions != null && !deletedFull.reactions.isEmpty()) {
                var reactions = new TLRPC.TL_messageReactions();
                reactions.results = new ArrayList<>();

                for (var reaction : deletedFull.reactions) {
                    var result = new TLRPC.TL_reactionCount();
                    result.count = reaction.count;
                    result.chosen = reaction.selfSelected;

                    if (reaction.isCustom) {
                        var custom = new TLRPC.TL_reactionCustomEmoji();
                        custom.document_id = reaction.documentId;
                        result.reaction = custom;
                    } else if (reaction.emoticon != null) {
                        var emoji = new TLRPC.TL_reactionEmoji();
                        emoji.emoticon = reaction.emoticon;
                        result.reaction = emoji;
                    }

                    reactions.results.add(result);
                }

                msg.reactions = reactions;
            }

            return msg;
        } catch (Exception e) {
            Log.e("Overgram", "Failed to create message from deleted", e);
            FileLog.e("createMessageFromDeleted", e);
            return null;
        }
    }

    /**
     * Restore media for a deleted message
     */
    private static void restoreMedia(com.overspend1.overgram.database.entities.DeletedMessage deleted, TLRPC.Message msg) {
        try {
            int documentType = deleted.documentType;

            if (documentType == OverConstants.DOCUMENT_TYPE_PHOTO) {
                restorePhoto(deleted, msg);
            } else if (documentType >= OverConstants.DOCUMENT_TYPE_FILE) {
                restoreDocument(deleted, msg);
            }
        } catch (Exception e) {
            Log.e("Overgram", "Failed to restore media for deleted message", e);
            FileLog.e("restoreMedia", e);
        }
    }

    private static void restorePhoto(com.overspend1.overgram.database.entities.DeletedMessage deleted, TLRPC.Message msg) {
        var media = new TLRPC.TL_messageMediaPhoto();
        var photo = new TLRPC.TL_photo();

        photo.id = 0; // Fake ID for deleted message
        photo.date = deleted.date;
        photo.file_reference = new byte[0];

        var photoSize = new TLRPC.TL_photoSize();
        photoSize.type = "x";
        photoSize.location = new TLRPC.TL_fileLocationToBeDeprecated();

        photo.sizes = new ArrayList<>();
        photo.sizes.add(photoSize);

        media.photo = photo;
        msg.media = media;
        msg.flags |= 512; // TLRPC.MESSAGE_FLAG_HAS_MEDIA
    }

    private static void restoreDocument(com.overspend1.overgram.database.entities.DeletedMessage deleted, TLRPC.Message msg) {
        var media = new TLRPC.TL_messageMediaDocument();
        var doc = new TLRPC.TL_document();

        doc.id = 0; // Fake ID for deleted message
        doc.date = deleted.date;
        doc.mime_type = deleted.mimeType != null ? deleted.mimeType : "application/octet-stream";
        doc.file_reference = new byte[0];

        // Restore attributes
        if (deleted.documentAttributesSerialized != null) {
            doc.attributes = OverMessageUtils.deserializeDocumentAttributes(deleted.documentAttributesSerialized);
        } else {
            doc.attributes = new ArrayList<>();
        }

        // Restore thumbnails
        if (deleted.thumbsSerialized != null) {
            doc.thumbs = OverMessageUtils.deserializePhotoSizes(deleted.thumbsSerialized);
        }

        // For stickers, restore full document
        if (deleted.documentType == OverConstants.DOCUMENT_TYPE_STICKER && deleted.documentSerialized != null) {
            var restored = OverMessageUtils.deserializeDocument(deleted.documentSerialized);
            if (restored != null) {
                doc = (TLRPC.TL_document) restored;
            }
        }

        media.document = doc;
        msg.media = media;
        msg.flags |= 512; // TLRPC.MESSAGE_FLAG_HAS_MEDIA
    }
}
