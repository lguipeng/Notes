package com.lguipeng.notes.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lguipeng.notes.BuildConfig;
import com.lguipeng.notes.R;
import com.lguipeng.notes.module.DataModule;
import com.lguipeng.notes.utils.SnackbarUtils;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.util.Arrays;
import java.util.List;

import butterknife.InjectView;

/**
 * Created by lgp on 2015/5/25.
 */
public class AboutActivity extends BaseActivity implements View.OnClickListener{
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.version_text)
    TextView versionTextView;
    @InjectView(R.id.blog_btn)
    Button blogButton;
    @InjectView(R.id.project_home_btn)
    Button projectHomeButton;
    private final static String WEIBO_PACKAGENAME = "com.sina.weibo";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initToolbar();
        initVersionText();
        blogButton.setOnClickListener(this);
        projectHomeButton.setOnClickListener(this);
    }



    @Override
    protected int getLayoutView() {
        return R.layout.activity_about;
    }

    @Override
    protected List<Object> getModules() {
        return Arrays.<Object>asList(new DataModule());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.blog_btn:
                startViewAction(BuildConfig.BLOG_URL);
                break;
            case R.id.project_home_btn:
                startViewAction(BuildConfig.PROJECT_URL);
                break;
            default:
                break;
        }
    }

    @Override
    protected void initToolbar(){
        super.initToolbar(toolbar);
        toolbar.setTitle(R.string.about);

    }

    private void initVersionText(){
        versionTextView.setText("v" + getVersion(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.share:
                //shareToWeChatTimeline();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private String getVersion(Context ctx){
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            return pi.versionName;
        }catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
        return "1.0.0";
    }

    private void startViewAction(String uriStr){
        try {
            Uri uri = Uri.parse(uriStr);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void share(String packages, Uri uri){
        Intent intent=new Intent(Intent.ACTION_SEND);
        if (uri != null){
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
        }else {
            intent.setType("text/plain");
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share));
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text, BuildConfig.APP_DOWNLOAD_URL));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (!TextUtils.isEmpty(packages))
            intent.setPackage(packages);
        startActivity(Intent.createChooser(intent, getString(R.string.share)));
    }

    private void shareToWeChat(int scene){
        IWXAPI api = WXAPIFactory.createWXAPI(this, BuildConfig.WECHAT_ID, true);
        api.registerApp(BuildConfig.WECHAT_ID);
        WXWebpageObject object = new WXWebpageObject();
        object.extInfo = getString(R.string.share_text, BuildConfig.APP_DOWNLOAD_URL);
        object.webpageUrl = "http://mp.weixin.qq.com/s?__biz=MzIwMDA4OTQ3MQ==&mid=209579314&idx=1&sn=4a4fced16713b73bc11f5bf08c739d6d&scene=2&from=timeline&isappinstalled=0#rd";
        WXMediaMessage msg = new WXMediaMessage(object);
        msg.mediaObject = object;
        msg.description = getString(R.string.app_name);
        SendMessageToWX.Req request = new SendMessageToWX.Req();
        request.message = msg;
        request.scene = scene;
        api.sendReq(request);
    }

    private void shareToWeChatTimeline(){
        shareToWeChat(SendMessageToWX.Req.WXSceneTimeline);
    }

    private void shareToWeChatSession(){
        shareToWeChat(SendMessageToWX.Req.WXSceneSession);
    }

    private void shareToWeChatFavorite(){
        shareToWeChat(SendMessageToWX.Req.WXSceneFavorite);
    }

    private void shareToWeibo(){
        if (isInstallApplication(WEIBO_PACKAGENAME)){
            //Uri uri = Uri.parse("file:///sdcard/Pictures/ic_launcher.png");
            share(WEIBO_PACKAGENAME, null);
        }else {
            SnackbarUtils.show(this, R.string.not_install_app);
        }
    }

    private boolean isInstallApplication(String packageName){
        try {
            PackageManager pm = this.getPackageManager();
            pm.getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
