package com.upyun.http.form.api;

import com.upyun.http.form.entity.MultiPartFormData;

/**
 * Created by aspsine on 15/12/28.
 */
public interface UpYunApi {

    void upload(String url, MultiPartFormData multiPartFormData, Object tag, UpYunCallback callback);

    void cancelAll();

    interface UpYunCallback {
        void onSuccess(String response);

        void onProgress(long uploaded, long total);

        void onFailure(Exception e);
    }
}
