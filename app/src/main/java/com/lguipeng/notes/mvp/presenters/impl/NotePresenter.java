package com.lguipeng.notes.mvp.presenters.impl;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.lguipeng.notes.R;
import com.lguipeng.notes.injector.ContextLifeCycle;
import com.lguipeng.notes.model.SNote;
import com.lguipeng.notes.mvp.presenters.Presenter;
import com.lguipeng.notes.mvp.views.View;
import com.lguipeng.notes.mvp.views.impl.NoteView;
import com.lguipeng.notes.ui.MainActivity;
import com.lguipeng.notes.utils.TimeUtils;

import net.tsz.afinal.FinalDb;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/**
 * Created by lgp on 2015/9/4.
 */
public class NotePresenter implements Presenter, android.view.View.OnFocusChangeListener,
        DialogInterface.OnClickListener, TextWatcher {
    private NoteView view;
    private final Context mContext;
    private FinalDb mFinalDb;
    private SNote note;
    private int operateMode = 0;
    public final static String OPERATE_NOTE_TYPE_KEY = "OPERATE_NOTE_TYPE_KEY";
    public final static int VIEW_NOTE_MODE = 0x00;
    public final static int EDIT_NOTE_MODE = 0x01;
    public final static int CREATE_NOTE_MODE = 0x02;
    @Inject
    public NotePresenter(@ContextLifeCycle("Activity") Context mContext, FinalDb mFinalDb) {
        this.mContext = mContext;
        this.mFinalDb = mFinalDb;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        EventBus.getDefault().registerSticky(this);
        view.showActivityInAnim();
    }

    @Override
    public void onResume() {

    }

    public void onPrepareOptionsMenu(){
        view.setDoneMenuItemVisible(false);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.done:
                saveNote();
                return true;
            case android.R.id.home:
                view.hideKeyBoard();
                if (view.isDoneMenuItemVisible()){
                    view.showNotSaveNoteDialog();
                    return true;
                }
                view.finishView();
            default:
                return false;
        }
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStop() {
        view.hideKeyBoard();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void attachView(View v) {
        this.view  = (NoteView)v;
    }

    public void attachIntent(Intent intent){
        parseIntent(intent);
    }

    public boolean onKeyDown(int keyCode){
        if (keyCode == KeyEvent.KEYCODE_BACK){
            view.hideKeyBoard();
            if (view.isDoneMenuItemVisible()){
                view.showNotSaveNoteDialog();
                return true;
            }
        }
        return false;
    }

    private void parseIntent(Intent intent){
        if (intent != null && intent.getExtras() != null){
            operateMode = intent.getExtras().getInt(OPERATE_NOTE_TYPE_KEY, 0);
        }
    }

    public void onEventMainThread(SNote note) {
        this.note = note;
        initToolbar();
        initEditText();
        initTextView();
    }

    private void initToolbar(){
        view.setToolbarTitle(R.string.view_note);
        switch (operateMode){
            case CREATE_NOTE_MODE: view.setToolbarTitle(R.string.new_note);break;
            case EDIT_NOTE_MODE: view.setToolbarTitle(R.string.edit_note);break;
            case VIEW_NOTE_MODE: view.setToolbarTitle(R.string.view_note);break;
            default:break;
        }
    }

    private void initEditText(){
        switch (operateMode){
            case EDIT_NOTE_MODE: view.initViewOnEditMode(note);break;
            case VIEW_NOTE_MODE: view.initViewOnViewMode(note);break;
            default:view.initViewOnCreateMode(note);break;
        }
    }

    private void initTextView(){
        view.setOperateTimeLineTextView(getOprTimeLineText(note));
    }

    private void saveNote(){
        view.hideKeyBoard();
        note.setLabel(view.getLabelText());
        note.setContent(view.getContentText());
        note.setLastOprTime(TimeUtils.getCurrentTimeInLong());
        note.setStatus(SNote.Status.NEED_PUSH.getValue());
        switch (operateMode){
            case CREATE_NOTE_MODE:
                note.setCreateTime(TimeUtils.getCurrentTimeInLong());
                mFinalDb.saveBindId(note);
                break;
            default:
                mFinalDb.update(note);
                break;
        }
        EventBus.getDefault().post(MainActivity.MainEvent.UPDATE_NOTE);
        view.finishView();
    }

    private String getOprTimeLineText(SNote note){
        if (note == null || note.getLastOprTime() == 0)
            return "";
        String create = mContext.getString(R.string.create);
        String edit = mContext.getString(R.string.last_update);
        StringBuilder sb = new StringBuilder();
        if (note.getLastOprTime() <= note.getCreateTime() || note.getCreateTime() == 0){
            sb.append(mContext.getString(R.string.note_log_text, create, TimeUtils.getTime(note.getLastOprTime())));
            return sb.toString();
        }
        sb.append(mContext.getString(R.string.note_log_text, edit, TimeUtils.getTime(note.getLastOprTime())));
        sb.append("\n");
        sb.append(mContext.getString(R.string.note_log_text, create, TimeUtils.getTime(note.getCreateTime())));
        return sb.toString();
    }

    @Override
    public void onFocusChange(android.view.View v, boolean hasFocus) {
        if (hasFocus){
            view.setToolbarTitle(R.string.edit_note);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (view.isDoneMenuItemNull())
            return;
        String labelSrc = view.getLabelText();
        String contentSrc = view.getContentText();
        String label = labelSrc.replaceAll("\\s*|\t|\r|\n", "");
        String content = contentSrc.replaceAll("\\s*|\t|\r|\n", "");
        if (!TextUtils.isEmpty(label) && !TextUtils.isEmpty(content)){
            if (TextUtils.equals(labelSrc, note.getLabel()) && TextUtils.equals(contentSrc, note.getContent())){
                view.setDoneMenuItemVisible(false);
                return;
            }
            view.setDoneMenuItemVisible(true);
        }else{
            view.setDoneMenuItemVisible(false);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which){
            case DialogInterface.BUTTON_POSITIVE:
                saveNote();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                view.finishView();
                break;
            default:
                break;
        }
    }
}
