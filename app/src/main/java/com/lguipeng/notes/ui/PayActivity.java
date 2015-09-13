package com.lguipeng.notes.ui;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.lguipeng.notes.R;

import butterknife.Bind;

/**
 * Created by lgp on 2015/6/1.
 */
public class PayActivity extends BaseActivity{
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showActivityInAnim();
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

    private void showActivityInAnim(){
        overridePendingTransition(R.anim.activity_right_left_anim, R.anim.activity_exit_anim);
    }

    private void showActivityExitAnim(){
        overridePendingTransition(R.anim.activity_exit_anim, R.anim.activity_left_right_anim);
    }

    @Override
    public void finish() {
        super.finish();
        showActivityExitAnim();
    }
}
