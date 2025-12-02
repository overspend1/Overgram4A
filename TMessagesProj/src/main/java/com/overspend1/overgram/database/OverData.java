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

import androidx.room.Room;
import com.overspend1.overgram.OverConstants;
import com.overspend1.overgram.database.dao.DeletedMessageDao;
import com.overspend1.overgram.database.dao.EditedMessageDao;
import org.telegram.messenger.ApplicationLoader;

public class OverData {
    private static OverDatabase database;
    private static EditedMessageDao editedMessageDao;
    private static DeletedMessageDao deletedMessageDao;

    static {
        create();
    }

    public static void create() {
        database = Room.databaseBuilder(ApplicationLoader.applicationContext, OverDatabase.class, OverConstants.AYU_DATABASE)
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

        editedMessageDao = database.editedMessageDao();
        deletedMessageDao = database.deletedMessageDao();
    }

    public static OverDatabase getDatabase() {
        return database;
    }

    public static EditedMessageDao getEditedMessageDao() {
        return editedMessageDao;
    }

    public static DeletedMessageDao getDeletedMessageDao() {
        return deletedMessageDao;
    }

    public static void clean() {
        database.close();

        ApplicationLoader.applicationContext.deleteDatabase(OverConstants.AYU_DATABASE);
    }
}
