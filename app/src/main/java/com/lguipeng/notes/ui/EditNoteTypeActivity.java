package com.lguipeng.notes.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.lguipeng.notes.R;
import com.lguipeng.notes.model.NoteType;
import com.lguipeng.notes.module.DataModule;
import com.lguipeng.notes.utils.JsonUtils;
import com.lguipeng.notes.utils.NoteConfig;
import com.lguipeng.notes.utils.PreferenceUtils;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Arrays;
import java.util.List;

import butterknife.InjectView;
import de.greenrobot.event.EventBus;

/**
 * Created by lgp on 2015/6/2.
 */
public class EditNoteTypeActivity extends BaseActivity{
    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.edit_root_view)
    LinearLayout editRootView;

    private MaterialEditText[] editTexts = new MaterialEditText[NoteType.ALL_COUNT - 1];

    private MenuItem doneMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initToolbar();
        initEditTextView();
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
                hideKeyBoard(editTexts[0]);
                NoteType type = new NoteType();
                for (MaterialEditText view : editTexts){
                    type.addType(view.getText().toString());
                }

                type.addType(getString(R.string.recycle_bin));
                String json = JsonUtils.jsonNoteType(type);
                preferenceUtils.saveParam(PreferenceUtils.NOTE_TYPE_KEY, json);
                EventBus.getDefault().post(NoteConfig.NOTE_TYPE_UPDATE_EVENT);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected int getLayoutView() {
        return R.layout.activity_edit_note_type;
    }

    @Override
    protected List<Object> getModules() {
        return Arrays.<Object>asList(new DataModule());
    }
    private void initToolbar(){
        super.initToolbar(toolbar);
        toolbar.setTitle(R.string.edit);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initEditTextView(){
        String json = preferenceUtils.getStringParam(PreferenceUtils.NOTE_TYPE_KEY);
        List<String> lists = JsonUtils.parseNoteType(json);
        if (lists == null)
            return;
        for (int i=0; i< editTexts.length; i++){
            MaterialEditText view = (MaterialEditText)getLayoutInflater().inflate(R.layout.edit_layout, null);
            view.addTextChangedListener(new SimpleTextWatcher());
            if (i < lists.size()){
                view.setText(lists.get(i));
                view.setSelection(lists.get(i).length());
            }
            editRootView.addView(view);
            editTexts[i] = view;
        }
    }

    class SimpleTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (doneMenuItem == null)
                return;
            boolean allFill = true;
            for (MaterialEditText view : editTexts){
                if (TextUtils.isEmpty(view.getText().toString()))
                    allFill = false;
            }
            if (allFill){
                doneMenuItem.setVisible(true);
            }else {
                doneMenuItem.setVisible(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    private void hideKeyBoard(EditText editText){
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }
}
