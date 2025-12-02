/*
 * Full implementation based on codebase analysis
 * Reverse-engineered from usage patterns in the Overgram/AyuGram codebase
 *
 * This implementation injects deleted messages into chat history
 */

package com.radolyn.ayugram.proprietary;

import android.util.Pair;
import android.util.SparseArray;

import androidx.collection.LongSparseArray;

import com.radolyn.ayugram.AyuConfig;
import com.radolyn.ayugram.database.entities.DeletedMessageFull;
import com.radolyn.ayugram.messages.AyuMessagesController;

import org.telegram.messenger.MessageObject;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AyuHistoryHook {

    /**
     * Get the minimum and maximum message IDs from a list of messages
     *
     * @param messArr ArrayList of MessageObject
     * @return Pair of integers (min, max) message IDs
     */
    public static Pair<Integer, Integer> getMinAndMaxIds(ArrayList<MessageObject> messArr) {
        if (messArr == null || messArr.isEmpty()) {
            return new Pair<>(0, 0);
        }

        int minId = Integer.MAX_VALUE;
        int maxId = Integer.MIN_VALUE;

        for (MessageObject msg : messArr) {
            if (msg != null && msg.messageOwner != null) {
                int id = msg.messageOwner.id;
                if (id < minId) minId = id;
                if (id > maxId) maxId = id;
            }
        }

        // If no valid messages found
        if (minId == Integer.MAX_VALUE || maxId == Integer.MIN_VALUE) {
            return new Pair<>(0, 0);
        }

        return new Pair<>(minId, maxId);
    }

    /**
     * Hook into message history loading to inject saved/deleted messages
     *
     * @param currentAccount Current account ID
     * @param messArr ArrayList of MessageObject to potentially modify
     * @param messagesDict Dictionary of messages by dialog and message ID
     * @param startId Start message ID
     * @param endId End message ID
     * @param dialogId Dialog ID
     * @param limit Message limit
     * @param topicId Topic ID (for forum topics)
     * @param isSecretChat Whether this is a secret chat
     */
    public static void doHook(
            int currentAccount,
            ArrayList<MessageObject> messArr,
            SparseArray<MessageObject>[] messagesDict,
            int startId,
            int endId,
            long dialogId,
            int limit,
            long topicId,
            boolean isSecretChat
    ) {
        // Don't inject messages if the feature is disabled for this dialog
        if (!AyuConfig.saveDeletedMessageFor(currentAccount, dialogId)) {
            return;
        }

        // Don't process if we don't have a valid range
        if (startId == 0 && endId == 0) {
            return;
        }

        try {
            long userId = UserConfig.getInstance(currentAccount).getClientUserId();
            AyuMessagesController controller = AyuMessagesController.getInstance();

            // Query deleted messages in the range
            List<DeletedMessageFull> deletedMessages = controller.getMessages(
                    userId,
                    dialogId,
                    topicId,
                    startId,
                    endId,
                    limit
            );

            if (deletedMessages == null || deletedMessages.isEmpty()) {
                return;
            }

            // Convert deleted messages to MessageObject instances
            List<MessageObject> restoredMessages = new ArrayList<>();
            for (DeletedMessageFull deletedMsg : deletedMessages) {
                MessageObject messageObject = reconstructMessageObject(deletedMsg, currentAccount);
                if (messageObject != null) {
                    restoredMessages.add(messageObject);
                }
            }

            if (restoredMessages.isEmpty()) {
                return;
            }

            // Merge restored messages with existing messages
            mergeMessages(messArr, messagesDict, restoredMessages, dialogId);

        } catch (Exception e) {
            // Silently fail - don't break chat loading if there's an error
            android.util.Log.e("AyuGram", "Error in history hook", e);
        }
    }

    /**
     * Reconstructs a MessageObject from a deleted message database entry
     */
    private static MessageObject reconstructMessageObject(DeletedMessageFull deletedMsg, int currentAccount) {
        try {
            TLRPC.TL_message msg = new TLRPC.TL_message();

            var deletedMessage = deletedMsg.message;

            // Set basic properties
            msg.id = deletedMessage.messageId;
            msg.message = deletedMessage.text;
            msg.date = deletedMessage.date;
            msg.edit_date = deletedMessage.editDate;
            msg.views = deletedMessage.views;
            msg.flags = deletedMessage.flags;
            msg.grouped_id = deletedMessage.groupedId;
            msg.dialog_id = deletedMessage.dialogId;

            // Mark as deleted for UI indication
            msg.flags |= 0x80000000; // Custom flag to indicate deleted message

            // Reconstruct peer IDs
            if (deletedMessage.peerId != 0) {
                msg.peer_id = createPeer(deletedMessage.peerId);
            }

            if (deletedMessage.fromId != 0) {
                msg.from_id = createPeer(deletedMessage.fromId);
            }

            // Deserialize entities
            if (deletedMessage.textEntities != null) {
                msg.entities = deserializeEntities(deletedMessage.textEntities);
            }

            // Reconstruct forward info
            if (deletedMessage.fwdFlags != 0) {
                msg.fwd_from = new TLRPC.TL_messageFwdHeader();
                msg.fwd_from.flags = deletedMessage.fwdFlags;
                msg.fwd_from.date = deletedMessage.fwdDate;
                if (deletedMessage.fwdFromId != 0) {
                    msg.fwd_from.from_id = createPeer(deletedMessage.fwdFromId);
                }
                if (deletedMessage.fwdName != null) {
                    msg.fwd_from.from_name = deletedMessage.fwdName;
                }
                if (deletedMessage.fwdPostAuthor != null) {
                    msg.fwd_from.post_author = deletedMessage.fwdPostAuthor;
                }
            }

            // Reconstruct reply info
            if (deletedMessage.replyFlags != 0 || deletedMessage.replyMessageId != 0) {
                msg.reply_to = new TLRPC.TL_messageReplyHeader();
                msg.reply_to.flags = deletedMessage.replyFlags;
                msg.reply_to.reply_to_msg_id = deletedMessage.replyMessageId;
                msg.reply_to.reply_to_top_id = deletedMessage.replyTopId;
                msg.reply_to.forum_topic = deletedMessage.replyForumTopic;
                if (deletedMessage.replyPeerId != 0) {
                    msg.reply_to.reply_to_peer_id = createPeer(deletedMessage.replyPeerId);
                }
            }

            // Reconstruct media
            reconstructMedia(msg, deletedMessage);

            // Create MessageObject
            return new MessageObject(currentAccount, msg, true, true);

        } catch (Exception e) {
            android.util.Log.e("AyuGram", "Error reconstructing message", e);
            return null;
        }
    }

    /**
     * Reconstructs media from deleted message data
     */
    private static void reconstructMedia(TLRPC.TL_message msg, com.radolyn.ayugram.database.entities.DeletedMessage deletedMessage) {
        if (deletedMessage.documentType == com.radolyn.ayugram.AyuConstants.DOCUMENT_TYPE_NONE) {
            msg.media = new TLRPC.TL_messageMediaEmpty();
            return;
        }

        if (deletedMessage.documentType == com.radolyn.ayugram.AyuConstants.DOCUMENT_TYPE_PHOTO) {
            msg.media = new TLRPC.TL_messageMediaPhoto();
            msg.media.photo = new TLRPC.TL_photo();
            msg.media.photo.id = deletedMessage.messageId; // Use message ID as fake photo ID

            if (deletedMessage.thumbsSerialized != null) {
                msg.media.photo.sizes = deserializeSizes(deletedMessage.thumbsSerialized);
            }

            // If we have a saved file, we could add a file_reference
            // but for now we'll just use the thumbs
        } else if (deletedMessage.documentType == com.radolyn.ayugram.AyuConstants.DOCUMENT_TYPE_STICKER ||
                   deletedMessage.documentType == com.radolyn.ayugram.AyuConstants.DOCUMENT_TYPE_FILE) {
            msg.media = new TLRPC.TL_messageMediaDocument();

            if (deletedMessage.documentSerialized != null) {
                msg.media.document = deserializeDocument(deletedMessage.documentSerialized);
            } else {
                msg.media.document = new TLRPC.TL_document();
            }

            if (deletedMessage.thumbsSerialized != null && msg.media.document != null) {
                msg.media.document.thumbs = deserializeSizes(deletedMessage.thumbsSerialized);
            }

            if (deletedMessage.documentAttributesSerialized != null && msg.media.document != null) {
                msg.media.document.attributes = deserializeAttributes(deletedMessage.documentAttributesSerialized);
            }
        }
    }

    /**
     * Merges restored messages into the existing message list
     */
    private static void mergeMessages(
            ArrayList<MessageObject> messArr,
            SparseArray<MessageObject>[] messagesDict,
            List<MessageObject> restoredMessages,
            long dialogId
    ) {
        // Add restored messages to the array
        for (MessageObject restoredMsg : restoredMessages) {
            // Check if message already exists (might not be deleted after all)
            boolean exists = false;
            for (MessageObject existingMsg : messArr) {
                if (existingMsg.messageOwner.id == restoredMsg.messageOwner.id) {
                    exists = true;
                    break;
                }
            }

            // Only add if it doesn't already exist
            if (!exists) {
                messArr.add(restoredMsg);

                // Also add to messagesDict if provided (use index 0 for current dialog)
                if (messagesDict != null && messagesDict.length > 0) {
                    messagesDict[0].put(restoredMsg.messageOwner.id, restoredMsg);
                }
            }
        }

        // Re-sort messages by ID (descending order typically)
        Collections.sort(messArr, new Comparator<MessageObject>() {
            @Override
            public int compare(MessageObject o1, MessageObject o2) {
                return Integer.compare(o2.messageOwner.id, o1.messageOwner.id);
            }
        });
    }

    // Helper methods (similar to AyuMessageUtils)

    private static TLRPC.Peer createPeer(long peerId) {
        if (peerId > 0) {
            TLRPC.TL_peerUser peer = new TLRPC.TL_peerUser();
            peer.user_id = peerId;
            return peer;
        } else if (peerId < -1000000000000L) {
            TLRPC.TL_peerChannel peer = new TLRPC.TL_peerChannel();
            peer.channel_id = -1000000000000L - peerId;
            return peer;
        } else {
            TLRPC.TL_peerChat peer = new TLRPC.TL_peerChat();
            peer.chat_id = -peerId;
            return peer;
        }
    }

    private static ArrayList<TLRPC.MessageEntity> deserializeEntities(byte[] data) {
        try {
            ArrayList<TLRPC.MessageEntity> entities = new ArrayList<>();
            org.telegram.tgnet.NativeByteBuffer buffer = new org.telegram.tgnet.NativeByteBuffer(data.length);
            buffer.writeBytes(data);
            buffer.rewind();

            int count = buffer.readInt32(false);
            for (int i = 0; i < count; i++) {
                TLRPC.MessageEntity entity = TLRPC.MessageEntity.TLdeserialize(buffer, buffer.readInt32(false), false);
                if (entity != null) {
                    entities.add(entity);
                }
            }
            buffer.reuse();
            return entities;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static ArrayList<TLRPC.PhotoSize> deserializeSizes(byte[] data) {
        try {
            ArrayList<TLRPC.PhotoSize> sizes = new ArrayList<>();
            org.telegram.tgnet.NativeByteBuffer buffer = new org.telegram.tgnet.NativeByteBuffer(data.length);
            buffer.writeBytes(data);
            buffer.rewind();

            int count = buffer.readInt32(false);
            for (int i = 0; i < count; i++) {
                TLRPC.PhotoSize size = TLRPC.PhotoSize.TLdeserialize(0, 0, 0, buffer, buffer.readInt32(false), false);
                if (size != null) {
                    sizes.add(size);
                }
            }
            buffer.reuse();
            return sizes;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static TLRPC.Document deserializeDocument(byte[] data) {
        try {
            org.telegram.tgnet.NativeByteBuffer buffer = new org.telegram.tgnet.NativeByteBuffer(data.length);
            buffer.writeBytes(data);
            buffer.rewind();

            TLRPC.Document document = TLRPC.Document.TLdeserialize(buffer, buffer.readInt32(false), false);
            buffer.reuse();
            return document;
        } catch (Exception e) {
            return null;
        }
    }

    private static ArrayList<TLRPC.DocumentAttribute> deserializeAttributes(byte[] data) {
        try {
            ArrayList<TLRPC.DocumentAttribute> attributes = new ArrayList<>();
            org.telegram.tgnet.NativeByteBuffer buffer = new org.telegram.tgnet.NativeByteBuffer(data.length);
            buffer.writeBytes(data);
            buffer.rewind();

            int count = buffer.readInt32(false);
            for (int i = 0; i < count; i++) {
                TLRPC.DocumentAttribute attr = TLRPC.DocumentAttribute.TLdeserialize(buffer, buffer.readInt32(false), false);
                if (attr != null) {
                    attributes.add(attr);
                }
            }
            buffer.reuse();
            return attributes;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
