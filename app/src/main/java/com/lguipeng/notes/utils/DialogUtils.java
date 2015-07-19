package com.lguipeng.notes.utils;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import com.lguipeng.notes.R;

/**
 * Created by lgp on 2015/7/19.
 */
public class DialogUtils {

    public static AlertDialog.Builder makeDialogBuilderByTheme(Context context){
        ThemeUtils.Theme theme = ThemeUtils.getCurrentTheme(context);
        AlertDialog.Builder builder;
        int style = R.style.RedDialogTheme;
        switch (theme){
            case BROWN:
                style = R.style.BrownDialogTheme;
                break;
            case BLUE:
                style = R.style.BlueDialogTheme;
                break;
            case BLUE_GREY:
                style = R.style.BlueGreyDialogTheme;
                break;
            case YELLOW:
                style = R.style.YellowDialogTheme;
                break;
            case DEEP_PURPLE:
                style = R.style.DeepPurpleDialogTheme;
                break;
            case PINK:
                style = R.style.PinkDialogTheme;
                break;
            case GREEN:
                style = R.style.GreenDialogTheme;
                break;
            default:
                break;
        }
        builder = new AlertDialog.Builder(context, style);
        return builder;
    }
}
