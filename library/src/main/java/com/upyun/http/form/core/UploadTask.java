package com.upyun.http.form.core;

import android.os.Process;

import com.upyun.http.form.UpYunError;
import com.upyun.http.form.api.OnCompleteListener;
import com.upyun.http.form.api.UpYunApi;
import com.upyun.http.form.entity.FileEntity;
import com.upyun.http.form.entity.MultiPartFormData;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by aspsine on 15/12/28.
 */
public class UploadTask implements Runnable {

    public static final String ENCODING = "utf-8";

    public static final int CONNECT_TIME_OUT = 16 * 1000;

    public static final int READ_TIME_OUT = 60 * 1000;

    private static final String TWO_HYPHENS = "--";

    private static final String END = "\r\n";

    private static final String BOUNDARY = "*-*-*_UpYun_*-*-*";

    private static final String QUOTATION_MARK = "\"";

    private final String mUrl;

    private final MultiPartFormData mMultiPartFormData;

    private final ResponseDelivery mDelivery;

    private final UpYunApi.UpYunCallback mCallback;

    private final Object mTag;

    private final OnCompleteListener mListener;

    private volatile boolean mCancel;

    public UploadTask(String url, MultiPartFormData multiPartFormData, ResponseDelivery delivery, UpYunApi.UpYunCallback callback, Object tag, OnCompleteListener listener) {
        this.mUrl = url;
        this.mMultiPartFormData = multiPartFormData;
        this.mDelivery = delivery;
        this.mCallback = callback;
        this.mTag = tag;
        this.mListener = listener;
        this.mCancel = false;
    }

    public void cancel() {
        this.mCancel = true;
    }

    public boolean isCanceled() {
        return mCancel;
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        try {
            executeUpLoad();
        } catch (UpYunError error) {
            mDelivery.onFailure(error, mCallback);
        }
    }

    void executeUpLoad() throws UpYunError {
        final URL url;
        try {
            url = new URL(mUrl);
        } catch (MalformedURLException e) {
            throw new UpYunError("bad url!", e);
        }
        HttpURLConnection connection = null;
        try {

            connection = (HttpURLConnection) url.openConnection();

            configConnection(connection);

            transferData(connection);

        } catch (IOException e) {
            throw new UpYunError(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void transferData(HttpURLConnection connection) throws IOException, UpYunError {
        DataOutputStream dataOutputStream = null;
        BufferedInputStream inputStream = null;
        try {
            dataOutputStream = new DataOutputStream(connection.getOutputStream());

            writeStringFormParams(dataOutputStream);

            writeFileFormParams(dataOutputStream);

            writeParamsEnd(dataOutputStream);

            final int code = connection.getResponseCode();

            if (code == HttpURLConnection.HTTP_OK) {
                inputStream = (BufferedInputStream) connection.getInputStream();

                readResponse(inputStream);
            } else {
                InputStream errorStream = connection.getErrorStream();
                readErrorResponse(code, errorStream);
            }
        } finally {
            if (dataOutputStream != null) {
                dataOutputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private void readErrorResponse(int code, InputStream inputStream) throws IOException, UpYunError {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuffer stringBuffer = new StringBuffer();
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
            checkCanceled();
            stringBuffer.append(line);
        }
        throw new UpYunError("code = " + code + " message=" + stringBuffer);
    }

    private void readResponse(BufferedInputStream inputStream) throws IOException, UpYunError {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuffer stringBuffer = new StringBuffer();
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
            checkCanceled();
            stringBuffer.append(line);
        }
        mCallback.onSuccess(stringBuffer.toString());
    }

    protected void configConnection(HttpURLConnection connection) throws ProtocolException {
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(CONNECT_TIME_OUT);
        connection.setReadTimeout(READ_TIME_OUT);
        connection.setRequestProperty("Charset", ENCODING);
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Cache-Control", "no-cache");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
    }

    protected void writeStringFormParams(DataOutputStream dataOutputStream) throws IOException, UpYunError {
        Map<String, String> params = mMultiPartFormData.getParams();
        for (String key : params.keySet()) {
            checkCanceled();
            String value = params.get(key);
            dataOutputStream.writeBytes(TWO_HYPHENS + BOUNDARY + END);
            dataOutputStream.writeBytes("Content-Disposition: form-data; name=" + QUOTATION_MARK + encodeKey(key) + QUOTATION_MARK + END);
            dataOutputStream.writeBytes("Content-Type: text/plain; charset=" + ENCODING + END);
            dataOutputStream.writeBytes(END);
            dataOutputStream.writeBytes(encodeValue(value) + END);
        }
        dataOutputStream.flush();
    }

    protected void writeFileFormParams(DataOutputStream dataOutputStream) throws IOException, UpYunError {
        Map<String, FileEntity> params = mMultiPartFormData.getFileParams();
        for (String key : params.keySet()) {
            FileEntity value = params.get(key);

            String fileName = value.getName();
            File file = value.getFile();

            dataOutputStream.writeBytes(TWO_HYPHENS + BOUNDARY + END);
            dataOutputStream.writeBytes("Content-Disposition: form-data; name=" + QUOTATION_MARK + key + QUOTATION_MARK + "; filename=" + QUOTATION_MARK + encodeKey(fileName) + QUOTATION_MARK + END);
            dataOutputStream.writeBytes("Content-Type: " + getContentType(file) + END);
            dataOutputStream.writeBytes(END);
            writeFileBytes(dataOutputStream, file);
            dataOutputStream.writeBytes(END);
        }
        dataOutputStream.flush();
    }


    protected void writeFileBytes(DataOutputStream dataOutputStream, File file) throws IOException, UpYunError {
        FileInputStream inputStream = new FileInputStream(file);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 16];
        int count = -1;
        while ((count = inputStream.read(buffer)) != -1) {
            checkCanceled();
            out.write(buffer, 0, count);
        }
        dataOutputStream.write(out.toByteArray());
        dataOutputStream.flush();
        inputStream.close();
    }

    public String getContentType(File file) {
        String fileName = file.getName();
        return URLConnection.guessContentTypeFromName(fileName);
    }

    protected void writeParamsEnd(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeBytes(TWO_HYPHENS + BOUNDARY + TWO_HYPHENS + END);
        dataOutputStream.writeBytes(END);
    }

    protected String encodeKey(String key) {
        return key;
    }

    protected String encodeValue(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, ENCODING);
    }

    private void checkCanceled() throws UpYunError {
        if (isCanceled()) {
            throw new UpYunError("Upload task has been canceled");
        }
    }

}
