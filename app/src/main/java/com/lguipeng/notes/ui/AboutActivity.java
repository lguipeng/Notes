package com.lguipeng.notes.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lguipeng.notes.BuildConfig;
import com.lguipeng.notes.R;
import com.lguipeng.notes.module.DataModule;

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

    private void initToolbar(){
        super.initToolbar(toolbar);
        toolbar.setTitle(R.string.about);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initVersionText(){
        versionTextView.setText("v" + getVersion(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

            default:
                return super.onOptionsItemSelected(item);
        }
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
}
