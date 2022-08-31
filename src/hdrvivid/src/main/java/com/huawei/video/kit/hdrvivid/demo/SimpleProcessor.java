/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.huawei.video.kit.hdrvivid.demo;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import android.content.Context;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import com.huawei.hms.videokit.hdrability.ability.HdrAbility;
import com.huawei.hms.videokit.hdrvivid.render.HdrVividRender;
import com.huawei.video.kit.hdrvivid.demo.base.SimplePacket;
import com.huawei.video.kit.hdrvivid.demo.hdr.HdrMetaData;
import com.huawei.video.kit.hdrvivid.demo.utils.Constants;
import com.huawei.video.kit.hdrvivid.demo.utils.DeviceUtils;
import com.huawei.video.kit.hdrvivid.demo.utils.SimpleErrorUtils;
import com.huawei.video.kit.hdrvivid.demo.utils.SimpleFileUtils;
import com.huawei.video.kit.hdrvivid.demo.utils.SimpleSetting;
import com.huawei.video.kit.hdrvivid.demo.utils.VideoInfoUtils;

/**
 * process mp4 file
 *
 * @since 2022/5/5
 */
public class SimpleProcessor {
    private static final String TAG = "SimpleProcessor";

    private static SimpleProcessor simpleProcessor = null;

    public static final int STATUS_PLAYING = 1;

    public static final int STATUS_PAUSEED = 2;

    public static final int STATUS_STOPED = 3;

    public int status = -1;

    private SurfaceView surfaceView;

    private HdrVividRender hdrVividRender = null;

    private SimpleJni simpleJni = null;

    private SimpleExtractor simpleExtractor = null;

    private SimpleCodec simpleCodec = null;

    private SimpleFileUtils simpleFileUtils = null;

    private VideoInfoUtils videoInfoUtils = null;

    HdrVividRender.StaticMetaData lastStaticMetaData = null;

    private boolean isScreenHdr = false;

    private SimpleProcessor() {
        simpleJni = SimpleJni.getInstance();
        simpleExtractor = new SimpleExtractor();
        simpleFileUtils = new SimpleFileUtils();
        videoInfoUtils = VideoInfoUtils.getInstance();
    }

    public static SimpleProcessor getInstance() {
        if (simpleProcessor == null) {
            synchronized (SimpleProcessor.class) {
                if (simpleProcessor == null) {
                    simpleProcessor = new SimpleProcessor();
                }
            }
        }
        return simpleProcessor;
    }

    public void init(Context context) {
        simpleFileUtils.initOutputDir();
        isScreenHdr = DeviceUtils.isScreenHdr(context);
        if (SimpleSetting.getInstance().getApiType() == Constants.API_TYPE_NATIVE) {
            simpleJni.init(SimpleSetting.getInstance(), isScreenHdr);
        }
    }

    public void release() {
        simpleJni.release();
    }

    public void initHdrAbility(SurfaceView surfaceView) {
        Log.i(TAG, "initHdrAbility");

        int ret = SimpleErrorUtils.SIMPLE_SUCCESS;
        ret = HdrAbility.init(surfaceView.getContext());
        if (ret != HdrAbility.HDR_ABILITY_SUCCESS) {
            Log.i(TAG, "HdrAbility init failed");
            SimpleErrorUtils.showInfoToast(SimpleErrorUtils.SIMPLE_ERR_HDR_ABILITY_NOT_SUPPORT);
        }
        boolean success = HdrAbility.setHdrAbility(true);
        if (!success) {
            Log.i(TAG, "HdrAbility setHdrAbility failed");
            SimpleErrorUtils.showInfoToast(SimpleErrorUtils.SIMPLE_ERR_HDR_ABILITY_NOT_SUPPORT);
        }
        ret = HdrAbility.setBrightness(SimpleSetting.getInstance().getBrightness());
        if (ret != HdrAbility.HDR_ABILITY_SUCCESS) {
            Log.i(TAG, "HdrAbility setBrightness failed");
            SimpleErrorUtils.showInfoToast(SimpleErrorUtils.SIMPLE_ERR_HDR_ABILITY_NOT_SUPPORT);
        }
    }

