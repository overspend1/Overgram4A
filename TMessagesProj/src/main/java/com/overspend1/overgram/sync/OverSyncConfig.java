/*
 * This is the source code of Overgram for Android.
 *
 * We do not and cannot prevent the use of our code,
 * but be respectful and credit the original author.
 *
 * Copyright @overspend1, 2024
 * Based on original AyuGram code by @Radolyn, 2023
 */

package com.overspend1.overgram.sync;

import com.overspend1.overgram.OverConfig;

public class OverSyncConfig {
    private static String getWebSocketProtocol() {
        return OverConfig.useSecureConnection ? "wss://" : "ws://";
    }

    private static String getHTTPProtocol() {
        return OverConfig.useSecureConnection ? "https://" : "http://";
    }

    public static String getWebSocketURL() {
        return getWebSocketProtocol() + OverConfig.getSyncServerURL() + "/sync/ws/v1";
    }

    public static String getUserDataURL() {
        return getHTTPProtocol() + OverConfig.getSyncServerURL() + "/user/v1";
    }

    public static String getRegisterDeviceURL() {
        return getHTTPProtocol() + OverConfig.getSyncServerURL() + "/sync/register/v1";
    }

    public static String getForceSyncURL() {
        return getHTTPProtocol() + OverConfig.getSyncServerURL() + "/sync/force/v1";
    }

    public static String getToken() {
        return OverConfig.getSyncServerToken();
    }

    public static String getProfileURL() {
        return getHTTPProtocol() + OverConfig.getSyncServerURL() + "/ui/profile?token=" + OverConfig.getSyncServerToken();
    }
}
