/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.huawei.video.kit.hdrvivid.demo;

import static android.media.MediaCodecInfo.CodecProfileLevel.HEVCProfileMain10;
import static android.media.MediaCodecInfo.CodecProfileLevel.HEVCProfileMain10HDR10;
import static android.media.MediaCodecInfo.CodecProfileLevel.HEVCProfileMain10HDR10Plus;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import com.huawei.video.kit.hdrvivid.demo.base.MediaFormatBuffer;
import com.huawei.video.kit.hdrvivid.demo.base.SimplePacket;
import com.huawei.video.kit.hdrvivid.demo.utils.Constants;

/**
 * wrapper of MediaCodec
 */
public class SimpleCodec {
    private static final String TAG = "SimpleCodec";

    private static final int THREAD_SLEEP_MS = 500;

    private static final int CODEC_IN_TIMEOUT_US = 5 * 1000;

    private static final int CODEC_OUT_TIMEOUT_US = 50 * 1000;

    private static final int RENDER_SLEEP_MS = 5;

    private static final int US2MS = 1000;

    private static final String MEDIAFORMAT_KEY_CSD = "csd";

    private static final String MEDIAFORMAT_KEY_CSD_0 = "csd-0";

    private static final String MEDIAFORMAT_KEY_CSD_1 = "csd-1";

    private static final String MEDIAFORMAT_KEY_CSD_2 = "csd-2";

    private Hashtable<Long, SimplePacket> packetTable = new Hashtable<>(10);

    private DecoderThread decoderThread = null;

    private volatile boolean threadRun = false;

    private volatile boolean isPlaying = true;

    private SimpleExtractor simpleExtractor = null;

    private final Object lock = new Object();

    public SimpleCodec() {
        simpleExtractor = new SimpleExtractor();
    }

    public void start(Surface surface, String filePath, int width, int height, int outputMode) {
        Log.i(TAG, "startDecoderThread begin=" + decoderThread);

        if (decoderThread == null) {
            Log.i(TAG, "startDecoderThread create and start thread");
            decoderThread = new DecoderThread(surface, filePath, width, height, outputMode);
            threadRun = true;
            decoderThread.start();
        }

        Log.i(TAG, "startDecoderThread end");
    }

    public void stop() {
        Log.i(TAG, "stopDecoderThread begin=" + decoderThread);

        if (decoderThread != null) {
            threadRun = false;
            decoderThread.interrupt();

            try {
                decoderThread.join();
            } catch (InterruptedException e) {
                Log.w(TAG, "thread join failed");
            } finally {
                decoderThread = null;
            }
        }

        Log.i(TAG, "stopDecoderThread end");
    }

    public void pause() {
        isPlaying = false;
    }

    public void play() {
        isPlaying = true;
    }

    public SimplePacket getSimplePacket(long ptsUs) {
        SimplePacket simplePacket = null;
        synchronized (lock) {
            Set<Map.Entry<Long, SimplePacket>> entrySet = packetTable.entrySet();
            for (Map.Entry<Long, SimplePacket> entry : entrySet) {
                if (entry.getKey() == ptsUs) {
                    simplePacket = entry.getValue();
                }
            }
            if (simplePacket != null) {
                packetTable.remove(ptsUs);
            }
        }
        return simplePacket;
    }

    private class DecoderThread extends Thread {
        private final Surface surface;

        private final String filePath;

        private int width;

        private int height;

        private int outputMode;

        public DecoderThread(Surface surface, String filePath, int width, int height, int outputMode) {
            this.surface = surface;
            this.filePath = filePath;
            this.width = width;
            this.height = height;
            this.outputMode = outputMode;
        }

        @Override
        public void run() {
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, width, height);
            if (outputMode == Constants.OUT_MODE_BUFFER) {
                // If the output mode is buffer mode, set not allow drop frame
                mediaFormat.setInteger(MediaFormat.KEY_ALLOW_FRAME_DROP, 0);
            }

            simpleExtractor.openSimpleExtractor(filePath);
            setMediaFormatCSDInfo(mediaFormat);

            MediaCodec decoder = createCodec(mediaFormat, surface);
            if (decoder == null) {
                return;
            }

            SimplePacket simplePacket = new SimplePacket();

            decoder.start();

            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            boolean isEOS = false;
            long firstPts = -1;
            long startMs = System.currentTimeMillis();

            while (!Thread.interrupted() && threadRun) {
                if (!isPlaying) {
                    try {
                        sleep(THREAD_SLEEP_MS);
                        startMs = System.currentTimeMillis();
                        firstPts = -1;
                    } catch (InterruptedException e) {
                        Log.i(TAG, "paused sleep interrupt");
                    }
                    continue;
                }

                if (!isEOS) {
                    int inIndex = decoder.dequeueInputBuffer(CODEC_IN_TIMEOUT_US);
                    if (inIndex >= 0) {
                        boolean result = simpleExtractor.getSimpleNextPacket(simplePacket);
                        if (!result) {
                            Log.i(TAG, "InputBuffer BUFFER_FLAG_END_OF_STREAM");
                            decoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            isEOS = true;
                        } else {
                            ByteBuffer buffer = decoder.getInputBuffer(inIndex);
                            buffer.clear();
                            buffer.put(simplePacket.data, 0, simplePacket.size);
                            decoder.queueInputBuffer(inIndex, 0, simplePacket.size, simplePacket.ptsUs, 0);
                            SimplePacket packet = cloneSimplePacket(simplePacket);
                            packetTable.put(packet.ptsUs, packet);
                        }
                    }
                }

                int outIndex = decoder.dequeueOutputBuffer(info, CODEC_OUT_TIMEOUT_US);
                switch (outIndex) {
                    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                        Log.i(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                        break;
                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                        MediaFormat mf = decoder.getOutputFormat();
                        width = mf.getInteger(MediaFormat.KEY_WIDTH);
                        height = mf.getInteger(MediaFormat.KEY_HEIGHT);

                        Log.i(TAG, "new format, WxH=" + width + "x" + height);
                        break;
                    case MediaCodec.INFO_TRY_AGAIN_LATER:
                        break;
                    default:
                        if (firstPts == -1) {
                            firstPts = info.presentationTimeUs;
                        }

                        while ((info.presentationTimeUs - firstPts) / US2MS > System.currentTimeMillis() - startMs) {
                            try {
                                sleep(RENDER_SLEEP_MS);
                            } catch (InterruptedException e) {
                                Log.i(TAG, "display paused sleep interrupt");
                                break;
                            }
                        }
                        boolean render = true;
                        if (info.presentationTimeUs < 0) {
                            render = false;
                        }
                        decoder.releaseOutputBuffer(outIndex, render);
                        break;
                }

                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.i(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                    break;
                }
            }

            decoder.stop();
            decoder.release();
            simpleExtractor.closeSimpleExtractor();

            Log.i(TAG, "thread finished");
        }
    }

