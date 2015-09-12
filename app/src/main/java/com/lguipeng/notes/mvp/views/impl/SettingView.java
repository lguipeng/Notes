package com.lguipeng.notes.mvp.views.impl;

import android.preference.Preference;
import android.support.annotation.StringRes;

import com.lguipeng.notes.mvp.views.View;


/**
 * Created by lgp on 2015/9/5.
 */
public interface SettingView extends View {
    void findPreference();
    void setRightHandModePreferenceChecked(boolean checked);
    void setCardLayoutPreferenceChecked(boolean checked);
    void setFeedbackPreferenceSummary(CharSequence c);
    void setFeedbackPreferenceClickListener(Preference.OnPreferenceClickListener l);
    void setEverNoteAccountPreferenceSummary(CharSequence c);
    void setEverNoteAccountPreferenceTitle(CharSequence c);
    void initPreferenceListView(android.view.View view);
    void showSnackbar(@StringRes int message);
    void showThemeChooseDialog();
    boolean isResume();
    void showUnbindEverNoteDialog();
    void toast(@StringRes int message);
    void finishView();
}
