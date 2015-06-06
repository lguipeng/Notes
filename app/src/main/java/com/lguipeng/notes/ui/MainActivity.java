package com.lguipeng.notes.ui;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.lguipeng.notes.R;
import com.lguipeng.notes.adpater.BaseRecyclerViewAdapter;
import com.lguipeng.notes.adpater.DrawerListAdapter;
import com.lguipeng.notes.adpater.NotesAdapter;
import com.lguipeng.notes.adpater.SimpleListAdapter;
import com.lguipeng.notes.listener.bmob.FindListenerImpl;
import com.lguipeng.notes.listener.bmob.SaveListenerImpl;
import com.lguipeng.notes.listener.bmob.UpdateListenerImpl;
import com.lguipeng.notes.model.CloudNote;
import com.lguipeng.notes.model.Note;
import com.lguipeng.notes.model.NoteOperateLog;
import com.lguipeng.notes.model.NoteType;
import com.lguipeng.notes.module.DataModule;
import com.lguipeng.notes.utils.AccountUtils;
import com.lguipeng.notes.utils.JsonUtils;
import com.lguipeng.notes.utils.NoteConfig;
import com.lguipeng.notes.utils.PreferenceUtils;
import com.lguipeng.notes.utils.SnackbarUtils;
import com.melnykov.fab.FloatingActionButton;
import com.melnykov.fab.ScrollDirectionListener;
import com.pnikosis.materialishprogress.ProgressWheel;

import net.tsz.afinal.FinalDb;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.InjectView;
import cn.bmob.v3.BmobQuery;
import de.greenrobot.event.EventBus;

/**
 * Created by lgp on 2015/5/24.
 */
