package com.lguipeng.notes.mvp.presenters;

import android.os.Bundle;

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

}
