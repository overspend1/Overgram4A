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

import android.text.TextUtils;
import com.google.android.exoplayer2.util.Log;
import com.overspend1.overgram.OverConstants;
import com.overspend1.overgram.database.entities.EditedMessage;
import com.overspend1.overgram.database.entities.OverMessageBase;
import com.overspend1.overgram.messages.OverMessagesController;
import com.overspend1.overgram.messages.OverSavePreferences;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.SerializedData;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

/**
 * Message utility class for mapping between Telegram messages and Overgram database entities
 */
public class OverMessageUtils {

    /**
     * Map message data from preferences to database entity (for saving)
     */
    public static void map(OverSavePreferences prefs, OverMessageBase entity) {
        var msg = prefs.getMessage();
        if (msg == null) {
            return;
        }

        entity.userId = prefs.getUserId();
        entity.dialogId = prefs.getDialogId();
        entity.messageId = prefs.getMessageId();
        entity.date = msg.date;
        entity.entityCreateDate = prefs.getRequestCatchTime();

        // Message flags
        entity.flags = msg.flags;
        entity.editDate = msg.edit_date;
        entity.views = msg.views;

        // Grouped messages
        if (msg.grouped_id != 0) {
            entity.groupedId = msg.grouped_id;
        }

        // Peer and from info
        entity.peerId = msg.peer_id != null ? MessageObject.getPeerId(msg.peer_id) : 0;
        entity.fromId = msg.from_id != null ? MessageObject.getPeerId(msg.from_id) : 0;
        entity.topicId = MessageObject.getTopicId(msg, false);

        // Forward info
        if (msg.fwd_from != null) {
            entity.fwdFlags = msg.fwd_from.flags;
            entity.fwdFromId = msg.fwd_from.from_id != null ? MessageObject.getPeerId(msg.fwd_from.from_id) : 0;
            entity.fwdName = msg.fwd_from.from_name;
            entity.fwdDate = msg.fwd_from.date;
            entity.fwdPostAuthor = msg.fwd_from.post_author;
        }

        // Reply info
        if (msg.reply_to != null) {
            entity.replyFlags = msg.reply_to.flags;
            entity.replyMessageId = msg.reply_to.reply_to_msg_id;
            entity.replyPeerId = msg.reply_to.reply_to_peer_id != null ? MessageObject.getPeerId(msg.reply_to.reply_to_peer_id) : 0;
            entity.replyTopId = msg.reply_to.reply_to_top_id;
            entity.replyForumTopic = msg.reply_to.forum_topic;
        }

        // Message text and entities
        entity.text = msg.message;
        if (msg.entities != null && !msg.entities.isEmpty()) {
            entity.textEntities = serializeTLList(msg.entities);
        }
    }

    /**
     * Map media data from preferences to database entity (for saving)
     */
    public static void mapMedia(OverSavePreferences prefs, OverMessageBase entity, boolean saveMedia) {
        var msg = prefs.getMessage();
        if (msg == null || msg.media == null) {
            return;
        }

        try {
            if (!saveMedia) {
                return;
            }

            if (msg.media instanceof TLRPC.TL_messageMediaPhoto && msg.media.photo != null) {
                handlePhoto(prefs, entity, msg.media.photo);
            } else if (msg.media instanceof TLRPC.TL_messageMediaDocument && msg.media.document != null) {
                handleDocument(prefs, entity, msg.media.document);
            }
        } catch (Exception e) {
            Log.e("Overgram", "Failed to map media", e);
            FileLog.e("mapMedia", e);
        }
    }

