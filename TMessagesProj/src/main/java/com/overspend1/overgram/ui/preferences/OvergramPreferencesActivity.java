/*
 * This is the source code of Overgram for Android.
 *
 * We do not and cannot prevent the use of our code,
 * but be respectful and credit the original author.
 *
 * Copyright @overspend1, 2024
 * Based on original AyuGram code by @Radolyn, 2023
 */

package com.overspend1.overgram.ui.preferences;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.exteragram.messenger.preferences.BasePreferencesActivity;
import com.overspend1.overgram.OverConfig;
import com.overspend1.overgram.OverConstants;
import com.overspend1.overgram.messages.OverMessagesController;
import com.overspend1.overgram.sync.OverSyncState;
import com.overspend1.overgram.ui.preferences.ai.AiPreferencesActivity;
import com.overspend1.overgram.ui.preferences.utils.OverUi;
import com.overspend1.overgram.utils.OverState;
import org.jetbrains.annotations.NotNull;
import org.telegram.messenger.*;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.*;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.RecyclerListView;

import java.util.Locale;

public class OvergramPreferencesActivity extends BasePreferencesActivity implements NotificationCenter.NotificationCenterDelegate {

    private static final int TOGGLE_BUTTON_VIEW = 1000;

    private int ghostEssentialsHeaderRow;
    private int ghostModeToggleRow;
    private int sendReadPacketsRow;
    private int sendOnlinePacketsRow;
    private int sendUploadProgressRow;
    private int sendOfflinePacketAfterOnlineRow;
    private int markReadAfterSendRow;
    private int useScheduledMessagesRow;
    private int ghostDividerRow;

    private int spyHeaderRow;
    private int saveDeletedMessagesRow;
    private int saveMessagesHistoryRow;
    private int spyDivider1Row;
    private int messageSavingBtnRow;
    private int spyDivider2Row;

    private int qolHeaderRow;
    private int keepAliveServiceRow;
    private int disableAdsRow;
    private int localPremiumRow;
    private int filtersRow;
    private int quickActionsRow;
    private int qolDividerRow;

    private int customizationHeaderRow;
    private int deletedMarkTextRow;
    private int editedMarkTextRow;
    private int showGhostToggleInDrawerRow;
    private int showKillButtonInDrawerRow;
    private int customizationDividerRow;

    private int liquidGlassHeaderRow;
    private int liquidGlassBtnRow;
    private int liquidGlassDividerRow;

    private int aiHeaderRow;
    private int aiSettingsRow;
    private int aiDividerRow;

    private int productivityHeaderRow;
    private int smartRepliesRow;
    private int autoTranslateIncomingRow;
    private int autoTranslateOutgoingRow;
    private int autoTranslateLangRow;
    private int autoTranslateIncomingLangRow;
    private int productivityDividerRow;

    private int yagizTranslatorHeaderRow;
    private int yagizTranslatorEnabledRow;
    private int yagizTranslatorApiTypeRow;
    private int yagizTranslatorAutoDetectRow;
    private int yagizTranslatorDividerRow;

    private int ayuSyncHeaderRow;
    private int ayuSyncStatusBtnRow;
    private int ayuSyncDividerRow;

    private int debugHeaderRow;
    private int WALModeRow;
    private int buttonsDividerRow;
    private int clearAyuDatabaseBtnRow;
    private int eraseLocalDatabaseBtnRow;

    private boolean ghostModeMenuExpanded;

