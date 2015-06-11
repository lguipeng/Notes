package com.lguipeng.notes.ui;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.lguipeng.notes.R;
import com.lguipeng.notes.module.DataModule;

import java.util.Arrays;
import java.util.List;

import butterknife.InjectView;

/**
 * Created by lgp on 2015/6/1.
 */
public class PayActivity extends BaseActivity{
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutView() {
        return R.layout.activity_pay;
    }

    @Override
    protected List<Object> getModules() {
        return Arrays.<Object>asList(new DataModule());
    }

    @Override
    protected void initToolbar(){
        super.initToolbar(toolbar);
        toolbar.setTitle(R.string.pay_for_me);
    }
}
