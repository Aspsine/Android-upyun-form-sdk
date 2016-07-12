package com.upyun.http.form.core;

import android.os.Looper;

import com.upyun.http.form.api.OnCompleteListener;
import com.upyun.http.form.api.UpYunApi;
import com.upyun.http.form.entity.MultiPartFormData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by aspsine on 15/12/28.
 */
public class UpYunApiManager implements UpYunApi {

    private static final int MAX_THREAD_NUM = 3;

    private static UpYunApiManager sInstance;

    private static Executor mExecutor;

    private final Map<Object, UploadTask> mTaskMap;

    private final ResponseDelivery mDelivery;

    private final OnCompleteListener mCompleteCallback;

    public static UpYunApiManager getInstance() {
        if (sInstance == null) {
            synchronized (UpYunApiManager.class) {
                if (sInstance == null) {
                    sInstance = new UpYunApiManager();
                }
            }
        }
        return sInstance;
    }

    private UpYunApiManager() {
        mDelivery = new ResponseDelivery(Looper.getMainLooper());
        mTaskMap = new LinkedHashMap<>();
        mExecutor = Executors.newFixedThreadPool(MAX_THREAD_NUM);
        mCompleteCallback = new OnCompleteListener() {
            @Override
            public void onComplete(Object tag) {
                mTaskMap.remove(tag);
            }
        };
    }

    @Override
    public void upload(String url, MultiPartFormData multiPartFormData, Object tag, UpYunCallback callback) {
        if (mTaskMap.containsKey(tag)) {
            mTaskMap.get(tag).cancel();
            mTaskMap.remove(tag);
        }
        UploadTask task = new UploadTask(url, multiPartFormData, mDelivery,callback, tag, mCompleteCallback);
        mExecutor.execute(task);
    }

    @Override
    public void cancelAll() {
        for (UploadTask task : mTaskMap.values()) {
            task.cancel();
        }
        mTaskMap.clear();
        mDelivery.removeCallbacksAndMessages(null);
    }
}
