package com.lguipeng.notes.utils;

import android.content.Context;

import com.lguipeng.notes.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lgp on 2015/5/25.
 */
public class TimeUtils {
    public static final long DAY_Millis = 24 * 60 * 60 * 1000;
    public static final long MONTH_Millis = 30 * DAY_Millis;
    public static final long YEAR_Millis = 365 * DAY_Millis;
    public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd    HH : mm");
    public static final SimpleDateFormat DATE_FORMAT_DATE_1    = new SimpleDateFormat(" HH : mm ");

    private TimeUtils() {
        throw new AssertionError();
    }

    /**
     * long time to string
     *
     * @param timeInMillis
     * @param dateFormat
     * @return
     */
    public static String getTime(long timeInMillis, SimpleDateFormat dateFormat) {
        return dateFormat.format(new Date(timeInMillis));
    }

    /**
     * long time to string, format is {@link #DEFAULT_DATE_FORMAT}
     *
     * @param timeInMillis
     * @return
     */
    public static String getTime(long timeInMillis) {
        return getTime(timeInMillis, DEFAULT_DATE_FORMAT);
    }

    @SuppressWarnings("Deprecated")
    public static String getConciseTime(long timeInMillis, long nowInMillis, Context context) {
        if (context == null)
            return "";
        Date date = new Date(timeInMillis);
        Date now = new Date(nowInMillis);

        if (now.getYear() == date.getYear()) {
            if (now.getMonth() == date.getMonth()) {
                if (now.getDate() == date.getDate())
                    return context.getString(R.string.today, getTime(timeInMillis, DATE_FORMAT_DATE_1));
                else{
                    return context.getString(R.string.before_day, now.getDate() - date.getDate());
                }
            }else {
                return context.getString(R.string.before_month, now.getMonth() - date.getMonth());
            }
        }
        return context.getString(R.string.before_year, now.getYear() - date.getYear());
    }


    public static String getConciseTime(long timeInMillis, Context context) {
        return getConciseTime(timeInMillis, getCurrentTimeInLong(), context);
    }
    /**
     * get current time in milliseconds
     *
     * @return
     */
    public static long getCurrentTimeInLong() {
        return System.currentTimeMillis();
    }

    /**
     * get current time in milliseconds, format is {@link #DEFAULT_DATE_FORMAT}
     *
     * @return
     */
    public static String getCurrentTimeInString() {
        return getTime(getCurrentTimeInLong());
    }

    /**
     * get current time in milliseconds
     *
     * @return
     */
    public static String getCurrentTimeInString(SimpleDateFormat dateFormat) {
        return getTime(getCurrentTimeInLong(), dateFormat);
    }
}
