/*
 * This is the source code of Overgram for Android.
 *
 * We do not and cannot prevent the use of our code,
 * but be respectful and credit the original author.
 *
 * Copyright @overspend1, 2024
 */

package com.overspend1.overgram.ui.preferences;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.exteragram.messenger.preferences.BasePreferencesActivity;
import com.overspend1.overgram.OverConfig;
import com.overspend1.overgram.ui.liquidglass.GlassParameters;
import com.overspend1.overgram.ui.liquidglass.LiquidGlassPreset;
import com.overspend1.overgram.ui.liquidglass.LiquidGlassEffect;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.*;

public class LiquidGlassPreferencesActivity extends BasePreferencesActivity {

    private int headerRow;
    private int enableRow;
    private int enableHintRow;
    private int divider1Row;

    private int applyToChatBubblesRow;
    private int applyToDialogsRow;
    private int applyToSystemRow;
    private int divider2Row;

    private int presetRow;
    private int blurRadiusRow;
    private int opacityRow;
    private int divider3Row;

    private int performanceRow;
    private int performanceHintRow;

    @Override
    protected void updateRowsId() {
        super.updateRowsId();

        headerRow = newRow();
        enableRow = newRow();
        enableHintRow = newRow();
        divider1Row = newRow();

        if (OverConfig.liquidGlassEnabled) {
            applyToChatBubblesRow = newRow();
            applyToDialogsRow = newRow();
            applyToSystemRow = newRow();
            divider2Row = newRow();

            presetRow = newRow();
            blurRadiusRow = newRow();
            opacityRow = newRow();
            divider3Row = newRow();

            performanceRow = newRow();
            performanceHintRow = newRow();
        } else {
            applyToChatBubblesRow = -1;
            applyToDialogsRow = -1;
            applyToSystemRow = -1;
            divider2Row = -1;
            presetRow = -1;
            blurRadiusRow = -1;
            opacityRow = -1;
            divider3Row = -1;
            performanceRow = -1;
            performanceHintRow = -1;
        }
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        if (position == enableRow) {
            OverConfig.liquidGlassEnabled ^= true;
            OverConfig.editor.putBoolean("liquidGlassEnabled", OverConfig.liquidGlassEnabled).apply();
            ((TextCheckCell) view).setChecked(OverConfig.liquidGlassEnabled);

            updateRowsId();
            listAdapter.notifyDataSetChanged();
        } else if (position == applyToChatBubblesRow) {
            OverConfig.liquidGlassApplyToChatBubbles ^= true;
            OverConfig.editor.putBoolean("liquidGlassApplyToChatBubbles", OverConfig.liquidGlassApplyToChatBubbles).apply();
            ((TextCheckCell) view).setChecked(OverConfig.liquidGlassApplyToChatBubbles);
        } else if (position == applyToDialogsRow) {
            OverConfig.liquidGlassApplyToDialogs ^= true;
            OverConfig.editor.putBoolean("liquidGlassApplyToDialogs", OverConfig.liquidGlassApplyToDialogs).apply();
            ((TextCheckCell) view).setChecked(OverConfig.liquidGlassApplyToDialogs);
        } else if (position == applyToSystemRow) {
            OverConfig.liquidGlassApplyToSystemSurfaces ^= true;
            OverConfig.editor.putBoolean("liquidGlassApplyToSystemSurfaces", OverConfig.liquidGlassApplyToSystemSurfaces).apply();
            ((TextCheckCell) view).setChecked(OverConfig.liquidGlassApplyToSystemSurfaces);
        } else if (position == presetRow) {
            showPresetSelector();
        } else if (position == blurRadiusRow) {
            showSlider(
                LocaleController.getString(R.string.LiquidGlassBlurRadius),
                0, 25, (int) OverConfig.liquidGlassBlurRadius,
                value -> {
                    OverConfig.liquidGlassBlurRadius = value;
                    OverConfig.editor.putFloat("liquidGlassBlurRadius", value).apply();
                    listAdapter.notifyItemChanged(blurRadiusRow);
                }
            );
        } else if (position == opacityRow) {
            showSlider(
                LocaleController.getString(R.string.LiquidGlassOpacity),
                0, 100, (int) (OverConfig.liquidGlassOpacity * 100),
                value -> {
                    OverConfig.liquidGlassOpacity = value / 100f;
                    OverConfig.editor.putFloat("liquidGlassOpacity", OverConfig.liquidGlassOpacity).apply();
                    listAdapter.notifyItemChanged(opacityRow);
                }
            );
        }
    }

