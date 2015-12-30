package com.upyun.http.form;

/**
 * Created by aspsine on 15/12/29.
 */
public class UpYunError extends Exception {

    public UpYunError(String detailMessage) {
        super(detailMessage);
    }

    public UpYunError(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public UpYunError(Throwable throwable) {
        super(throwable);
    }
}
