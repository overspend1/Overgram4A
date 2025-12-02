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

import android.util.Log;
import com.overspend1.overgram.OverConstants;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;

public class OverSyncState {
    private static int lastSent;
    private static int lastReceived;
    private static OverSyncConnectionState connectionState = OverSyncConnectionState.NotRegistered;
    private static int registerStatusCode;

    public static int getLastSent() {
        return lastSent;
    }

    public static void setLastSent(int lastSent) {
        OverSyncState.lastSent = lastSent;

        AndroidUtilities.runOnUIThread(() -> NotificationCenter.getGlobalInstance().postNotificationName(OverConstants.AYUSYNC_LAST_SENT_CHANGED));
    }

    public static int getLastReceived() {
        return lastReceived;
    }

    public static void setLastReceived(int lastReceived) {
        OverSyncState.lastReceived = lastReceived;

        AndroidUtilities.runOnUIThread(() -> NotificationCenter.getGlobalInstance().postNotificationName(OverConstants.AYUSYNC_LAST_RECEIVED_CHANGED));
    }

    public static OverSyncConnectionState getConnectionState() {
        return connectionState;
    }

    public static void setConnectionState(OverSyncConnectionState connectionState) {
        Log.d("OvergramSync", "setConnectionState: " + connectionState);
        OverSyncState.connectionState = connectionState;

        AndroidUtilities.runOnUIThread(() -> NotificationCenter.getGlobalInstance().postNotificationName(OverConstants.AYUSYNC_STATE_CHANGED));
    }

    public static String getConnectionStateString() {
        String status;
        switch (getConnectionState()) {
            case Connected:
                status = LocaleController.getString(R.string.AyuSyncStatusOk);
                break;
            case Disconnected:
                status = LocaleController.getString(R.string.AyuSyncStatusErrorDisconnected);
                break;
            case NotRegistered:
                status = LocaleController.getString(R.string.AyuSyncStatusErrorNotRegistered);
                break;
            case NoToken:
                status = LocaleController.getString(R.string.AyuSyncStatusErrorNoToken);
                break;
            case InvalidToken:
                status = LocaleController.getString(R.string.AyuSyncStatusErrorInvalidToken);
                break;
            case NoMVP:
                status = LocaleController.getString(R.string.AyuSyncStatusErrorNoMVP);
                break;
            default:
                status = "unknown";
                break;
        }

        return status;
    }

    public static int getRegisterStatusCode() {
        return registerStatusCode;
    }

    public static void setRegisterStatusCode(int registerStatusCode) {
        OverSyncState.registerStatusCode = registerStatusCode;

        AndroidUtilities.runOnUIThread(() -> NotificationCenter.getGlobalInstance().postNotificationName(OverConstants.AYUSYNC_REGISTER_STATUS_CODE_CHANGED));
    }
}
