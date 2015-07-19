package com.lguipeng.notes.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.lguipeng.notes.App;
import com.lguipeng.notes.ui.BaseActivity;
import com.lguipeng.notes.utils.PreferenceUtils;

import java.util.List;

import dagger.ObjectGraph;

/**
 * Created by lgp on 2015/5/26.
 */
public abstract class BaseFragment extends PreferenceFragment {

    private ObjectGraph activityGraph;
    protected BaseActivity activity;
    protected PreferenceUtils preferenceUtils;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getActivity() != null && getActivity() instanceof BaseActivity){
            this.activity = (BaseActivity)getActivity();
        }
        preferenceUtils = PreferenceUtils.getInstance(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getModules() != null){
            activityGraph = ((App) getActivity().getApplication()).createScopedGraph(getModules().toArray());
            activityGraph.inject(this);
        }
    }

    @Override
    public void onDetach() {
        activity = null;
        super.onDetach();
    }

    protected  List<Object> getModules(){
        return null;
    }
}
