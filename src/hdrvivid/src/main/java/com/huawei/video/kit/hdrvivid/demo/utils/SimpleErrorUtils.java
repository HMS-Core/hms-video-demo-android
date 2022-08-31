/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.huawei.video.kit.hdrvivid.demo.utils;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class SimpleErrorUtils {
    private static final String TAG = "ErrorUtils";

    public static final int SIMPLE_SUCCESS = 0;

    public static final int SIMPLE_ERR_UNKNOWN = 10000;

    public static final int SIMPLE_ERR_SDK_START_FAILED = 10001;

    public static final int SIMPLE_ERR_SDK_STOP_FAILED = 10002;

    public static final int SIMPLE_ERR_SDK_SET_FAILED = 10003;

    public static final int SIMPLE_ERR_WRONG_INPUT_PARAM = 10004;

    public static final int SIMPLE_ERR_FORMAT_NOT_SUPPORT = 10005;

    public static final int SIMPLE_ERR_MEDIACODEC_INIT_FAILED = 10006;

    public static final int SIMPLE_ERR_HDR_ABILITY_NOT_SUPPORT = 10007;

    public static final int SIMPLE_INFO_RENDER_NOT_SUPPORT = 10100;

    public static final Map<Integer, String> ERR_MAP = new HashMap<Integer, String>() {
        {
            put(SIMPLE_ERR_UNKNOWN, "Unknown");
            put(SIMPLE_ERR_SDK_START_FAILED, "SDK start failed");
            put(SIMPLE_ERR_SDK_STOP_FAILED, "SDK stop failed");
            put(SIMPLE_ERR_SDK_SET_FAILED, "SDK set failed");
            put(SIMPLE_ERR_WRONG_INPUT_PARAM, "Wrong input param");
            put(SIMPLE_ERR_FORMAT_NOT_SUPPORT, "Video source file format not support");
            put(SIMPLE_ERR_MEDIACODEC_INIT_FAILED, "MediaCodec init failed");
            put(SIMPLE_ERR_HDR_ABILITY_NOT_SUPPORT, "HDR ability not support");
            put(SIMPLE_INFO_RENDER_NOT_SUPPORT,
                "Rendering only supported RGBA format image in this demo project, You can check the files in the output directory.");
        }
    };

    private static Context context = null;

    private static Handler mainHandler = new Handler(Looper.getMainLooper());

    private static String errorDesc = "";

    public static void setContext(Context context) {
        SimpleErrorUtils.context = context;
    }

    public static void showErrToast(int errCode) {
        if (!ERR_MAP.containsKey(errCode)) {
            return;
        }
        errorDesc = "Error: " + errCode + " " + ERR_MAP.get(errCode);
        showToast();
        Log.e(TAG, errorDesc);
    }

    public static void showInfoToast(int infoCode) {
        if (!ERR_MAP.containsKey(infoCode)) {
            return;
        }
        errorDesc = "Info: " + ERR_MAP.get(infoCode);
        showToast();
    }

    public static void showToast() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, errorDesc, Toast.LENGTH_LONG).show();
            }
        });
    }
}