    /**
     * Map from EditedMessage to TLRPC.Message (for displaying)
     */
    public static void map(EditedMessage editedMessage, TLRPC.Message msg, int accountId) {
        msg.id = editedMessage.messageId;
        msg.dialog_id = editedMessage.dialogId;
        msg.date = editedMessage.date;
        msg.flags = editedMessage.flags;
        msg.edit_date = editedMessage.editDate;
        msg.views = editedMessage.views;

        if (editedMessage.groupedId != 0) {
            msg.grouped_id = editedMessage.groupedId;
        }

        // Peer info
        if (editedMessage.peerId != 0) {
            msg.peer_id = MessagesController.getInstance(accountId).getPeer(editedMessage.peerId);
        }
        if (editedMessage.fromId != 0) {
            msg.from_id = MessagesController.getInstance(accountId).getPeer(editedMessage.fromId);
        }

        // Forward info
        if (editedMessage.fwdFlags != 0) {
            var fwd = new TLRPC.TL_messageFwdHeader();
            fwd.flags = editedMessage.fwdFlags;
            if (editedMessage.fwdFromId != 0) {
                fwd.from_id = MessagesController.getInstance(accountId).getPeer(editedMessage.fwdFromId);
            }
            fwd.from_name = editedMessage.fwdName;
            fwd.date = editedMessage.fwdDate;
            fwd.post_author = editedMessage.fwdPostAuthor;
            msg.fwd_from = fwd;
        }

        // Reply info
        if (editedMessage.replyFlags != 0) {
            var reply = new TLRPC.TL_messageReplyHeader();
            reply.flags = editedMessage.replyFlags;
            reply.reply_to_msg_id = editedMessage.replyMessageId;
            if (editedMessage.replyPeerId != 0) {
                reply.reply_to_peer_id = MessagesController.getInstance(accountId).getPeer(editedMessage.replyPeerId);
            }
            reply.reply_to_top_id = editedMessage.replyTopId;
            reply.forum_topic = editedMessage.replyForumTopic;
            msg.reply_to = reply;
        }

        // Text and entities
        msg.message = editedMessage.text != null ? editedMessage.text : "";
        if (editedMessage.textEntities != null) {
            msg.entities = deserializeEntities(editedMessage.textEntities);
        }
    }

    /**
     * Map media from EditedMessage to TLRPC.Message (for displaying)
     */
    public static void mapMedia(EditedMessage editedMessage, TLRPC.Message msg) {
        if (TextUtils.isEmpty(editedMessage.mediaPath)) {
            return;
        }

        try {
            var documentType = editedMessage.documentType;

            if (documentType == OverConstants.DOCUMENT_TYPE_PHOTO) {
                restorePhoto(editedMessage, msg);
            } else if (documentType >= OverConstants.DOCUMENT_TYPE_FILE) {
                restoreDocument(editedMessage, msg);
            }
        } catch (Exception e) {
            Log.e("Overgram", "Failed to restore media", e);
            FileLog.e("mapMedia restore", e);
        }
    }

    // === Private helper methods ===

    private static void handlePhoto(OverSavePreferences prefs, OverMessageBase entity, TLRPC.Photo photo) {
        try {
            var accountId = prefs.getAccountId();
            var path = FileLoader.getInstance(accountId).getPathToMessage(prefs.getMessage());

            if (path != null && path.exists()) {
                var destPath = new File(OverMessagesController.attachmentsPath,
                        prefs.getMessage().id + "_photo.jpg");

                Files.copy(path.toPath(), destPath.toPath(), StandardCopyOption.REPLACE_EXISTING);

                entity.mediaPath = destPath.getAbsolutePath();
                entity.documentType = OverConstants.DOCUMENT_TYPE_PHOTO;
            }
        } catch (Exception e) {
            Log.e("Overgram", "Failed to save photo", e);
            FileLog.e("handlePhoto", e);
        }
    }

