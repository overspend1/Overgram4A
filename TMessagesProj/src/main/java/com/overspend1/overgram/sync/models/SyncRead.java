/*
 * This is the source code of Overgram for Android.
 *
 * We do not and cannot prevent the use of our code,
 * but be respectful and credit the original author.
 *
 * Copyright @overspend1, 2024
 * Based on original AyuGram code by @Radolyn, 2023
 */

package com.overspend1.overgram.sync.models;

public class SyncRead implements SyncEvent {
    public String type = "sync_read";
    public long userId;
    public SyncReadArgs args;

    public static class SyncReadArgs {
        public long dialogId;
        public int untilId;
        public int unread;
    }
}
