package com.upyun.http.form.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.upyun.http.form.api.UpYunApi;

/**
 * Created by aspsine on 15/12/28.
 */
public class ResponseDelivery extends Handler implements Delivery {

    public ResponseDelivery(Looper looper) {
        super(looper);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        final int what = msg.what;
        final Status status = (Status) msg.obj;
        switch (what) {
            case Status.STATUS_PROGRESS:
                if (status != null) {
                    status.callback.onProgress(status.uploaded, status.total);
                }
                break;
            case Status.STATUS_SUCCESS:
                if (status != null) {
                    status.callback.onSuccess(status.response);
                }
                break;
            case Status.STATUS_FAILURE:
                if (status != null) {
                    status.callback.onFailure(status.e);
                }
                break;
        }
    }

    @Override
    public void onProgress(long uploaded, long total, UpYunApi.UpYunCallback callback) {
        synchronized (ResponseDelivery.class) {
            Message message = obtainMessage();
            message.what = Status.STATUS_PROGRESS;
            message.obj = new Status(uploaded, total, callback);
            message.sendToTarget();
        }
    }

    @Override
    public void onSuccess(String response, UpYunApi.UpYunCallback callback) {
        synchronized (ResponseDelivery.class) {
            Message message = obtainMessage();
            message.what = Status.STATUS_SUCCESS;
            message.obj = new Status(response, callback);
            message.sendToTarget();
        }
    }

    @Override
    public void onFailure(Exception e, UpYunApi.UpYunCallback callback) {
        synchronized (ResponseDelivery.class) {
            Message message = obtainMessage();
            message.what = Status.STATUS_FAILURE;
            message.obj = new Status(e, callback);
            message.sendToTarget();
        }
    }

    static class Status {
        static final int STATUS_PROGRESS = 100;

        static final int STATUS_SUCCESS = 200;

        static final int STATUS_FAILURE = 300;

        UpYunApi.UpYunCallback callback;
        long uploaded;
        long total;
        String response;
        Exception e;

        public Status(long uploaded, long total, UpYunApi.UpYunCallback callback) {
            this.uploaded = uploaded;
            this.total = total;
            this.callback = callback;
        }

        public Status(String response, UpYunApi.UpYunCallback callback) {
            this.response = response;
            this.callback = callback;
        }

        public Status(Exception e, UpYunApi.UpYunCallback callback) {
            this.e = e;
            this.callback = callback;
        }
    }
}
