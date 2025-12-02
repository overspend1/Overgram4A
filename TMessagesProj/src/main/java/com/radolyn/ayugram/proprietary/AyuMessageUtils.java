/*
 * Full implementation based on codebase analysis
 * Reverse-engineered from usage patterns in the Overgram/AyuGram codebase
 *
 * This implementation provides message history and anti-recall functionality
 */

package com.radolyn.ayugram.proprietary;

import android.text.TextUtils;
import android.util.Log;

import com.radolyn.ayugram.AyuConstants;
import com.radolyn.ayugram.database.entities.DeletedMessage;
import com.radolyn.ayugram.database.entities.EditedMessage;
import com.radolyn.ayugram.messages.AyuMessagesController;
import com.radolyn.ayugram.messages.AyuSavePreferences;

import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.AbstractSerializedData;
import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.TLRPC;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class AyuMessageUtils {

    /**
     * Maps data from AyuSavePreferences to EditedMessage entity
     */
    public static void map(AyuSavePreferences prefs, EditedMessage revision) {
        if (prefs == null || revision == null) return;
        mapCommon(prefs, revision);
    }

    /**
     * Maps data from AyuSavePreferences to DeletedMessage entity
     */
    public static void map(AyuSavePreferences prefs, DeletedMessage deletedMessage) {
        if (prefs == null || deletedMessage == null) return;
        mapCommon(prefs, deletedMessage);
    }

    /**
     * Common mapping logic for both EditedMessage and DeletedMessage
     */
    private static void mapCommon(AyuSavePreferences prefs, Object target) {
        var msg = prefs.getMessage();
        if (msg == null) return;

        Class<?> baseClass;
        try {
            baseClass = Class.forName("com.radolyn.ayugram.database.entities.AyuMessageBase");
        } catch (ClassNotFoundException e) {
            return;
        }

        try {
            // Set basic fields
            baseClass.getField("userId").setLong(target, prefs.getUserId());
            baseClass.getField("dialogId").setLong(target, prefs.getDialogId());
            baseClass.getField("messageId").setInt(target, prefs.getMessageId());
            baseClass.getField("topicId").setLong(target, prefs.getTopicId());
            baseClass.getField("entityCreateDate").setInt(target, prefs.getRequestCatchTime());
            baseClass.getField("date").setInt(target, msg.date);
            baseClass.getField("editDate").setInt(target, msg.edit_date);
            baseClass.getField("views").setInt(target, msg.views);
            baseClass.getField("flags").setInt(target, msg.flags);
            baseClass.getField("groupedId").setLong(target, msg.grouped_id);

            // Parse peer IDs
            if (msg.peer_id != null) {
                long peerId = getPeerId(msg.peer_id);
                baseClass.getField("peerId").setLong(target, peerId);
            }

            if (msg.from_id != null) {
                long fromId = getPeerId(msg.from_id);
                baseClass.getField("fromId").setLong(target, fromId);
            }

            // Handle forward info
            if (msg.fwd_from != null) {
                baseClass.getField("fwdFlags").setInt(target, msg.fwd_from.flags);
                if (msg.fwd_from.from_id != null) {
                    baseClass.getField("fwdFromId").setLong(target, getPeerId(msg.fwd_from.from_id));
                }
                if (msg.fwd_from.from_name != null) {
                    baseClass.getField("fwdName").set(target, msg.fwd_from.from_name);
                }
                baseClass.getField("fwdDate").setInt(target, msg.fwd_from.date);
                if (msg.fwd_from.post_author != null) {
                    baseClass.getField("fwdPostAuthor").set(target, msg.fwd_from.post_author);
                }
            }

            // Handle reply info
            if (msg.reply_to != null) {
                baseClass.getField("replyFlags").setInt(target, msg.reply_to.flags);
                baseClass.getField("replyMessageId").setInt(target, msg.reply_to.reply_to_msg_id);
                if (msg.reply_to.reply_to_peer_id != null) {
                    baseClass.getField("replyPeerId").setLong(target, getPeerId(msg.reply_to.reply_to_peer_id));
                }
                baseClass.getField("replyTopId").setInt(target, msg.reply_to.reply_to_top_id);
                baseClass.getField("replyForumTopic").setBoolean(target, msg.reply_to.forum_topic);
            }

            // Set message text
            if (!TextUtils.isEmpty(msg.message)) {
                baseClass.getField("text").set(target, msg.message);
            }

            // Serialize message entities
            if (msg.entities != null && !msg.entities.isEmpty()) {
                byte[] serialized = serializeEntities(msg.entities);
                baseClass.getField("textEntities").set(target, serialized);
            }

        } catch (Exception e) {
            Log.e("AyuGram", "Error mapping message fields", e);
        }
    }

    /**
     * Maps media from message to EditedMessage entity
     */
    public static void mapMedia(AyuSavePreferences prefs, EditedMessage revision, boolean copyMedia) {
        if (prefs == null || revision == null) return;
        mapMediaCommon(prefs, revision, copyMedia, prefs.getAccountId());
    }

    /**
     * Maps media from message to DeletedMessage entity
     */
    public static void mapMedia(AyuSavePreferences prefs, DeletedMessage deletedMessage, boolean copyMedia) {
        if (prefs == null || deletedMessage == null) return;
        mapMediaCommon(prefs, deletedMessage, copyMedia, prefs.getAccountId());
    }

    /**
     * Common media mapping logic
     */
    private static void mapMediaCommon(AyuSavePreferences prefs, Object target, boolean copyMedia, int accountId) {
        var msg = prefs.getMessage();
        if (msg == null || msg.media == null) return;

        Class<?> baseClass;
        try {
            baseClass = Class.forName("com.radolyn.ayugram.database.entities.AyuMessageBase");
        } catch (ClassNotFoundException e) {
            return;
        }

        try {
            // Handle photo media
            if (msg.media instanceof TLRPC.TL_messageMediaPhoto && msg.media.photo != null) {
                baseClass.getField("documentType").setInt(target, AyuConstants.DOCUMENT_TYPE_PHOTO);

                if (copyMedia) {
                    File photoFile = FileLoader.getInstance(accountId).getPathToMessage(msg);
                    if (photoFile.exists()) {
                        File savedFile = saveMediaFile(photoFile, msg.id);
                        if (savedFile != null) {
                            baseClass.getField("mediaPath").set(target, savedFile.getAbsolutePath());
                        }
                    }
                }

                // Serialize photo thumbs
                if (msg.media.photo.sizes != null) {
                    byte[] serialized = serializeSizes(msg.media.photo.sizes);
                    baseClass.getField("thumbsSerialized").set(target, serialized);
                }
            }
            // Handle document media (files, videos, stickers, etc.)
            else if (msg.media instanceof TLRPC.TL_messageMediaDocument && msg.media.document != null) {
                var document = msg.media.document;

                // Determine document type
                int docType = AyuConstants.DOCUMENT_TYPE_FILE;
                for (var attr : document.attributes) {
                    if (attr instanceof TLRPC.TL_documentAttributeSticker) {
                        docType = AyuConstants.DOCUMENT_TYPE_STICKER;
                        break;
                    }
                }
                baseClass.getField("documentType").setInt(target, docType);

                // Save file if needed
                if (copyMedia && docType != AyuConstants.DOCUMENT_TYPE_STICKER) {
                    File docFile = FileLoader.getInstance(accountId).getPathToMessage(msg);
                    if (docFile.exists()) {
                        File savedFile = saveMediaFile(docFile, msg.id);
                        if (savedFile != null) {
                            baseClass.getField("mediaPath").set(target, savedFile.getAbsolutePath());
                        }
                    }
                }

                // Set MIME type
                if (!TextUtils.isEmpty(document.mime_type)) {
                    baseClass.getField("mimeType").set(target, document.mime_type);
                }

                // Serialize document
                byte[] docSerialized = serializeDocument(document);
                baseClass.getField("documentSerialized").set(target, docSerialized);

                // Serialize thumbs
                if (document.thumbs != null) {
                    byte[] thumbs = serializeSizes(document.thumbs);
                    baseClass.getField("thumbsSerialized").set(target, thumbs);
                }

                // Serialize attributes
                if (document.attributes != null) {
                    byte[] attrs = serializeAttributes(document.attributes);
                    baseClass.getField("documentAttributesSerialized").set(target, attrs);
                }
            }
        } catch (Exception e) {
            Log.e("AyuGram", "Error mapping media", e);
        }
    }

    /**
     * Maps data from EditedMessage entity back to TLRPC.Message
     */
    public static void map(EditedMessage editedMessage, TLRPC.TL_message msg, int currentAccount) {
        if (editedMessage == null || msg == null) return;

        msg.id = editedMessage.messageId;
        msg.message = editedMessage.text;
        msg.date = editedMessage.date;
        msg.edit_date = editedMessage.editDate;
        msg.views = editedMessage.views;
        msg.flags = editedMessage.flags;
        msg.grouped_id = editedMessage.groupedId;
        msg.dialog_id = editedMessage.dialogId;

        // Reconstruct peer IDs
        if (editedMessage.peerId != 0) {
            msg.peer_id = createPeer(editedMessage.peerId);
        }

        if (editedMessage.fromId != 0) {
            msg.from_id = createPeer(editedMessage.fromId);
        }

        // Deserialize entities
        if (editedMessage.textEntities != null) {
            msg.entities = deserializeEntities(editedMessage.textEntities);
        }

        // Reconstruct forward info
        if (editedMessage.fwdFlags != 0) {
            msg.fwd_from = new TLRPC.TL_messageFwdHeader();
            msg.fwd_from.flags = editedMessage.fwdFlags;
            msg.fwd_from.date = editedMessage.fwdDate;
            if (editedMessage.fwdFromId != 0) {
                msg.fwd_from.from_id = createPeer(editedMessage.fwdFromId);
            }
            if (!TextUtils.isEmpty(editedMessage.fwdName)) {
                msg.fwd_from.from_name = editedMessage.fwdName;
            }
            if (!TextUtils.isEmpty(editedMessage.fwdPostAuthor)) {
                msg.fwd_from.post_author = editedMessage.fwdPostAuthor;
            }
        }

        // Reconstruct reply info
        if (editedMessage.replyFlags != 0 || editedMessage.replyMessageId != 0) {
            msg.reply_to = new TLRPC.TL_messageReplyHeader();
            msg.reply_to.flags = editedMessage.replyFlags;
            msg.reply_to.reply_to_msg_id = editedMessage.replyMessageId;
            msg.reply_to.reply_to_top_id = editedMessage.replyTopId;
            msg.reply_to.forum_topic = editedMessage.replyForumTopic;
            if (editedMessage.replyPeerId != 0) {
                msg.reply_to.reply_to_peer_id = createPeer(editedMessage.replyPeerId);
            }
        }
    }

    /**
     * Maps media from EditedMessage entity back to TLRPC.Message
     */
    public static void mapMedia(EditedMessage editedMessage, TLRPC.TL_message msg) {
        if (editedMessage == null || msg == null) return;

        if (editedMessage.documentType == AyuConstants.DOCUMENT_TYPE_NONE) {
            msg.media = new TLRPC.TL_messageMediaEmpty();
            return;
        }

        if (editedMessage.documentType == AyuConstants.DOCUMENT_TYPE_PHOTO) {
            msg.media = new TLRPC.TL_messageMediaPhoto();
            msg.media.photo = new TLRPC.TL_photo();
            msg.media.photo.id = editedMessage.messageId; // Fake ID

            if (editedMessage.thumbsSerialized != null) {
                msg.media.photo.sizes = deserializeSizes(editedMessage.thumbsSerialized);
            }
        } else if (editedMessage.documentType == AyuConstants.DOCUMENT_TYPE_STICKER ||
                   editedMessage.documentType == AyuConstants.DOCUMENT_TYPE_FILE) {
            msg.media = new TLRPC.TL_messageMediaDocument();

            if (editedMessage.documentSerialized != null) {
                msg.media.document = deserializeDocument(editedMessage.documentSerialized);
            } else {
                msg.media.document = new TLRPC.TL_document();
            }

            if (editedMessage.thumbsSerialized != null && msg.media.document != null) {
                msg.media.document.thumbs = deserializeSizes(editedMessage.thumbsSerialized);
            }

            if (editedMessage.documentAttributesSerialized != null && msg.media.document != null) {
                msg.media.document.attributes = deserializeAttributes(editedMessage.documentAttributesSerialized);
            }
        }
    }

    // Helper methods

    private static long getPeerId(TLRPC.Peer peer) {
        if (peer instanceof TLRPC.TL_peerUser) {
            return peer.user_id;
        } else if (peer instanceof TLRPC.TL_peerChat) {
            return -peer.chat_id;
        } else if (peer instanceof TLRPC.TL_peerChannel) {
            return -1000000000000L - peer.channel_id;
        }
        return 0;
    }

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

    private static File saveMediaFile(File source, int messageId) {
        try {
            File destDir = AyuMessagesController.attachmentsPath;
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            String extension = "";
            String name = source.getName();
            int lastDot = name.lastIndexOf('.');
            if (lastDot > 0) {
                extension = name.substring(lastDot);
            }

            File destFile = new File(destDir, "msg_" + messageId + "_" + System.currentTimeMillis() + extension);

            copyFile(source, destFile);
            return destFile;
        } catch (Exception e) {
            Log.e("AyuGram", "Failed to save media file", e);
            return null;
        }
    }

    private static void copyFile(File source, File dest) throws IOException {
        try (FileInputStream inStream = new FileInputStream(source);
             FileOutputStream outStream = new FileOutputStream(dest);
             FileChannel inChannel = inStream.getChannel();
             FileChannel outChannel = outStream.getChannel()) {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
    }

    private static byte[] serializeEntities(ArrayList<TLRPC.MessageEntity> entities) {
        try {
            NativeByteBuffer buffer = new NativeByteBuffer(entities.size() * 128);
            buffer.writeInt32(entities.size());
            for (TLRPC.MessageEntity entity : entities) {
                entity.serializeToStream(buffer);
            }
            byte[] result = new byte[buffer.length()];
            buffer.rewind();
            buffer.readBytes(result, 0, result.length, false);
            buffer.reuse();
            return result;
        } catch (Exception e) {
            Log.e("AyuGram", "Failed to serialize entities", e);
            return null;
        }
    }

    private static ArrayList<TLRPC.MessageEntity> deserializeEntities(byte[] data) {
        try {
            ArrayList<TLRPC.MessageEntity> entities = new ArrayList<>();
            NativeByteBuffer buffer = new NativeByteBuffer(data.length);
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
            Log.e("AyuGram", "Failed to deserialize entities", e);
            return new ArrayList<>();
        }
    }

    private static byte[] serializeSizes(ArrayList<TLRPC.PhotoSize> sizes) {
        try {
            NativeByteBuffer buffer = new NativeByteBuffer(sizes.size() * 256);
            buffer.writeInt32(sizes.size());
            for (TLRPC.PhotoSize size : sizes) {
                size.serializeToStream(buffer);
            }
            byte[] result = new byte[buffer.length()];
            buffer.rewind();
            buffer.readBytes(result, 0, result.length, false);
            buffer.reuse();
            return result;
        } catch (Exception e) {
            Log.e("AyuGram", "Failed to serialize sizes", e);
            return null;
        }
    }

    private static ArrayList<TLRPC.PhotoSize> deserializeSizes(byte[] data) {
        try {
            ArrayList<TLRPC.PhotoSize> sizes = new ArrayList<>();
            NativeByteBuffer buffer = new NativeByteBuffer(data.length);
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
            Log.e("AyuGram", "Failed to deserialize sizes", e);
            return new ArrayList<>();
        }
    }

    private static byte[] serializeDocument(TLRPC.Document document) {
        try {
            NativeByteBuffer buffer = new NativeByteBuffer(4096);
            document.serializeToStream(buffer);
            byte[] result = new byte[buffer.length()];
            buffer.rewind();
            buffer.readBytes(result, 0, result.length, false);
            buffer.reuse();
            return result;
        } catch (Exception e) {
            Log.e("AyuGram", "Failed to serialize document", e);
            return null;
        }
    }

    private static TLRPC.Document deserializeDocument(byte[] data) {
        try {
            NativeByteBuffer buffer = new NativeByteBuffer(data.length);
            buffer.writeBytes(data);
            buffer.rewind();

            TLRPC.Document document = TLRPC.Document.TLdeserialize(buffer, buffer.readInt32(false), false);
            buffer.reuse();
            return document;
        } catch (Exception e) {
            Log.e("AyuGram", "Failed to deserialize document", e);
            return null;
        }
    }

    private static byte[] serializeAttributes(ArrayList<TLRPC.DocumentAttribute> attributes) {
        try {
            NativeByteBuffer buffer = new NativeByteBuffer(attributes.size() * 128);
            buffer.writeInt32(attributes.size());
            for (TLRPC.DocumentAttribute attr : attributes) {
                attr.serializeToStream(buffer);
            }
            byte[] result = new byte[buffer.length()];
            buffer.rewind();
            buffer.readBytes(result, 0, result.length, false);
            buffer.reuse();
            return result;
        } catch (Exception e) {
            Log.e("AyuGram", "Failed to serialize attributes", e);
            return null;
        }
    }

    private static ArrayList<TLRPC.DocumentAttribute> deserializeAttributes(byte[] data) {
        try {
            ArrayList<TLRPC.DocumentAttribute> attributes = new ArrayList<>();
            NativeByteBuffer buffer = new NativeByteBuffer(data.length);
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
            Log.e("AyuGram", "Failed to deserialize attributes", e);
            return new ArrayList<>();
        }
    }
}
