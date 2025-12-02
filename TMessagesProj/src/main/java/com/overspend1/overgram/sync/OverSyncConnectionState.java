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

public enum OverSyncConnectionState {
    Connected,
    Disconnected,
    NotRegistered,
    NoToken,
    InvalidToken,
    NoMVP
}