    private static void handleDocument(OverSavePreferences prefs, OverMessageBase entity, TLRPC.Document document) {
        try {
            var accountId = prefs.getAccountId();
            var msg = prefs.getMessage();
            var path = FileLoader.getInstance(accountId).getPathToMessage(msg);

            if (path == null || !path.exists()) {
                return;
            }

            entity.documentType = OverConstants.DOCUMENT_TYPE_FILE;
            entity.mimeType = document.mime_type;

            // Determine document type
            for (var attr : document.attributes) {
                if (attr instanceof TLRPC.TL_documentAttributeSticker) {
                    entity.documentType = OverConstants.DOCUMENT_TYPE_STICKER;
                    entity.documentSerialized = serializeTLObject(document);
                    return; // Stickers don't need file copy
                } else if (attr instanceof TLRPC.TL_documentAttributeAnimated) {
                    entity.documentType = OverConstants.DOCUMENT_TYPE_GIF;
                } else if (attr instanceof TLRPC.TL_documentAttributeVideo) {
                    if (((TLRPC.TL_documentAttributeVideo) attr).round_message) {
                        entity.documentType = OverConstants.DOCUMENT_TYPE_ROUND;
                    } else {
                        entity.documentType = OverConstants.DOCUMENT_TYPE_VIDEO;
                    }
                } else if (attr instanceof TLRPC.TL_documentAttributeAudio) {
                    if (((TLRPC.TL_documentAttributeAudio) attr).voice) {
                        entity.documentType = OverConstants.DOCUMENT_TYPE_VOICE;
                    } else {
                        entity.documentType = OverConstants.DOCUMENT_TYPE_AUDIO;
                    }
                }
            }

            // Copy file
            var ext = getFileExtension(document);
            var destPath = new File(OverMessagesController.attachmentsPath,
                    document.id + "_" + document.date + ext);

            Files.copy(path.toPath(), destPath.toPath(), StandardCopyOption.REPLACE_EXISTING);

            entity.mediaPath = destPath.getAbsolutePath();
            entity.documentAttributesSerialized = serializeTLList(document.attributes);

            // Save thumbnails
            if (document.thumbs != null && !document.thumbs.isEmpty()) {
                entity.thumbsSerialized = serializeTLList(document.thumbs);
            }
        } catch (Exception e) {
            Log.e("Overgram", "Failed to save document", e);
            FileLog.e("handleDocument", e);
        }
    }

    private static void restorePhoto(EditedMessage editedMessage, TLRPC.Message msg) {
        var media = new TLRPC.TL_messageMediaPhoto();
        var photo = new TLRPC.TL_photo();

        photo.id = 0; // Fake ID
        photo.date = editedMessage.date;
        photo.file_reference = new byte[0];

        // Create photo size
        var photoSize = new TLRPC.TL_photoSize();
        photoSize.type = "x";
        photoSize.location = new TLRPC.TL_fileLocationToBeDeprecated();
        photoSize.size = (int) new File(editedMessage.mediaPath).length();

        photo.sizes = new ArrayList<>();
        photo.sizes.add(photoSize);

        media.photo = photo;
        msg.media = media;
        msg.flags |= 512; // TLRPC.MESSAGE_FLAG_HAS_MEDIA
    }

    private static void restoreDocument(EditedMessage editedMessage, TLRPC.Message msg) {
        var media = new TLRPC.TL_messageMediaDocument();
        var doc = new TLRPC.TL_document();

        doc.id = 0; // Fake ID
        doc.date = editedMessage.date;
        doc.mime_type = editedMessage.mimeType != null ? editedMessage.mimeType : "application/octet-stream";
        doc.file_reference = new byte[0];
        doc.size = new File(editedMessage.mediaPath).length();

        // Restore attributes
        if (editedMessage.documentAttributesSerialized != null) {
            doc.attributes = deserializeDocumentAttributes(editedMessage.documentAttributesSerialized);
        } else {
            doc.attributes = new ArrayList<>();
        }

        // Restore thumbnails
        if (editedMessage.thumbsSerialized != null) {
            doc.thumbs = deserializePhotoSizes(editedMessage.thumbsSerialized);
        }

        // For stickers, restore full document
        if (editedMessage.documentType == OverConstants.DOCUMENT_TYPE_STICKER && editedMessage.documentSerialized != null) {
            var restored = deserializeDocument(editedMessage.documentSerialized);
            if (restored != null) {
                doc = (TLRPC.TL_document) restored;
            }
        }

        media.document = doc;
        msg.media = media;
        msg.flags |= 512; // TLRPC.MESSAGE_FLAG_HAS_MEDIA
    }

    private static String getFileExtension(TLRPC.Document document) {
        String ext = "";
        for (var attr : document.attributes) {
            if (attr instanceof TLRPC.TL_documentAttributeFilename) {
                var filename = ((TLRPC.TL_documentAttributeFilename) attr).file_name;
                int dotIndex = filename.lastIndexOf('.');
                if (dotIndex > 0) {
                    ext = filename.substring(dotIndex);
                }
            }
        }
        if (TextUtils.isEmpty(ext)) {
            ext = "." + getExtensionFromMimeType(document.mime_type);
        }
        return ext;
    }

