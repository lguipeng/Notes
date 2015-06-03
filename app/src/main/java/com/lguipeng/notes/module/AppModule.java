package com.lguipeng.notes.module;

import android.app.Application;
import android.content.Context;

import com.lguipeng.notes.App;

import dagger.Module;
import dagger.Provides;

/**
 * Created by lgp on 2015/5/26.
 */
@Module(
        injects = {
                App.class
        },
        library = true
)
public class AppModule {
    private App app;

    public AppModule(App app) {
        this.app = app;
    }

    @Provides
    Application provideApplication() {
        return app;
    }

    @Provides
    Context provideContext() {
        return app.getApplicationContext();
    }
}
