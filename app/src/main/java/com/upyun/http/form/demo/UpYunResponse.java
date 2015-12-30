package com.upyun.http.form.demo;

import com.google.gson.annotations.SerializedName;

/**
 * Created by aspsine on 15/12/28.
 */
public class UpYunResponse {
    private int code;
    private String message;
    private String url;
    private String time;
    @SerializedName("image-width")
    private String imageWidth;
    @SerializedName("image-height")
    private String imageHeight;
    @SerializedName("image-frames")
    private String imageFrames;
    @SerializedName("image-type")
    private String imageType;
    private String sign;
}
