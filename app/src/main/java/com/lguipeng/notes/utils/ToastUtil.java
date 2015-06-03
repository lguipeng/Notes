package com.lguipeng.notes.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.lguipeng.notes.R;

/**
 * Author: lgp
 * Date: 2014/12/31.
 */
public class ToastUtil {

    public static void show(Context mContext, String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    public static void show(Context mContext, int message) {
        //Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        Toast toast = new Toast(mContext);
        View view = LayoutInflater.from(mContext).inflate(R.layout.toast_layout, null, false);
        TextView textView = (TextView)view.findViewById(R.id.toast_text);
        textView.setText(message);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

}