public class MainActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener{

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.refresher)
    SwipeRefreshLayout refreshLayout;

    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;

    @InjectView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @InjectView(R.id.edit_note_type)
    Button editNoteTypeButton;

    @InjectView(R.id.left_drawer_listview)
    ListView mDrawerMenuListView;

    @InjectView(R.id.left_drawer)
    View drawerRootView;

    @InjectView(R.id.fab)
    FloatingActionButton fab;

    @InjectView(R.id.progress_wheel)
    ProgressWheel progressWheel;

    @Inject
    FinalDb finalDb;

    private ActionBarDrawerToggle mDrawerToggle;

    private SearchView searchView;

    private NotesAdapter recyclerAdapter;

    private int mCurrentNoteType;

    private boolean rightHandOn = false;

    private boolean cardLayout = true;

    private boolean hasUpdateNote = false;

    private PreferenceUtils mPreferenceUtils;

    private boolean hasEditClick = false;

    private  List<String> noteTypelist;

    private boolean hasSyncing = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferenceUtils = PreferenceUtils.getInstance(getApplicationContext());
        initToolbar();
        initDrawerView();
        initRecyclerView();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (rightHandOn != preferenceUtils.getBooleanParam(getString(R.string.right_hand_mode_key))){
            rightHandOn = !rightHandOn;
            if (rightHandOn){
                setMenuListViewGravity(Gravity.END);
            }else{
                setMenuListViewGravity(Gravity.START);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasUpdateNote){
            changeToSelectNoteType(mCurrentNoteType);
            hasUpdateNote = false;
        }
        if (cardLayout != preferenceUtils.getBooleanParam(getString(R.string.card_note_item_layout_key), true)){
            changeItemLayout(!cardLayout);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void onEvent(Integer event){
        switch (event){
            case NoteConfig.NOTE_UPDATE_EVENT:
                hasUpdateNote = true;
                break;
            case NoteConfig.NOTE_TYPE_UPDATE_EVENT:
                initDrawerListView();
        }
    }

    @Override
    protected int getLayoutView() {
        return R.layout.activity_main;
    }

    @Override
    protected List<Object> getModules() {
        return Arrays.<Object>asList(new DataModule());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
        if (toolbar != null){
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openOrCloseDrawer();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        //searchItem.expandActionView();
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        ComponentName componentName = getComponentName();

        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(componentName));
        searchView.setQueryHint(getString(R.string.search_note));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                recyclerAdapter.setUpFactor();
                recyclerAdapter.getFilter().filter(s);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        Intent intent;
        switch (item.getItemId()){
            case R.id.setting:
                intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
                return true;
            case R.id.sync:
                sync();
                return true;
            case R.id.about:
                intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
                return true;
            case android.R.id.home:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mDrawerLayout.isDrawerOpen(drawerRootView)){
            mDrawerLayout.closeDrawer(drawerRootView);
            return true;
        }
        moveTaskToBack(true);
        return super.onKeyDown(keyCode, event);
    }

    private void initToolbar(){
        super.initToolbar(toolbar);
    }

    private void initDrawerListView(){
        String json = mPreferenceUtils.getStringParam(PreferenceUtils.NOTE_TYPE_KEY);
        if (!TextUtils.isEmpty(json)){
            noteTypelist = JsonUtils.parseNoteType(json);
        }else{
            noteTypelist = Arrays.asList(getResources().getStringArray(R.array.drawer_content));
            NoteType type = new NoteType();
            type.setTypes(noteTypelist);
            String text = JsonUtils.jsonNoteType(type);
            mPreferenceUtils.saveParam(PreferenceUtils.NOTE_TYPE_KEY, text);
        }

        SimpleListAdapter adapter = new DrawerListAdapter(this, noteTypelist);
        mDrawerMenuListView.setAdapter(adapter);
        mDrawerMenuListView.setItemChecked(mCurrentNoteType, true);
        toolbar.setTitle(noteTypelist.get(mCurrentNoteType));
    }

    private void initDrawerView() {
        initDrawerListView();
        mDrawerMenuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDrawerMenuListView.setItemChecked(position, true);
                openOrCloseDrawer();
                mCurrentNoteType = position;
                changeToSelectNoteType(mCurrentNoteType);
                if (mCurrentNoteType == NoteConfig.NOTE_TRASH_TYPE) {
                    fab.hide();
                    fab.setVisibility(View.INVISIBLE);
                } else {
                    fab.setVisibility(View.VISIBLE);
                    fab.show();
                }
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, 0, 0){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
                toolbar.setTitle(R.string.app_name);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
                toolbar.setTitle(noteTypelist.get(mCurrentNoteType));
                if (hasEditClick){
                    Intent intent = new Intent(MainActivity.this, EditNoteTypeActivity.class);
                    startActivity(intent);
                    hasEditClick = false;
                }
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.setScrimColor(getColor(R.color.drawer_scrim_color));
        rightHandOn = preferenceUtils.getBooleanParam(getString(R.string.right_hand_mode_key));
        if (rightHandOn){
            setMenuListViewGravity(Gravity.END);
        }

        editNoteTypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hasEditClick = true;
                openOrCloseDrawer();

            }
        });
    }

    private void initRecyclerView(){
        showProgressWheel(true);
        initItemLayout();
        recyclerView.setHasFixedSize(true);
        recyclerAdapter = new NotesAdapter(initItemData(mCurrentNoteType));
        recyclerAdapter.setOnInViewClickListener(R.id.notes_item_root,
                new BaseRecyclerViewAdapter.onInternalClickListenerImpl<Note>() {
                    @Override
                    public void OnClickListener(View parentV, View v, Integer position, Note values) {
                        super.OnClickListener(parentV, v, position, values);
                        if (mCurrentNoteType == NoteConfig.NOTE_TRASH_TYPE)
                            return;
                        startNoteActivity(NoteActivity.VIEW_NOTE_TYPE, values);
                    }
                });
        recyclerAdapter.setOnInViewClickListener(R.id.note_more,
                new BaseRecyclerViewAdapter.onInternalClickListenerImpl<Note>() {
                    @Override
                    public void OnClickListener(View parentV, View v, Integer position, Note values) {
                        super.OnClickListener(parentV, v, position, values);
                        showPopupMenu(v, values);
                    }
                });
        recyclerAdapter.setFirstOnly(false);
        recyclerAdapter.setDuration(300);
        recyclerView.setAdapter(recyclerAdapter);
        fab.attachToRecyclerView(recyclerView, new ScrollDirectionListener() {
            @Override
            public void onScrollDown() {
                recyclerAdapter.setDownFactor();
            }

            @Override
            public void onScrollUp() {
                recyclerAdapter.setUpFactor();
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Note note = new Note();
                note.setType(mCurrentNoteType);
                startNoteActivity(NoteActivity.CREATE_NOTE_TYPE, note);
            }
        });
        showProgressWheel(false);
        refreshLayout.setColorSchemeColors(getColorPrimary());
        refreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {
        sync();
    }

    private void changeToSelectNoteType(int type){
        showProgressWheel(true);
        recyclerAdapter.setList(initItemData(type));
        showProgressWheel(false);
    }

    private void openOrCloseDrawer() {
        if (mDrawerLayout.isDrawerOpen(drawerRootView)) {
            mDrawerLayout.closeDrawer(drawerRootView);
        } else {
            mDrawerLayout.openDrawer(drawerRootView);
        }
    }

    private void showPopupMenu(View view, final Note note) {
        PopupMenu popup = new PopupMenu(this, view);
        //Inflating the Popup using xml file
        String move = getString(R.string.move_to);
        if (mCurrentNoteType == NoteConfig.NOTE_TRASH_TYPE){
            for (int i=0; i< noteTypelist.size()-1; i++){
                popup.getMenu().add(Menu.NONE, i, Menu.NONE, move + noteTypelist.get(i));
            }
            popup.getMenu().add(Menu.NONE, noteTypelist.size()-1, Menu.NONE, getString(R.string.delete_forever));
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();
                    if (id < noteTypelist.size() - 1) {
                        note.setType(id);
                        finalDb.update(note);
                        changeToSelectNoteType(mCurrentNoteType);
                    } else {
                        showDeleteForeverDialog(note);
                    }
                    return true;
                }
            });

        } else {
            popup.getMenuInflater()
                    .inflate(R.menu.menu_notes_more, popup.getMenu());
            popup.setOnMenuItemClickListener(
                    new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.delete_forever:
                                    showDeleteForeverDialog(note);
                                    break;
                                case R.id.edit:
                                    startNoteActivity(NoteActivity.EDIT_NOTE_TYPE, note);
                                    break;
                                case R.id.move_to_trash:
                                    note.setType(NoteConfig.NOTE_TRASH_TYPE);
                                    finalDb.update(note);
                                    changeToSelectNoteType(mCurrentNoteType);
                                    break;
                                default:
                                    break;
                            }
                            return true;
                        }
                    });
        }
        popup.show(); //showing popup menu
    }

    private void showDeleteForeverDialog(final Note note){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle(R.string.delete_forever_message);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        for (NoteOperateLog log : note.getLogs().getList()){
                            finalDb.delete(log);
                        }
                        finalDb.delete(note);
                        changeToSelectNoteType(mCurrentNoteType);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
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

    private void startNoteActivity(int oprType, Note value){
        Intent intent = new Intent(this, NoteActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(NoteActivity.OPERATE_NOTE_TYPE_KEY, oprType);
        EventBus.getDefault().postSticky(value);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void setMenuListViewGravity(int gravity){
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) drawerRootView.getLayoutParams();
        params.gravity = gravity;
        drawerRootView.setLayoutParams(params);
    }

    private List<Note> initItemData(int noteType) {
        List<Note> itemList = null;
        switch (noteType){
            case NoteConfig.NOTE_STUDY_TYPE:
            case NoteConfig.NOTE_WORK_TYPE:
            case NoteConfig.NOTE_OTHER_TYPE:
            case NoteConfig.NOTE_TRASH_TYPE:
                itemList = finalDb.findAllByWhere(Note.class, "type = " + noteType, "lastOprTime", true);
                break;
            default:
                break;
        }
        return itemList;
    }

    private void showProgressWheel(boolean visible){
        if (visible){
            if (!progressWheel.isSpinning())
                progressWheel.spin();
        }else{
            progressWheel.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (progressWheel.isSpinning()) {
                        progressWheel.stopSpinning();
                    }
                }
            }, 500);
        }
    }

    private void syncNotes(final String account){
        new Thread(){
            @Override
            public void run() {
                BmobQuery<CloudNote> query = new BmobQuery<>();
                query.addWhereEqualTo("email", account);
                query.findObjects(MainActivity.this, new FindListenerImpl<CloudNote>(){
                    CloudNote cloudNote;
                    @Override
                    public void onSuccess(List<CloudNote> notes) {
                        List<Note> list = finalDb.findAll(Note.class);
                        if (notes != null && notes.size() >= 1){
                            cloudNote = notes.get(0);
                            long localVersion = preferenceUtils.getLongParam(account);
                            if (cloudNote.getVersion() > localVersion){
                                //pull notes
                                preferenceUtils.saveParam(PreferenceUtils.NOTE_TYPE_KEY, cloudNote.getNoteType());
                                for (String string : cloudNote.getNoteList()) {
                                    Note note = JsonUtils.parseNote(string);
                                    if (note == null)
                                        continue;
                                    finalDb.saveBindId(note);
                                    NoteOperateLog log = new NoteOperateLog();
                                    log.setTime(note.getLastOprTime());
                                    log.setType(NoteConfig.NOTE_CREATE_OPR);
                                    log.setNote(note);
                                    finalDb.save(log);
                                }
                                preferenceUtils.saveParam(account, cloudNote.getVersion());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        initDrawerListView();
                                        changeToSelectNoteType(mCurrentNoteType);
                                        onSyncSuccess();
                                    }
                                });
                                return;
                            }else {
                                //upload notes
                                cloudNote.setVersion(++localVersion);
                            }
                        }else {
                            cloudNote = new CloudNote();
                            cloudNote.setEmail(account);
                            cloudNote.setVersion(1);

                        }
                        cloudNote.clearNotes();
                        for (Note note : list){
                            cloudNote.addNote(note);
                        }
                        String json = mPreferenceUtils.getStringParam(PreferenceUtils.NOTE_TYPE_KEY);
                        cloudNote.setNoteType(json);
                        if (TextUtils.isEmpty(cloudNote.getObjectId())){
                            cloudNote.save(MainActivity.this, new SaveListenerImpl() {
                                @Override
                                public void onSuccess() {
                                    preferenceUtils.saveParam(account, cloudNote.getVersion());
                                    onSyncSuccess();
                                }

                                @Override
                                public void onFailure(int i, String s) {
                                    super.onFailure(i, s);
                                    onSyncFail();
                                }
                            });
                        }else{
                            cloudNote.update(MainActivity.this, new UpdateListenerImpl() {
                                @Override
                                public void onSuccess() {
                                    preferenceUtils.saveParam(account, cloudNote.getVersion());
                                    onSyncSuccess();
                                }

                                @Override
                                public void onFailure(int i, String s) {
                                    super.onFailure(i, s);
                                    onSyncFail();
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(int i, String s) {
                        super.onError(i, s);
                        onSyncFail();
                    }
                });
            }
        }.start();
    }

    private void sync(){
        if (hasSyncing)
            return;
        String account = preferenceUtils.getStringParam(getString(R.string.sync_account_key));
        if (TextUtils.isEmpty(account)){
            AccountUtils.findValidAccount(getApplicationContext(), new AccountUtils.AccountFinderListener() {
                @Override
                protected void onNone() {
                    if (refreshLayout.isRefreshing()){
                        refreshLayout.setRefreshing(false);
                    }
                    SnackbarUtils.show(MainActivity.this, R.string.no_account_tip);
                }

                @Override
                protected void onOne(String account) {
                    preferenceUtils.saveParam(getString(R.string.sync_account_key), account);
                    hasSyncing = true;
                    syncNotes(account);
                }

                @Override
                protected void onMore(List<String> accountItems) {
                    if (refreshLayout.isRefreshing()){
                        refreshLayout.setRefreshing(false);
                    }
                    SnackbarUtils.show(MainActivity.this, R.string.no_account_tip);
                }
            });

        }else {
            if (!refreshLayout.isRefreshing()){
                refreshLayout.setRefreshing(true);
            }
            hasSyncing = true;
            syncNotes(account);
        }
    }

    private void onSyncSuccess(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hasSyncing = false;
                refreshLayout.setRefreshing(false);
                SnackbarUtils.show(MainActivity.this, R.string.sync_success);
            }
        });
    }

    private void onSyncFail(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hasSyncing = false;
                refreshLayout.setRefreshing(false);
                SnackbarUtils.show(MainActivity.this, R.string.sync_fail);
            }
        });
    }

    private void changeItemLayout(boolean flow){
        cardLayout = flow;
        if (!flow){
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        }else {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
        }
    }

    private void initItemLayout(){
        if (preferenceUtils.getBooleanParam(getString(R.string.card_note_item_layout_key), true)){
            cardLayout = true;
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
        }else {
            cardLayout = false;
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        }
    }
}
