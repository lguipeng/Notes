package com.lguipeng.notes.ui.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;

import com.lguipeng.notes.App;
import com.lguipeng.notes.R;
import com.lguipeng.notes.ui.BaseActivity;
import com.lguipeng.notes.utils.PreferenceUtils;
import com.lguipeng.notes.utils.ThemeUtils;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityGraph = ((App) getActivity().getApplication()).createScopedGraph(getModules().toArray());
        activityGraph.inject(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null && getActivity() instanceof BaseActivity){
            activity = (BaseActivity)getActivity();
        }
        preferenceUtils = PreferenceUtils.getInstance(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activityGraph = null;
    }

    protected AlertDialog.Builder generateDialogBuilder(){
        ThemeUtils.Theme theme = getCurrentTheme();
        AlertDialog.Builder builder;
        switch (theme){
            case BROWN:
                builder = new AlertDialog.Builder(getActivity(), R.style.BrownDialogTheme);
                break;
            case BLUE:
                builder = new AlertDialog.Builder(getActivity(), R.style.BlueDialogTheme);
                break;
            case BLUE_GREY:
                builder = new AlertDialog.Builder(getActivity(), R.style.BlueGreyDialogTheme);
                break;
            default:
                builder = new AlertDialog.Builder(getActivity(), R.style.RedDialogTheme);
                break;
        }
        return builder;
    }

    protected ThemeUtils.Theme getCurrentTheme(){
        int value = preferenceUtils.getIntParam(getString(R.string.change_theme_key), 0);
        return ThemeUtils.Theme.mapValueToTheme(value);
    }

    protected abstract List<Object> getModules();
}
