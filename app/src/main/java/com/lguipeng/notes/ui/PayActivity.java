package com.lguipeng.notes.ui;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.lguipeng.notes.R;

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
        testCode();
    }

    @Override
    protected int getLayoutView() {
        return R.layout.activity_pay;
    }


    @Override
    protected void initToolbar(){
        super.initToolbar(toolbar);
        toolbar.setTitle(R.string.pay_for_me);
    }

    private void testCode(){

    }
}
