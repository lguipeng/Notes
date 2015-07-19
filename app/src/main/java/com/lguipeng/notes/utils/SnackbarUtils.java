package com.lguipeng.notes.utils;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.view.View;

import com.lguipeng.notes.ui.BaseActivity;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;
import com.nispok.snackbar.listeners.EventListener;

/**
 * Author: lgp
 * Date: 2014/12/31.
 */
public class SnackbarUtils {

    public static final long DURATION = Snackbar.SnackbarDuration.LENGTH_SHORT.getDuration() / 2 ;

    public static void show(Activity activity, int message) {
        show(activity, message, null);
    }

    public static void show(Activity activity, int message, View floatButton) {

        EventListener listener = new FloatButtonEventListener(floatButton);
        SnackbarManager.show(
                Snackbar.with(activity.getApplicationContext())
                        .color(0xff323232)
                        .duration(DURATION)
                        .eventListener(listener)
                        .text(message), activity);
    }

    public static void showAction(Activity activity, int message, int action, ActionClickListener listener) {
       showAction(activity, message, action, listener, null);
    }

    public static void showAction(Activity activity, int message, int action, ActionClickListener listener, View floatButton) {
        EventListener eventListener = new FloatButtonEventListener(floatButton);
        SnackbarManager.show(Snackbar.with(activity)
                .text(message)
                .color(0xff323232)
                .actionColor(snackbarColor(activity))
                .actionLabel(action)
                .duration(DURATION)
                .eventListener(eventListener)
                .actionListener(listener), activity);
    }

    public static void dismiss(){
        SnackbarManager.dismiss();
    }

    private static int snackbarColor(Activity activity){
        int color = Color.BLACK;
        if (activity instanceof BaseActivity){
            color = (((BaseActivity) activity)).getColorPrimary();
        }
        return color;
    }

    private static class FloatButtonEventListener implements EventListener{
        private View floatButton;

        public FloatButtonEventListener(View floatButton) {
            this.floatButton = floatButton;
        }

        @Override
        public void onShow(Snackbar snackbar) {
            if (!judgeHasAnim())
                return;
            Animator anim = ObjectAnimator.ofFloat(floatButton, "translationY", 0, -snackbar.getHeight());
            //anim.setInterpolator(new FastOutSlowInInterpolator());
            anim.setDuration(DURATION / 4).start();
        }

        @Override
        public void onShowByReplace(Snackbar snackbar) {

        }

        @Override
        public void onShown(Snackbar snackbar) {

        }

        @Override
        public void onDismiss(Snackbar snackbar) {
            if (!judgeHasAnim())
                return;
            Animator anim = ObjectAnimator.ofFloat(floatButton, "translationY", -snackbar.getHeight(), 0);
            anim.setDuration(DURATION / 2).start();
        }

        @Override
        public void onDismissByReplace(Snackbar snackbar) {

        }

        @Override
        public void onDismissed(Snackbar snackbar) {
            if (!judgeHasAnim())
                return;
            ViewHelper.clear(floatButton);
        }

        private boolean judgeHasAnim(){
            return floatButton != null;
        }
    }
}