    @Override
    protected void updateRowsId() {
        super.updateRowsId();

        ghostEssentialsHeaderRow = newRow();
        ghostModeToggleRow = newRow();
        if (ghostModeMenuExpanded) {
            sendReadPacketsRow = newRow();
            sendOnlinePacketsRow = newRow();
            sendUploadProgressRow = newRow();
            sendOfflinePacketAfterOnlineRow = newRow();
        } else {
            sendReadPacketsRow = -1;
            sendOnlinePacketsRow = -1;
            sendUploadProgressRow = -1;
            sendOfflinePacketAfterOnlineRow = -1;
        }
        markReadAfterSendRow = newRow();
        useScheduledMessagesRow = newRow();
        ghostDividerRow = newRow();

        spyHeaderRow = newRow();
        saveDeletedMessagesRow = newRow();
        saveMessagesHistoryRow = newRow();
        spyDivider1Row = newRow();
        messageSavingBtnRow = newRow();
        spyDivider2Row = newRow();

        qolHeaderRow = newRow();
        keepAliveServiceRow = newRow();
        disableAdsRow = newRow();
        localPremiumRow = newRow();
        filtersRow = newRow();
        quickActionsRow = newRow();
        qolDividerRow = newRow();

        customizationHeaderRow = newRow();
        deletedMarkTextRow = newRow();
        editedMarkTextRow = newRow();
        showGhostToggleInDrawerRow = newRow();
        showKillButtonInDrawerRow = newRow();
        customizationDividerRow = newRow();

        liquidGlassHeaderRow = newRow();
        liquidGlassBtnRow = newRow();
        liquidGlassDividerRow = newRow();

        aiHeaderRow = newRow();
        aiSettingsRow = newRow();
        aiDividerRow = newRow();

        productivityHeaderRow = newRow();
        smartRepliesRow = newRow();
        autoTranslateIncomingRow = newRow();
        autoTranslateOutgoingRow = newRow();
        autoTranslateLangRow = newRow();
        autoTranslateIncomingLangRow = newRow();
        productivityDividerRow = newRow();

        yagizTranslatorHeaderRow = newRow();
        yagizTranslatorEnabledRow = newRow();
        yagizTranslatorApiTypeRow = newRow();
        yagizTranslatorAutoDetectRow = newRow();
        yagizTranslatorDividerRow = newRow();

        ayuSyncHeaderRow = newRow();
        ayuSyncStatusBtnRow = newRow();
        ayuSyncDividerRow = newRow();

        debugHeaderRow = newRow();
        WALModeRow = newRow();
        buttonsDividerRow = newRow();
        clearAyuDatabaseBtnRow = newRow();
        eraseLocalDatabaseBtnRow = newRow();
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        // todo: register `MESSAGES_DELETED_NOTIFICATION` on all notification centers, not only on the current account

        NotificationCenter.getInstance(UserConfig.selectedAccount).addObserver(this, OverConstants.MESSAGES_DELETED_NOTIFICATION);
        NotificationCenter.getGlobalInstance().addObserver(this, OverConstants.AYUSYNC_STATE_CHANGED);

        return true;
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == OverConstants.MESSAGES_DELETED_NOTIFICATION) {
            // recalculate database size
            if (listAdapter != null) {
                listAdapter.notifyItemChanged(clearAyuDatabaseBtnRow);
            }
        } else if (id == OverConstants.AYUSYNC_STATE_CHANGED) {
            if (listAdapter != null) {
                listAdapter.notifyItemChanged(ayuSyncStatusBtnRow);
            }
        }
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();

