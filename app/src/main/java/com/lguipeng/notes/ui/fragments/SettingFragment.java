package com.lguipeng.notes.ui.fragments;

import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceScreen;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.evernote.client.android.asyncclient.EvernoteCallback;
import com.evernote.edam.type.User;
import com.jenzz.materialpreference.CheckBoxPreference;
import com.jenzz.materialpreference.Preference;
import com.jenzz.materialpreference.SwitchPreference;
import com.lguipeng.notes.R;
import com.lguipeng.notes.adpater.ColorsListAdapter;
import com.lguipeng.notes.model.SNote;
import com.lguipeng.notes.module.DataModule;
import com.lguipeng.notes.ui.MainActivity;
import com.lguipeng.notes.ui.PayActivity;
import com.lguipeng.notes.utils.DialogUtils;
import com.lguipeng.notes.utils.EverNoteUtils;
import com.lguipeng.notes.utils.FileUtils;
import com.lguipeng.notes.utils.NotesLog;
import com.lguipeng.notes.utils.PreferenceUtils;
import com.lguipeng.notes.utils.SnackbarUtils;
import com.lguipeng.notes.utils.ThemeUtils;
import com.lguipeng.notes.utils.ThreadExecutorPool;

import net.tsz.afinal.FinalDb;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/**
 * Created by lgp on 2015/5/26.
 */
public class SettingFragment extends BaseFragment implements EvernoteCallback<User>{
    public static final String PREFERENCE_FILE_NAME = "note.settings";
    private SwitchPreference rightHandModeSwitch;
    private Preference feedbackPreference;
    private Preference payMePreference;
    private Preference giveFavorPreference;
    private Preference everAccountPreference;
    private boolean showNoteHistoryLog;
    private boolean rightHandMode;
    private boolean cardLayout;
    @Inject
    EverNoteUtils mEverNoteUtils;
    @Inject
    ThreadExecutorPool mThreadExecutorPool;
    @Inject
    FileUtils mFileUtils;
    @Inject
    FinalDb mFinalDb;
    @Inject
    Application app;
    private boolean backuping = false;
    public static SettingFragment newInstance(){
        SettingFragment fragment = new SettingFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        addPreferencesFromResource(R.xml.prefs);
        getPreferenceManager().setSharedPreferencesName(PREFERENCE_FILE_NAME);
        rightHandMode = preferenceUtils.getBooleanParam(getString(R.string.right_hand_mode_key));
        rightHandModeSwitch = (SwitchPreference)findPreference(getString(R.string.right_hand_mode_key));
        rightHandModeSwitch.setChecked(rightHandMode);
        cardLayout = preferenceUtils.getBooleanParam(getString(R.string.card_note_item_layout_key), true);
        CheckBoxPreference cardLayoutPreference = (CheckBoxPreference)findPreference(getString(R.string.card_note_item_layout_key));
        cardLayoutPreference.setChecked(cardLayout);
        feedbackPreference = (Preference)findPreference(getString(R.string.advice_feedback_key));
        everAccountPreference = (Preference)findPreference(getString(R.string.ever_note_account_key));
        payMePreference = (Preference)findPreference(getString(R.string.pay_for_me_key));
        giveFavorPreference = (Preference)findPreference(getString(R.string.give_favor_key));
        initFeedbackPreference();
        initEverAccount();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView listView = (ListView)view.findViewById(android.R.id.list);
        listView.setHorizontalScrollBarEnabled(false);
        listView.setVerticalScrollBarEnabled(false);
        listView.setDivider(new ColorDrawable(getResources().getColor(R.color.grey)));
        listView.setDividerHeight((int) getResources().getDimension(R.dimen.preference_divider_height));
        listView.setFooterDividersEnabled(false);
        listView.setHeaderDividersEnabled(false);
    }

