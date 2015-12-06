package com.lguipeng.notes.ui;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lguipeng.notes.App;
import com.lguipeng.notes.R;
import com.lguipeng.notes.adpater.base.BaseRecyclerViewAdapter;
import com.lguipeng.notes.adpater.DrawerListAdapter;
import com.lguipeng.notes.adpater.NotesAdapter;
import com.lguipeng.notes.adpater.SimpleListAdapter;
import com.lguipeng.notes.injector.component.DaggerActivityComponent;
import com.lguipeng.notes.injector.module.ActivityModule;
import com.lguipeng.notes.model.SNote;
import com.lguipeng.notes.mvp.presenters.impl.MainPresenter;
import com.lguipeng.notes.mvp.views.impl.MainView;
import com.lguipeng.notes.utils.DialogUtils;
import com.lguipeng.notes.utils.SnackbarUtils;
import com.lguipeng.notes.utils.ToolbarUtils;
import com.lguipeng.notes.view.BetterFab;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by lgp on 2015/5/24.
 */
public class MainActivity extends BaseActivity implements MainView{
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.refresher) SwipeRefreshLayout refreshLayout;
    @Bind(R.id.recyclerView) RecyclerView recyclerView;
    @Bind(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @Bind(R.id.left_drawer_listview) ListView mDrawerMenuListView;
    @Bind(R.id.left_drawer) View drawerRootView;
    @Bind(R.id.fab) BetterFab fab;
    @Bind(R.id.coordinator_layout) CoordinatorLayout coordinatorLayout;
    @Bind(R.id.progress_wheel) ProgressWheel progressWheel;
    @Inject MainPresenter mainPresenter;
    private ActionBarDrawerToggle mDrawerToggle;
    private NotesAdapter recyclerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializePresenter();
        mainPresenter.onCreate(savedInstanceState);
    }

    private void initializePresenter() {
        mainPresenter.attachView(this);
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mainPresenter.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mainPresenter.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainPresenter.onResume();
    }

    @Override
    protected void onPause() {
        mainPresenter.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        mainPresenter.onStop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        mainPresenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void initToolbar(){
        ToolbarUtils.initToolbar(toolbar, this);
    }

    @Override
    public void initDrawerView(List<String> list) {
        SimpleListAdapter adapter = new DrawerListAdapter(this, list);
        mDrawerMenuListView.setAdapter(adapter);
        mDrawerMenuListView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
                mainPresenter.onDrawerItemSelect(position));
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, 0, 0){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
                mainPresenter.onDrawerOpened();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
                mainPresenter.onDrawerClosed();
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.setScrimColor(getCompactColor(R.color.drawer_scrim_color));
    }

    @Override
    public void initRecyclerView(List<SNote> notes){
        recyclerAdapter = new NotesAdapter(notes, this);
        recyclerView.setHasFixedSize(true);
        recyclerAdapter.setOnInViewClickListener(R.id.notes_item_root,
                new BaseRecyclerViewAdapter.onInternalClickListenerImpl<SNote>() {
                    @Override
                    public void OnClickListener(View parentV, View v, Integer position, SNote values) {
                        super.OnClickListener(parentV, v, position, values);
                        mainPresenter.onRecyclerViewItemClick(position, values);
                    }
                });
        recyclerAdapter.setOnInViewClickListener(R.id.note_more,
                new BaseRecyclerViewAdapter.onInternalClickListenerImpl<SNote>() {
                    @Override
                    public void OnClickListener(View parentV, View v, Integer position, SNote values) {
                        super.OnClickListener(parentV, v, position, values);
                        mainPresenter.showPopMenu(v, position, values);
                    }
                });
        recyclerAdapter.setFirstOnly(false);
        recyclerAdapter.setDuration(300);
        recyclerView.setAdapter(recyclerAdapter);
        refreshLayout.setColorSchemeColors(getColorPrimary());
        refreshLayout.setOnRefreshListener(mainPresenter);
    }

    @Override
    public void setToolbarTitle(String title) {
        toolbar.setTitle(title);
    }

    @Override
    public void switchNoteTypePage(List<SNote> notes) {
        recyclerAdapter.setList(notes);
        recyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void addNote(SNote note) {
        recyclerAdapter.add(note);
    }

    @Override
    public void updateNote(SNote note) {
        recyclerAdapter.update(note);
    }

    @Override
    public void removeNote(SNote note) {
        recyclerAdapter.remove(note);
    }

    @Override
    public void scrollRecyclerViewToTop() {
        recyclerView.smoothScrollToPosition(0);
    }

    @Override
    public void closeDrawer() {
        if (mDrawerLayout.isDrawerOpen(drawerRootView)) {
            mDrawerLayout.closeDrawer(drawerRootView);
        }
    }

    @Override
    public void openOrCloseDrawer() {
        if (mDrawerLayout.isDrawerOpen(drawerRootView)) {
            mDrawerLayout.closeDrawer(drawerRootView);
        } else {
            mDrawerLayout.openDrawer(drawerRootView);
        }
    }

    @Override
    public void setDrawerItemChecked(int position) {
        mDrawerMenuListView.setItemChecked(position, true);
    }

    @Override
    public boolean isDrawerOpen() {
        return mDrawerLayout.isDrawerOpen(drawerRootView);
    }

    @Override
    public void setMenuGravity(int gravity) {
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) drawerRootView.getLayoutParams();
        params.gravity = gravity;
        drawerRootView.setLayoutParams(params);
    }

    @Override
    public void showFab(boolean visible) {
        fab.setForceHide(!visible);
    }

    @Override
    public void showProgressWheel(boolean visible){
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

    @Override
    public void stopRefresh() {
        refreshLayout.setRefreshing(false);
    }

    @Override
    public void startRefresh() {
        refreshLayout.setRefreshing(true);
    }

    @Override
    public boolean isRefreshing() {
        return refreshLayout.isRefreshing();
    }

    @Override
    public void enableSwipeRefreshLayout(boolean enable) {
        refreshLayout.setEnabled(enable);
    }

    @Override
    public void setLayoutManager(RecyclerView.LayoutManager manager) {
        recyclerView.setLayoutManager(manager);
    }

    @Override
    public void showNormalPopupMenu(View view, SNote note) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater()
                .inflate(R.menu.menu_notes_more, popup.getMenu());
        popup.setOnMenuItemClickListener((item -> mainPresenter.onPopupMenuClick(item.getItemId(), note)));
        popup.show();
    }

    @Override
    public void showTrashPopupMenu(View view, SNote note) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater()
                .inflate(R.menu.menu_notes_trash_more, popup.getMenu());
        popup.setOnMenuItemClickListener((item -> mainPresenter.onPopupMenuClick(item.getItemId(), note)));
        popup.show();
    }

    @Override
    public void moveTaskToBack() {
        super.moveTaskToBack(true);
    }

    @Override
    public void reCreate() {
        super.recreate();
    }

    @Override
    public void showSnackbar(int message) {
        SnackbarUtils.show(fab, message);
    }

    @Override
    public void showGoBindEverNoteSnackbar(int message, int action) {
        SnackbarUtils.showAction(fab, message
                , action, mainPresenter);
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
        if (toolbar != null){
            toolbar.setNavigationOnClickListener((view) -> mainPresenter.OnNavigationOnClick());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        //searchItem.expandActionView();
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
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
        MenuItemCompat.setOnActionExpandListener(searchItem, mainPresenter);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        if (mainPresenter.onOptionsItemSelected(item.getItemId())){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mainPresenter.onKeyDown(keyCode) || super.onKeyDown(keyCode, event);
    }

    @OnClick(R.id.fab)
    public void newNote(View view){
        mainPresenter.newNote();
    }

    @Override
    public void showDeleteForeverDialog(final SNote note){
        AlertDialog.Builder builder = DialogUtils.makeDialogBuilderByTheme(this);
        builder.setTitle(R.string.delete_tip);
        DialogInterface.OnClickListener listener = (DialogInterface dialog, int which) ->
            mainPresenter.onDeleteForeverDialogClick(note, which);
        builder.setPositiveButton(R.string.sure, listener);
        builder.setNegativeButton(R.string.cancel, listener);
        builder.show();
    }
}
