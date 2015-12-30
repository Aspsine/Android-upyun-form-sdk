package com.upyun.http.form.entity;

import java.util.Map;

/**
 * Created by aspsine on 15/12/28.
 */
public class MultiPartFormData {

    private Map<String, String> params;
    private Map<String, FileEntity> fileParams;

    public MultiPartFormData() {
    }

    public MultiPartFormData(Map<String, String> params, Map<String, FileEntity> fileParams) {
        this.params = params;
        this.fileParams = fileParams;
    }

    public Map<String, FileEntity> getFileParams() {
        return fileParams;
    }

    public void setFileParams(Map<String, FileEntity> fileParams) {
        this.fileParams = fileParams;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

}