    public void releaseHdrAbility() {
        Log.i(TAG, "releaseHdrAbility");
        boolean success = HdrAbility.setHdrAbility(false);
        if (!success) {
            Log.i(TAG, "HdrAbility setHdrAbility failed");
            SimpleErrorUtils.showInfoToast(SimpleErrorUtils.SIMPLE_ERR_HDR_ABILITY_NOT_SUPPORT);
        }
    }

    public void startPlay() {
        Log.i(TAG, "startPlay begin, filePath=" + SimpleSetting.getInstance().getFilePath());
        int ret = SimpleErrorUtils.SIMPLE_SUCCESS;

        if (SimpleSetting.getInstance().getApiType() == Constants.API_TYPE_JAVA) {
            ret = startPlayWithJavaAPI();
        } else {
            ret = startPlayWithNativeAPI();
        }
        if (ret != SimpleErrorUtils.SIMPLE_SUCCESS) {
            SimpleErrorUtils.showErrToast(ret);
            return;
        }

        if (Constants.BUFFER_OUTPUT_ENABLE) {
            if (SimpleSetting.getInstance().getOutputMode() == Constants.OUT_MODE_BUFFER) {
                SimpleSetting.getInstance().setBufferOutFilePath(simpleFileUtils.genOutputFilePath());

                if (SimpleSetting.getInstance().getApiType() == Constants.API_TYPE_JAVA) {
                    simpleFileUtils.openOutputFile();
                } else {
                    SimpleJni.getInstance().setBufferOutFilePath(SimpleSetting.getInstance().getBufferOutFilePath());
                }
            }
        }

        if (SimpleSetting.getInstance().getOutputMode() == Constants.OUT_MODE_BUFFER
            && SimpleSetting.getInstance().getOutputColorFormat() != HdrVividRender.COLORFORMAT_R8G8B8A8) {
            SimpleErrorUtils.showInfoToast(SimpleErrorUtils.SIMPLE_INFO_RENDER_NOT_SUPPORT);
        }

        status = STATUS_PLAYING;

        Log.d(TAG, "startPlay end");
    }

    public void stopPlay() {
        Log.d(TAG, "stopPlay begin");
        int ret = 0;

        if (status == STATUS_STOPED) {
            return;
        }
        status = STATUS_STOPED;

        if (SimpleSetting.getInstance().getApiType() == Constants.API_TYPE_JAVA) {
            if (simpleCodec != null) {
                simpleCodec.stop();
                simpleCodec = null;
            }
            if (hdrVividRender != null) {
                ret = hdrVividRender.stop();
                if (ret != HdrVividRender.HDRVIVID_SUCCESS) {
                    SimpleErrorUtils.showErrToast(SimpleErrorUtils.SIMPLE_ERR_SDK_STOP_FAILED);
                }
                hdrVividRender.release();
                hdrVividRender = null;
            }

            if (SimpleSetting.getInstance().getOutputMode() == Constants.OUT_MODE_BUFFER
                && SimpleSetting.getInstance().getOutputColorFormat() == HdrVividRender.COLORFORMAT_R8G8B8A8) {
                simpleJni.nativeStopRender();
            }

            lastStaticMetaData = null;
        } else {
            ret = simpleJni.stopPlay();
            if (ret != SimpleErrorUtils.SIMPLE_SUCCESS) {
                SimpleErrorUtils.showErrToast(ret);
            }
        }

        if (Constants.BUFFER_OUTPUT_ENABLE && (SimpleSetting.getInstance().getApiType() == Constants.API_TYPE_JAVA)) {
            simpleFileUtils.closeOutputFile();
        }
        Log.d(TAG, "stopPlay end");
    }

    public void pausePlay() {
        status = STATUS_PAUSEED;
        if (SimpleSetting.getInstance().getApiType() == Constants.API_TYPE_JAVA) {
            simpleCodec.pause();
        } else {
            simpleJni.pause();
        }
    }

    public void resumePlay() {
        status = STATUS_PLAYING;
        if (SimpleSetting.getInstance().getApiType() == Constants.API_TYPE_JAVA) {
            simpleCodec.play();
        } else {
            simpleJni.play();
        }
    }

