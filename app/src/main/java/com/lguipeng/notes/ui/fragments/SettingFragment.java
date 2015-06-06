package com.lguipeng.notes.ui.fragments;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceScreen;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.jenzz.materialpreference.CheckBoxPreference;
import com.jenzz.materialpreference.Preference;
import com.jenzz.materialpreference.SwitchPreference;
import com.lguipeng.notes.R;
import com.lguipeng.notes.module.DataModule;
import com.lguipeng.notes.ui.PayActivity;
import com.lguipeng.notes.utils.AccountUtils;
import com.lguipeng.notes.utils.PreferenceUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lgp on 2015/5/26.
 */
public class SettingFragment extends BaseFragment{

    public static final String PREFERENCE_FILE_NAME = "note.settings";

    private CheckBoxPreference showNoteHistoryLogCheckBox;

    private SwitchPreference rightHandModeSwitch;

    private Preference feedbackPreference;

    private Preference payMePreference;

    private Preference giveFavorPreference;

    private Preference accountPreference;

    private boolean showNoteHistoryLog;

    private boolean rightHandMode;

    private boolean cardLayout;

    private  List<String> accountItems = new ArrayList<>();

    private PreferenceUtils preferenceUtils;

    public static SettingFragment newInstance(){
        SettingFragment fragment = new SettingFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceUtils = PreferenceUtils.getInstance(getActivity());
        addPreferencesFromResource(R.xml.prefs);
        getPreferenceManager().setSharedPreferencesName(PREFERENCE_FILE_NAME);
        showNoteHistoryLog = preferenceUtils.getBooleanParam(getString(R.string.show_note_history_log_key));
        rightHandMode = preferenceUtils.getBooleanParam(getString(R.string.right_hand_mode_key));
        showNoteHistoryLogCheckBox = (CheckBoxPreference)findPreference(getString(R.string.show_note_history_log_key));
        showNoteHistoryLogCheckBox.setChecked(showNoteHistoryLog);
        rightHandModeSwitch = (SwitchPreference)findPreference(getString(R.string.right_hand_mode_key));
        rightHandModeSwitch.setChecked(rightHandMode);
        cardLayout = preferenceUtils.getBooleanParam(getString(R.string.card_note_item_layout_key), true);
        CheckBoxPreference cardLayoutPreference = (CheckBoxPreference)findPreference(getString(R.string.card_note_item_layout_key));
        cardLayoutPreference.setChecked(cardLayout);
        feedbackPreference = (Preference)findPreference(getString(R.string.advice_feedback_key));
        accountPreference = (Preference)findPreference(getString(R.string.sync_account_key));
        payMePreference = (Preference)findPreference(getString(R.string.pay_for_me_key));
        giveFavorPreference = (Preference)findPreference(getString(R.string.give_favor_key));
        initFeedbackPreference();
        initAccountPreference();
    }

    public SettingFragment() {
        super();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,  android.preference.Preference preference) {
        if (preference == null)
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        String key = preference.getKey();
        if (TextUtils.equals(key, getString(R.string.right_hand_mode_key))){
            rightHandMode = !rightHandMode;
            preferenceUtils.saveParam(getString(R.string.right_hand_mode_key), rightHandMode);
        }

        if (TextUtils.equals(key, getString(R.string.card_note_item_layout_key))){
            cardLayout = !cardLayout;
            preferenceUtils.saveParam(getString(R.string.card_note_item_layout_key), cardLayout);
        }

        if (TextUtils.equals(key, getString(R.string.show_note_history_log_key))){
            showNoteHistoryLog = !showNoteHistoryLog;
            preferenceUtils.saveParam(getString(R.string.show_note_history_log_key), showNoteHistoryLog);
        }

        if (TextUtils.equals(key, getString(R.string.pay_for_me_key))){
            Intent intent = new Intent(getActivity(), PayActivity.class);
            startActivity(intent);
        }

        if (TextUtils.equals(key, getString(R.string.give_favor_key))){
            giveFavor();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    protected List<Object> getModules() {
        return Arrays.<Object>asList(new DataModule());
    }

    private void initFeedbackPreference(){
        Uri uri = Uri.parse("mailto:lgpszu@163.com");
        final Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        PackageManager pm = getActivity().getPackageManager();
        List<ResolveInfo> infos = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (infos == null || infos.size() <= 0){
            feedbackPreference.setSummary(getString(R.string.no_email_app_tip));
            return;
        }
        feedbackPreference.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                startActivity(intent);
                return true;
            }
        });

    }

    private void initAccountPreference(){
        AccountUtils.findValidAccount(getActivity(), new AccountUtils.AccountFinderListener() {
            @Override
            protected void onNone() {
                accountPreference.setSummary(getString(R.string.no_account_tip));
            }

            @Override
            protected void onOne(String account) {
                accountPreference.setSummary(account);
                preferenceUtils.saveParam(getString(R.string.sync_account_key), account);
            }

            @Override
            protected void onMore(List<String> items) {
                SettingFragment.this.accountItems = items;
                String account = preferenceUtils.getStringParam(getString(R.string.sync_account_key));
                if (!isHasAccountSave()) {
                    accountPreference.setSummary(getString(R.string.multi_account_choose_tip));
                } else {
                    accountPreference.setSummary(account);
                }

                accountPreference.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(android.preference.Preference preference) {
                        showAccountChooseDialog(accountItems.toArray(new String[accountItems.size()]));
                        return true;
                    }
                });
            }
        });
    }

    private void showAccountChooseDialog(final CharSequence[] text){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme);
        builder.setTitle(R.string.choose_account);
        builder.setItems(text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                accountPreference.setSummary(text[which]);
                String account = preferenceUtils.getStringParam(getString(R.string.sync_account_key));
                if (TextUtils.equals(account, text[which]))
                    return;
                preferenceUtils.saveParam(getString(R.string.sync_account_key), text[which].toString());
            }
        });
        builder.show();
    }

    private void giveFavor(){
        try{
            Uri uri = Uri.parse("market://details?id="+ getActivity().getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }catch(ActivityNotFoundException e){
            e.printStackTrace();
        }
    }
}
