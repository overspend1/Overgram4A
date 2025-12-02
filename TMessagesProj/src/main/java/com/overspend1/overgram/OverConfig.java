/*
 * This is the source code of Overgram for Android.
 *
 * We do not and cannot prevent the use of our code,
 * but be respectful and credit the original author.
 *
 * Copyright @overspend1, 2024
 * Based on original AyuGram code by @Radolyn, 2023
 */

package com.overspend1.overgram;

import android.app.Activity;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import org.telegram.messenger.*;

import java.util.ArrayList;
import java.util.Arrays;

public class OverConfig {
    private static final Object sync = new Object();

    public static SharedPreferences preferences;
    public static SharedPreferences.Editor editor;

    public static boolean sendReadPackets;
    public static boolean sendOnlinePackets;
    public static boolean sendOfflinePacketAfterOnline;
    public static boolean sendUploadProgress;
    public static boolean useScheduledMessages;
    public static boolean saveDeletedMessages;
    public static boolean saveMessagesHistory;

    public static boolean saveMedia;
    public static boolean saveMediaInPrivateChats;
    public static boolean saveMediaInPublicChannels;
    public static boolean saveMediaInPrivateChannels;
    public static boolean saveMediaInPublicGroups;
    public static boolean saveMediaInPrivateGroups;
    public static boolean saveFormatting;
    public static boolean saveReactions;
    public static boolean saveForBots;

    public static boolean markReadAfterSend;
    public static boolean keepAliveService;
    public static boolean disableAds;
    public static boolean localPremium;
    public static boolean regexFiltersEnabled;
    public static boolean regexFiltersInChats;
    public static boolean regexFiltersCaseInsensitive;
    public static boolean showGhostToggleInDrawer;
    public static boolean showKillButtonInDrawer;
    public static boolean syncEnabled;
    public static boolean useSecureConnection;
    public static boolean WALMode;

    // ~ Liquid Glass
    public static boolean liquidGlassEnabled;
    public static boolean liquidGlassApplyToChatBubbles;
    public static boolean liquidGlassApplyToDialogs;
    public static int liquidGlassPreset;
    public static float liquidGlassBlurRadius;
    public static float liquidGlassOpacity;

    // AI / Gemini
    public static boolean geminiEnabled;
    public static String geminiApiKey;
    public static String geminiModel;
    public static boolean turkishSmartTranslate;

    private static String key(String base, long dialogId) {
        return base + "_" + dialogId;
    }

    public static boolean isGeminiAllowedForDialog(long dialogId) {
        return preferences.getBoolean(key("geminiChatEnabled", dialogId), true);
    }

    public static void setGeminiAllowedForDialog(long dialogId, boolean enabled) {
        preferences.edit().putBoolean(key("geminiChatEnabled", dialogId), enabled).apply();
    }

    public static boolean isTurkishTranslateForDialog(long dialogId) {
        return preferences.getBoolean(key("turkishSmartTranslateChat", dialogId), turkishSmartTranslate);
    }

    public static void setTurkishTranslateForDialog(long dialogId, boolean enabled) {
        preferences.edit().putBoolean(key("turkishSmartTranslateChat", dialogId), enabled).apply();
    }

    private static boolean configLoaded;

    static {
        loadConfig();
    }

    public static void loadConfig() {
        synchronized (sync) {
            if (configLoaded) {
                return;
            }

            preferences = ApplicationLoader.applicationContext.getSharedPreferences("ayuconfig", Activity.MODE_PRIVATE);
            editor = preferences.edit();

            // ~ Ghost essentials
            sendReadPackets = preferences.getBoolean("sendReadPackets", true);
            sendOnlinePackets = preferences.getBoolean("sendOnlinePackets", true);
            sendUploadProgress = preferences.getBoolean("sendUploadProgress", true);
            sendOfflinePacketAfterOnline = preferences.getBoolean("sendOfflinePacketAfterOnline", false);

            markReadAfterSend = preferences.getBoolean("markReadAfterSend", true);
            useScheduledMessages = preferences.getBoolean("useScheduledMessages", false);

            // ~ Message edits & deletion history
            saveDeletedMessages = preferences.getBoolean("saveDeletedMessages", true);
            saveMessagesHistory = preferences.getBoolean("saveMessagesHistory", true);

            // ~ Message saving preferences
            saveMedia = preferences.getBoolean("saveMedia", true);
            saveMediaInPrivateChats = preferences.getBoolean("saveMediaInPrivateChats", true);
            saveMediaInPublicChannels = preferences.getBoolean("saveMediaInPublicChannels", false);
            saveMediaInPrivateChannels = preferences.getBoolean("saveMediaInPrivateChannels", true);
            saveMediaInPublicGroups = preferences.getBoolean("saveMediaInPublicGroups", false);
            saveMediaInPrivateGroups = preferences.getBoolean("saveMediaInPrivateGroups", true);
            saveForBots = preferences.getBoolean("saveForBots", true);

            saveFormatting = preferences.getBoolean("saveFormatting", true);
            saveReactions = preferences.getBoolean("saveReactions", true);

            // ~ Useful features
            keepAliveService = preferences.getBoolean("keepAliveService", true);
            disableAds = preferences.getBoolean("disableAds", true);
            localPremium = preferences.getBoolean("localPremium", false);
            regexFiltersEnabled = preferences.getBoolean("regexFiltersEnabled", false);
            regexFiltersInChats = preferences.getBoolean("regexFiltersInChats", false);
            regexFiltersCaseInsensitive = preferences.getBoolean("regexFiltersCaseInsensitive", true);
            // regexFilters

            // ~ Customization
            // deletedMarkText
            // editedMarkText
            showGhostToggleInDrawer = preferences.getBoolean("showGhostToggleInDrawer", true);
            showKillButtonInDrawer = preferences.getBoolean("showKillButtonInDrawer", false);

            // ~ OvergramSync
            // syncServerURL
            // syncServerToken
            syncEnabled = preferences.getBoolean("syncEnabled", false);
            useSecureConnection = preferences.getBoolean("useSecureConnection", !BuildVars.isBetaApp());

            // ~ Debug
            WALMode = preferences.getBoolean("walMode", true);

            // ~ Liquid Glass
            liquidGlassEnabled = preferences.getBoolean("liquidGlassEnabled", false);
            liquidGlassApplyToChatBubbles = preferences.getBoolean("liquidGlassApplyToChatBubbles", true);
            liquidGlassApplyToDialogs = preferences.getBoolean("liquidGlassApplyToDialogs", false);
            liquidGlassPreset = preferences.getInt("liquidGlassPreset", 1); // Default: STANDARD
            liquidGlassBlurRadius = preferences.getFloat("liquidGlassBlurRadius", 15f);
            liquidGlassOpacity = preferences.getFloat("liquidGlassOpacity", 0.75f);

            // AI
            geminiEnabled = preferences.getBoolean("geminiEnabled", false);
            geminiApiKey = preferences.getString("geminiApiKey", "");
            geminiModel = preferences.getString("geminiModel", "gemini-2.5-flash");
            turkishSmartTranslate = preferences.getBoolean("turkishSmartTranslate", false);

            configLoaded = true;
        }
    }