    public void resizeOutputSize(int width, int height) {
        if (SimpleSetting.getInstance().getApiType() == Constants.API_TYPE_JAVA) {
            if (hdrVividRender != null) {
                Log.d(TAG, "hdrVividRender setOutputSurfaceSize, width=" + width + ", height=" + height);
                int ret = hdrVividRender.setOutputSurfaceSize(width, height);
                if (ret != HdrVividRender.HDRVIVID_SUCCESS) {
                    Log.e(TAG, "setOutputSurfaceSize, ret: " + ret);
                }
            }
        } else {
            simpleProcessor.setOutputSurface(surfaceView);
        }
    }

    public void setOutputSurface(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
        simpleJni.setOutSurface(surfaceView.getHolder().getSurface());
    }

    private int startPlayWithJavaAPI() {
        Log.i(TAG, "startPlayWithJavaAPI begin");
        int ret = 0;

        simpleCodec = new SimpleCodec();

        lastStaticMetaData = new HdrVividRender.StaticMetaData();

        hdrVividRender = new HdrVividRender();

        Log.i(TAG, "setLogCallBack");
        hdrVividRender.setLogCallBack(new HdrVividRender.LogCallback() {
            @Override
            public int onOutputLogInfo(int level, String info) {
                String avcl = "HVDemoJava";
                switch (level) {
                    case HdrVividRender.HDRVIVID_LOG_INFO:
                        Log.i(avcl, info);
                        break;
                    case HdrVividRender.HDRVIVID_LOG_DEBUG:
                        Log.d(avcl, info);
                        break;
                    case HdrVividRender.HDRVIVID_LOG_WARN:
                        Log.w(avcl, info);
                        break;
                    case HdrVividRender.HDRVIVID_LOG_ERROR:
                        Log.e(avcl, info);
                        break;
                    default:
                        Log.i(avcl, info);
                        break;
                }
                return 0;
            }
        });
        if (!hdrVividRender.init()) {
            return SimpleErrorUtils.SIMPLE_ERR_SDK_START_FAILED;
        }

        ret = hdrVividRender.setTransFunc(videoInfoUtils.getVideoInfo().getTf());
        if (ret != HdrVividRender.HDRVIVID_SUCCESS) {
            return SimpleErrorUtils.SIMPLE_ERR_SDK_START_FAILED;
        }

        ret = hdrVividRender.setInputVideoSize(videoInfoUtils.getVideoInfo().getWidth(),
            videoInfoUtils.getVideoInfo().getHeight());
        if (ret != HdrVividRender.HDRVIVID_SUCCESS) {
            return SimpleErrorUtils.SIMPLE_ERR_SDK_START_FAILED;
        }

        Surface inputSurface = hdrVividRender.createInputSurface();

        if (SimpleSetting.getInstance().getOutputMode() == Constants.OUT_MODE_SURFACE) {
            ret = hdrVividRender.setOutputSurfaceSize(surfaceView.getWidth(), surfaceView.getHeight());
            if (ret != HdrVividRender.HDRVIVID_SUCCESS) {
                return SimpleErrorUtils.SIMPLE_ERR_SDK_START_FAILED;
            }

            ret = hdrVividRender.configure(inputSurface, new HdrVividRender.InputCallback() {
                @Override
                public int onGetDynamicMetaData(HdrVividRender hdrVividRender, long pts) {
                    Log.d(TAG, "hdrVividRender onGetDynamicMetaData pts: " + pts);
                    simpleProcessor.onGetDynamicMetaData(pts);
                    return 0;
                }
            }, surfaceView.getHolder().getSurface(), null);
            if (ret != HdrVividRender.HDRVIVID_SUCCESS) {
                return SimpleErrorUtils.SIMPLE_ERR_SDK_START_FAILED;
            }
        } else {
            ret = hdrVividRender.setColorSpace(SimpleSetting.getInstance().getOutputColorSpace());
            if (ret != HdrVividRender.HDRVIVID_SUCCESS) {
                return SimpleErrorUtils.SIMPLE_ERR_SDK_START_FAILED;
            }
            ret = hdrVividRender.setColorFormat(SimpleSetting.getInstance().getOutputColorFormat());
            if (ret != HdrVividRender.HDRVIVID_SUCCESS) {
                return SimpleErrorUtils.SIMPLE_ERR_SDK_START_FAILED;
            }
            ret = hdrVividRender.configure(inputSurface, new HdrVividRender.InputCallback() {

                @Override
                public int onGetDynamicMetaData(HdrVividRender hdrVividRender, long pts) {
                    simpleProcessor.onGetDynamicMetaData(pts);
                    return 0;
                }
            }, null, new HdrVividRender.OutputCallback() {
                @Override
                public void onOutputBufferAvailable(HdrVividRender hdrVividRender, ByteBuffer byteBuffer,
                    HdrVividRender.BufferInfo bufferInfo) {
                    Log.d(TAG, "onOutputBufferAvailable: " + byteBuffer.position() + " " + byteBuffer.limit() + " "
                        + bufferInfo.flags + " " + bufferInfo.ptsUs);
                    if (status == STATUS_PLAYING) {
                        renderBuffer(byteBuffer, VideoInfoUtils.getInstance().getVideoInfo().getWidth(),
                            VideoInfoUtils.getInstance().getVideoInfo().getHeight());
                    }
                    byteBuffer.position(0);
                }
            });
            if (ret != HdrVividRender.HDRVIVID_SUCCESS) {
                return SimpleErrorUtils.SIMPLE_ERR_SDK_START_FAILED;
            }
        }

        ret = hdrVividRender.start();
        if (ret != HdrVividRender.HDRVIVID_SUCCESS) {
            return SimpleErrorUtils.SIMPLE_ERR_SDK_START_FAILED;
        }

        if (SimpleSetting.getInstance().getOutputMode() == Constants.OUT_MODE_BUFFER
            && SimpleSetting.getInstance().getOutputColorFormat() == HdrVividRender.COLORFORMAT_R8G8B8A8) {
            simpleJni.nativeStartRender(videoInfoUtils.getVideoInfo().getWidth(),
                videoInfoUtils.getVideoInfo().getHeight(), surfaceView.getHolder().getSurface());
        }

        simpleCodec.start(inputSurface, SimpleSetting.getInstance().getFilePath(),
            videoInfoUtils.getVideoInfo().getWidth(), videoInfoUtils.getVideoInfo().getHeight(),
            SimpleSetting.getInstance().getOutputMode());

        return SimpleErrorUtils.SIMPLE_SUCCESS;
    }

