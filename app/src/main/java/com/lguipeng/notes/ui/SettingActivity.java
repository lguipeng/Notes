package com.lguipeng.notes.ui;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.evernote.client.android.login.EvernoteLoginFragment;
import com.lguipeng.notes.R;
import com.lguipeng.notes.module.DataModule;
import com.lguipeng.notes.ui.fragments.SettingFragment;

import java.util.Arrays;
import java.util.List;

import butterknife.InjectView;
import de.greenrobot.event.EventBus;

/**
 * Created by lgp on 2015/5/24.
 */
public class SettingActivity extends BaseActivity implements EvernoteLoginFragment.ResultCallback{
    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    protected int getLayoutView() {
        return R.layout.activity_setting;
    }

    @Override
    protected List<Object> getModules() {
        return Arrays.<Object>asList(new DataModule());
    }

    @Override
    protected void initToolbar(){
        super.initToolbar(toolbar);
        toolbar.setTitle(R.string.setting);
    }

    private void init(){
        SettingFragment settingFragment = SettingFragment.newInstance();
        getFragmentManager().beginTransaction().replace(R.id.fragment_content, settingFragment).commit();
    }

    @Override
    public void onLoginFinished(boolean result) {
        EventBus.getDefault().post(result);
    }

}
