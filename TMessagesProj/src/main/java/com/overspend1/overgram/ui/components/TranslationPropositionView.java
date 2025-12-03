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
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

/**
 * TranslationPropositionView - Fluid animated translation preview
 *
 * Shows original and translated text with smooth slide-up animation
 * Users can accept (checkmark) or cancel (X) the translation
 */
public class TranslationPropositionView extends FrameLayout {

    private LinearLayout contentLayout;
    private TextView originalLabel;
    private TextView originalText;
    private TextView translatedLabel;
    private TextView translatedText;
    private View acceptButton;
    private View cancelButton;
    private TextView acceptIcon;
    private TextView cancelIcon;

    private Paint backgroundPaint;
    private RectF backgroundRect;
    private float animationProgress = 0f;
    private boolean isShowing = false;

    private OnTranslationAcceptListener acceptListener;
    private OnTranslationCancelListener cancelListener;

    private Theme.ResourcesProvider resourcesProvider;

    public interface OnTranslationAcceptListener {
        void onAccept(String translatedText);
    }

    public interface OnTranslationCancelListener {
        void onCancel();
    }

    public TranslationPropositionView(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.resourcesProvider = resourcesProvider;

        setWillNotDraw(false);
        setVisibility(GONE);
        setAlpha(0f);

        // Background paint
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundRect = new RectF();

        // Content container
        contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(
            AndroidUtilities.dp(16),
            AndroidUtilities.dp(12),
            AndroidUtilities.dp(16),
            AndroidUtilities.dp(12)
        );

        // Original text section
        originalLabel = new TextView(context);
        originalLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11);
        originalLabel.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
        originalLabel.setText(LocaleController.getString("OriginalText", R.string.OriginalText).toUpperCase());
        originalLabel.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
        contentLayout.addView(originalLabel, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 4));

        originalText = new TextView(context);
        originalText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        originalText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider));
        originalText.setMaxLines(3);
        originalText.setEllipsize(TextUtils.TruncateAt.END);
        contentLayout.addView(originalText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 12));

        // Translated text section
        translatedLabel = new TextView(context);
        translatedLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11);
        translatedLabel.setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton, resourcesProvider));
        translatedLabel.setText(LocaleController.getString("TranslatedText", R.string.TranslatedText).toUpperCase());
        translatedLabel.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
        contentLayout.addView(translatedLabel, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 4));

        translatedText = new TextView(context);
        translatedText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        translatedText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider));
        translatedText.setMaxLines(3);
        translatedText.setEllipsize(TextUtils.TruncateAt.END);
        translatedText.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
        contentLayout.addView(translatedText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        addView(contentLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP, 48, 0, 48, 0));

        // Action buttons
        acceptButton = new View(context) {
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setColor(Theme.getColor(Theme.key_featuredStickers_addButton, resourcesProvider));
                canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, Math.min(getWidth(), getHeight()) / 2f, paint);
            }
        };
        acceptButton.setOnClickListener(v -> handleAccept());
        addView(acceptButton, LayoutHelper.createFrame(40, 40, Gravity.RIGHT | Gravity.CENTER_VERTICAL, 0, 0, 4, 0));

        acceptIcon = new TextView(context);
        acceptIcon.setText("✓");
        acceptIcon.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        acceptIcon.setTextColor(0xFFFFFFFF);
        acceptIcon.setGravity(Gravity.CENTER);
        acceptIcon.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
        addView(acceptIcon, LayoutHelper.createFrame(40, 40, Gravity.RIGHT | Gravity.CENTER_VERTICAL, 0, 0, 4, 0));

        cancelButton = new View(context) {
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setColor(Theme.getColor(Theme.key_text_RedBold, resourcesProvider));
                canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, Math.min(getWidth(), getHeight()) / 2f, paint);
            }
        };
        cancelButton.setOnClickListener(v -> handleCancel());
        addView(cancelButton, LayoutHelper.createFrame(40, 40, Gravity.LEFT | Gravity.CENTER_VERTICAL, 4, 0, 0, 0));

        cancelIcon = new TextView(context);
        cancelIcon.setText("✕");
        cancelIcon.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        cancelIcon.setTextColor(0xFFFFFFFF);
        cancelIcon.setGravity(Gravity.CENTER);
        cancelIcon.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
        addView(cancelIcon, LayoutHelper.createFrame(40, 40, Gravity.LEFT | Gravity.CENTER_VERTICAL, 4, 0, 0, 0));

        // Add ripple effect to buttons
        acceptButton.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector, resourcesProvider), 3));
        cancelButton.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector, resourcesProvider), 3));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw rounded background
        backgroundPaint.setColor(Theme.getColor(Theme.key_chat_inBubble, resourcesProvider));
        backgroundPaint.setAlpha((int) (255 * animationProgress * 0.95f));

        backgroundRect.set(0, 0, getWidth(), getHeight());
        canvas.drawRoundRect(backgroundRect, AndroidUtilities.dp(12), AndroidUtilities.dp(12), backgroundPaint);
    }

    /**
     * Show translation proposition with fluid animation
     */
    public void showProposition(String original, String translated,
                                OnTranslationAcceptListener acceptListener,
                                OnTranslationCancelListener cancelListener) {
        if (isShowing) {
            return;
        }

        this.acceptListener = acceptListener;
        this.cancelListener = cancelListener;

        // Set text
        originalText.setText(original);
        translatedText.setText(translated);

        // Prepare for animation
        setVisibility(VISIBLE);
        isShowing = true;

        // Slide up + fade in animation
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(400);
        animator.setInterpolator(new OvershootInterpolator(0.8f));
        animator.addUpdateListener(animation -> {
            animationProgress = (float) animation.getAnimatedValue();

            // Slide up
            setTranslationY(AndroidUtilities.dp(20) * (1f - animationProgress));

            // Fade in
            setAlpha(animationProgress);

            // Scale buttons
            acceptButton.setScaleX(animationProgress);
            acceptButton.setScaleY(animationProgress);
            acceptIcon.setScaleX(animationProgress);
            acceptIcon.setScaleY(animationProgress);
            cancelButton.setScaleX(animationProgress);
            cancelButton.setScaleY(animationProgress);
            cancelIcon.setScaleX(animationProgress);
            cancelIcon.setScaleY(animationProgress);

            invalidate();
        });
        animator.start();

        // Add subtle bounce to text
        animateTextEntrance(originalText, 100);
        animateTextEntrance(translatedText, 200);
    }

    /**
     * Hide with fluid animation
     */
    public void hideProposition() {
        if (!isShowing) {
            return;
        }

        ValueAnimator animator = ValueAnimator.ofFloat(1f, 0f);
        animator.setDuration(300);
        animator.addUpdateListener(animation -> {
            animationProgress = (float) animation.getAnimatedValue();

            // Slide down
            setTranslationY(AndroidUtilities.dp(20) * (1f - animationProgress));

            // Fade out
            setAlpha(animationProgress);

            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(GONE);
                isShowing = false;
            }
        });
        animator.start();
    }

    private void animateTextEntrance(TextView textView, long delay) {
        textView.setAlpha(0f);
        textView.setTranslationY(AndroidUtilities.dp(10));

        textView.animate()
            .alpha(1f)
            .translationY(0)
            .setDuration(300)
            .setStartDelay(delay)
            .setInterpolator(new OvershootInterpolator(0.8f))
            .start();
    }

    private void handleAccept() {
        // Pulse animation
        acceptButton.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(100)
            .withEndAction(() -> {
                acceptButton.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start();
            })
            .start();

        acceptIcon.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(100)
            .withEndAction(() -> {
                acceptIcon.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start();
            })
            .start();

        // Callback and hide
        AndroidUtilities.runOnUIThread(() -> {
            if (acceptListener != null) {
                acceptListener.onAccept(translatedText.getText().toString());
            }
            hideProposition();
        }, 150);
    }

    private void handleCancel() {
        // Pulse animation
        cancelButton.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(100)
            .withEndAction(() -> {
                cancelButton.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start();
            })
            .start();

        cancelIcon.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(100)
            .withEndAction(() -> {
                cancelIcon.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start();
            })
            .start();

        // Callback and hide
        AndroidUtilities.runOnUIThread(() -> {
            if (cancelListener != null) {
                cancelListener.onCancel();
            }
            hideProposition();
        }, 150);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Consume all touch events to prevent click-through
        return true;
    }

    public boolean isShowing() {
        return isShowing;
    }
}
