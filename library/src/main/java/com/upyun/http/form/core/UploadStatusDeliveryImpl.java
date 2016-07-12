package com.upyun.http.form.core;

import android.os.Handler;
import android.os.Message;

/**
 * Created by aspsine on 15/12/31.
 */
public class UploadStatusDeliveryImpl implements UploadStatusDelivery {

    private Handler mHandler;

    private UploadStatusDeliveryRunnable mRunnable;

    public UploadStatusDeliveryImpl(Handler handler) {
        this.mHandler = handler;
        mRunnable = new UploadStatusDeliveryRunnable();
    }

    @Override
    public void deliver(UploadStatus status) {
        mRunnable.setUploadStatus(status);
        mHandler.post(mRunnable);
    }

    private static class UploadStatusDeliveryRunnable implements Runnable {
        private UploadStatus mStatus;

        @Override
        public void run() {

        }

        public void setUploadStatus(UploadStatus uploadStatus) {
            this.mStatus = uploadStatus;
        }
    }
}
