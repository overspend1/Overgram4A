/*
 * This is the source code of Overgram for Android.
 *
 * We do not and cannot prevent the use of our code,
 * but be respectful and credit the original author.
 *
 * Copyright @overspend1, 2024
 * Based on original AyuGram code by @Radolyn, 2023
 */

package com.overspend1.overgram;

import android.text.TextUtils;
import android.util.LongSparseArray;
import com.exteragram.messenger.utils.ChatUtils;
import org.telegram.messenger.MessageObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class OverFilter {
    private static ArrayList<Pattern> patterns;
    private static LongSparseArray<HashMap<Integer, Boolean>> filteredCache;

    public static void rebuildCache() {
        var filters = OverConfig.getRegexFilters();

        var flags = Pattern.MULTILINE;
        if (OverConfig.regexFiltersCaseInsensitive) {
            flags |= Pattern.CASE_INSENSITIVE;
        }

        patterns = new ArrayList<>();
        for (var filter : filters) {
            patterns.add(Pattern.compile(filter, flags));
        }

        filteredCache = new LongSparseArray<>();
    }

    private static boolean isFiltered(CharSequence text) {
        if (!OverConfig.regexFiltersEnabled) {
            return false;
        }

        if (TextUtils.isEmpty(text)) {
            return false;
        }

        for (var pattern : patterns) {
            if (pattern.matcher(text).find()) {
                return true;
            }
        }

        return false;
    }

    public static boolean isFiltered(MessageObject msg, MessageObject.GroupedMessages group) {
        if (!OverConfig.regexFiltersEnabled) {
            return false;
        }

        if (msg == null) {
            return false;
        }

        if (patterns == null) {
            rebuildCache();
        }

        Boolean res;

        var cached = filteredCache.get(msg.getDialogId());
        if (cached != null) {
            res = cached.get(msg.getId());
            if (res != null) {
                return res;
            }
        }

        res = isFiltered(ChatUtils.getMessageText(msg, group));

        if (cached == null) {
            cached = new HashMap<>();
            filteredCache.put(msg.getDialogId(), cached);
        }

        cached.put(msg.getId(), res);

        if (group != null && group.messages != null && !group.messages.isEmpty()) {
            for (var m : group.messages) {
                cached.put(m.getId(), res);
            }
        }

        return res;
    }
}
