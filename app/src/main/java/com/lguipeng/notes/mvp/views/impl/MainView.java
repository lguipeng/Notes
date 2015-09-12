package com.lguipeng.notes.mvp.views.impl;

import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;

import com.lguipeng.notes.model.SNote;
import com.lguipeng.notes.mvp.views.View;

import java.util.List;

/**
 * Created by lgp on 2015/9/4.
 */
public interface MainView extends View {
    void initToolbar();
    void initDrawerView(List<String> list);
    void setToolbarTitle(String title);
    void showProgressWheel(boolean visible);
    void switchNoteTypePage(List<SNote> notes);
    void setDrawerItemChecked(int position);
    boolean isDrawerOpen();
    void closeDrawer();
    void openOrCloseDrawer();
    void setMenuGravity(int gravity);
    void showFab(boolean visible);
    void stopRefresh();
    void startRefresh();
    void enableSwipeRefreshLayout(boolean enable);
    void setLayoutManager(RecyclerView.LayoutManager manager);
    void initRecyclerView(List<SNote> notes);
    void showTrashPopupMenu(android.view.View view, SNote note);
    void showNormalPopupMenu(android.view.View view, SNote note);
    void showDeleteForeverDialog(SNote note);
    void showSnackbar(@StringRes int message);
    void showGoBindEverNoteSnackbar(@StringRes int message, @StringRes int action);
    void moveTaskToBack();
    void reCreate();
}
