package com.lguipeng.notes.ui.fragments;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.jenzz.materialpreference.CheckBoxPreference;
import com.jenzz.materialpreference.Preference;
import com.lguipeng.notes.R;
import com.lguipeng.notes.adpater.ColorsListAdapter;
import com.lguipeng.notes.injector.component.DaggerFragmentComponent;
import com.lguipeng.notes.injector.module.FragmentModule;
import com.lguipeng.notes.mvp.presenters.impl.SettingPresenter;
import com.lguipeng.notes.mvp.views.impl.SettingView;
import com.lguipeng.notes.ui.SettingActivity;
import com.lguipeng.notes.utils.DialogUtils;
import com.lguipeng.notes.utils.SnackbarUtils;
import com.lguipeng.notes.utils.ThemeUtils;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by lgp on 2015/5/26.
 */
public class SettingFragment extends PreferenceFragment implements SettingView{
    public static final String PREFERENCE_FILE_NAME = "note.settings";
    private CheckBoxPreference rightHandModePreference;
    private Preference feedbackPreference;
    private CheckBoxPreference cardLayoutPreference;
    private Preference payMePreference;
    private Preference giveFavorPreference;
    private Preference everAccountPreference;
    @Inject SettingPresenter settingPresenter;
    private SettingActivity activity;
    public static SettingFragment newInstance(){
        SettingFragment fragment = new SettingFragment();
        return fragment;
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getActivity() != null && getActivity() instanceof SettingActivity){
            this.activity = (SettingActivity)getActivity();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeDependencyInjector();
        addPreferencesFromResource(R.xml.prefs);
        getPreferenceManager().setSharedPreferencesName(PREFERENCE_FILE_NAME);
        initializePresenter();
        settingPresenter.onCreate(savedInstanceState);
    }

    private void initializePresenter() {
        settingPresenter.attachView(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        settingPresenter.onViewCreated(view);
    }

    public SettingFragment() {
        super();
    }

    private void initializeDependencyInjector() {
        DaggerFragmentComponent.builder()
                .fragmentModule(new FragmentModule())
                .activityComponent(activity.getActivityComponent())
                .build().inject(this);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,  android.preference.Preference preference) {
        settingPresenter.onPreferenceTreeClick(preference);
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onDestroy() {
        settingPresenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void findPreference() {
        rightHandModePreference = (CheckBoxPreference)findPreference(getString(R.string.right_hand_mode_key));
        cardLayoutPreference = (CheckBoxPreference)findPreference(getString(R.string.card_note_item_layout_key));
        feedbackPreference = (Preference)findPreference(getString(R.string.advice_feedback_key));
        everAccountPreference = (Preference)findPreference(getString(R.string.ever_note_account_key));
        payMePreference = (Preference)findPreference(getString(R.string.pay_for_me_key));
        giveFavorPreference = (Preference)findPreference(getString(R.string.give_favor_key));
    }

    @Override
    public void setRightHandModePreferenceChecked(boolean checked) {
        rightHandModePreference.setChecked(checked);
    }

    @Override
    public void setCardLayoutPreferenceChecked(boolean checked) {
        cardLayoutPreference.setChecked(checked);
    }

    @Override
    public void setFeedbackPreferenceSummary(CharSequence c) {
        feedbackPreference.setSummary(c);
    }

    @Override
    public void setFeedbackPreferenceClickListener(android.preference.Preference.OnPreferenceClickListener l) {
        feedbackPreference.setOnPreferenceClickListener(l);
    }

    @Override
    public void setEverNoteAccountPreferenceSummary(CharSequence c) {
        everAccountPreference.setSummary(c);
    }

    @Override
    public void setEverNoteAccountPreferenceTitle(CharSequence c) {
        everAccountPreference.setTitle(c);
    }

    @Override
    public void initPreferenceListView(View view) {
        ListView listView = (ListView)view.findViewById(android.R.id.list);
        listView.setHorizontalScrollBarEnabled(false);
        listView.setVerticalScrollBarEnabled(false);
        listView.setDivider(new ColorDrawable(getResources().getColor(R.color.grey)));
        listView.setDividerHeight((int) getResources().getDimension(R.dimen.preference_divider_height));
        listView.setFooterDividersEnabled(false);
        listView.setHeaderDividersEnabled(false);
    }

    @Override
    public void showThemeChooseDialog(){
        AlertDialog.Builder builder = DialogUtils.makeDialogBuilder(activity);
        builder.setTitle(R.string.change_theme);
        Integer[] res = new Integer[]{R.drawable.red_round, R.drawable.brown_round, R.drawable.blue_round,
                R.drawable.blue_grey_round, R.drawable.yellow_round, R.drawable.deep_purple_round,
                R.drawable.pink_round, R.drawable.green_round};
        List<Integer> list = Arrays.asList(res);
        ColorsListAdapter adapter = new ColorsListAdapter(getActivity(), list);
        adapter.setCheckItem(ThemeUtils.getCurrentTheme(activity).getIntValue());
        GridView gridView = (GridView)LayoutInflater.from(activity).inflate(R.layout.colors_panel_layout, null);
        gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        gridView.setCacheColorHint(0);
        gridView.setAdapter(adapter);
        builder.setView(gridView);
        final AlertDialog dialog = builder.show();
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            dialog.dismiss();
            settingPresenter.onThemeChoose(position);
        });
    }

    @Override
    public void showUnbindEverNoteDialog(){
        AlertDialog.Builder builder = DialogUtils.makeDialogBuilder(activity);
        builder.setTitle(R.string.has_unbind_ever_note_tip);
        builder.setPositiveButton(R.string.sure, settingPresenter);
        builder.setNegativeButton(R.string.cancel, settingPresenter);
        builder.show();
    }

    @Override
    public void showSnackbar(int message) {
        if (activity != null)
            SnackbarUtils.show(activity, message);
    }

    @Override
    public boolean isResume() {
        return isResumed();
    }

    @Override
    public void toast(int message) {
        if (activity == null)
            return;
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void reload() {
        if (activity != null){
            activity.reload(false);
        }
    }
}
