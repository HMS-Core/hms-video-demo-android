/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.huawei.video.kit.hdrvivid.demo;

import java.nio.ByteBuffer;

import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;

import com.huawei.video.kit.hdrvivid.demo.base.MediaFormatBuffer;
import com.huawei.video.kit.hdrvivid.demo.base.SimplePacket;
import com.huawei.video.kit.hdrvivid.demo.utils.SimpleErrorUtils;
import com.huawei.video.kit.hdrvivid.demo.utils.SimpleSetting;
import com.huawei.video.kit.hdrvivid.demo.utils.VideoInfoUtils;

/**
 * jni interface
 *
 * @since 2022/5/5
 */
public class SimpleJni {
    private static final String TAG = "SimpleJni";

    private static final int THREAD_SLEEP_MS = 100;

    private static volatile SimpleJni simpleJni = null;

    private SurfaceTexture nativeSurfaceTexture = null;

    private volatile boolean status = false;

    static {
        System.loadLibrary("SimpleJni");
    }

    private SimpleJni() {
    }

    public static SimpleJni getInstance() {
        if (simpleJni == null) {
            synchronized (SimpleJni.class) {
                if (simpleJni == null) {
                    simpleJni = new SimpleJni();
                }
            }
        }
        return simpleJni;
    }

    public void init(SimpleSetting simpleSetting, boolean isScreenHdr) {
        if (status) {
            Log.w(TAG, "init failed, status is true");
            return;
        }

        nativeInit(simpleSetting);
        status = true;
    }

    public void release() {
        if (!status) {
            Log.w(TAG, "release failed, status is false");
            return;
        }

        status = false;

        try {
            Thread.sleep(THREAD_SLEEP_MS);
        } catch (InterruptedException e) {
            Log.w(TAG, "thread sleep occur an exception");
        }

        nativeRelease();
    }

    public void setInputSurface(Surface surface) {
        if (!status) {
            return;
        }

        nativeSetInputSurface(surface);
    }

    public void setOutSurface(Surface surface) {
        if (!status) {
            return;
        }

        nativeSetOutSurface(surface);
    }

    public void setBufferOutFilePath(String bufferOutFilePath) {
        if (bufferOutFilePath == null) {
            return;
        }

        nativeSetBufferOutFilePath(bufferOutFilePath);
    }

    public int startPlay(String decoderName) {
        if (!status) {
            return SimpleErrorUtils.SIMPLE_ERR_UNKNOWN;
        }

        return nativeStartPlay(decoderName);
    }

    public int stopPlay() {
        if (!status) {
            return SimpleErrorUtils.SIMPLE_ERR_UNKNOWN;
        }

        return nativeStopPlay();
    }

    public void pause() {
        if (!status) {
            return;
        }

        nativePausePlay();
    }

    public void play() {
        if (!status) {
            return;
        }

        nativeResumePlay();
    }

    public Surface createSurface4Native() {
        if (!status) {
            return null;
        }

        nativeSurfaceTexture = new SurfaceTexture(nativeGetTextureId());
        nativeSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                if (!status) {
                    Log.w(TAG, "onFrameAvailable, status is false");
                    return;
                }
                nativeUpdateTexture();
            }
        });
        return new Surface(nativeSurfaceTexture);
    }

    public long onUpdateTexImageCallback4Native() {
        if (!status) {
            Log.w(TAG, "onUpdateTexImageCallback4Native, status is false");
            return -1;
        }
        nativeSurfaceTexture.updateTexImage();
        long ptsUs = nativeSurfaceTexture.getTimestamp();
        VideoInfoUtils.getInstance().increaseFrame(ptsUs / 1000);
        return ptsUs;
    }

    public void onReceiveFrameCallback(long ptsUs) {
        VideoInfoUtils.getInstance().increaseFrame(ptsUs);
    }

    private native void nativeInit(SimpleSetting simpleSetting);

    private native void nativeRelease();

    private native void nativeSetInputSurface(Surface surface);

    private native void nativeSetOutSurface(Surface surface);

    private native void nativeSetBufferOutFilePath(String bufferOutFilePath);

    private native int nativeStartPlay(String decoderName);

    private native int nativeStopPlay();

    private native void nativePausePlay();

    private native void nativeResumePlay();

    private native int nativeGetTextureId();

    private native int nativeUpdateTexture();

    // Native Extractor

    public native long nativeOpenExtractor(String filePath);

    public native void nativeCloseExtractor(long handleExtractor);

    public native boolean nativeGetNextPacket(long handleExtractor, SimplePacket simplePacket);

    public native boolean nativeGetMediaFormatBuffer(long handleExtractor, MediaFormatBuffer mediaFormatBuffer,
        String name);

    public native boolean nativeIsEof(long handleExtractor);

    public native void nativeSetHwCodec(long handleExtractor, boolean hwCodec);

    public native long nativeGetDurationUs(long handleExtractor);

    // Native Render

    public native void nativeStartRender(int width, int height, Surface surface);

    public native void nativeRender(ByteBuffer buffer, int dataSize);

    public native void nativeStopRender();
}
