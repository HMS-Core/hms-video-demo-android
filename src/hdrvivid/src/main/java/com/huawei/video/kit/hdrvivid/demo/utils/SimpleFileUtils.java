/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.huawei.video.kit.hdrvivid.demo.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

public class SimpleFileUtils {
    private static final String TAG = "SimpleFileUtils";

    private FileOutputStream fos = null;

    public void initOutputDir() {
        File dir = new File(Constants.BUFFER_OUTPUT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public String genOutputFilePath() {
        String filePath = SimpleSetting.getInstance().getFilePath();
        String fileName = filePath.substring(Constants.SRC_MOVIE_FILE_DIR.length(), filePath.length());
        fileName = fileName.replace(Constants.SRC_FILE_NAME_SUFFIX, "");

        SimpleDateFormat df = new SimpleDateFormat(Constants.FILE_NAME_DATE_FORMAT);
        String date = df.format(new Date());
        String outputFilePath = Constants.BUFFER_OUTPUT_DIR + fileName + date + Constants.DST_FILE_NAME_SUFFIX;

        Log.i(TAG, "output buffer file path is " + outputFilePath);
        return outputFilePath;
    }

    public void openOutputFile() {
        try {
            fos = new FileOutputStream(SimpleSetting.getInstance().getBufferOutFilePath());
        } catch (FileNotFoundException e) {
            Log.e(TAG, "open file exception");
        }
    }

    public void closeOutputFile() {
        try {
            if (fos != null) {
                fos.close();
                fos = null;
            }
        } catch (IOException e) {
            Log.e(TAG, "close file exception");
        }
    }

    public void writeOutputFile(ByteBuffer buffer) {
        try {
            if (fos != null) {
                FileChannel channel = fos.getChannel();
                channel.write(buffer);
            }
        } catch (IOException e) {
            Log.e(TAG, "write file exception");
        }
    }

}
