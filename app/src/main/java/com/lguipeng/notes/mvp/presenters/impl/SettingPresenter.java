package com.lguipeng.notes.mvp.presenters.impl;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import com.lguipeng.notes.R;
import com.lguipeng.notes.injector.ContextLifeCycle;
import com.lguipeng.notes.mvp.presenters.Presenter;
import com.lguipeng.notes.mvp.views.View;
import com.lguipeng.notes.mvp.views.impl.SettingView;
import com.lguipeng.notes.ui.PayActivity;
import com.lguipeng.notes.utils.EverNoteUtils;
import com.lguipeng.notes.utils.FileUtils;
import com.lguipeng.notes.utils.NotesLog;
import com.lguipeng.notes.utils.ObservableUtils;
import com.lguipeng.notes.utils.PreferenceUtils;
import com.lguipeng.notes.utils.ThemeUtils;

import net.tsz.afinal.FinalDb;

import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by lgp on 2015/9/5.
 */
public class SettingPresenter implements Presenter, DialogInterface.OnClickListener{
    private SettingView view;
    private final Context mContext;
    private FinalDb mFinalDb;
    private EverNoteUtils mEverNoteUtils;
    private ObservableUtils mObservableUtils;
    private PreferenceUtils mPreferenceUtils;
    private FileUtils mFileUtils;
    private boolean backuping = false;
    private boolean isRightHandMode = false;
    private boolean isCardLayout = false;
    private MainPresenter.NotifyEvent<Void> event;
    @Inject
    public SettingPresenter(@ContextLifeCycle("Activity") Context mContext, FinalDb mFinalDb, EverNoteUtils mEverNoteUtils,
                            PreferenceUtils mPreferenceUtils, ObservableUtils mObservableUtils, FileUtils mFileUtils) {
        this.mContext = mContext;
        this.mFinalDb = mFinalDb;
        this.mEverNoteUtils = mEverNoteUtils;
        this.mPreferenceUtils = mPreferenceUtils;
        this.mObservableUtils = mObservableUtils;
        this.mFileUtils = mFileUtils;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        view.findPreference();
        initOtherPreference();
        initFeedbackPreference();
        initEverAccountPreference();
    }

