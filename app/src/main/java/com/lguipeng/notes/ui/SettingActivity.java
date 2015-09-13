package com.lguipeng.notes.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.evernote.client.android.EvernoteSession;
import com.lguipeng.notes.App;
import com.lguipeng.notes.R;
import com.lguipeng.notes.injector.component.DaggerActivityComponent;
import com.lguipeng.notes.injector.module.ActivityModule;
import com.lguipeng.notes.ui.fragments.SettingFragment;

import butterknife.Bind;
import de.greenrobot.event.EventBus;

/**
 * Created by lgp on 2015/5/24.
 */
public class SettingActivity extends BaseActivity{
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showActivityInAnim();
        init();
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
    protected int getLayoutView() {
        return R.layout.activity_setting;
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

    private void showActivityInAnim(){
        overridePendingTransition(R.anim.activity_right_left_anim, R.anim.activity_exit_anim);
    }

    private void showActivityExitAnim(){
        overridePendingTransition(R.anim.activity_exit_anim, R.anim.activity_left_right_anim);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EvernoteSession.REQUEST_CODE_LOGIN){
            boolean result = resultCode == RESULT_OK;
            EventBus.getDefault().post(result);
        }
    }

    @Override
    public void finish() {
        super.finish();
        showActivityExitAnim();
    }
}
