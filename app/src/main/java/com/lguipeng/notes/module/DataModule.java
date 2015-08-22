package com.lguipeng.notes.module;

import android.content.Context;

import com.lguipeng.notes.BuildConfig;
import com.lguipeng.notes.ui.MainActivity;
import com.lguipeng.notes.ui.NoteActivity;
import com.lguipeng.notes.ui.fragments.SettingFragment;
import com.lguipeng.notes.utils.EverNoteUtils;
import com.lguipeng.notes.utils.FileUtils;
import com.lguipeng.notes.utils.ThreadExecutorPool;

import net.tsz.afinal.FinalDb;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by lgp on 2015/5/26.
 */
@Module(
        injects = {
                MainActivity.class,
                NoteActivity.class,
                SettingFragment.class,
        },
        addsTo = AppModule.class
)
public class DataModule {

    @Provides @Singleton
    FinalDb.DaoConfig provideDaoConfig(Context context) {
        FinalDb.DaoConfig config = new FinalDb.DaoConfig();
        config.setDbName("notes.db");
        config.setDbVersion(2);
        config.setDebug(BuildConfig.DEBUG);
        config.setContext(context);
        config.setDbUpdateListener((db, oldVersion, newVersion) -> {
            if (newVersion == 2 && oldVersion == 1) {
                db.execSQL("ALTER TABLE '" + "notes" + "' ADD COLUMN " +
                        "`createTime`" + " INTEGER;");
                db.execSQL("ALTER TABLE '" + "notes" + "' ADD COLUMN " +
                        "status" + " INTEGER;");
                db.execSQL("ALTER TABLE '" + "notes" + "' ADD COLUMN " +
                        "guid" + " TEXT;");
                db.execSQL("UPDATE '" + "notes" + "' SET type = 0 " +
                        "WHERE type = 1 OR type = 2;");
                db.execSQL("UPDATE '" + "notes" + "' SET type = 1 " +
                        "WHERE type = 3;");
                db.execSQL("UPDATE '" + "notes" + "' SET status = 2 " +
                        "WHERE type = 1;");
            }
        });
        return config;
    }

    @Provides @Singleton
    FinalDb provideFinalDb(FinalDb.DaoConfig config) {
        return FinalDb.create(config);
    }

    @Provides @Singleton
    EverNoteUtils provideEverNoteUtils(Context context, ThreadExecutorPool pool, FinalDb finalDb) {
        return EverNoteUtils.getInstance(context, pool, finalDb);
    }

    @Provides @Singleton
    ThreadExecutorPool provideThreadExecutorPool() {
        return new ThreadExecutorPool();
    }

    @Provides @Singleton
    FileUtils provideFileUtils() {
        return new FileUtils();
    }
}
