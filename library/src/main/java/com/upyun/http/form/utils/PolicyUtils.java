package com.upyun.http.form.utils;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by aspsine on 15/12/28.
 */
public class PolicyUtils {

    public static String getPolicy(Map<String, String> paramMap) {

        JSONObject obj = new JSONObject(paramMap);

        return Base64Coder.encodeString(obj.toString());
    }
}
