package com.lguipeng.notes.ui.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.lguipeng.notes.App;

import java.util.List;

import dagger.ObjectGraph;

/**
 * Created by lgp on 2015/5/26.
 */
public abstract class BaseFragment extends PreferenceFragment {

    private ObjectGraph activityGraph;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityGraph = ((App) getActivity().getApplication()).createScopedGraph(getModules().toArray());
        activityGraph.inject(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activityGraph = null;
    }

    protected abstract List<Object> getModules();
}
