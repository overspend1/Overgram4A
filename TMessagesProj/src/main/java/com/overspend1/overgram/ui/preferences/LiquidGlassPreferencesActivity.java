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
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.exteragram.messenger.preferences.BasePreferencesActivity;
import com.overspend1.overgram.OverConfig;
import com.overspend1.overgram.ui.liquidglass.LiquidGlassPreset;
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
            if (OverConfig.liquidGlassEnabled) {
                listAdapter.notifyItemRangeInserted(divider1Row + 1, 11);
            } else {
                listAdapter.notifyItemRangeRemoved(divider1Row + 1, 11);
            }
        } else if (position == applyToChatBubblesRow) {
            OverConfig.liquidGlassApplyToChatBubbles ^= true;
            OverConfig.editor.putBoolean("liquidGlassApplyToChatBubbles", OverConfig.liquidGlassApplyToChatBubbles).apply();
            ((TextCheckCell) view).setChecked(OverConfig.liquidGlassApplyToChatBubbles);
        } else if (position == applyToDialogsRow) {
            OverConfig.liquidGlassApplyToDialogs ^= true;
            OverConfig.editor.putBoolean("liquidGlassApplyToDialogs", OverConfig.liquidGlassApplyToDialogs).apply();
            ((TextCheckCell) view).setChecked(OverConfig.liquidGlassApplyToDialogs);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(title);

        // Simple implementation - can be enhanced with actual slider view
        builder.setMessage("Current value: " + current + "\nUse custom slider implementation here");
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
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
}
