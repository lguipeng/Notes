package com.lguipeng.notes.adpater;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.lguipeng.notes.R;

import java.util.List;

/**
 * Created by lgp on 2015/6/7.
 */
public class ColorsListAdapter extends BaseListAdapter<Integer>{

    private int checkItem;

    public ColorsListAdapter(Context context, List<Integer> list) {
        super(context, list);
    }

    @Override
    public View bindView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.colors_image_layout, null);
            holder = new Holder();
            holder.imageView1 = (ImageView)convertView.findViewById(R.id.img_1);
            holder.imageView2 = (ImageView)convertView.findViewById(R.id.img_2);
            convertView.setTag(holder);
        }else{
            holder = (Holder)convertView.getTag();
        }
        holder.imageView1.setImageResource(list.get(position));
        if (checkItem == position){
            holder.imageView2.setImageResource(R.drawable.ic_done_white);
        }
        return convertView;
    }

    public int getCheckItem() {
        return checkItem;
    }

    public void setCheckItem(int checkItem) {
        this.checkItem = checkItem;
    }

    static class Holder {
        ImageView imageView1;
        ImageView imageView2;
    }
}
