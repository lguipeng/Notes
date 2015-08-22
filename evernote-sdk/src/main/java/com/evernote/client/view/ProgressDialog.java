package com.evernote.client.view;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.evernote.androidsdk.R;
import com.pnikosis.materialishprogress.ProgressWheel;


/**
 * Created by lgp on 2015/8/22.
 */
public class ProgressDialog extends AlertDialog {
    public ProgressDialog(Context context) {
        super(context, R.style.esdk_GreenDialogTheme);
    }

    public ProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_progress, null);
        setView(view);
        ProgressWheel wheel = (ProgressWheel)view.findViewById(R.id.progress_wheel);
        wheel.spin();
        super.onCreate(savedInstanceState);
    }
}
