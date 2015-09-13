package com.lguipeng.notes.mvp.presenters.impl;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.lguipeng.notes.R;
import com.lguipeng.notes.injector.ContextLifeCycle;
import com.lguipeng.notes.model.SNote;
import com.lguipeng.notes.mvp.presenters.Presenter;
import com.lguipeng.notes.mvp.views.View;
import com.lguipeng.notes.mvp.views.impl.MainView;
import com.lguipeng.notes.ui.AboutActivity;
import com.lguipeng.notes.ui.NoteActivity;
import com.lguipeng.notes.ui.SettingActivity;
import com.lguipeng.notes.utils.EverNoteUtils;
import com.lguipeng.notes.utils.ObservableUtils;
import com.lguipeng.notes.utils.PreferenceUtils;

import net.tsz.afinal.FinalDb;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by lgp on 2015/9/4.
 */
public class MainPresenter implements Presenter, android.view.View.OnClickListener, SwipeRefreshLayout.OnRefreshListener,
        PopupMenu.OnMenuItemClickListener, MenuItemCompat.OnActionExpandListener {
    private MainView view;
    private final Context mContext;
    private FinalDb mFinalDb;
    private EverNoteUtils mEverNoteUtils;
    private ObservableUtils mObservableUtils;
    private PreferenceUtils mPreferenceUtils;
    private List<String> drawerList;
    private SNote.NoteType mCurrentNoteTypePage = SNote.NoteType.getDefault();
    private boolean isCardItemLayout = true;
    private boolean isRightHandMode = false;
    private final String  CURRENT_NOTE_TYPE_KEY = "CURRENT_NOTE_TYPE_KEY";
    @Inject
    public MainPresenter(@ContextLifeCycle ("Activity")Context context, FinalDb finalDb, PreferenceUtils preferenceUtils,
                         ObservableUtils mObservableUtils, EverNoteUtils everNoteUtils) {
        this.mContext = context;
        this.mFinalDb = finalDb;
        this.mPreferenceUtils = preferenceUtils;
        this.mEverNoteUtils = everNoteUtils;
        this.mObservableUtils = mObservableUtils;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null){
            int value = savedInstanceState.getInt(CURRENT_NOTE_TYPE_KEY);
            mCurrentNoteTypePage = SNote.NoteType.mapValueToStatus(value);
        }
        view.initToolbar();
        initDrawer();
        initMenuGravity();
        initItemLayoutManager();
        initRecyclerView();
        EventBus.getDefault().register(this);
    }

    public void onSaveInstanceState(Bundle outState){
        outState.putInt(CURRENT_NOTE_TYPE_KEY, mCurrentNoteTypePage.getValue());
    }


    @Override
    public void onStart() {
    }


    @Override
    public void onResume() {
        if (isRightHandMode != mPreferenceUtils.getBooleanParam(mContext
                .getString(R.string.right_hand_mode_key))){
            isRightHandMode = !isRightHandMode;
            if (isRightHandMode){
                view.setMenuGravity(Gravity.END);
            }else{
                view.setMenuGravity(Gravity.START);
            }
        }
        if (isCardItemLayout != mPreferenceUtils.getBooleanParam(mContext
                .getString(R.string.card_note_item_layout_key), true)){
            switchItemLayoutManager(!isCardItemLayout);
        }
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onStop() {
        view.closeDrawer();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
    }

    public boolean onKeyDown(int keyCode){
        if (keyCode == KeyEvent.KEYCODE_BACK){
            if (view.isDrawerOpen()){
                view.closeDrawer();
            }else {
                view.moveTaskToBack();
            }
            return true;
        }
        return false;
    }

    @Override
    public void attachView(View view) {
        this.view = (MainView)view;
    }

    public boolean onOptionsItemSelected(int id){
        switch (id){
            case R.id.setting: startSettingActivity();return true;
            case R.id.sync:
                if (view.isRefreshing()){
                    return true;
                }
                view.startRefresh();
                onRefresh();
                return true;
            case R.id.about: startAboutActivity();return true;
        }
        return false;
    }

    @Override
    public void onClick(android.view.View v) {
        startSettingActivity();
    }

    @Override
    public void onRefresh() {
        sync(EverNoteUtils.SyncType.ALL, false);
    }

    private void initDrawer(){
        drawerList = Arrays.asList(mContext.getResources()
                .getStringArray(R.array.drawer_content));
        view.initDrawerView(drawerList);
        view.setDrawerItemChecked(mCurrentNoteTypePage.getValue());
        view.setToolbarTitle(drawerList.get(mCurrentNoteTypePage.getValue()));
    }

    private void initMenuGravity(){
        boolean end = mPreferenceUtils.getBooleanParam(mContext.getString(R.string.right_hand_mode_key));
        if (end){
            view.setMenuGravity(Gravity.END);
        }else {
            view.setMenuGravity(Gravity.START);
        }
        isRightHandMode = end;
    }

    public void onDrawerItemSelect(int position){
        mCurrentNoteTypePage = SNote.NoteType.mapValueToStatus(position);
        switchNoteTypePage(mCurrentNoteTypePage);
        view.setDrawerItemChecked(position);
        switch (mCurrentNoteTypePage){
            case TRASH:
                view.showFab(false);view.enableSwipeRefreshLayout(false);break;
            default:
                view.showFab(true);view.enableSwipeRefreshLayout(true);break;
        }
    }

    private boolean onMenuItemActionExpand(){
        view.enableSwipeRefreshLayout(false);
        view.showFab(false);
        return true;
    }

    private boolean onMenuItemActionCollapse(){
        if (mCurrentNoteTypePage != SNote.NoteType.TRASH){
            view.enableSwipeRefreshLayout(true);
            view.showFab(true);
        }
        return true;
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return onMenuItemActionExpand();
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        return onMenuItemActionCollapse();
    }

    public void OnNavigationOnClick(){
        view.openOrCloseDrawer();
    }

    private void refreshNoteTypePage(){
        switchNoteTypePage(mCurrentNoteTypePage);
    }

    public void switchNoteTypePage(SNote.NoteType type){
        view.showProgressWheel(true);
        //TODO 分页，避免数据过多时加载太慢
        mObservableUtils.getLocalNotesByType(mFinalDb, type.getValue())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((notes) -> {
                    view.switchNoteTypePage(notes);
                    view.closeDrawer();
                    view.showProgressWheel(false);
                }, (e) -> {
                    e.printStackTrace();
                    view.showProgressWheel(false);
                });
    }

    public void onDrawerOpened(){
        view.setToolbarTitle(mContext.getResources().getString(R.string.app_name));
    }

    public void onDrawerClosed(){
        view.setToolbarTitle(drawerList.get(mCurrentNoteTypePage.getValue()));
    }

    public void initItemLayoutManager(){
        boolean card = mPreferenceUtils.getBooleanParam(mContext.getString(R.string.card_note_item_layout_key), true);
        switchItemLayoutManager(card);
    }

    private void switchItemLayoutManager(boolean card){
        if (card){
            view.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
        }else {
            view.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        }
        isCardItemLayout = card;
    }

    public void initRecyclerView(){
        view.showProgressWheel(true);
        mObservableUtils.getLocalNotesByType(mFinalDb, mCurrentNoteTypePage.getValue())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((notes) -> {
                    view.initRecyclerView(notes);
                    view.showProgressWheel(false);
                }, (e) -> {
                    e.printStackTrace();
                    view.showProgressWheel(false);
                });
    }


    private void startNoteActivity(int type, SNote value){
        Intent intent = new Intent(mContext, NoteActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(NotePresenter.OPERATE_NOTE_TYPE_KEY, type);
        EventBus.getDefault().postSticky(value);
        intent.putExtras(bundle);
        mContext.startActivity(intent);
    }

    public void startSettingActivity(){
        Intent intent = new Intent(mContext, SettingActivity.class);
        mContext.startActivity(intent);
    }

    public void startAboutActivity(){
        Intent intent = new Intent(mContext, AboutActivity.class);
        mContext.startActivity(intent);
    }

    public void onRecyclerViewItemClick(int position, SNote value){
        if (mCurrentNoteTypePage == SNote.NoteType.TRASH){
            return;
        }
        startNoteActivity(NotePresenter.VIEW_NOTE_MODE, value);
    }

    public void showPopMenu(android.view.View v, int position, SNote value){
        if (mCurrentNoteTypePage == SNote.NoteType.TRASH){
            view.showTrashPopupMenu(v, value);
        }else {
            view.showNormalPopupMenu(v, value);
        }
    }

    private void moveToTrash(SNote note){
        if (note == null)
            return;
        note.setType(SNote.NoteType.TRASH);
        note.setStatus(SNote.Status.NEED_REMOVE);
        mFinalDb.update(note);
        view.removeNote(note);
        sync(EverNoteUtils.SyncType.PUSH, true);
    }

    private void recoverNote(SNote note){
        if (note == null)
            return;
        note.setType(SNote.NoteType.NORMAL);
        note.setStatus(SNote.Status.NEED_PUSH);
        mFinalDb.update(note);
        view.removeNote(note);
        sync(EverNoteUtils.SyncType.PUSH, true);
    }

    public void onDeleteForeverDialogClick(SNote note, int which){
        if (which == Dialog.BUTTON_POSITIVE){
            mFinalDb.delete(note);
            view.removeNote(note);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    public boolean onPopupMenuClick(int id, SNote note){
        switch (id){
            case R.id.edit:
                startNoteActivity(NotePresenter.EDIT_NOTE_MODE, note);
                break;
            case R.id.move_to_trash:
                moveToTrash(note);
                break;
            case R.id.delete:
                view.showDeleteForeverDialog(note);
                break;
            case R.id.recover:
                recoverNote(note);
                break;
            default:
                break;
        }
        return true;
    }

    public void newNote(){
        SNote note = new SNote();
        note.setType(mCurrentNoteTypePage);
        startNoteActivity(NotePresenter.CREATE_NOTE_MODE, note);
    }

    public void sync(EverNoteUtils.SyncType type, boolean silence){
        //mEverNoteUtils.sync();
        mObservableUtils.sync(mEverNoteUtils, type)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((result -> {
                    if (!silence)
                        onEventMainThread(result);
                }));
    }

    public void onEventMainThread(EverNoteUtils.SyncResult result){
        if (result != EverNoteUtils.SyncResult.START)
            view.stopRefresh();
        switch (result){
            case ERROR_NOT_LOGIN: view.showGoBindEverNoteSnackbar(R.string.unbind_ever_note_tip, R.string.go_bind);break;
            case ERROR_EXPUNGE: view.showSnackbar(R.string.expunge_error);break;
            case ERROR_DELETE: view.showSnackbar(R.string.delete_error);break;
            case ERROR_FREQUENT_API: view.showSnackbar(R.string.frequent_api_tip);break;
            case ERROR_AUTH_EXPIRED: view.showSnackbar(R.string.error_auth_expired_tip);break;
            case ERROR_PERMISSION_DENIED: view.showSnackbar(R.string.error_permission_deny);break;
            case ERROR_QUOTA_EXCEEDED: view.showSnackbar(R.string.error_permission_deny);break;
            case ERROR_OTHER: view.showSnackbar(R.string.sync_fail);break;
            case START: break;
            case SUCCESS_SILENCE: break;
            case SUCCESS:view.showSnackbar(R.string.sync_success);refreshNoteTypePage();break;
        }
    }

    public void onEventMainThread(NotifyEvent event){
        switch (event.getType()){
            case NotifyEvent.REFRESH_LIST:
                view.startRefresh();
                onRefresh();
                break;
            case NotifyEvent.CREATE_NOTE:
                if (event.getData() instanceof SNote){
                    view.addNote((SNote) event.getData());
                    view.scrollRecyclerViewToTop();
                }
                sync(EverNoteUtils.SyncType.PUSH, true);
                break;
            case NotifyEvent.UPDATE_NOTE:
                if (event.getData() instanceof SNote){
                    view.updateNote((SNote) event.getData());
                    view.scrollRecyclerViewToTop();
                }
                sync(EverNoteUtils.SyncType.PUSH, true);
                break;
            case NotifyEvent.CHANGE_THEME:
                view.reCreate();
                break;
        }
    }

    public static class NotifyEvent<T>{
        public static final int REFRESH_LIST = 0;
        public static final int CREATE_NOTE = 1;
        public static final int UPDATE_NOTE = 2;
        public static final int CHANGE_THEME = 3;
        public static final int CHANGE_ITEM_LAYOUT = 4;
        public static final int CHANGE_MENU_GRAVITY = 5;
        private int type;
        private T data;
        @IntDef({REFRESH_LIST, CREATE_NOTE, UPDATE_NOTE, CHANGE_THEME,
                CHANGE_ITEM_LAYOUT, CHANGE_MENU_GRAVITY})
        public @interface Type {
        }

        public @Type int getType() {
            return type;
        }

        public void setType(@Type int type) {
            this.type = type;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }
    }
}
