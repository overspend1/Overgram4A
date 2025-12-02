/*
 * This is the source code of Overgram for Android.
 *
 * We do not and cannot prevent the use of our code,
 * but be respectful and credit the original author.
 *
 * Copyright @overspend1, 2024
 * Based on original AyuGram code by @Radolyn, 2023
 */

package com.overspend1.overgram.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.overspend1.overgram.database.dao.DeletedMessageDao;
import com.overspend1.overgram.database.dao.EditedMessageDao;
import com.overspend1.overgram.database.entities.DeletedMessage;
import com.overspend1.overgram.database.entities.DeletedMessageReaction;
import com.overspend1.overgram.database.entities.EditedMessage;

@Database(entities = {
        EditedMessage.class,
        DeletedMessage.class,
        DeletedMessageReaction.class
}, version = 21)
public abstract class OverDatabase extends RoomDatabase {
    public abstract EditedMessageDao editedMessageDao();

    public abstract DeletedMessageDao deletedMessageDao();
}