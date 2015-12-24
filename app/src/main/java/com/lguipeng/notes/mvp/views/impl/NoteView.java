package com.lguipeng.notes.mvp.views.impl;

import android.support.annotation.StringRes;

import com.lguipeng.notes.model.SNote;
import com.lguipeng.notes.mvp.views.View;

/**
 * Created by lgp on 2015/9/4.
 */
public interface NoteView extends View {
    void finishView();
    void setToolbarTitle(String title);
    void setToolbarTitle(@StringRes int title);
    void initViewOnEditMode(SNote note);
    void initViewOnViewMode(SNote note);
    void initViewOnCreateMode(SNote note);
    void setOperateTimeLineTextView(String text);
    void setDoneMenuItemVisible(boolean visible);
    boolean isDoneMenuItemVisible();
    boolean isDoneMenuItemNull();
    String getLabelText();
    String getContentText();
    void hideKeyBoard();
    void showKeyBoard();
    void showNotSaveNoteDialog();
}
