package com.lguipeng.notes.ui;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.lguipeng.notes.R;
import com.lguipeng.notes.module.DataModule;
import com.lguipeng.notes.ui.fragments.SettingFragment;

import java.util.Arrays;
import java.util.List;

import butterknife.InjectView;

/**
 * Created by lgp on 2015/5/24.
 */
public class SettingActivity extends BaseActivity{
    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        initToolbar();
    }

    @Override
    protected int getLayoutView() {
        return R.layout.activity_setting;
    }

    @Override
    protected List<Object> getModules() {
        return Arrays.<Object>asList(new DataModule());
    }

    private void initToolbar(){
        super.initToolbar(toolbar);
        toolbar.setTitle(R.string.setting);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void init(){
        SettingFragment settingFragment = SettingFragment.newInstance();
        getFragmentManager().beginTransaction().replace(R.id.fragment_content, settingFragment).commit();
    }

}
