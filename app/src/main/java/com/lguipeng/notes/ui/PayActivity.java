package com.lguipeng.notes.ui;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

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
        initToolbar();
    }

    @Override
    protected int getLayoutView() {
        return R.layout.activity_pay;
    }

    @Override
    protected List<Object> getModules() {
        return Arrays.<Object>asList(new DataModule());
    }
    private void initToolbar(){
        super.initToolbar(toolbar);
        toolbar.setTitle(R.string.pay_for_me);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
