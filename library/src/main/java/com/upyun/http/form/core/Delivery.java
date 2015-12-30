package com.upyun.http.form.core;

import com.upyun.http.form.api.UpYunApi;

/**
 * Created by aspsine on 15/12/28.
 */
public interface Delivery {
    void onProgress(long uploaded, long total, UpYunApi.UpYunCallback callback);

    void onSuccess(String response, UpYunApi.UpYunCallback callback);

    void onFailure(Exception e, UpYunApi.UpYunCallback callback);


}
