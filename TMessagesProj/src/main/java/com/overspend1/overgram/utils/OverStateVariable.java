/*
 * This is the source code of Overgram for Android.
 *
 * We do not and cannot prevent the use of our code,
 * but be respectful and credit the original author.
 *
 * Copyright @overspend1, 2024
 * Based on original AyuGram code by @Radolyn, 2023
 */

package com.overspend1.overgram.utils;

public class OverStateVariable {
    private final Object sync = new Object();

    public boolean val;
    public int resetAfter;

    public boolean process() {
        synchronized (sync) {
            if (resetAfter == -1) {
                return val;
            }

            resetAfter -= 1;
            var currentVal = val;

            if (resetAfter == 0) {
                val = false;
            }

            return currentVal;
        }
    }
}
