package com.lguipeng.notes.adpater;

import android.content.Context;

import com.lguipeng.notes.R;

import java.util.List;

/**
 * Created by lgp on 2015/5/24.
 */
public class DrawerListAdapter extends SimpleListAdapter{

    public DrawerListAdapter(Context mContext, List<String> list) {
        super(mContext, list);
    }

    @Override
    protected int getLayout() {
        return R.layout.drawer_list_item_layout;
    }
}
