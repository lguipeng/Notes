package com.lguipeng.notes.module;

import android.content.Context;

import com.lguipeng.notes.ui.AboutActivity;
import com.lguipeng.notes.ui.EditNoteTypeActivity;
import com.lguipeng.notes.ui.MainActivity;
import com.lguipeng.notes.ui.NoteActivity;
import com.lguipeng.notes.ui.PayActivity;
import com.lguipeng.notes.ui.SettingActivity;
import com.lguipeng.notes.ui.fragments.SettingFragment;

import net.tsz.afinal.FinalDb;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by lgp on 2015/5/26.
 */
@Module(
        injects = {
                AboutActivity.class,
                MainActivity.class,
                NoteActivity.class,
                SettingActivity.class,
                SettingFragment.class,
                PayActivity.class,
                EditNoteTypeActivity.class
        },
        addsTo = AppModule.class,
        library = true
)
public class DataModule {

    @Provides @Singleton
    FinalDb.DaoConfig provideDaoConfig(Context context) {
        FinalDb.DaoConfig config = new FinalDb.DaoConfig();
        config.setDbName("notes.db");
        config.setDbVersion(1);
        config.setDebug(true);
        config.setContext(context);
        return config;
    }

    @Provides @Singleton
    FinalDb provideFinalDb(FinalDb.DaoConfig config) {
        return FinalDb.create(config);
    }
}
