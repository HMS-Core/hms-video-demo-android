/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 * Description: decode H.265 ES by MediaCodec or libavcodec
 * Create: 2022-05-20
 */

#ifndef SIMPLE_CODEC_H
#define SIMPLE_CODEC_H

#include <map>
#include <atomic>
#include <thread>
#include <media/NdkMediaCodec.h>
#include "SimpleExtractor.h"

class SimpleCodec {
public:
    SimpleCodec();

    ~SimpleCodec();

    void SetHdrVividRender(HdrVividRender *hdrVividRender);

    void StartMc(std::string filePath, std::string decoderName, ANativeWindow *surface, int32_t width, int32_t height);

    void StopMc();

    void Pause();

    void Play();

    void SetStaticMetaData();

    void SetDynamicMetaData(int64_t ptsUs);

    void setJavaVm(JavaVM *javaVm);

private:
    void ThreadMc();

    AMediaCodec *InitMc();

    void RefreshStaticMetaData(SimplePacket *simplePacket);

    void InsertSimplePacket(SimplePacket *srcPacket);

    SimplePacket *CloneSimplePacket(SimplePacket *srcPacket);

    HdrVividBuffer *GetDynamicMetaData(int64_t ptsUs);

private:
    static constexpr const char *mimeHevc{"video/hevc"};
    static constexpr int32_t threadSleepUs{500 * 1000};
    static constexpr int32_t codecInTimeoutUs{5 * 1000};
    static constexpr int32_t codecOutTimeoutUs{50 * 1000};
    static constexpr int32_t renderSleepUs{5 * 1000};
    static const int32_t maxDmSize = 128;

    std::string m_filePath;
    std::string m_decoderName;
    ANativeWindow *m_surface{nullptr};
    int32_t m_width{-1};
    int32_t m_height{-1};

    std::thread m_stdThread;
    std::mutex m_stdMutex;
    std::atomic<bool> m_threadRun{false};
    std::atomic<bool> m_isPlaying{true};

    HdrVividRender *m_hdrVividRender{nullptr};

    std::mutex m_metaDataMutex;
    StaticMetaData m_staticMetaData;
    StaticMetaData m_lastStaticMetaData;
    HdrVividBuffer m_dynamicMetaData;
    std::map<int64_t, SimplePacket *> m_simplePacketMap;

    JavaVM *m_javaVm{nullptr};
};

#endif // SIMPLE_CODEC_H