        NotificationCenter.getInstance(UserConfig.selectedAccount).removeObserver(this, OverConstants.MESSAGES_DELETED_NOTIFICATION);
        NotificationCenter.getGlobalInstance().removeObserver(this, OverConstants.AYUSYNC_STATE_CHANGED);
    }

    private void updateGhostViews() {
        var isActive = OverConfig.isGhostModeActive();

        listAdapter.notifyItemChanged(ghostModeToggleRow, payload);
        listAdapter.notifyItemChanged(sendReadPacketsRow, !isActive);
        listAdapter.notifyItemChanged(sendOnlinePacketsRow, !isActive);
        listAdapter.notifyItemChanged(sendUploadProgressRow, !isActive);
        listAdapter.notifyItemChanged(sendOfflinePacketAfterOnlineRow, isActive);

        NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.mainUserInfoChanged);
    }

    private void toggleLocalPremium() {
        var newState = !OverConfig.localPremium;

        OverConfig.editor.putBoolean("localPremium", OverConfig.localPremium = newState).apply();
        listAdapter.notifyItemChanged(localPremiumRow, OverConfig.localPremium);

        getMessagesController().updatePremium(OverConfig.localPremium);
        NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.currentUserPremiumStatusChanged);
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.premiumStatusChangedGlobal);

        getMediaDataController().loadPremiumPromo(false);
        getMediaDataController().loadReactions(false, true);
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        if (position == ghostModeToggleRow) {
            ghostModeMenuExpanded ^= true;
            updateRowsId();
            listAdapter.notifyItemChanged(ghostModeToggleRow, payload);
            if (ghostModeMenuExpanded) {
                listAdapter.notifyItemRangeInserted(ghostModeToggleRow + 1, 4);
            } else {
                listAdapter.notifyItemRangeRemoved(ghostModeToggleRow + 1, 4);
            }
        } else if (position == sendReadPacketsRow) {
            OverConfig.editor.putBoolean("sendReadPackets", OverConfig.sendReadPackets ^= true).apply();
            ((CheckBoxCell) view).setChecked(OverConfig.sendReadPackets, true);

            OverState.setAllowReadPacket(false, -1);
            updateGhostViews();
        } else if (position == sendOnlinePacketsRow) {
            OverConfig.editor.putBoolean("sendOnlinePackets", OverConfig.sendOnlinePackets ^= true).apply();
            ((CheckBoxCell) view).setChecked(OverConfig.sendOnlinePackets, true);

            updateGhostViews();
        } else if (position == sendUploadProgressRow) {
            OverConfig.editor.putBoolean("sendUploadProgress", OverConfig.sendUploadProgress ^= true).apply();
            ((CheckBoxCell) view).setChecked(OverConfig.sendUploadProgress, true);

            updateGhostViews();
        } else if (position == sendOfflinePacketAfterOnlineRow) {
            OverConfig.editor.putBoolean("sendOfflinePacketAfterOnline", OverConfig.sendOfflinePacketAfterOnline ^= true).apply();
            ((CheckBoxCell) view).setChecked(OverConfig.sendOfflinePacketAfterOnline, true);

            updateGhostViews();
        } else if (position == markReadAfterSendRow) {
            OverConfig.editor.putBoolean("markReadAfterSend", OverConfig.markReadAfterSend ^= true).apply();
            ((TextCheckCell) view).setChecked(OverConfig.markReadAfterSend);

            OverState.setAllowReadPacket(false, -1);

            if (OverConfig.markReadAfterSend && OverConfig.useScheduledMessages) {
                OverConfig.editor.putBoolean("useScheduledMessages", OverConfig.useScheduledMessages ^= true).apply();

                listAdapter.notifyItemChanged(useScheduledMessagesRow, false);
            }
        } else if (position == useScheduledMessagesRow) {
            OverConfig.editor.putBoolean("useScheduledMessages", OverConfig.useScheduledMessages ^= true).apply();
            ((TextCheckCell) view).setChecked(OverConfig.useScheduledMessages);

            OverState.setAutomaticallyScheduled(false, -1);

            if (OverConfig.useScheduledMessages && OverConfig.markReadAfterSend) {
                OverConfig.editor.putBoolean("markReadAfterSend", OverConfig.markReadAfterSend ^= true).apply();

                listAdapter.notifyItemChanged(markReadAfterSendRow, false);
            }
        } else if (position == saveDeletedMessagesRow) {
            OverConfig.editor.putBoolean("saveDeletedMessages", OverConfig.saveDeletedMessages ^= true).apply();
            ((TextCheckCell) view).setChecked(OverConfig.saveDeletedMessages);
        } else if (position == saveMessagesHistoryRow) {
            OverConfig.editor.putBoolean("saveMessagesHistory", OverConfig.saveMessagesHistory ^= true).apply();
            ((TextCheckCell) view).setChecked(OverConfig.saveMessagesHistory);
        } else if (position == messageSavingBtnRow) {
            presentFragment(new MessageSavingPreferencesActivity());
        } else if (position == keepAliveServiceRow) {
            OverConfig.editor.putBoolean("keepAliveService", OverConfig.keepAliveService ^= true).apply();
            ((TextCheckCell) view).setChecked(OverConfig.keepAliveService);
        } else if (position == disableAdsRow) {
            OverConfig.editor.putBoolean("disableAds", OverConfig.disableAds ^= true).apply();
            ((TextCheckCell) view).setChecked(OverConfig.disableAds);
        } else if (position == localPremiumRow) {
            toggleLocalPremium();
        } else if (position == filtersRow) {
            NotificationsCheckCell checkCell = (NotificationsCheckCell) view;
            if (LocaleController.isRTL && x <= AndroidUtilities.dp(76) || !LocaleController.isRTL && x >= view.getMeasuredWidth() - AndroidUtilities.dp(76)) {
                OverConfig.editor.putBoolean("regexFiltersEnabled", OverConfig.regexFiltersEnabled ^= true).apply();
                checkCell.setChecked(OverConfig.regexFiltersEnabled, 0);
            } else {
                presentFragment(new RegexFiltersPreferencesActivity());
            }
        } else if (position == quickActionsRow) {
            showQuickActions();
        } else if (position == showGhostToggleInDrawerRow) {
            OverConfig.editor.putBoolean("showGhostToggleInDrawer", OverConfig.showGhostToggleInDrawer ^= true).apply();
            ((TextCheckCell) view).setChecked(OverConfig.showGhostToggleInDrawer);

            NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.mainUserInfoChanged);
        } else if (position == showKillButtonInDrawerRow) {
            OverConfig.editor.putBoolean("showKillButtonInDrawer", OverConfig.showKillButtonInDrawer ^= true).apply();
            ((TextCheckCell) view).setChecked(OverConfig.showKillButtonInDrawer);

            NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.mainUserInfoChanged);
        } else if (position == deletedMarkTextRow) {
            OverUi.spawnEditBox(
                    getParentActivity(),
                    ((TextCell) view),
                    LocaleController.getString(R.string.DeletedMarkText),
                    OverConfig::getDeletedMark,
                    "deletedMarkText",
                    OverConstants.DEFAULT_DELETED_MARK
            );
        } else if (position == editedMarkTextRow) {
            OverUi.spawnEditBox(
                    getParentActivity(),
                    ((TextCell) view),
                    LocaleController.getString(R.string.EditedMarkText),
                    OverConfig::getEditedMark,
                    "editedMarkText",
                    LocaleController.getString("EditedMessage", R.string.EditedMessage) // don't remove key
            );
        } else if (position == liquidGlassBtnRow) {
            presentFragment(new LiquidGlassPreferencesActivity());
        } else if (position == aiSettingsRow) {
            presentFragment(new AiPreferencesActivity());
        } else if (position == smartRepliesRow) {
            OverConfig.editor.putBoolean("smartQuickReplies", OverConfig.smartQuickReplies ^= true).apply();
            ((TextCheckCell) view).setChecked(OverConfig.smartQuickReplies);
        } else if (position == autoTranslateIncomingRow) {
            OverConfig.editor.putBoolean("autoTranslateIncomingDefault", OverConfig.autoTranslateIncomingDefault ^= true).apply();
            ((TextCheckCell) view).setChecked(OverConfig.autoTranslateIncomingDefault);
        } else if (position == autoTranslateOutgoingRow) {
            OverConfig.editor.putBoolean("autoTranslateOutgoingDefault", OverConfig.autoTranslateOutgoingDefault ^= true).apply();
            ((TextCheckCell) view).setChecked(OverConfig.autoTranslateOutgoingDefault);
        } else if (position == autoTranslateLangRow) {
            showLanguagePicker(false); // false = outgoing language
        } else if (position == autoTranslateIncomingLangRow) {
            showLanguagePicker(true); // true = incoming language
        } else if (position == yagizTranslatorEnabledRow) {
            OverConfig.editor.putBoolean("yagizTranslatorEnabled", OverConfig.yagizTranslatorEnabled ^= true).apply();
            ((TextCheckCell) view).setChecked(OverConfig.yagizTranslatorEnabled);
        } else if (position == yagizTranslatorApiTypeRow) {
            showYagizTranslatorApiPicker();
        } else if (position == yagizTranslatorAutoDetectRow) {
            OverConfig.editor.putBoolean("yagizTranslatorAutoDetect", OverConfig.yagizTranslatorAutoDetect ^= true).apply();
            ((TextCheckCell) view).setChecked(OverConfig.yagizTranslatorAutoDetect);
        } else if (position == ayuSyncStatusBtnRow) {
            presentFragment(new OverSyncPreferencesActivity());
        } else if (position == WALModeRow) {
            OverConfig.editor.putBoolean("WALMode", OverConfig.WALMode ^= true).apply();
            ((TextCheckCell) view).setChecked(OverConfig.WALMode);
        } else if (position == clearAyuDatabaseBtnRow) {
            OverMessagesController.getInstance().clean();

            // reset size
            ((TextCell) view).setValue("…");

            BulletinFactory.of(this).createSimpleBulletin(R.raw.info, LocaleController.getString(R.string.ClearAyuDatabaseNotification)).show();
        } else if (position == eraseLocalDatabaseBtnRow) {
            getMessagesStorage().clearLocalDatabase();

            try {
                getMessagesStorage().getDatabase().executeFast("DELETE FROM messages_v2").stepThis().dispose();
            } catch (Exception e) {
                FileLog.e(e);
                BulletinFactory.of(this).createSimpleBulletin(R.raw.error, LocaleController.getString(R.string.ErrorOccurred)).show();
            }

            try {
                getMessagesStorage().getDatabase().executeFast("DELETE FROM dialogs").stepThis().dispose();
            } catch (Exception e) {
                FileLog.e(e);
                BulletinFactory.of(this).createSimpleBulletin(R.raw.error, LocaleController.getString(R.string.ErrorOccurred)).show();
            }

            BulletinFactory.of(this).createSimpleBulletin(R.raw.info, LocaleController.getString(R.string.RestartRequired)).show();
        }
    }

    @Override
    protected String getTitle() {
        return LocaleController.getString(R.string.AyuPreferences);
    }

    @Override
    protected BaseListAdapter createAdapter(Context context) {
        return new ListAdapter(context);
    }

    private int getGhostModeSelectedCount() {
        int count = 0;
        if (!OverConfig.sendReadPackets) count++;
        if (!OverConfig.sendOnlinePackets) count++;
        if (!OverConfig.sendUploadProgress) count++;
        if (OverConfig.sendOfflinePacketAfterOnline) count++;

        return count;
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
                    textCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    if (position == messageSavingBtnRow) {
                        textCell.setText(LocaleController.getString(R.string.MessageSavingBtn), false);
                    } else if (position == deletedMarkTextRow) {
                        textCell.setTextAndValue(LocaleController.getString(R.string.DeletedMarkText), OverConfig.getDeletedMark(), true);
                    } else if (position == editedMarkTextRow) {
                        textCell.setTextAndValue(LocaleController.getString(R.string.EditedMarkText), OverConfig.getEditedMark(), true);
                    } else if (position == quickActionsRow) {
                        textCell.setTextAndValue(LocaleController.getString(R.string.OvergramQuickActions),
                                LocaleController.getString(R.string.OvergramQuickActionsHint),
                                true);
                    } else if (position == liquidGlassBtnRow) {
                        textCell.setTextAndValue(LocaleController.getString(R.string.LiquidGlassHeader),
                            OverConfig.liquidGlassEnabled ? LocaleController.getString("NotificationsOn", R.string.NotificationsOn) : LocaleController.getString("NotificationsOff", R.string.NotificationsOff),
                            true);
                    } else if (position == aiSettingsRow) {
                        textCell.setTextAndValue(LocaleController.getString(R.string.OvergramAiSettings),
                                OverConfig.geminiEnabled ? LocaleController.getString("NotificationsOn", R.string.NotificationsOn) : LocaleController.getString("NotificationsOff", R.string.NotificationsOff),
                                true);
                    } else if (position == autoTranslateLangRow) {
                        String langCode = OverConfig.autoTranslateOutgoingLangDefault;
                        String langName = getLanguageDisplayName(langCode);
                        textCell.setTextAndValue(LocaleController.getString(R.string.AutoTranslateOutgoingLang), langName, true);
                    } else if (position == autoTranslateIncomingLangRow) {
                        String langCode = OverConfig.autoTranslateIncomingLangDefault;
                        String langName = getLanguageDisplayName(langCode);
                        textCell.setTextAndValue(LocaleController.getString(R.string.AutoTranslateIncomingLang), langName, true);
                    } else if (position == yagizTranslatorApiTypeRow) {
                        String apiName = com.overspend1.overgram.translator.YagizTranslator.getApiName(OverConfig.yagizTranslatorApiType);
                        textCell.setTextAndValue(LocaleController.getString(R.string.YagizTranslatorApiType), apiName, true);
                    } else if (position == ayuSyncStatusBtnRow) {
                        var status = OverSyncState.getConnectionStateString();

                        textCell.setTextAndValue(LocaleController.getString(R.string.AyuSyncStatusTitle), status, false);
                    } else if (position == clearAyuDatabaseBtnRow) {
                        var file = ApplicationLoader.applicationContext.getDatabasePath(OverConstants.AYU_DATABASE);
                        var size = file.exists() ? file.length() : 0;

                        textCell.setTextAndValueAndIcon(LocaleController.getString(R.string.ClearAyuDatabase), AndroidUtilities.formatFileSize(size), R.drawable.msg_clear_solar, true);
                        textCell.setColors(Theme.key_text_RedBold, Theme.key_text_RedBold);
                    } else if (position == eraseLocalDatabaseBtnRow) {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.EraseLocalDatabase), R.drawable.msg_archive, false);
                        textCell.setColors(Theme.key_text_RedBold, Theme.key_text_RedBold);
                    }
                    break;
                case 3:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == ghostEssentialsHeaderRow) {
                        headerCell.setText(LocaleController.getString(R.string.GhostEssentialsHeader));
                    } else if (position == spyHeaderRow) {
                        headerCell.setText(LocaleController.getString(R.string.SpyEssentialsHeader));
                    } else if (position == qolHeaderRow) {
                        headerCell.setText(LocaleController.getString(R.string.QoLTogglesHeader));
                    } else if (position == customizationHeaderRow) {
                        headerCell.setText(LocaleController.getString(R.string.CustomizationHeader));
                    } else if (position == liquidGlassHeaderRow) {
                        headerCell.setText(LocaleController.getString(R.string.LiquidGlassHeader));
                    } else if (position == aiHeaderRow) {
                        headerCell.setText(LocaleController.getString(R.string.OvergramAiHeader));
                    } else if (position == productivityHeaderRow) {
                        headerCell.setText(LocaleController.getString(R.string.ProductivityHeader));
                    } else if (position == yagizTranslatorHeaderRow) {
                        headerCell.setText(LocaleController.getString(R.string.YagizTranslatorHeader));
                    } else if (position == ayuSyncHeaderRow) {
                        headerCell.setText(LocaleController.getString(R.string.AyuSyncHeader));
                    } else if (position == debugHeaderRow) {
                        headerCell.setText(LocaleController.getString("SettingsDebug", R.string.SettingsDebug));
                    }
                    break;
                case 5:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    textCheckCell.setEnabled(true, null);
                    if (position == markReadAfterSendRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString(R.string.MarkReadAfterSend), OverConfig.markReadAfterSend, true);
                    } else if (position == useScheduledMessagesRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString(R.string.UseScheduledMessages), OverConfig.useScheduledMessages, false);
                    } else if (position == saveDeletedMessagesRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString(R.string.SaveDeletedMessages), OverConfig.saveDeletedMessages, true);
                    } else if (position == saveMessagesHistoryRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString(R.string.SaveMessagesHistory), OverConfig.saveMessagesHistory, false);
                    } else if (position == keepAliveServiceRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString(R.string.KeepAliveService), OverConfig.keepAliveService, true);
                    } else if (position == disableAdsRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString(R.string.DisableAds), OverConfig.disableAds, true);
                    } else if (position == localPremiumRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString(R.string.LocalPremium) + " β", OverConfig.localPremium, true);
                    } else if (position == showGhostToggleInDrawerRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString(R.string.ShowGhostToggleInDrawer), OverConfig.showGhostToggleInDrawer, true);
                    } else if (position == showKillButtonInDrawerRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString(R.string.ShowKllButtonInDrawer), OverConfig.showKillButtonInDrawer, false);
                    } else if (position == WALModeRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString(R.string.WALMode), OverConfig.WALMode, false);
                    } else if (position == smartRepliesRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString(R.string.SmartQuickReplies), OverConfig.smartQuickReplies, true);
                    } else if (position == autoTranslateIncomingRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString(R.string.AutoTranslateIncoming), OverConfig.autoTranslateIncomingDefault, true);
                    } else if (position == autoTranslateOutgoingRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString(R.string.AutoTranslateOutgoing), OverConfig.autoTranslateOutgoingDefault, true);
                    } else if (position == yagizTranslatorEnabledRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString(R.string.YagizTranslatorEnabled), OverConfig.yagizTranslatorEnabled, true);
                    } else if (position == yagizTranslatorAutoDetectRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString(R.string.YagizTranslatorAutoDetect), OverConfig.yagizTranslatorAutoDetect, false);
                    }
                    break;
                case 18:
                    TextCheckCell2 checkCell = (TextCheckCell2) holder.itemView;
                    if (position == ghostModeToggleRow) {
                        int selectedCount = getGhostModeSelectedCount();
                        checkCell.setTextAndCheck(LocaleController.getString(R.string.GhostModeToggle), OverConfig.isGhostModeActive(), true, true);
                        checkCell.setCollapseArrow(String.format(Locale.US, "%d/4", selectedCount), !ghostModeMenuExpanded, () -> {
                            OverConfig.toggleGhostMode();
                            updateGhostViews();
                        });
                    }
                    checkCell.getCheckBox().setColors(Theme.key_switchTrack, Theme.key_switchTrackChecked, Theme.key_windowBackgroundWhite, Theme.key_windowBackgroundWhite);
                    checkCell.getCheckBox().setDrawIconType(0);
                    break;
                case 19:
                    CheckBoxCell checkBoxCell = (CheckBoxCell) holder.itemView;
                    if (position == sendReadPacketsRow) {
                        checkBoxCell.setText(LocaleController.getString(R.string.DontSendReadPackets), "", !OverConfig.sendReadPackets, true, true);
                    } else if (position == sendOnlinePacketsRow) {
                        checkBoxCell.setText(LocaleController.getString(R.string.DontSendOnlinePackets), "", !OverConfig.sendOnlinePackets, true, true);
                    } else if (position == sendUploadProgressRow) {
                        checkBoxCell.setText(LocaleController.getString(R.string.DontSendUploadProgress), "", !OverConfig.sendUploadProgress, true, true);
                    } else if (position == sendOfflinePacketAfterOnlineRow) {
                        checkBoxCell.setText(LocaleController.getString(R.string.SendOfflinePacketAfterOnline), "", OverConfig.sendOfflinePacketAfterOnline, true, true);
                    }
                    checkBoxCell.setPad(1);
                    break;
                case TOGGLE_BUTTON_VIEW:
                    NotificationsCheckCell notificationsCheckCell = (NotificationsCheckCell) holder.itemView;
                    if (position == filtersRow) {
                        var count = OverConfig.getRegexFilters().size();
                        notificationsCheckCell.setTextAndValueAndCheck(LocaleController.getString(R.string.RegexFilters), count + " " + LocaleController.getString(R.string.RegexFiltersAmount), OverConfig.regexFiltersEnabled, false);
                    }
                    break;
            }
        }

        @NonNull
        @NotNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            if (viewType == TOGGLE_BUTTON_VIEW) {
                var view = new NotificationsCheckCell(mContext);
                view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                return new RecyclerListView.Holder(view);
            }
            return super.onCreateViewHolder(parent, viewType);
        }

        @Override
        public int getItemViewType(int position) {
            if (
                    position == ghostDividerRow ||
                            position == spyDivider1Row ||
                            position == spyDivider2Row ||
                            position == qolDividerRow ||
                    position == customizationDividerRow ||
                    position == liquidGlassDividerRow ||
                    position == aiDividerRow ||
                    position == productivityDividerRow ||
                    position == yagizTranslatorDividerRow ||
                    position == ayuSyncDividerRow ||
                    position == buttonsDividerRow
            ) {
                return 1;
            } else if (
                            position == messageSavingBtnRow ||
                            position == deletedMarkTextRow ||
                            position == editedMarkTextRow ||
                            position == quickActionsRow ||
                            position == liquidGlassBtnRow ||
                            position == aiSettingsRow ||
                            position == autoTranslateLangRow ||
                            position == autoTranslateIncomingLangRow ||
                            position == yagizTranslatorApiTypeRow ||
                            position == ayuSyncStatusBtnRow ||
                            position == clearAyuDatabaseBtnRow ||
                            position == eraseLocalDatabaseBtnRow
            ) {
                return 2;
            } else if (
                    position == ghostEssentialsHeaderRow ||
                            position == spyHeaderRow ||
                            position == qolHeaderRow ||
                            position == customizationHeaderRow ||
                            position == liquidGlassHeaderRow ||
                            position == aiHeaderRow ||
                            position == productivityHeaderRow ||
                            position == yagizTranslatorHeaderRow ||
                            position == ayuSyncHeaderRow ||
                            position == debugHeaderRow
            ) {
                return 3;
            } else if (
                    position == ghostModeToggleRow
            ) {
                return 18;
            } else if (
                    position >= sendReadPacketsRow && position <= sendOfflinePacketAfterOnlineRow
            ) {
                return 19;
            } else if (
                    position == filtersRow
            ) {
                return TOGGLE_BUTTON_VIEW;
            } else if (
                    position == smartRepliesRow ||
                    position == autoTranslateIncomingRow ||
                    position == autoTranslateOutgoingRow ||
                    position == yagizTranslatorEnabledRow ||
                    position == yagizTranslatorAutoDetectRow
            ) {
                return 5;
            }
            return 5;
        }
    }

    private void showQuickActions() {
        if (getParentActivity() == null) {
            return;
        }
        String[] options = new String[] {
                LocaleController.getString(R.string.OvergramQuickActionToggleGlass),
                LocaleController.getString(R.string.OvergramQuickActionToggleGhost),
                LocaleController.getString(R.string.OvergramQuickActionOpenGlass),
                LocaleController.getString(R.string.OvergramQuickActionOpenAi)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(LocaleController.getString(R.string.OvergramQuickActions));
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: {
                    OverConfig.liquidGlassEnabled = !OverConfig.liquidGlassEnabled;
                    OverConfig.editor.putBoolean("liquidGlassEnabled", OverConfig.liquidGlassEnabled).apply();
                    listAdapter.notifyItemChanged(liquidGlassBtnRow, payload);
                    BulletinFactory.of(this).createSimpleBulletin(
                            OverConfig.liquidGlassEnabled ? R.raw.done : R.raw.deactivate,
                            OverConfig.liquidGlassEnabled ? LocaleController.getString("NotificationsOn", R.string.NotificationsOn) : LocaleController.getString("NotificationsOff", R.string.NotificationsOff)
                    ).show();
                    break;
                }
                case 1: {
                    OverConfig.toggleGhostMode();
                    updateGhostViews();
                    BulletinFactory.of(this).createSimpleBulletin(
                            OverConfig.isGhostModeActive() ? R.raw.done : R.raw.deactivate,
                            OverConfig.isGhostModeActive() ? LocaleController.getString(R.string.GhostModeEnabled) : LocaleController.getString(R.string.GhostModeDisabled)
                    ).show();
                    break;
                }
                case 2:
                    presentFragment(new LiquidGlassPreferencesActivity());
                    break;
                case 3:
                    presentFragment(new AiPreferencesActivity());
                    break;
                default:
                    break;
            }
        });
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        showDialog(builder.create());
    }

    private String getLanguageDisplayName(String langCode) {
        if (langCode == null || langCode.isEmpty()) {
            langCode = "en";
        }

        // Common language names
        switch (langCode.toLowerCase()) {
            case "en": return "English";
            case "es": return "Spanish";
            case "fr": return "French";
            case "de": return "German";
            case "it": return "Italian";
            case "pt": return "Portuguese";
            case "ru": return "Russian";
            case "ja": return "Japanese";
            case "ko": return "Korean";
            case "zh": return "Chinese";
            case "ar": return "Arabic";
            case "hi": return "Hindi";
            case "tr": return "Turkish";
            case "pl": return "Polish";
            case "uk": return "Ukrainian";
            case "nl": return "Dutch";
            case "sv": return "Swedish";
            case "no": return "Norwegian";
            case "da": return "Danish";
            case "fi": return "Finnish";
            default: return langCode.toUpperCase();
        }
    }

    private void showLanguagePicker(boolean isIncoming) {
        if (getParentActivity() == null) {
            return;
        }

        String[][] languages = {
            {"en", "English"},
            {"es", "Spanish"},
            {"fr", "French"},
            {"de", "German"},
            {"it", "Italian"},
            {"pt", "Portuguese"},
            {"ru", "Russian"},
            {"ja", "Japanese"},
            {"ko", "Korean"},
            {"zh", "Chinese"},
            {"ar", "Arabic"},
            {"hi", "Hindi"},
            {"tr", "Turkish"},
            {"pl", "Polish"},
            {"uk", "Ukrainian"},
            {"nl", "Dutch"},
            {"sv", "Swedish"},
            {"no", "Norwegian"},
            {"da", "Danish"},
            {"fi", "Finnish"}
        };

        String[] options = new String[languages.length];
        for (int i = 0; i < languages.length; i++) {
            options[i] = languages[i][1];
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(LocaleController.getString(isIncoming ? R.string.AutoTranslateIncomingLang : R.string.AutoTranslateOutgoingLang));
        builder.setItems(options, (dialog, which) -> {
            String selectedLang = languages[which][0];
            if (isIncoming) {
                OverConfig.autoTranslateIncomingLangDefault = selectedLang;
                OverConfig.editor.putString("autoTranslateIncomingLangDefault", selectedLang).apply();
                listAdapter.notifyItemChanged(autoTranslateIncomingLangRow);
            } else {
                OverConfig.autoTranslateOutgoingLangDefault = selectedLang;
                OverConfig.editor.putString("autoTranslateOutgoingLangDefault", selectedLang).apply();
                listAdapter.notifyItemChanged(autoTranslateLangRow);
            }
        });
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        showDialog(builder.create());
    }

    private void showYagizTranslatorApiPicker() {
        if (getParentActivity() == null) {
            return;
        }

        String[] apiNames = {
            com.overspend1.overgram.translator.YagizTranslator.getApiName(com.overspend1.overgram.translator.YagizTranslator.API_GEMINI),
            com.overspend1.overgram.translator.YagizTranslator.getApiName(com.overspend1.overgram.translator.YagizTranslator.API_GOOGLE),
            com.overspend1.overgram.translator.YagizTranslator.getApiName(com.overspend1.overgram.translator.YagizTranslator.API_DEEPL)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(LocaleController.getString(R.string.YagizTranslatorApiType));
        builder.setItems(apiNames, (dialog, which) -> {
            OverConfig.yagizTranslatorApiType = which;
            OverConfig.editor.putInt("yagizTranslatorApiType", which).apply();
            listAdapter.notifyItemChanged(yagizTranslatorApiTypeRow);
        });
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        showDialog(builder.create());
    }
}