    private int startPlayWithNativeAPI() {
        Log.i(TAG, "startPlayWithNativeAPI begin");

        if (SimpleSetting.getInstance().getInputMode() == Constants.INPUT_MODE_SURFACE) {
            Surface inputSurface = simpleJni.createSurface4Native();
            simpleJni.setInputSurface(inputSurface);
        }

        return simpleJni.startPlay(SimpleCodec.getDecoderName());
    }

    private void onGetDynamicMetaData(long pts) {
        if (simpleCodec == null) {
            return;
        }
        SimplePacket simplePacket = simpleCodec.getSimplePacket(pts);
        setStaticMetaData(simplePacket);
        setDynamicMetaData(simplePacket);
        videoInfoUtils.increaseFrame(pts);
    }

    private void setStaticMetaData(SimplePacket simplePacket) {
        if (simplePacket == null) {
            return;
        }

        HdrMetaData hmd = simplePacket.hmd;

        if (isEquals(hmd)) {
            return;
        }

        lastStaticMetaData.gDisplayPrimariesX = simplePacket.hmd.gX;
        lastStaticMetaData.gDisplayPrimariesY = simplePacket.hmd.gY;
        lastStaticMetaData.bDisplayPrimariesX = simplePacket.hmd.bX;
        lastStaticMetaData.bDisplayPrimariesY = simplePacket.hmd.bY;
        lastStaticMetaData.rDisplayPrimariesX = simplePacket.hmd.rX;
        lastStaticMetaData.rDisplayPrimariesY = simplePacket.hmd.rY;
        lastStaticMetaData.whitePointX = simplePacket.hmd.whitePointX;
        lastStaticMetaData.whitePointY = simplePacket.hmd.whitePointY;
        lastStaticMetaData.maxDisplayMasteringLum = simplePacket.hmd.maxDisplayMasteringLum;
        lastStaticMetaData.minDisplayMasteringLum = simplePacket.hmd.minDisplayMasteringLum;
        lastStaticMetaData.maxContentLightLevel = simplePacket.hmd.maxContentLightLevel;
        lastStaticMetaData.maxPicAverageLightLevel = simplePacket.hmd.maxPicAverageLightLevel;

        Log.i(TAG,
            "setStaticMetaData: " + simplePacket.ptsUs + ", mdcv= " + lastStaticMetaData.gDisplayPrimariesX + " "
                + lastStaticMetaData.gDisplayPrimariesY + " " + lastStaticMetaData.bDisplayPrimariesX + " "
                + lastStaticMetaData.bDisplayPrimariesY + " " + lastStaticMetaData.rDisplayPrimariesX + " "
                + lastStaticMetaData.rDisplayPrimariesY + " " + lastStaticMetaData.whitePointX + " "
                + lastStaticMetaData.whitePointY + " " + lastStaticMetaData.maxDisplayMasteringLum + " "
                + lastStaticMetaData.minDisplayMasteringLum + ", clli=" + lastStaticMetaData.maxContentLightLevel + " "
                + lastStaticMetaData.maxPicAverageLightLevel);

        int ret = hdrVividRender.setStaticMetaData(lastStaticMetaData);
        if (ret != HdrVividRender.HDRVIVID_SUCCESS) {
            Log.e(TAG, "setStaticMetaData, ret: " + ret);
        }
    }

