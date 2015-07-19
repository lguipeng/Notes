package com.lguipeng.notes.ui;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lguipeng.notes.R;
import com.lguipeng.notes.adpater.BaseRecyclerViewAdapter;
import com.lguipeng.notes.adpater.DrawerListAdapter;
import com.lguipeng.notes.adpater.NotesAdapter;
import com.lguipeng.notes.adpater.SimpleListAdapter;
import com.lguipeng.notes.model.SNote;
import com.lguipeng.notes.module.DataModule;
import com.lguipeng.notes.utils.DialogUtils;
import com.lguipeng.notes.utils.EverNoteUtils;
import com.lguipeng.notes.utils.SnackbarUtils;
import com.lguipeng.notes.utils.ThreadExecutorPool;
import com.lguipeng.notes.utils.ToolbarUtils;
import com.melnykov.fab.FloatingActionButton;
import com.melnykov.fab.ScrollDirectionListener;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.ActionClickListener;
import com.pnikosis.materialishprogress.ProgressWheel;

import net.tsz.afinal.FinalDb;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * Created by lgp on 2015/5/24.
 */
public class MainActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, ActionClickListener{

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.refresher)
    SwipeRefreshLayout refreshLayout;

    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;

    @InjectView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

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

    @Inject
    EverNoteUtils mEverNoteUtils;

    @Inject
    ThreadExecutorPool mThreadExecutorPool;

    private ActionBarDrawerToggle mDrawerToggle;

    private SearchView searchView;

    private NotesAdapter recyclerAdapter;

    private SNote.NoteType mCurrentNoteTypePage = SNote.NoteType.getDefault();

    private boolean rightHandOn = false;

    private boolean cardLayout = true;

    private  List<String> noteTypelist;

    private final String  CURRENT_NOTE_TYPE_KEY = "CURRENT_NOTE_TYPE_KEY";

    private final String  PROGRESS_WHEEL_KEY = "PROGRESS_WHEEL_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null){
            int value = savedInstanceState.getInt(CURRENT_NOTE_TYPE_KEY);
            mCurrentNoteTypePage = SNote.NoteType.mapValueToStatus(value);
            progressWheel.onRestoreInstanceState(savedInstanceState.getParcelable(PROGRESS_WHEEL_KEY));
        }
        initToolbar();
        initDrawerView();
        initRecyclerView();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_NOTE_TYPE_KEY, mCurrentNoteTypePage.getValue());
        Parcelable parcelable = progressWheel.onSaveInstanceState();
        outState.putParcelable(PROGRESS_WHEEL_KEY, parcelable);
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
        if (cardLayout != preferenceUtils.getBooleanParam(getString(R.string.card_note_item_layout_key), true)){
            changeItemLayout(!cardLayout);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeDrawer();
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

    public void onEventMainThread(MainEvent event){
        switch (event){
            case UPDATE_NOTE:
                changeToSelectNoteType(mCurrentNoteTypePage);
                mEverNoteUtils.syncSilence(EverNoteUtils.SyncType.PUSH);
                break;
            case CHANGE_THEME:
                this.recreate();
                break;
        }
    }

    public void onEventMainThread(EverNoteUtils.SyncResult result){
        if (result != EverNoteUtils.SyncResult.START)
            refreshLayout.setRefreshing(false);
        switch (result){
            case ERROR_NOT_LOGIN:
                showSnackbar(R.string.unbind_ever_note_tip, R.string.go_bind);
                break;
            case ERROR_EXPUNGE:
                showSnackbar(R.string.expunge_error);
                break;
            case ERROR_DELETE:
                showSnackbar(R.string.delete_error);
                break;
            case ERROR_FREQUENT_API:
                showSnackbar(R.string.frequent_api_tip);
                break;
            case ERROR_AUTH_EXPIRED:
                showSnackbar(R.string.error_auth_expired_tip);
                break;
            case ERROR_PERMISSION_DENIED:
                showSnackbar(R.string.error_permission_deny);
                break;
            case ERROR_QUOTA_EXCEEDED:
                showSnackbar(R.string.error_permission_deny);
                break;
            case ERROR_OTHER:
                showSnackbar(R.string.sync_fail);
                break;
            case START:
                //SnackbarUtils.show(this, R.string.syncing);
                break;
            case SUCCESS_SILENCE:
                break;
            case SUCCESS:
                showSnackbar(R.string.sync_success);
                changeToSelectNoteType(mCurrentNoteTypePage);
                break;
        }
    }

    private void showSnackbar(int message){
        SnackbarUtils.show(this, message, fab);
    }

    private void showSnackbar(int message, int action){
        SnackbarUtils.showAction(this, message
                , action, this, fab);
    }

    @Override
    public void onActionClicked(Snackbar snackbar) {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
        SnackbarUtils.dismiss();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
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
            toolbar.setNavigationOnClickListener((view) -> openOrCloseDrawer());
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
                recyclerAdapter.getFilter().filter(s);
                return true;
            }
        });
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                recyclerAdapter.setUpFactor();
                refreshLayout.setEnabled(false);
                fab.hide();
                fab.setVisibility(View.INVISIBLE);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                refreshLayout.setEnabled(true);
                if (mCurrentNoteTypePage == SNote.NoteType.TRASH)
                    return true;
                fab.show();
                fab.setVisibility(View.VISIBLE);
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
                //sync();
                refreshLayout.setRefreshing(true);
                mEverNoteUtils.sync();
                return true;
            case R.id.about:
                intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            if (mDrawerLayout.isDrawerOpen(drawerRootView)){
                mDrawerLayout.closeDrawer(drawerRootView);
            }else {
                moveTaskToBack(true);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void initToolbar(){
        ToolbarUtils.initToolbar(toolbar, this);
    }

    private void initDrawerView() {
        noteTypelist = Arrays.asList(getResources().getStringArray(R.array.drawer_content));
        SimpleListAdapter adapter = new DrawerListAdapter(this, noteTypelist);
        mDrawerMenuListView.setAdapter(adapter);
        toolbar.setTitle(noteTypelist.get(mCurrentNoteTypePage.getValue()));
        mDrawerMenuListView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            mCurrentNoteTypePage = SNote.NoteType.mapValueToStatus(position);
            changeToSelectNoteType(mCurrentNoteTypePage);
            mDrawerMenuListView.setItemChecked(position, true);
            if (mCurrentNoteTypePage == SNote.NoteType.TRASH) {
                fab.hide();
                fab.setVisibility(View.INVISIBLE);
                refreshLayout.setEnabled(false);
            } else {
                fab.setVisibility(View.VISIBLE);
                fab.show();
                refreshLayout.setEnabled(true);
            }
        });

        mDrawerMenuListView.setItemChecked(mCurrentNoteTypePage.getValue(), true);
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
                toolbar.setTitle(noteTypelist.get(mCurrentNoteTypePage.getValue()));
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.setScrimColor(getColor(R.color.drawer_scrim_color));
        rightHandOn = preferenceUtils.getBooleanParam(getString(R.string.right_hand_mode_key));
        if (rightHandOn){
            setMenuListViewGravity(Gravity.END);
        }
    }

    private void initRecyclerView(){
        showProgressWheel(true);
        initItemLayout();
        recyclerView.setHasFixedSize(true);
        recyclerAdapter = new NotesAdapter(initItemData(mCurrentNoteTypePage.getValue()), this);
        recyclerAdapter.setOnInViewClickListener(R.id.notes_item_root,
                new BaseRecyclerViewAdapter.onInternalClickListenerImpl<SNote>() {
                    @Override
                    public void OnClickListener(View parentV, View v, Integer position, SNote values) {
                        super.OnClickListener(parentV, v, position, values);
                        if (mCurrentNoteTypePage == SNote.NoteType.TRASH){
                            return;
                        }
                        refreshLayout.setEnabled(true);
                        startNoteActivity(NoteActivity.VIEW_NOTE_TYPE, values);
                    }
                });
        recyclerAdapter.setOnInViewClickListener(R.id.note_more,
                new BaseRecyclerViewAdapter.onInternalClickListenerImpl<SNote>() {
                    @Override
                    public void OnClickListener(View parentV, View v, Integer position, SNote values) {
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
        showProgressWheel(false);
        refreshLayout.setColorSchemeColors(getColorPrimary());
        refreshLayout.setOnRefreshListener(this);
    }

    @OnClick(R.id.fab)
    public void newNote(View view){
        SNote note = new SNote();
        note.setType(mCurrentNoteTypePage);
        startNoteActivity(NoteActivity.CREATE_NOTE_TYPE, note);
    }

    @Override
    public void onRefresh() {
        mEverNoteUtils.sync();
    }

    private void changeToSelectNoteType(final SNote.NoteType type){
        showProgressWheel(true);
        mThreadExecutorPool.execute(() -> {
            final List<SNote> list = initItemData(type.getValue());
            recyclerAdapter.setList(list);
            runOnUiThread(() -> {
                recyclerAdapter.notifyDataSetChanged();
                closeDrawer();
            });
        });
        showProgressWheel(false);
    }

    private void openDrawer() {
        if (!mDrawerLayout.isDrawerOpen(drawerRootView)) {
            mDrawerLayout.openDrawer(drawerRootView);
        }
    }

    private void closeDrawer() {
        if (mDrawerLayout.isDrawerOpen(drawerRootView)) {
            mDrawerLayout.closeDrawer(drawerRootView);
        }
    }

    private void openOrCloseDrawer() {
        if (mDrawerLayout.isDrawerOpen(drawerRootView)) {
            mDrawerLayout.closeDrawer(drawerRootView);
        } else {
            mDrawerLayout.openDrawer(drawerRootView);
        }
    }

    private void showPopupMenu(View view, final SNote note) {
        PopupMenu popup = new PopupMenu(this, view);
        //Inflating the Popup using xml file
        if (mCurrentNoteTypePage == SNote.NoteType.TRASH){
            popup.getMenuInflater()
                    .inflate(R.menu.menu_notes_trash_more, popup.getMenu());
            popup.setOnMenuItemClickListener((item) -> {
                int id = item.getItemId();
                switch (id) {
                    case R.id.delete:
                        showDeleteForeverDialog(note);
                        break;
                    case R.id.recover:
                        note.setType(SNote.NoteType.NORMAL);
                        note.setStatus(SNote.Status.NEED_PUSH);
                        finalDb.update(note);
                        changeToSelectNoteType(mCurrentNoteTypePage);
                        try {
                            mEverNoteUtils.syncSilence(EverNoteUtils.SyncType.PUSH);
                        }catch (Exception e){
                            e.printStackTrace();
                            EventBus.getDefault().post(EverNoteUtils.SyncResult.ERROR_RECOVER);
                        }
                        break;
                    default:
                        break;
                }
                return true;
            });

        } else {
            popup.getMenuInflater()
                    .inflate(R.menu.menu_notes_more, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.edit:
                        startNoteActivity(NoteActivity.EDIT_NOTE_TYPE, note);
                        break;
                    case R.id.move_to_trash:
                        note.setType(SNote.NoteType.TRASH);
                        note.setStatus(SNote.Status.NEED_REMOVE);
                        finalDb.update(note);
                        changeToSelectNoteType(mCurrentNoteTypePage);
                        try {
                            mEverNoteUtils.syncSilence(EverNoteUtils.SyncType.PUSH);
                        } catch (Exception e) {
                            e.printStackTrace();
                            EventBus.getDefault().post(EverNoteUtils.SyncResult.ERROR_DELETE);
                        }
                        break;
                    default:
                        break;
                }
                return true;
            });
        }
        popup.show(); //showing popup menu
    }

    private void showDeleteForeverDialog(final SNote note){
        AlertDialog.Builder builder = DialogUtils.makeDialogBuilderByTheme(this);
        builder.setTitle(R.string.delete_tip);
        DialogInterface.OnClickListener listener = (DialogInterface dialog, int which) -> {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    finalDb.delete(note);
                    changeToSelectNoteType(mCurrentNoteTypePage);
                    // ever note permission denny, so remove
                        /*
                        mThreadExecutorPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    mEverNoteUtils.expungeNote(guid);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }finally {
                                    EventBus.getDefault().post(EverNoteUtils.SyncResult.ERROR_EXPUNGE);
                                }
                            }
                        });
                        */
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

    private void startNoteActivity(int oprType, SNote value){
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

    private List<SNote> initItemData(int type) {
        List<SNote> itemList;
        itemList = finalDb.findAllByWhere(SNote.class, "type = " + type
                , "lastOprTime", true);
        return itemList;
    }

    private void showProgressWheel(boolean visible){
        progressWheel.setBarColor(getColorPrimary());
        if (visible){
            if (!progressWheel.isSpinning())
                progressWheel.spin();
        }else{
            progressWheel.postDelayed(() -> {
                if (progressWheel.isSpinning()) {
                    progressWheel.stopSpinning();
                }
            }, 300);
        }
    }

    private void onSyncSuccess(){
        runOnUiThread(() -> showSnackbar(R.string.sync_success));
    }

    private void onSyncFail(){
        runOnUiThread(() -> showSnackbar( R.string.sync_fail));
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

    /*
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
                        List<SNote> list = finalDb.findAll(SNote.class);
                        if (notes != null && notes.size() >= 1){
                            cloudNote = notes.get(0);
                            long localVersion = preferenceUtils.getLongParam(account);
                            if (cloudNote.getVersion() > localVersion){
                                //pull notes
                                preferenceUtils.saveParam(PreferenceUtils.NOTE_TYPE_KEY, cloudNote.getNoteType());
                                for (String string : cloudNote.getNoteList()) {
                                    SNote note = JsonUtils.parseNote(string);
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
                                        changeToSelectNoteType(mCurrentNoteTypePage);
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
                        for (SNote note : list){
                            cloudNote.addNote(note);
                        }
                        String json = preferenceUtils.getStringParam(PreferenceUtils.NOTE_TYPE_KEY);
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
    */

    public enum MainEvent{
        UPDATE_NOTE,
        CHANGE_THEME
    }
}
