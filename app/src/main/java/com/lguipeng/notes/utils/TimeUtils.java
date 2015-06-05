package com.lguipeng.notes.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lgp on 2015/5/25.
 */
public class TimeUtils {
    private static final String TODAY = "今天";
    private static final String BEFORE_TODAY = "天前";
    private static final String BEFORE_MONTH = "个月前";
    private static final String BEFORE_YEAR = "年前";
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
    public static String getConciseTime(long timeInMillis, long nowInMillis) {
        Date date = new Date(timeInMillis);
        Date now = new Date(nowInMillis);

        if (now.getYear() == date.getYear()) {
            if (now.getMonth() == date.getMonth()) {
                if (now.getDate() == date.getDate())
                    return TODAY + getTime(timeInMillis, DATE_FORMAT_DATE_1);
                else{
                    return now.getDate() - date.getDate() + BEFORE_TODAY;
                }
            }else {
                return now.getMonth() - date.getMonth() + BEFORE_MONTH;
            }
        }
        return now.getYear() - date.getYear() + BEFORE_YEAR;
    }


    public static String getConciseTime(long timeInMillis) {
        return getConciseTime(timeInMillis, getCurrentTimeInLong());
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
