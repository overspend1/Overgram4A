/*
 * This is the source code of Overgram for Android.
 *
 * We do not and cannot prevent the use of our code,
 * but be respectful and credit the original author.
 *
 * Copyright @overspend1, 2024
 */

package com.overspend1.overgram.ui.components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.overspend1.overgram.OverConfig;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Smart quick replies suggestion bar that shows contextual short replies
 */
public class SmartQuickRepliesView extends FrameLayout {

    private HorizontalScrollView scrollView;
    private LinearLayout repliesContainer;
    private ValueAnimator heightAnimator;
    private boolean isVisible = false;
    private OnReplySelectedListener listener;

    private static final int REPLY_HEIGHT_DP = 36;
    private static final int CONTAINER_PADDING_DP = 8;

    // Contextual reply suggestions based on common patterns
    private static final String[][] REPLY_SETS = {
        // General acknowledgments
        {"👍", "👌", "✅", "Thanks!", "OK", "Got it"},
        // Affirmative responses
        {"Yes", "Sure", "Absolutely", "Of course", "Definitely"},
        // Negative responses
        {"No", "Not really", "Maybe later", "I don't think so"},
        // Time-related
        {"Later", "Tomorrow", "Soon", "Give me a minute", "On my way"},
        // Questions
        {"What?", "When?", "Where?", "Why?", "How?"},
        // Greetings
        {"Hi!", "Hello!", "Hey!", "Good morning", "Good evening"},
        // Farewells
        {"Bye!", "See you!", "Talk later", "Good night", "Take care"},
        // Reactions
        {"😂", "😊", "😍", "🤔", "😢", "😡"}
    };

    public interface OnReplySelectedListener {
        void onReplySelected(String reply);
    }

    public SmartQuickRepliesView(Context context) {
        super(context);
        init();
    }

    private void init() {
        setVisibility(GONE);
        setAlpha(0f);

        // Create horizontal scroll view
        scrollView = new HorizontalScrollView(getContext());
        scrollView.setHorizontalScrollBarEnabled(false);
        scrollView.setOverScrollMode(OVER_SCROLL_NEVER);

        // Create container for reply chips
        repliesContainer = new LinearLayout(getContext());
        repliesContainer.setOrientation(LinearLayout.HORIZONTAL);
        int padding = AndroidUtilities.dp(CONTAINER_PADDING_DP);
        repliesContainer.setPadding(padding, padding / 2, padding, padding / 2);

        scrollView.addView(repliesContainer, new HorizontalScrollView.LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        ));

        addView(scrollView, new FrameLayout.LayoutParams(
            LayoutParams.MATCH_PARENT,
            AndroidUtilities.dp(REPLY_HEIGHT_DP + CONTAINER_PADDING_DP)
        ));