    private GlassParameters buildGlassParameters() {
        LiquidGlassPreset preset = LiquidGlassPreset.fromId(OverConfig.liquidGlassPreset);
        GlassParameters params = preset.toParameters();
        params.blurRadius = OverConfig.liquidGlassBlurRadius;
        params.opacity = OverConfig.liquidGlassOpacity;
        params.clamp();
        return params;
    }

    private void showPresetSelector() {
        LiquidGlassPreset[] presets = LiquidGlassPreset.values();
        String[] names = new String[presets.length];
        for (int i = 0; i < presets.length; i++) {
            names[i] = presets[i].name + " - " + presets[i].getDescription();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(LocaleController.getString(R.string.LiquidGlassPreset));
        builder.setItems(names, (dialog, which) -> {
            LiquidGlassPreset preset = presets[which];
            OverConfig.liquidGlassPreset = preset.id;
            OverConfig.liquidGlassBlurRadius = preset.blurRadius;
            OverConfig.liquidGlassOpacity = preset.opacity;

            OverConfig.editor.putInt("liquidGlassPreset", OverConfig.liquidGlassPreset).apply();
            OverConfig.editor.putFloat("liquidGlassBlurRadius", OverConfig.liquidGlassBlurRadius).apply();
            OverConfig.editor.putFloat("liquidGlassOpacity", OverConfig.liquidGlassOpacity).apply();

            listAdapter.notifyItemChanged(presetRow);
            listAdapter.notifyItemChanged(blurRadiusRow);
            listAdapter.notifyItemChanged(opacityRow);
        });
        showDialog(builder.create());
    }

    private void showSlider(String title, int min, int max, int current, SliderCallback callback) {
        Context context = getParentActivity();
        if (context == null) {
            return;
        }

        int initial = current;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = AndroidUtilities.dp(20);
        container.setPadding(pad, pad, pad, pad);

        TextView valueView = new TextView(context);
        valueView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        valueView.setTextSize(16);
        valueView.setText(String.valueOf(current));
        container.addView(valueView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        SeekBar seekBar = new SeekBar(context);
        seekBar.setMax(max - min);
        seekBar.setProgress(current - min);
        container.addView(seekBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Glass preview
        GlassPreviewView previewView = new GlassPreviewView(context);
        previewView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(120)));
        previewView.setEffect(new LiquidGlassEffect(buildGlassParameters()));
        container.addView(previewView);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = min + progress;
                valueView.setText(String.valueOf((int) value));
                callback.onValueChanged(value);
                // Refresh preview using current config
                if (previewView.getEffect() != null) {
                    previewView.getEffect().setParameters(buildGlassParameters());
                    previewView.invalidate();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        builder.setView(container);
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), (dialog, which) -> {
            // revert if cancelled
            callback.onValueChanged(initial);
            if (previewView.getEffect() != null) {
                previewView.getEffect().setParameters(buildGlassParameters());
                previewView.invalidate();
            }
        });
        showDialog(builder.create());
    }

    interface SliderCallback {
        void onValueChanged(float value);
    }

    @Override
    protected String getTitle() {
        return LocaleController.getString(R.string.LiquidGlassHeader);
    }

    @Override
    protected BaseListAdapter createAdapter(Context context) {
        return new ListAdapter(context);
    }

    private class ListAdapter extends BaseListAdapter {

