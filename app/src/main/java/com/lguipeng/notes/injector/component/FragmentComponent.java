package com.lguipeng.notes.injector.component;

import com.lguipeng.notes.injector.Fragment;
import com.lguipeng.notes.injector.module.FragmentModule;
import com.lguipeng.notes.ui.fragments.SettingFragment;

import dagger.Component;

/**
 * Created by lgp on 2015/9/3.
 */
@Fragment
@Component(dependencies = {ActivityComponent.class}, modules = {FragmentModule.class})
public interface FragmentComponent {
    void inject(SettingFragment fragment);
}
