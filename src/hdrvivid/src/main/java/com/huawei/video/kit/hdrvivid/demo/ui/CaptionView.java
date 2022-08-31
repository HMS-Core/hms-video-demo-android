/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.huawei.video.kit.hdrvivid.demo.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

/**
 * Caption View
 *
 * @since 2022/5/5
 */
public class CaptionView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private static final String TAG = "CaptionView";

    private Paint mPaint;

    private SurfaceHolder mSurfaceHolder;

    private Thread mThread;

    private String info = "This video is wonderful.";

    private int textSize = 50;

    private void initial() {
        Log.d(TAG, "initial");
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);

        mThread = new Thread(this);

        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
    }

    public CaptionView(Context context) {
        super(context);
        initial();
    }

    public CaptionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initial();
    }

    public void setCaptionInfo(String info) {
        this.info = info;
    }

    @Override
    public void run() {
        Log.d(TAG, "run");
        draw();
    }

    public void draw() {
        Canvas mCanvas = null;
        int x = getContext().getResources().getDisplayMetrics().widthPixels / 2;
        int y = getContext().getResources().getDisplayMetrics().heightPixels / 2;
        Log.d(TAG, "draw " + x + " " + y + " " + info);
        try {
            mCanvas = mSurfaceHolder.lockCanvas();
            mCanvas.drawColor(Color.TRANSPARENT);
            mPaint.setTextSize(textSize);
            mCanvas.drawText(info, x, y, mPaint);
        } catch (Exception e) {
            Log.e(TAG, "caption view draw failed");
        } finally {
            if (mCanvas != null) {
                mSurfaceHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        mThread.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

    }
}
