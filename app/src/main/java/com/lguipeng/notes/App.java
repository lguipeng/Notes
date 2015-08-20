package com.lguipeng.notes;

import android.app.Application;

import com.evernote.client.android.EvernoteSession;
import com.lguipeng.notes.module.AppModule;

import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;

/**
 * Created by lgp on 2015/5/24.
 */
public class App extends Application{
    private ObjectGraph objectGraph;
    @Override
    public void onCreate() {
        super.onCreate();
        objectGraph = ObjectGraph.create(getModules().toArray());
        objectGraph.inject(this);
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

    private List<Object> getModules() {
        return Arrays.asList(new AppModule(this));
    }

    public ObjectGraph createScopedGraph(Object... modules) {
        return objectGraph.plus(modules);
    }
}