        public ListAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, boolean payload) {
            switch (holder.getItemViewType()) {
                case 1:
                    holder.itemView.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case 2:
                    TextCell textCell = (TextCell) holder.itemView;
                    if (position == presetRow) {
                        LiquidGlassPreset preset = LiquidGlassPreset.fromId(OverConfig.liquidGlassPreset);
                        textCell.setTextAndValue(
                            LocaleController.getString(R.string.LiquidGlassPreset),
                            preset.name,
                            true
                        );
                    } else if (position == blurRadiusRow) {
                        textCell.setTextAndValue(
                            LocaleController.getString(R.string.LiquidGlassBlurRadius),
                            String.format("%.0f dp", OverConfig.liquidGlassBlurRadius),
                            true
                        );
                    } else if (position == opacityRow) {
                        textCell.setTextAndValue(
                            LocaleController.getString(R.string.LiquidGlassOpacity),
                            String.format("%.0f%%", OverConfig.liquidGlassOpacity * 100),
                            false
                        );
                    }
                    break;
                case 3:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == headerRow) {
                        headerCell.setText(LocaleController.getString(R.string.LiquidGlassHeader));
                    } else if (position == performanceRow) {
                        headerCell.setText(LocaleController.getString(R.string.LiquidGlassPerformance));
                    }
                    break;
                case 4:
                    TextInfoPrivacyCell infoCell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == enableHintRow) {
                        infoCell.setText(LocaleController.getString(R.string.LiquidGlassEnableHint));
                    } else if (position == performanceHintRow) {
                        infoCell.setText(LocaleController.getString(R.string.LiquidGlassPerformanceHint));
                    }
                    break;
                case 5:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    if (position == enableRow) {
                        textCheckCell.setTextAndCheck(
                            LocaleController.getString(R.string.LiquidGlassEnable),
                            OverConfig.liquidGlassEnabled,
                            false
                        );
                    } else if (position == applyToChatBubblesRow) {
                        textCheckCell.setTextAndCheck(
                            LocaleController.getString(R.string.LiquidGlassApplyToChatBubbles),
                            OverConfig.liquidGlassApplyToChatBubbles,
                            true
                        );
                    } else if (position == applyToDialogsRow) {
                        textCheckCell.setTextAndCheck(
                            LocaleController.getString(R.string.LiquidGlassApplyToDialogs),
                            OverConfig.liquidGlassApplyToDialogs,
                            false
                        );
                    } else if (position == applyToSystemRow) {
                        textCheckCell.setTextAndCheck(
                            LocaleController.getString(R.string.LiquidGlassApplyToSystem),
                            OverConfig.liquidGlassApplyToSystemSurfaces,
                            false
                        );
                    }
                    break;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == divider1Row || position == divider2Row || position == divider3Row) {
                return 1;
            } else if (position == presetRow || position == blurRadiusRow || position == opacityRow) {
                return 2;
            } else if (position == headerRow || position == performanceRow) {
                return 3;
            } else if (position == enableHintRow || position == performanceHintRow) {
                return 4;
            }
            return 5;
        }
    }

    /**
     * Small preview surface that renders the current glass parameters.
     */
    private static class GlassPreviewView extends View {
        private LiquidGlassEffect effect;
        private Bitmap background;
        private final RectF rect = new RectF();
        private final Paint bgPaint = new Paint();

        public GlassPreviewView(Context context) {
            super(context);
            setWillNotDraw(false);
        }

        public void setEffect(LiquidGlassEffect effect) {
            this.effect = effect;
            invalidate();
        }

        public LiquidGlassEffect getEffect() {
            return effect;
        }

        private void ensureBackground() {
            int w = Math.max(1, getWidth());
            int h = Math.max(1, getHeight());
            if (background != null && !background.isRecycled() && background.getWidth() == w && background.getHeight() == h) {
                return;
            }
            if (background != null && !background.isRecycled()) {
                background.recycle();
            }
            background = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(background);
            int topColor = Theme.getColor(Theme.key_windowBackgroundWhite);
            int bottomColor = Theme.getColor(Theme.key_actionBarDefault);
            LinearGradient gradient = new LinearGradient(0, 0, w, h, topColor, bottomColor, Shader.TileMode.CLAMP);
            bgPaint.setShader(gradient);
            canvas.drawRect(0, 0, w, h, bgPaint);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            ensureBackground();
            if (effect != null && background != null && !background.isRecycled()) {
                rect.set(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
                effect.apply(canvas, rect, background);
            }
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            if (background != null && !background.isRecycled()) {
                background.recycle();
            }
            background = null;
        }
    }
}
