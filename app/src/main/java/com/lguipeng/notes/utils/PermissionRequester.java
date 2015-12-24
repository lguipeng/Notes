package com.lguipeng.notes.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import com.lguipeng.notes.ui.ShadowActivity;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by lgp on 2015/12/13.
 */
public class PermissionRequester {

    private static PermissionRequester mPermissionChecker;

    private Context mContext;

    private RequestPermissionsResultCallBack mPermissionsResultCallBack;

    private List<String> mAlreadyGrantedPermission = new ArrayList<>();

    public static PermissionRequester getInstance(Context context){
        if (mPermissionChecker == null) {
            synchronized (PermissionRequester.class) {
                if (mPermissionChecker == null) {
                    mPermissionChecker = new PermissionRequester(context);
                }
            }
        }
        return mPermissionChecker;
    }

    private PermissionRequester(Context mContext) {
        this.mContext = mContext;
    }

    public void request(RequestPermissionsResultCallBack callBack, String... permissions){
        if (permissions == null || permissions.length <= 0){
            return;
        }
        this.mPermissionsResultCallBack = callBack;
        List<String> list = new ArrayList<>();
        for (String permission : permissions){
            if (!checkIsGranted(permission)){
                list.add(permission);
            }else {
                mAlreadyGrantedPermission.add(permission);
            }
        }
        if (list.size() >= 1){
            requestPermissions(list.toArray(new String[1]));
        }else {
            int[] results = new int[permissions.length];
            for (int i=0; i<permissions.length; i++){
                results[i] = PackageManager.PERMISSION_GRANTED;
            }
            onRequestPermissionsResult(permissions, results);
        }
    }

    private void requestPermissions(String... permissions) {
        if (permissions == null || permissions.length == 0)
            return;
        startShadowActivity(permissions);
    }

    private void startShadowActivity(String... permissions) {
        Intent intent = new Intent(mContext, ShadowActivity.class);
        intent.putExtra("permissions", permissions);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    private boolean checkIsGranted(String permission){
        return ContextCompat.checkSelfPermission(mContext, permission) ==
                PackageManager.PERMISSION_GRANTED;
    }

    public void onRequestPermissionsResult(String[] permissions, int[] grantResults){
        if (mAlreadyGrantedPermission.size() > 0){
            String newPermission[] = new String[permissions.length + mAlreadyGrantedPermission.size()];
            int[] newGrantResult = new int[permissions.length + mAlreadyGrantedPermission.size()];
            int i;
            for (i=0; i<permissions.length; i++){
                newPermission[i] = permissions[i];
                newGrantResult[i] = grantResults[i];
            }
            for (String p : mAlreadyGrantedPermission){
                newPermission[i] = p;
                newGrantResult[i] = PackageManager.PERMISSION_GRANTED;
                i++;
            }
            dispatchRequestPermissionsResult(newPermission, newGrantResult);
        }else {
            dispatchRequestPermissionsResult(permissions, grantResults);
        }
        mPermissionsResultCallBack = null;
        mAlreadyGrantedPermission.clear();
    }

    private void dispatchRequestPermissionsResult(String[] permissions, int[] grantResults) {
        if (mPermissionsResultCallBack == null){
            return;
        }
        if (permissions == null || permissions.length <=0 ||
                grantResults == null || grantResults.length <= 0 ||
                grantResults.length != permissions.length){
            mPermissionsResultCallBack.onError();
        }else {
            mPermissionsResultCallBack.onRequestPermissionsResult(permissions, grantResults);
        }

    }

    public static class RequestPermissionsResultCallBackImpl implements RequestPermissionsResultCallBack{
        @Override
        public void onRequestPermissionsResult(String[] permissions, int[] grantResults) {

        }

        @Override
        public void onError() {

        }
    }

    public interface RequestPermissionsResultCallBack{
        // for request more than one permission in a time
        void onRequestPermissionsResult(String[] permissions, int[] grantResults);
        // for request permission error
        void onError();
    }

}