    public SettingFragment() {
        super();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,  android.preference.Preference preference) {
        if (!isResumed() || preference == null){
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
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

        if (TextUtils.equals(key, getString(R.string.change_theme_key))){
           showThemeChooseDialog();
        }

        if (TextUtils.equals(key, getString(R.string.pay_for_me_key))){
            Intent intent = new Intent(getActivity(), PayActivity.class);
            startActivity(intent);
        }

        if (TextUtils.equals(key, getString(R.string.give_favor_key))){
            giveFavor();
        }

        if (TextUtils.equals(key, getString(R.string.backup_local_key))){
            backupLocal();
        }

        if (TextUtils.equals(key, getString(R.string.ever_note_account_key))){
            if (mEverNoteUtils.isLogin()){
                showUnbindEverNoteDialog();
            }else {
                authEverNote();
            }
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    protected List<Object> getModules() {
        return Arrays.<Object>asList(new DataModule());
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
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
        feedbackPreference.setOnPreferenceClickListener((preference -> {
            startActivity(intent);
            return true;
        }));

    }

    private void initEverAccount(){
        String everAccount = preferenceUtils.getStringParam(PreferenceUtils.EVERNOTE_ACCOUNT_KEY);
        if (!TextUtils.isEmpty(everAccount)){
            onGetUserSuccess(everAccount);
        }else {
            if (mEverNoteUtils.isLogin()){
                fillEverAccount();
            }else {
                onGetUserException(null);
            }
        }
    }

    private void fillEverAccount(){
        try {
            mEverNoteUtils.getUser(this);
        }catch (Exception e){
            onGetUserException(e);
        }
    }

    @Override
    public void onSuccess(User user) {
        if (user != null){
            onGetUserSuccess(user);
        }else {
            onGetUserException(new Exception("user is null"));
        }
    }


    @Override
    public void onException(Exception e) {
        onGetUserException(e);
        NotesLog.e(e.getMessage());
    }

    private void onGetUserException(Exception e){
        everAccountPreference.setTitle(R.string.bind_ever_note);
        everAccountPreference.setSummary("");
        if (e != null){
            NotesLog.e(e.getMessage());
        }
    }

    private void onGetUserSuccess(User user){
        onGetUserSuccess(mEverNoteUtils.getUserAccount(user));
    }

    private void onGetUserSuccess(String user){
        everAccountPreference.setSummary(user);
        everAccountPreference.setTitle(R.string.unbind_ever_note);
    }

    public void onEventMainThread(Boolean result){
        handleLoginResult(result);
    }

    private void handleLoginResult(boolean result){
        if (result && activity != null){
            try {
                initEverAccount();
                SnackbarUtils.show(activity, R.string.bind_ever_note_success);
                EventBus.getDefault().post(MainActivity.MainEvent.REFRESH_LIST);
            }catch (Exception e){
                SnackbarUtils.show(activity, R.string.bind_ever_note_fail);
                e.printStackTrace();
                NotesLog.e(e.getMessage());
            }
        }else if (activity != null){
            SnackbarUtils.show(activity, R.string.bind_ever_note_fail);
        }
    }

    private void showUnbindEverNoteDialog(){
        AlertDialog.Builder builder = DialogUtils.makeDialogBuilderByTheme(activity);
        builder.setTitle(R.string.has_unbind_ever_note_tip);
        DialogInterface.OnClickListener listener = (dialog, which) -> {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    mEverNoteUtils.logout();
                    onGetUserException(null);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
                default:
                    break;
            }
        };
        builder.setPositiveButton(R.string.sure, listener);
        builder.setNegativeButton(R.string.cancel, listener);
        builder.show();
    }

    private void showThemeChooseDialog(){
        AlertDialog.Builder builder = DialogUtils.makeDialogBuilderByTheme(activity);
        builder.setTitle(R.string.change_theme);
        Integer[] res = new Integer[]{R.drawable.red_round, R.drawable.brown_round, R.drawable.blue_round,
                R.drawable.blue_grey_round, R.drawable.yellow_round, R.drawable.deep_purple_round,
                R.drawable.pink_round, R.drawable.green_round};
        List<Integer> list = Arrays.asList(res);
        ColorsListAdapter adapter = new ColorsListAdapter(getActivity(), list);
        adapter.setCheckItem(ThemeUtils.getCurrentTheme(activity).getIntValue());
        GridView gridView = (GridView)LayoutInflater.from(getActivity()).inflate(R.layout.colors_panel_layout, null);
        gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        gridView.setCacheColorHint(0);
        gridView.setAdapter(adapter);
        builder.setView(gridView);
        final AlertDialog dialog = builder.show();
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            dialog.dismiss();
            int value = ThemeUtils.getCurrentTheme(activity).getIntValue();
            if (value != position) {
                preferenceUtils.saveParam(getString(R.string.change_theme_key), position);
                notifyChangeTheme();
            }
        });
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

    private void notifyChangeTheme(){
        if (activity == null)
            return;
        //ThemeUtils.changTheme(activity, theme);
        //activity.recreate();
        EventBus.getDefault().post(MainActivity.MainEvent.CHANGE_THEME);
        activity.finish();
    }

    private void authEverNote(){
        mEverNoteUtils.auth(activity);
    }

    private void backupLocal(){
        //已经备份中，直接返回
        if (backuping)
            return;
        backuping = true;
        Toast.makeText(app.getApplicationContext(),
                getString(R.string.backup_local), Toast.LENGTH_SHORT).show();
        mThreadExecutorPool.execute(() -> {
            List<SNote> notes = mFinalDb.findAllByWhere(SNote.class, " type = 0");
            mFileUtils.backupSNotes(notes);
            backuping = false;
            if (activity != null){
                activity.runOnUiThread(() -> {
                    Toast.makeText(app.getApplicationContext(),
                            getString(R.string.backup_local_done), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