    public static boolean isGhostModeActive() {
        return !sendReadPackets && !sendOnlinePackets && !sendUploadProgress && sendOfflinePacketAfterOnline;
    }

    public static void setGhostMode(boolean enabled) {
        sendReadPackets = !enabled;
        sendOnlinePackets = !enabled;
        sendUploadProgress = !enabled;
        sendOfflinePacketAfterOnline = enabled;

        OverConfig.editor.putBoolean("sendReadPackets", OverConfig.sendReadPackets).apply();
        OverConfig.editor.putBoolean("sendOnlinePackets", OverConfig.sendOnlinePackets).apply();
        OverConfig.editor.putBoolean("sendUploadProgress", OverConfig.sendUploadProgress).apply();
        OverConfig.editor.putBoolean("sendOfflinePacketAfterOnline", OverConfig.sendOfflinePacketAfterOnline).apply();
    }

    public static void toggleGhostMode() {
        // giga move
        setGhostMode(!isGhostModeActive());
    }

    public static boolean saveDeletedMessageFor(int accountId, long dialogId) {
        if (!OverConfig.saveDeletedMessages) {
            return false;
        }

        var user = MessagesController.getInstance(accountId).getUser(Math.abs(dialogId));
        if (user == null) {
            return true;
        }

        return !user.bot || OverConfig.saveForBots;
    }

    public static boolean saveEditedMessageFor(int accountId, long dialogId) {
        if (!OverConfig.saveMessagesHistory) {
            return false;
        }

        var user = MessagesController.getInstance(accountId).getUser(Math.abs(dialogId));
        if (user == null) {
            return true;
        }

        return !user.bot || OverConfig.saveForBots;
    }

    public static String getDeletedMark() {
        return OverConfig.preferences.getString("deletedMarkText", OverConstants.DEFAULT_DELETED_MARK);
    }

    public static String getEditedMark() {
        return OverConfig.preferences.getString("editedMarkText", LocaleController.getString("EditedMessage", R.string.EditedMessage));
    }

    public static String getWALMode() {
        return OverConfig.WALMode ? "WAL" : "OFF";
    }

    public static String getSyncServerURL() {
        return preferences.getString("syncServerURL", OverConstants.DEFAULT_AYUSYNC_SERVER);
    }

    public static String getSyncServerToken() {
        return preferences.getString("syncServerToken", "");
    }

    public static ArrayList<String> getRegexFilters() {
        var str = preferences.getString("regexFilters", "[]");
        var arr = new Gson().fromJson(str, String[].class);

        return new ArrayList<>(Arrays.asList(arr));
    }

    public static void addFilter(String text) {
        var list = getRegexFilters();
        list.add(0, text);

        var str = new Gson().toJson(list);
        editor.putString("regexFilters", str).apply();

        OverFilter.rebuildCache();
    }

    public static void editFilter(int filterIdx, String text) {
        var list = getRegexFilters();
        list.set(filterIdx, text);

        var str = new Gson().toJson(list);
        editor.putString("regexFilters", str).apply();

        OverFilter.rebuildCache();
    }

    public static void removeFilter(int filterIdx) {
        var list = getRegexFilters();
        list.remove(filterIdx);

        var str = new Gson().toJson(list);
        editor.putString("regexFilters", str).apply();

        OverFilter.rebuildCache();
    }
}
