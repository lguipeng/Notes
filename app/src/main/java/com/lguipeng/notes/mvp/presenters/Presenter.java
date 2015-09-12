package com.lguipeng.notes.mvp.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.StringRes;

import com.lguipeng.notes.mvp.views.View;

/**
 * Created by lgp on 2015/9/4.
 */
public interface Presenter {
    void onCreate (Bundle savedInstanceState);

    void onResume();

    void onStart ();

    void onPause();

    void onStop ();

    void onDestroy();

    void attachView (View v);

    default String getString(Context context, @StringRes int string){
        if (context != null)
            return context.getString(string);
        return "";
    }
}