        // Start with a default set
        showDefaultReplies();
    }

    /**
     * Show quick replies with animation
     */
    public void show() {
        if (isVisible) return;

        isVisible = true;
        setVisibility(VISIBLE);

        // Animate height and alpha
        if (heightAnimator != null) {
            heightAnimator.cancel();
        }

        heightAnimator = ValueAnimator.ofFloat(0f, 1f);
        heightAnimator.setDuration(250);
        heightAnimator.setInterpolator(new OvershootInterpolator(0.8f));
        heightAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            setAlpha(value);
            setTranslationY((1f - value) * AndroidUtilities.dp(10));
        });
        heightAnimator.start();
    }

    /**
     * Hide quick replies with animation
     */
    public void hide() {
        if (!isVisible) return;

        isVisible = false;

        if (heightAnimator != null) {
            heightAnimator.cancel();
        }

        heightAnimator = ValueAnimator.ofFloat(1f, 0f);
        heightAnimator.setDuration(200);
        heightAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            setAlpha(value);
            setTranslationY((1f - value) * AndroidUtilities.dp(10));
        });
        heightAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(GONE);
            }
        });
        heightAnimator.start();
    }

    /**
     * Update replies based on the last received message
     */
    public void updateRepliesForMessage(MessageObject messageObject) {
        if (messageObject == null || messageObject.messageOwner == null) {
            showDefaultReplies();
            return;
        }

        String messageText = messageObject.messageOwner.message;
        if (TextUtils.isEmpty(messageText)) {
            showDefaultReplies();
            return;
        }

        String lowerText = messageText.toLowerCase().trim();
        List<String> contextualReplies = new ArrayList<>();

        // Analyze message content and suggest appropriate replies
        if (isQuestion(lowerText)) {
            contextualReplies.addAll(Arrays.asList("Yes", "No", "Maybe", "Not sure", "Let me check"));
        } else if (isGreeting(lowerText)) {
            contextualReplies.addAll(Arrays.asList("Hi!", "Hello!", "Hey there!", "What's up?"));
        } else if (isThanks(lowerText)) {
            contextualReplies.addAll(Arrays.asList("You're welcome!", "No problem!", "Anytime!", "Happy to help!"));
        } else if (isTimeRelated(lowerText)) {
            contextualReplies.addAll(Arrays.asList("Sure", "OK", "Give me a minute", "On my way", "Almost there"));
        } else {
            // Default positive responses
            contextualReplies.addAll(Arrays.asList("👍", "OK", "Got it", "Thanks!", "Sure"));
        }

        // Add some emoji reactions
        contextualReplies.addAll(Arrays.asList("😊", "😂", "👌"));

        showReplies(contextualReplies);
    }

    /**
     * Show default set of quick replies
     */
    public void showDefaultReplies() {
        List<String> defaultReplies = Arrays.asList("👍", "👌", "OK", "Thanks!", "Yes", "No", "😊", "🤔");
        showReplies(defaultReplies);
    }

    /**
     * Display a specific set of replies
     */
    private void showReplies(List<String> replies) {
        repliesContainer.removeAllViews();

        for (String reply : replies) {
            TextView replyChip = createReplyChip(reply);
            repliesContainer.addView(replyChip);
        }
    }

    /**
     * Create a single reply chip view
     */
    private TextView createReplyChip(String text) {
        TextView chip = new TextView(getContext()) {
            private Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private RectF rect = new RectF();

            @Override
            protected void onDraw(Canvas canvas) {
                // Draw rounded background
                backgroundPaint.setColor(Theme.getColor(Theme.key_chat_inBubble));
                int radius = AndroidUtilities.dp(18);
                rect.set(0, 0, getWidth(), getHeight());
                canvas.drawRoundRect(rect, radius, radius, backgroundPaint);

                super.onDraw(canvas);
            }
        };

        chip.setText(text);
        chip.setTextColor(Theme.getColor(Theme.key_chat_messageTextIn));
        chip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        chip.setGravity(Gravity.CENTER);
        chip.setSingleLine();
        chip.setMaxLines(1);
        chip.setEllipsize(TextUtils.TruncateAt.END);

        int paddingH = AndroidUtilities.dp(16);
        int paddingV = AndroidUtilities.dp(8);
        chip.setPadding(paddingH, paddingV, paddingH, paddingV);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LayoutParams.WRAP_CONTENT,
            AndroidUtilities.dp(REPLY_HEIGHT_DP)
        );
        params.rightMargin = AndroidUtilities.dp(8);
        chip.setLayoutParams(params);

        // Add ripple effect
        chip.setBackground(Theme.createSelectorDrawable(
            Theme.getColor(Theme.key_listSelector),
            Theme.RIPPLE_MASK_ROUNDRECT_6DP
        ));

        chip.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReplySelected(text);
            }
        });

        return chip;
    }

    // Message analysis helpers

    private boolean isQuestion(String text) {
        return text.contains("?") ||
               text.startsWith("what") || text.startsWith("when") ||
               text.startsWith("where") || text.startsWith("why") ||
               text.startsWith("how") || text.startsWith("can you") ||
               text.startsWith("could you") || text.startsWith("would you") ||
               text.startsWith("do you") || text.startsWith("are you") ||
               text.startsWith("will you") || text.startsWith("is it");
    }

    private boolean isGreeting(String text) {
        return text.startsWith("hi") || text.startsWith("hello") ||
               text.startsWith("hey") || text.startsWith("good morning") ||
               text.startsWith("good afternoon") || text.startsWith("good evening") ||
               text.contains("how are you") || text.contains("what's up");
    }

    private boolean isThanks(String text) {
        return text.contains("thank") || text.contains("thanks") ||
               text.contains("thx") || text.contains("ty");
    }

    private boolean isTimeRelated(String text) {
        return text.contains("when") || text.contains("time") ||
               text.contains("now") || text.contains("later") ||
               text.contains("tomorrow") || text.contains("today") ||
               text.contains("come") || text.contains("arrive");
    }

    /**
     * Set the listener for reply selection
     */
    public void setOnReplySelectedListener(OnReplySelectedListener listener) {
        this.listener = listener;
    }

    /**
     * Check if currently visible
     */
    public boolean isShowing() {
        return isVisible;
    }
}
