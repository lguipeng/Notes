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
import com.lguipeng.notes.model.Note;
import com.lguipeng.notes.model.NoteOperateLog;
import com.lguipeng.notes.module.DataModule;
import com.lguipeng.notes.utils.NoteConfig;
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

    private Note note;

    public final static String OPERATE_NOTE_TYPE_KEY = "OPERATE_NOTE_TYPE_KEY";

    public final static String NOTE_VALUE_KEY = "NOTE_VALUE_KEY";

    public final static String NOTE_TYPE_KEY = "NOTE_TYPE_KEY";


    public final static int VIEW_NOTE_TYPE = 0x00;
    public final static int EDIT_NOTE_TYPE = 0x01;
    public final static int CREATE_NOTE_TYPE = 0x02;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseIntent(getIntent());
        EventBus.getDefault().registerSticky(this);
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
        return Arrays.<Object>asList(new DataModule());
    }

    public void onEventMainThread(Note note) {
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

    private void initToolbar(){
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
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (doneMenuItem.isVisible()){
                    showNotSaveNoteDialog();
                    return;
                }
                finish();
            }
        });
    }

    private void initEditText(){
        switch (operateNoteType){
            case EDIT_NOTE_TYPE:
                labelEditText.requestFocus();
                labelEditText.setText(note.getLabel());
                contentEditText.setText(note.getContent());
                labelEditText.setSelection(note.getLabel().length());
                contentEditText.setSelection(note.getContent().length());
                break;
            case VIEW_NOTE_TYPE:
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
        boolean all = preferenceUtils.getBooleanParam(getString(R.string.show_note_history_log_key));
        oprTimeLineTextView.setText(getOprTimeLineText(note, all));
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            if (doneMenuItem.isVisible()){
                showNotSaveNoteDialog();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showNotSaveNoteDialog(){
        hideKeyBoard(labelEditText);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle(R.string.not_save_note_leave_tip);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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

        NoteOperateLog log = new NoteOperateLog();
        log.setTime(TimeUtils.getCurrentTimeInLong());
        switch (operateNoteType){
            case CREATE_NOTE_TYPE:
                finalDb.saveBindId(note);
                log.setType(NoteConfig.NOTE_CREATE_OPR);
                log.setNote(note);
                finalDb.save(log);
                break;
            default:
                finalDb.update(note);
                log.setType(NoteConfig.NOTE_EDIT_OPR);
                log.setNote(note);
                finalDb.save(log);
                break;
        }
        EventBus.getDefault().post(NoteConfig.NOTE_UPDATE_EVENT);
        finish();
    }

    class SimpleTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (doneMenuItem == null)
                return;
            if (!TextUtils.isEmpty(labelEditText.getText().toString()) &&
                    !TextUtils.isEmpty(contentEditText.getText().toString())){
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

    private String getOprTimeLineText(Note note, boolean all){
        if (note == null || note.getLogs() == null)
            return "";
        String create = getString(R.string.create);
        String edit = getString(R.string.edit);
        StringBuilder sb = new StringBuilder();
        if (note.getLogs().getList().size() <= 0){
            return "";
        }
        NoteOperateLog log;
        List<NoteOperateLog> logs = note.getLogs().getList();
        int size = logs.size();
        if (!all){
            log = logs.get(size - 1);
            if (log.getType() == NoteConfig.NOTE_CREATE_OPR){
                sb.append(create + " :  " + TimeUtils.getTime(log.getTime()));
            }else{
                sb.append(edit + " :  " + TimeUtils.getTime(log.getTime()));
            }
            return sb.toString();
        }
        for (int i=size-1; i>=0; i--){
            log = logs.get(i);
            if (log.getType() == NoteConfig.NOTE_CREATE_OPR){
                sb.append(create + " :  " + TimeUtils.getTime(log.getTime()));
            }else{
                sb.append(edit + " :  " + TimeUtils.getTime(log.getTime()));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private void hideKeyBoard(EditText editText){
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }
}
