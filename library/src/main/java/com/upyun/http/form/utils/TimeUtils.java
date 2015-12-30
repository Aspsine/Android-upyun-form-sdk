package com.upyun.http.form.utils;

/**
 * Created by aspsine on 15/12/29.
 */
public class TimeUtils {

    public static String getExpiration(int timeOut) {
        return String.valueOf(System.currentTimeMillis() + timeOut);
    }
}
