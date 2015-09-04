package com.lguipeng.notes;

import android.app.Application;

import com.evernote.client.android.EvernoteSession;
import com.lguipeng.notes.injector.component.AppComponent;
import com.lguipeng.notes.injector.component.DaggerAppComponent;
import com.lguipeng.notes.injector.module.AppModule;

/**
 * Created by lgp on 2015/5/24.
 */
public class App extends Application{
    private AppComponent mAppComponent;
    @Override
    public void onCreate() {
        super.onCreate();
        initializeInjector();
        buildEverNoteSession();
    }

    private void buildEverNoteSession(){
        EvernoteSession.EvernoteService service;
        if (BuildConfig.DEBUG)
            service = EvernoteSession.EvernoteService.SANDBOX;
        else
            service = EvernoteSession.EvernoteService.PRODUCTION;
        new EvernoteSession.Builder(this)
                .setEvernoteService(service)
                .setSupportAppLinkedNotebooks(false)
                .build(BuildConfig.EVER_NOTE_KEY, BuildConfig.EVER_NOTE_SECRET)
                .asSingleton();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }


    private void initializeInjector() {
        mAppComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    public AppComponent getAppComponent() {
        return mAppComponent;
    }

}
