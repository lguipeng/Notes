package com.lguipeng.notes.utils;

import android.support.v7.widget.Toolbar;

import com.lguipeng.notes.R;
import com.lguipeng.notes.ui.BaseActivity;

/**
 * Created by lgp on 2015/7/19.
 */
public class ToolbarUtils {

    public static void initToolbar(Toolbar toolbar, BaseActivity activity){
        if (toolbar == null || activity == null)
            return;
        toolbar.setBackgroundColor(activity.getColorPrimary());
        toolbar.setTitle(R.string.app_name);
        toolbar.setTitleTextColor(activity.getColor(R.color.action_bar_title_color));
        toolbar.collapseActionView();
        activity.setSupportActionBar(toolbar);
        if (activity.getSupportActionBar() != null){
            activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
