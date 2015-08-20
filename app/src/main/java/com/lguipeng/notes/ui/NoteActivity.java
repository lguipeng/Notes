package com.lguipeng.notes.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.lguipeng.notes.R;
import com.lguipeng.notes.model.SNote;
import com.lguipeng.notes.module.DataModule;
import com.lguipeng.notes.utils.DialogUtils;
import com.lguipeng.notes.utils.TimeUtils;
import com.rengwuxian.materialedittext.MaterialEditText;

import net.tsz.afinal.FinalDb;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.InjectView;
import de.greenrobot.event.EventBus;

/**
 * Created by lgp on 2015/5/25.
 */
public class NoteActivity extends BaseActivity{
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.label_edit_text)
    MaterialEditText labelEditText;
    @InjectView(R.id.content_edit_text)
    MaterialEditText contentEditText;
    @InjectView(R.id.opr_time_line_text)
    TextView oprTimeLineTextView;
    @Inject
    FinalDb finalDb;
    private MenuItem doneMenuItem;
    private int operateNoteType = 0;
    private SNote note;
    public final static String OPERATE_NOTE_TYPE_KEY = "OPERATE_NOTE_TYPE_KEY";
    public final static int VIEW_NOTE_TYPE = 0x00;
    public final static int EDIT_NOTE_TYPE = 0x01;
    public final static int CREATE_NOTE_TYPE = 0x02;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseIntent(getIntent());
        EventBus.getDefault().registerSticky(this);
        showActivityInAnim();
    }

    @Override
    protected void onStop() {
        hideKeyBoard(labelEditText);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }


    @Override
    protected int getLayoutView() {
        return R.layout.activity_note;
    }

    @Override
    protected List<Object> getModules() {
        return Arrays.asList(new DataModule());
    }

    public void onEventMainThread(SNote note) {
        this.note = note;
        initToolbar();
        initEditText();
        initTextView();
    }

    private void parseIntent(Intent intent){
        if (intent != null && intent.getExtras() != null){
            operateNoteType = intent.getExtras().getInt(OPERATE_NOTE_TYPE_KEY, 0);
        }
    }

    @Override
    protected void initToolbar(){
        super.initToolbar(toolbar);
        toolbar.setTitle(R.string.view_note);
        switch (operateNoteType){
            case CREATE_NOTE_TYPE:
                toolbar.setTitle(R.string.new_note);
                break;
            case EDIT_NOTE_TYPE:
                toolbar.setTitle(R.string.edit_note);
                break;
            case VIEW_NOTE_TYPE:
                toolbar.setTitle(R.string.view_note);
                break;
            default:
                break;
        }
    }

    private void initEditText(){
        switch (operateNoteType){
            case EDIT_NOTE_TYPE:
                showKeyBoard();
                labelEditText.requestFocus();
                labelEditText.setText(note.getLabel());
                contentEditText.setText(note.getContent());
                labelEditText.setSelection(note.getLabel().length());
                contentEditText.setSelection(note.getContent().length());
                break;
            case VIEW_NOTE_TYPE:
                hideKeyBoard(labelEditText);
                labelEditText.setText(note.getLabel());
                contentEditText.setText(note.getContent());
                labelEditText.setOnFocusChangeListener(new SimpleOnFocusChangeListener());
                contentEditText.setOnFocusChangeListener(new SimpleOnFocusChangeListener());
                break;
            default:
                labelEditText.requestFocus();
                break;
        }
        labelEditText.addTextChangedListener(new SimpleTextWatcher());
        contentEditText.addTextChangedListener(new SimpleTextWatcher());
    }

    private void initTextView(){
        //boolean all = preferenceUtils.getBooleanParam(getString(R.string.show_note_history_log_key));
        oprTimeLineTextView.setText(getOprTimeLineText(note));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        doneMenuItem = menu.getItem(0);
        doneMenuItem.setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.done:
                saveNote();
                return true;
            case android.R.id.home:
                hideKeyBoard(labelEditText);
                if (doneMenuItem.isVisible()){
                    showNotSaveNoteDialog();
                    return true;
                }
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            hideKeyBoard(labelEditText);
            if (doneMenuItem != null && doneMenuItem.isVisible()){
                showNotSaveNoteDialog();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showNotSaveNoteDialog(){

        AlertDialog.Builder builder = DialogUtils.makeDialogBuilderByTheme(this);
        builder.setTitle(R.string.not_save_note_leave_tip);
        DialogInterface.OnClickListener listener = (DialogInterface dialog, int which) ->{
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    saveNote();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    NoteActivity.this.finish();
                    break;
                default:
                    break;
            }
        };
        builder.setPositiveButton(R.string.sure, listener);
        builder.setNegativeButton(R.string.cancel, listener);
        builder.show();
    }

    private void saveNote(){
        hideKeyBoard(labelEditText);
        note.setLabel(labelEditText.getText().toString());
        note.setContent(contentEditText.getText().toString());
        note.setLastOprTime(TimeUtils.getCurrentTimeInLong());
        note.setStatus(SNote.Status.NEED_PUSH.getValue());
        switch (operateNoteType){
            case CREATE_NOTE_TYPE:
                note.setCreateTime(TimeUtils.getCurrentTimeInLong());
                finalDb.saveBindId(note);
                break;
            default:
                finalDb.update(note);
                break;
        }
        EventBus.getDefault().post(MainActivity.MainEvent.UPDATE_NOTE);
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        showActivityExitAnim();
    }

    class SimpleTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (doneMenuItem == null)
                return;
            String labelSrc = labelEditText.getText().toString();
            String contentSrc = contentEditText.getText().toString();
            String label = labelSrc.replaceAll("\\s*|\t|\r|\n", "");
            String content = contentSrc.replaceAll("\\s*|\t|\r|\n", "");
            if (!TextUtils.isEmpty(label) && !TextUtils.isEmpty(content)){
                if (TextUtils.equals(labelSrc, note.getLabel()) && TextUtils.equals(contentSrc, note.getContent())){
                    doneMenuItem.setVisible(false);
                    return;
                }
                doneMenuItem.setVisible(true);
            }else{
                doneMenuItem.setVisible(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    class SimpleOnFocusChangeListener implements View.OnFocusChangeListener {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus && toolbar != null){
                toolbar.setTitle(R.string.edit_note);
            }
        }
    }

    private String getOprTimeLineText(SNote note){
        if (note == null || note.getLastOprTime() == 0)
            return "";
        String create = getString(R.string.create);
        String edit = getString(R.string.last_update);
        StringBuilder sb = new StringBuilder();
        if (note.getLastOprTime() <= note.getCreateTime() || note.getCreateTime() == 0){
            sb.append(getString(R.string.note_log_text, create, TimeUtils.getTime(note.getLastOprTime())));
            return sb.toString();
        }
        sb.append(getString(R.string.note_log_text, edit, TimeUtils.getTime(note.getLastOprTime())));
        sb.append("\n");
        sb.append(getString(R.string.note_log_text, create, TimeUtils.getTime(note.getCreateTime())));
        return sb.toString();
    }

    private void hideKeyBoard(EditText editText){
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    private void showKeyBoard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void showActivityInAnim(){
        overridePendingTransition(R.anim.activity_down_up_anim, R.anim.activity_exit_anim);
    }
    private void showActivityExitAnim(){
        overridePendingTransition(R.anim.activity_exit_anim, R.anim.activity_up_down_anim);
    }
}
