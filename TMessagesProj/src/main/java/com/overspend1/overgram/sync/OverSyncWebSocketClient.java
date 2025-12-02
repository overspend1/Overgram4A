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

import com.google.android.exoplayer2.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.overspend1.overgram.OverConfig;
import com.overspend1.overgram.OverUtils;
import dev.gustavoavila.websocketclient.WebSocketClient;
import dev.gustavoavila.websocketclient.exceptions.InvalidServerHandshakeException;

import java.net.URI;
import java.net.URISyntaxException;

public class OverSyncWebSocketClient extends WebSocketClient {

    private static OverSyncWebSocketClient instance;

    private OverSyncWebSocketClient(URI uri) {
        super(uri);
    }

    public static boolean create() {
        if (instance != null) {
            return true;
        }

        URI url;
        try {
            url = new URI(OverSyncConfig.getWebSocketURL());
        } catch (URISyntaxException e) {
            return false;
        }

        Log.d("OvergramSync", "Creating new WebSocket client");

        instance = new OverSyncWebSocketClient(url);

        instance.setConnectTimeout(5000);
        instance.setReadTimeout(60000);
        instance.addHeader("X-APP-PACKAGE", OverUtils.getPackageName());
        instance.addHeader("X-DEVICE-IDENTIFIER", OverUtils.getDeviceIdentifier());
        instance.addHeader("Authorization", OverSyncConfig.getToken());
        instance.enableAutomaticReconnection(1500);
        instance.connect();

        return true;
    }

    public static OverSyncWebSocketClient getInstance() {
        if (instance == null) {
            create();
        }

        return instance;
    }

    public static void nullifyInstance() {
        if (instance == null) {
            return;
        }

        OverSyncState.setConnectionState(OverSyncConnectionState.Disconnected);

        try {
            // crashed once with "java.lang.IllegalStateException: Timer already cancelled."
            instance.close(200, 0, "nullified");
        } catch (Exception e) {
            Log.e("OvergramSync", "Error while closing WebSocket", e);
        }

        instance = null;
    }

    @Override
    public void send(String message) {
        try {
            super.send(message);

            OverSyncState.setLastSent((int) (System.currentTimeMillis() / 1000));
        } catch (Exception e) {
            Log.e("OvergramSync", "Error while sending message", e);
        }
    }

    @Override
    public void onOpen() {
        OverSyncState.setConnectionState(OverSyncConnectionState.Connected);

        Log.d("OvergramSync", "Connected to the origin");
    }

    @Override
    public void onTextReceived(String message) {
        OverSyncState.setLastReceived((int) (System.currentTimeMillis() / 1000));

        try {
            var response = new Gson().fromJson(message, JsonObject.class);
            OverSyncController.getInstance().invokeHandler(response);
        } catch (Exception e) {
            Log.e("OvergramSync", "Error while invoking handler", e);
        }
    }

    @Override
    public void onBinaryReceived(byte[] data) {
        Log.d("OvergramSync", "binary received");
    }

    @Override
    public void onPingReceived(byte[] data) {
//        Log.d("OvergramSync", "ping!");
    }

    @Override
    public void onPongReceived(byte[] data) {
//        Log.d("OvergramSync", "pong!");
    }

    @Override
    public void onException(Exception e) {
        OverSyncState.setConnectionState(OverSyncConnectionState.Disconnected);

        Log.e("OvergramSync", e.toString());

        if ((e instanceof InvalidServerHandshakeException) && OverConfig.syncEnabled && instance == this) {
            // this fucking library doesn't support any other exception except `IOException`
            // so we have to reinitialize instance

            // using reflection call doesn't work

            OverSyncController.nullifyInstance();

            try {
                Thread.sleep(1500);
            } catch (Exception e2) {
                Log.d("OvergramSync", "jaBBa", e2);
            }

            OverSyncController.create();
        }
    }

    @Override
    public void onCloseReceived(int reason, String description) {
        OverSyncState.setConnectionState(OverSyncConnectionState.Disconnected);

        Log.d("OvergramSync", "Disconnected from the origin: " + description);
    }
}
