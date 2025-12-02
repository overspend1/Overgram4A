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

public class SyncForceFinish implements SyncEvent {
    public String type = "sync_force_finish";
    public long userId;
    public SyncForceFinish.SyncForceFinishArgs args;

    public static class SyncForceFinishArgs {
    }
}
