/*
 * This is the source code of Overgram for Android.
 *
 * We do not and cannot prevent the use of our code,
 * but be respectful and credit the original author.
 *
 * Copyright @overspend1, 2024
 * Based on original AyuGram code by @Radolyn, 2023
 */

package com.overspend1.overgram.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity()
public class DeletedMessageReaction {
    @PrimaryKey(autoGenerate = true)
    public long fakeReactionId;

    public long deletedMessageId;

    public String emoticon;
    public long documentId;
    public boolean isCustom;

    public int count;
    public boolean selfSelected;
}
