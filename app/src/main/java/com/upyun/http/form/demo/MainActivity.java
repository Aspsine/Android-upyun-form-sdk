package com.upyun.http.form.demo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.upyun.http.form.UpYunConstants;
import com.upyun.http.form.api.UpYunApi;
import com.upyun.http.form.core.UpYunApiManager;
import com.upyun.http.form.entity.FileEntity;
import com.upyun.http.form.entity.MultiPartFormData;
import com.upyun.http.form.utils.MD5;
import com.upyun.http.form.utils.PolicyUtils;
import com.upyun.http.form.utils.TimeUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE = 100;

    private ProgressDialog mProgressDialog;

    private ImageView ivPhoto;

    private String mFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivPhoto = (ImageView) findViewById(R.id.ivPhoto);

        Button btnChoose = (Button) findViewById(R.id.btnChoose);
        btnChoose.setOnClickListener(this);

        Button btnUpload = (Button) findViewById(R.id.btnUpload);
        btnUpload.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnChoose:
                choosePhoto();
                break;
            case R.id.btnUpload:
                uploadUpYun();
                break;
        }
    }

    private void choosePhoto() {
        Intent intents = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intents.setType("image/*");
        startActivityForResult(intents, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                Uri uri = data.getData();
                showPicture(uri);
                mFilePath = FileUtils.getImagePath(this, uri);
            }
        }
    }

    private void showPicture(Uri uri) {
        Picasso.with(this).load(uri).into(ivPhoto);
    }

    private void uploadUpYun() {
        String filePath = mFilePath;
        // 10min
        String expiration = TimeUtils.getExpiration(10 * 60 * 1000);
        String reportId = "1234";

        // policy
        Map<String, String> policyParams = new HashMap<>();
        policyParams.put(UpYunConstants.Key.BUCKET, Params.BUCKET);
        policyParams.put(UpYunConstants.Key.SAVE_KEY, Params.SAVE_KEY);
        policyParams.put(UpYunConstants.Key.EXPIRATION, expiration);
        policyParams.put(UpYunConstants.Key.NOTIFY_URL, Constants.NOTIFY_URL);
        policyParams.put(UpYunConstants.Key.EXT_PARAM, reportId);
        String policy = PolicyUtils.getPolicy(policyParams);

        // signature
        String signature = MD5.md5(policy + "&" + Constants.API_KEY);

        Map<String, String> params = new HashMap<>();
        params.put(UpYunConstants.Key.SIGNATURE, signature);
        params.put(UpYunConstants.Key.POLICY, policy);

        File file = new File(filePath);
        Map<String, FileEntity> fileParams = new HashMap<>();
        fileParams.put("file", new FileEntity(file.getName(), file));

        MultiPartFormData data = new MultiPartFormData(params, fileParams);

        showDialog();

        String url = Constants.UPYUN_API_URL + Params.BUCKET;

        UpYunApiManager.getInstance().upload(url, data, filePath, new UpYunApi.UpYunCallback() {
            @Override
            public void onSuccess(String response) {
                dismissDialog();
                Log.i("onSuccess", "response = " + response);
                Toast.makeText(MainActivity.this, String.valueOf(response), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(long uploaded, long total) {
                int progress = (int) ((uploaded / (float)total) * 100);
                setDialogProgress(progress);
                Log.i("onProgress", "uploaded = " + progress);
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                dismissDialog();
            }
        });
    }

    private void setDialogProgress(int progress) {
        if (0 <= progress && progress <= 100 && mProgressDialog.getProgress() != progress) {
            mProgressDialog.setProgress(progress);
            mProgressDialog.setMessage("正在上传 " + progress + "%");
        }
    }

    private void showDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        setDialogProgress(0);
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    private void dismissDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
