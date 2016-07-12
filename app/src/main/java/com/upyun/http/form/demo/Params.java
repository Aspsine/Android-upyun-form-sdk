package com.upyun.http.form.demo;

/**
 * Created by aspsine on 15/12/30.
 */
public class Params {

    public static final String BUCKET = "*******";
    public static final String SAVE_KEY = "***yourpath***/{filename}{.suffix}";

    public static final String ALLOW_FILE_TYPE = "jpg,jpeg,bmp,png,gif";

    /**
     * 1k~20mb
     */
    public static final String CONTENT_LENGTH_RANGE = 1024 + "," + (1024 * 1024 * 20);
}
