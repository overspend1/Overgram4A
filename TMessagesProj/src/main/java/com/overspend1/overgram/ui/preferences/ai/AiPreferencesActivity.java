/*
 * Overgram AI preferences
 */

package com.overspend1.overgram.ui.preferences.ai;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.exteragram.messenger.preferences.BasePreferencesActivity;
import com.overspend1.overgram.OverConfig;
import com.overspend1.overgram.ui.preferences.utils.OverUi;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;

public class AiPreferencesActivity extends BasePreferencesActivity {

    private int headerRow;
    private int enableGeminiRow;
    private int apiKeyRow;
    private int modelRow;
    private int turkishTranslateRow;
    private int dividerRow;

    @Override
    protected void updateRowsId() {
        super.updateRowsId();
        headerRow = newRow();
        enableGeminiRow = newRow();
        apiKeyRow = newRow();
        modelRow = newRow();
        dividerRow = newRow();
        turkishTranslateRow = newRow();
    }

    @Override
    protected String getTitle() {
        return LocaleController.getString(R.string.OvergramAiHeader);
    }

    @Override
    protected BaseListAdapter createAdapter(Context context) {
        return new ListAdapter(context);
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        if (position == enableGeminiRow) {
            OverConfig.editor.putBoolean("geminiEnabled", OverConfig.geminiEnabled ^= true).apply();
            ((TextCheckCell) view).setChecked(OverConfig.geminiEnabled);
        } else if (position == apiKeyRow) {
            OverUi.spawnEditBox(
                getParentActivity(),
                (TextCell) view,
                LocaleController.getString(R.string.OvergramGeminiKeyTitle),
                () -> OverConfig.geminiApiKey,
                "geminiApiKey",
                "",
                s -> OverConfig.geminiApiKey = s
            );
        } else if (position == modelRow) {
            OverUi.spawnEditBox(
                getParentActivity(),
                (TextCell) view,
                LocaleController.getString(R.string.OvergramGeminiModelTitle),
                () -> OverConfig.geminiModel,
                "geminiModel",
                "gemini-2.5-flash",
                s -> OverConfig.geminiModel = s
            );
        } else if (position == turkishTranslateRow) {
            OverConfig.editor.putBoolean("turkishSmartTranslate", OverConfig.turkishSmartTranslate ^= true).apply();
            ((TextCheckCell) view).setChecked(OverConfig.turkishSmartTranslate);
        }
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
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    headerCell.setText(LocaleController.getString(R.string.OvergramAiHeader));
                    break;
                case 3:
                    TextCell textCell = (TextCell) holder.itemView;
                    textCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    if (position == apiKeyRow) {
                        String value = TextUtils.isEmpty(OverConfig.geminiApiKey) ? LocaleController.getString(R.string.OvergramGeminiKeyEmpty) : "••••••";
                        textCell.setTextAndValue(LocaleController.getString(R.string.OvergramGeminiKeyTitle), value, true);
                    } else if (position == modelRow) {
                        textCell.setTextAndValue(LocaleController.getString(R.string.OvergramGeminiModelTitle), OverConfig.geminiModel, true);
                    }
                    break;
                case 4:
                    TextCheckCell checkCell = (TextCheckCell) holder.itemView;
                    if (position == enableGeminiRow) {
                        checkCell.setTextAndCheck(LocaleController.getString(R.string.OvergramGeminiEnable), OverConfig.geminiEnabled, true);
                    } else if (position == turkishTranslateRow) {
                        checkCell.setTextAndCheck(LocaleController.getString(R.string.OvergramTurkishTranslateEnable), OverConfig.turkishSmartTranslate, false);
                    }
                    break;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == headerRow) {
                return 2;
            } else if (position == dividerRow) {
                return 1;
            } else if (position == apiKeyRow || position == modelRow) {
                return 3;
            }
            return 4;
        }
    }
}