    private static String getExtensionFromMimeType(String mimeType) {
        if (mimeType == null) return "bin";
        if (mimeType.startsWith("image/")) return "jpg";
        if (mimeType.startsWith("video/")) return "mp4";
        if (mimeType.startsWith("audio/")) return "mp3";
        return "bin";
    }

    // === Serialization helpers ===

    private static byte[] serializeTLObject(TLObject obj) {
        if (obj == null) return null;
        SerializedData data = new SerializedData();
        try {
            obj.serializeToStream(data);
            return data.toByteArray();
        } catch (Exception e) {
            Log.e("Overgram", "Failed to serialize TL object", e);
            return null;
        } finally {
            data.cleanup();
        }
    }

    private static <T extends TLObject> byte[] serializeTLList(ArrayList<T> list) {
        if (list == null) return null;
        SerializedData data = new SerializedData();
        try {
            data.writeInt32(list.size());
            for (var item : list) {
                item.serializeToStream(data);
            }
            return data.toByteArray();
        } catch (Exception e) {
            Log.e("Overgram", "Failed to serialize TL list", e);
            return null;
        } finally {
            data.cleanup();
        }
    }

    public static ArrayList<TLRPC.MessageEntity> deserializeEntities(byte[] data) {
        ArrayList<TLRPC.MessageEntity> list = new ArrayList<>();
        if (data == null || data.length == 0) return list;
        SerializedData sd = new SerializedData(data);
        try {
            int count = sd.readInt32(false);
            for (int i = 0; i < count; i++) {
                int constructor = sd.readInt32(false);
                var entity = TLRPC.MessageEntity.TLdeserialize(sd, constructor, false);
                if (entity != null) {
                    list.add(entity);
                }
            }
        } catch (Exception e) {
            Log.e("Overgram", "Failed to deserialize entities", e);
        } finally {
            sd.cleanup();
        }
        return list;
    }

    public static ArrayList<TLRPC.DocumentAttribute> deserializeDocumentAttributes(byte[] data) {
        ArrayList<TLRPC.DocumentAttribute> list = new ArrayList<>();
        if (data == null || data.length == 0) return list;
        SerializedData sd = new SerializedData(data);
        try {
            int count = sd.readInt32(false);
            for (int i = 0; i < count; i++) {
                int constructor = sd.readInt32(false);
                var attr = TLRPC.DocumentAttribute.TLdeserialize(sd, constructor, false);
                if (attr != null) {
                    list.add(attr);
                }
            }
        } catch (Exception e) {
            Log.e("Overgram", "Failed to deserialize document attributes", e);
        } finally {
            sd.cleanup();
        }
        return list;
    }

    public static ArrayList<TLRPC.PhotoSize> deserializePhotoSizes(byte[] data) {
        ArrayList<TLRPC.PhotoSize> list = new ArrayList<>();
        if (data == null || data.length == 0) return list;
        SerializedData sd = new SerializedData(data);
        try {
            int count = sd.readInt32(false);
            for (int i = 0; i < count; i++) {
                int constructor = sd.readInt32(false);
                var size = TLRPC.PhotoSize.TLdeserialize(0, 0, 0, sd, constructor, false);
                if (size != null) {
                    list.add(size);
                }
            }
        } catch (Exception e) {
            Log.e("Overgram", "Failed to deserialize photo sizes", e);
        } finally {
            sd.cleanup();
        }
        return list;
    }

    public static TLRPC.Document deserializeDocument(byte[] data) {
        if (data == null || data.length == 0) return null;
        SerializedData sd = new SerializedData(data);
        try {
            int constructor = sd.readInt32(false);
            return TLRPC.Document.TLdeserialize(sd, constructor, false);
        } catch (Exception e) {
            Log.e("Overgram", "Failed to deserialize document", e);
            return null;
        } finally {
            sd.cleanup();
        }
    }
}
