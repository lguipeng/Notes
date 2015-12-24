package com.lguipeng.notes.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.lguipeng.notes.utils.PermissionRequester;

/**
 * Created by lgp on 2015/12/13.
 */
@TargetApi(Build.VERSION_CODES.M)
public class ShadowActivity extends Activity {
    private PermissionRequester.RequestPermissionsResultCallBack mPermissionsResultCallBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            handleIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String[] permissions = intent.getStringArrayExtra("permissions");
        ActivityCompat.requestPermissions(this, permissions, 0x44);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0x44){
            PermissionRequester.getInstance(this).onRequestPermissionsResult(permissions, grantResults);
        }
        finish();
    }


}