    private boolean isEquals(HdrMetaData hmd) {
        return hmd.gX == lastStaticMetaData.gDisplayPrimariesX && hmd.gY == lastStaticMetaData.gDisplayPrimariesY
            && hmd.bX == lastStaticMetaData.bDisplayPrimariesX && hmd.bY == lastStaticMetaData.bDisplayPrimariesY
            && hmd.rX == lastStaticMetaData.rDisplayPrimariesX && hmd.rY == lastStaticMetaData.rDisplayPrimariesY
            && hmd.whitePointX == lastStaticMetaData.whitePointX && hmd.whitePointY == lastStaticMetaData.whitePointY
            && hmd.maxDisplayMasteringLum == lastStaticMetaData.maxDisplayMasteringLum
            && hmd.minDisplayMasteringLum == lastStaticMetaData.minDisplayMasteringLum
            && hmd.maxContentLightLevel == lastStaticMetaData.maxContentLightLevel
            && hmd.maxPicAverageLightLevel == lastStaticMetaData.maxPicAverageLightLevel;
    }

    private void setDynamicMetaData(SimplePacket simplePacket) {
        if (simplePacket == null || simplePacket.hmd.dmSize <= 0) {
            return;
        }

        ByteBuffer dynamicMetaData = ByteBuffer.allocateDirect(simplePacket.hmd.dmSize).order(ByteOrder.nativeOrder());
        dynamicMetaData.put(simplePacket.hmd.dmData);
        int ret = hdrVividRender.setDynamicMetaData(simplePacket.ptsUs, dynamicMetaData);
        if (ret != HdrVividRender.HDRVIVID_SUCCESS) {
            Log.e(TAG, "setDynamicMetaData, ret: " + ret);
        }
        Log.d(TAG, "setDynamicMetaData: " + simplePacket.ptsUs + " " + Arrays.toString(dynamicMetaData.array()));
    }

    /**
     * render from buffer, only support YV12 and R8G8B8A8
     *
     * @param buffer buffer return by hdr vivid kit
     * @param width width of the buffers in pixels.
     * @param height height of the buffers in pixels.
     */
    private void renderBuffer(ByteBuffer buffer, int width, int height) {
        buffer.position(0);
        if (SimpleSetting.getInstance().getOutputMode() == Constants.OUT_MODE_BUFFER
            && SimpleSetting.getInstance().getOutputColorFormat() == HdrVividRender.COLORFORMAT_R8G8B8A8) {
            simpleJni.nativeRender(buffer, buffer.limit());
        }

        if (Constants.BUFFER_OUTPUT_ENABLE) {
            simpleFileUtils.writeOutputFile(buffer);
        }
        Log.d(TAG, "renderBuffer end");
    }
}