    private MediaCodec createCodec(MediaFormat format, Surface surface) {
        String decoderName = getDecoderName();
        if (decoderName == null) {
            Log.i(TAG, "get decoderName failed");
            return null;
        }

        MediaCodec codec = null;
        try {
            codec = MediaCodec.createByCodecName(decoderName);
        } catch (IOException e) {
            Log.i(TAG, "createByCodecName failed, name=" + decoderName + ", error=" + e.toString());
        }
        if (codec == null) {
            Log.d(TAG, "create decoder failed");
            return null;
        }

        codec.configure(format, surface, null, 0);
        return codec;
    }

    public static String getDecoderName() {
        boolean found = false;
        String decoderName = null;

        MediaCodecList mcList = new MediaCodecList(MediaCodecList.ALL_CODECS);
        MediaCodecInfo[] mcInfos = mcList.getCodecInfos();
        for (MediaCodecInfo mci : mcInfos) {
            if (mci.isEncoder()) {
                continue;
            }

            String[] types = mci.getSupportedTypes();
            String typesArr = Arrays.toString(types);
            if (!typesArr.contains("hevc")) {
                continue;
            }

            for (String type : types) {
                MediaCodecInfo.CodecCapabilities codecCapabilities = mci.getCapabilitiesForType(type);
                for (MediaCodecInfo.CodecProfileLevel codecProfileLevel : codecCapabilities.profileLevels) {
                    if (codecProfileLevel.profile == HEVCProfileMain10
                        || codecProfileLevel.profile == HEVCProfileMain10HDR10
                        || codecProfileLevel.profile == HEVCProfileMain10HDR10Plus) {
                        found = true;
                        decoderName = mci.getName();
                        break;
                    }
                }

                if (found) {
                    break;
                }
            }

            if (found) {
                break;
            }
        }

        return decoderName;
    }

    private SimplePacket cloneSimplePacket(SimplePacket srcPacket) {
        SimplePacket dstPacket = new SimplePacket();
        dstPacket.ptsUs = srcPacket.ptsUs;
        dstPacket.hmd.gX = srcPacket.hmd.gX;
        dstPacket.hmd.gY = srcPacket.hmd.gY;
        dstPacket.hmd.bX = srcPacket.hmd.bX;
        dstPacket.hmd.bY = srcPacket.hmd.bY;
        dstPacket.hmd.rX = srcPacket.hmd.rX;
        dstPacket.hmd.rY = srcPacket.hmd.rY;
        dstPacket.hmd.whitePointX = srcPacket.hmd.whitePointX;
        dstPacket.hmd.whitePointY = srcPacket.hmd.whitePointY;
        dstPacket.hmd.maxDisplayMasteringLum = srcPacket.hmd.maxDisplayMasteringLum;
        dstPacket.hmd.minDisplayMasteringLum = srcPacket.hmd.minDisplayMasteringLum;
        dstPacket.hmd.maxContentLightLevel = srcPacket.hmd.maxContentLightLevel;
        dstPacket.hmd.maxPicAverageLightLevel = srcPacket.hmd.maxPicAverageLightLevel;

        if (srcPacket.hmd.dmSize > 0) {
            dstPacket.hmd.dmData = Arrays.copyOf(srcPacket.hmd.dmData, srcPacket.hmd.dmSize);
            dstPacket.hmd.dmSize = srcPacket.hmd.dmSize;
        }

        return dstPacket;
    }

    private void setMediaFormatBuffer(MediaFormat mediaFormat, String name) {
        MediaFormatBuffer mediaFormatBuffer = new MediaFormatBuffer();
        if (simpleExtractor.getMediaFormatBuffer(mediaFormatBuffer, name)) {
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(mediaFormatBuffer.size);
            byteBuffer.put(mediaFormatBuffer.data);
            byteBuffer.position(0);
            mediaFormat.setByteBuffer(name, byteBuffer);
        }
    }

    private void setMediaFormatCSDInfo(MediaFormat mediaFormat) {
        setMediaFormatBuffer(mediaFormat, MEDIAFORMAT_KEY_CSD);
        setMediaFormatBuffer(mediaFormat, MEDIAFORMAT_KEY_CSD_0);
        setMediaFormatBuffer(mediaFormat, MEDIAFORMAT_KEY_CSD_1);
        setMediaFormatBuffer(mediaFormat, MEDIAFORMAT_KEY_CSD_2);
    }
}