    public void onViewCreated(android.view.View v){
        view.initPreferenceListView(v);
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroy() {
        //ignore change theme event
        if (event != null &&
                event.getType() != MainPresenter.NotifyEvent.CHANGE_THEME){
            EventBus.getDefault().post(event);
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void attachView(View v) {
        this.view = (SettingView)v;
    }

    public boolean onPreferenceTreeClick(Preference preference){
        if (view.isResume() && preference == null){
            return false;
        }
        String key = preference.getKey();
        if (TextUtils.equals(key, getString(mContext, R.string.right_hand_mode_key))){
            isRightHandMode = !isRightHandMode;
            mPreferenceUtils.saveParam(getString(mContext, R.string.right_hand_mode_key), isRightHandMode);
        }

        if (TextUtils.equals(key, getString(mContext, R.string.card_note_item_layout_key))){
            isCardLayout = !isCardLayout;
            mPreferenceUtils.saveParam(getString(mContext, R.string.card_note_item_layout_key), isCardLayout);
        }

        if (TextUtils.equals(key, getString(mContext, R.string.change_theme_key))){
            view.showThemeChooseDialog();
        }

        if (TextUtils.equals(key, getString(mContext, R.string.pay_for_me_key))){
            Intent intent = new Intent(mContext, PayActivity.class);
            mContext.startActivity(intent);
        }

        if (TextUtils.equals(key, mContext.getString(R.string.give_favor_key))){
            giveFavor();
        }

        if (TextUtils.equals(key, mContext.getString(R.string.backup_local_key))){
            backupLocal();
        }

        if (TextUtils.equals(key, mContext.getString(R.string.ever_note_account_key))){
            if (mEverNoteUtils.isLogin()){
                view.showUnbindEverNoteDialog();
            }else {
                authEverNote();
            }
        }
        return false;
    }

    private void initFeedbackPreference(){
        Uri uri = Uri.parse("mailto:lgpszu@163.com");
        final Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        PackageManager pm = mContext.getPackageManager();
        List<ResolveInfo> infos = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (infos == null || infos.size() <= 0){
            view.setFeedbackPreferenceSummary(mContext.getString(R.string.no_email_app_tip));
            return;
        }
        Preference.OnPreferenceClickListener l = (preference -> {
            mContext.startActivity(intent);
            return true;
        });
        view.setFeedbackPreferenceClickListener(l);
    }

    private void initEverAccountPreference(){
        String everAccount = mPreferenceUtils.getStringParam(PreferenceUtils.EVERNOTE_ACCOUNT_KEY);
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

    private void initOtherPreference(){
        isCardLayout = mPreferenceUtils.getBooleanParam(getString(mContext,
                R.string.card_note_item_layout_key), true);
        isRightHandMode = mPreferenceUtils.getBooleanParam(getString(mContext,
                R.string.right_hand_mode_key));
        view.setCardLayoutPreferenceChecked(isCardLayout);
        view.setRightHandModePreferenceChecked(isRightHandMode);
    }

    private void onGetUserSuccess(String user){
        view.setEverNoteAccountPreferenceSummary(user);
        view.setEverNoteAccountPreferenceTitle(mContext.getString(R.string.unbind_ever_note));
    }

    private void onGetUserException(Throwable e){
        view.setEverNoteAccountPreferenceTitle(mContext.getString(R.string.bind_ever_note));
        view.setEverNoteAccountPreferenceSummary("");
        if (e != null){
            NotesLog.e(e.getMessage());
        }
    }

    private void fillEverAccount(){
        mObservableUtils.getEverNoteUser(mEverNoteUtils)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((user) ->
                        onGetUserSuccess(mEverNoteUtils.getUserAccount(user))
                        , (e) -> onGetUserException(e));
    }

    public void onEventMainThread(Boolean result){
        handleLoginResult(result);
    }

    private void handleLoginResult(boolean result){
        if (result){
            try {
                initEverAccountPreference();
                view.showSnackbar(R.string.bind_ever_note_success);
                if (event == null){
                    event = new MainPresenter.NotifyEvent<>();
                }
                event.setType(MainPresenter.NotifyEvent.REFRESH_LIST);
                //EventBus.getDefault().post(MainActivity.MainEvent.REFRESH_LIST);
            }catch (Exception e){
                view.showSnackbar(R.string.bind_ever_note_fail);
                NotesLog.e(e.getMessage());
            }
        }else {
            view.showSnackbar(R.string.bind_ever_note_fail);
        }
    }

    private void authEverNote(){
        if (mContext instanceof Activity)
            mEverNoteUtils.auth((Activity)mContext);
    }

    private void giveFavor(){
        try{
            Uri uri = Uri.parse("market://details?id="+ mContext.getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }catch(ActivityNotFoundException e){
            e.printStackTrace();
        }
    }

    private void backupLocal(){
        //已经备份中，直接返回
        if (backuping)
            return;
        backuping = true;
        view.toast(R.string.backup_local);
        mObservableUtils.backNotes(mContext, mFinalDb, mFileUtils)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((success) -> {
                    view.toast(R.string.backup_local_done);
                    backuping = false;
                }, (e) -> {
                    onGetUserException(e);
                    view.toast(R.string.backup_local_fail);
                    backuping = false;
                });
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
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
    }

    public void onThemeChoose(int position) {
        int value = ThemeUtils.getCurrentTheme(mContext).getIntValue();
        if (value != position) {
            mPreferenceUtils.saveParam(getString(mContext, R.string.change_theme_key), position);
            notifyChangeTheme();
        }
    }

    private void notifyChangeTheme(){
        if (event == null){
            event = new MainPresenter.NotifyEvent<>();
        }
        event.setType(MainPresenter.NotifyEvent.CHANGE_THEME);
        //post change theme event immediately
        EventBus.getDefault().post(event);
        view.finishView();
    }

    private String getString(Context context, @StringRes int string){
        if (context != null)
            return context.getString(string);
        return "";
    }
}
