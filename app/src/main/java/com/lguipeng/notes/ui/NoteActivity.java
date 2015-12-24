package com.lguipeng.notes.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.lguipeng.notes.App;
import com.lguipeng.notes.R;
import com.lguipeng.notes.injector.component.DaggerActivityComponent;
import com.lguipeng.notes.injector.module.ActivityModule;
import com.lguipeng.notes.model.SNote;
import com.lguipeng.notes.mvp.presenters.impl.NotePresenter;
import com.lguipeng.notes.mvp.views.impl.NoteView;
import com.lguipeng.notes.utils.DialogUtils;
import com.rengwuxian.materialedittext.MaterialEditText;

import javax.inject.Inject;

import butterknife.Bind;

/**
 * Created by lgp on 2015/5/25.
 */
public class NoteActivity extends BaseActivity implements NoteView{
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.label_edit_text) MaterialEditText labelEditText;
    @Bind(R.id.content_edit_text) MaterialEditText contentEditText;
    @Bind(R.id.opr_time_line_text) TextView oprTimeLineTextView;
    @Inject NotePresenter notePresenter;
    private MenuItem doneMenuItem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializePresenter();
        notePresenter.onCreate(savedInstanceState);
    }

    private void initializePresenter() {
        notePresenter.attachView(this);
        notePresenter.attachIntent(getIntent());
    }

    @Override
    protected void initializeDependencyInjector() {
        App app = (App) getApplication();
        mActivityComponent = DaggerActivityComponent.builder()
                .activityModule(new ActivityModule(this))
                .appComponent(app.getAppComponent())
                .build();
        mActivityComponent.inject(this);
    }

    @Override
    protected void onStop() {
        notePresenter.onStop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        notePresenter.onDestroy();
        super.onDestroy();
    }


    @Override
    protected int getLayoutView() {
        return R.layout.activity_note;
    }

    @Override
    protected void initToolbar(){
        super.initToolbar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        doneMenuItem = menu.getItem(0);
        notePresenter.onPrepareOptionsMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (notePresenter.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return notePresenter.onKeyDown(keyCode) || super.onKeyDown(keyCode, event);
    }

    @Override
    public void finishView() {
        finish();
    }

    @Override
    public void setToolbarTitle(String title) {
        if (toolbar != null){
            toolbar.setTitle(title);
        }
    }

    @Override
    public void setToolbarTitle(int title) {
        if (toolbar != null){
            toolbar.setTitle(title);
        }
    }

    @Override
    public void initViewOnEditMode(SNote note) {
        showKeyBoard();
        labelEditText.requestFocus();
        labelEditText.setText(note.getLabel());
        contentEditText.setText(note.getContent());
        labelEditText.setSelection(note.getLabel().length());
        contentEditText.setSelection(note.getContent().length());
        labelEditText.addTextChangedListener(notePresenter);
        contentEditText.addTextChangedListener(notePresenter);
    }

    @Override
    public void initViewOnViewMode(SNote note) {
        hideKeyBoard();
        labelEditText.setText(note.getLabel());
        contentEditText.setText(note.getContent());
        labelEditText.setOnFocusChangeListener(notePresenter);
        contentEditText.setOnFocusChangeListener(notePresenter);
        labelEditText.addTextChangedListener(notePresenter);
        contentEditText.addTextChangedListener(notePresenter);
    }

    @Override
    public void initViewOnCreateMode(SNote note) {
        labelEditText.requestFocus();
        //labelEditText.addTextChangedListener(notePresenter);
        contentEditText.addTextChangedListener(notePresenter);
    }

    @Override
    public void setOperateTimeLineTextView(String text) {
        oprTimeLineTextView.setText(text);
    }

    @Override
    public void setDoneMenuItemVisible(boolean visible) {
        if (doneMenuItem != null){
            doneMenuItem.setVisible(visible);
        }
    }

    @Override
    public boolean isDoneMenuItemVisible() {
        return doneMenuItem != null && doneMenuItem.isVisible();
    }

    @Override
    public boolean isDoneMenuItemNull() {
        return doneMenuItem == null;
    }

    @Override
    public String getLabelText() {
        return labelEditText.getText().toString();
    }

    @Override
    public String getContentText() {
        return contentEditText.getText().toString();
    }

    @Override
    public void showNotSaveNoteDialog(){
        AlertDialog.Builder builder = DialogUtils.makeDialogBuilder(this);
        builder.setTitle(R.string.not_save_note_leave_tip);
        builder.setPositiveButton(R.string.sure, notePresenter);
        builder.setNegativeButton(R.string.cancel, notePresenter);
        builder.show();
    }

    @Override
    public void hideKeyBoard(){
        hideKeyBoard(labelEditText);
    }

    @Override
    public void showKeyBoard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void hideKeyBoard(EditText editText){
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }
}
