package com.upyun.http.form.core;

/**
 * Created by aspsine on 15/12/31.
 */
public interface CallBack {

    void onSuccess(String response);

    void onProgress(long uploaded, long total);

    void onFailure(Exception e);

}
