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

import java.util.ArrayList;

public class SyncBatch implements SyncEvent {
    public String type = "sync_batch";
    public long userId;
    public SyncBatch.SyncBatchArgs args;

    public static class SyncBatchArgs {
        public ArrayList<SyncEvent> events = new ArrayList<>();
    }
}
