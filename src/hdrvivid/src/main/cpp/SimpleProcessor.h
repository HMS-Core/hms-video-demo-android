/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 * Description: simple processor
 * Create: 2022-05-10
 */

#ifndef SIMPLE_PROCESSOR_H
#define SIMPLE_PROCESSOR_H

#include <string>
#include "SimpleLog.h"
#include "SimpleCommon.h"
#include "HdrVividKits.h"
#include "SimpleCodec.h"
#include "SimplePlayer.h"

struct VideoInfo {
    uint32_t width;
    uint32_t height;
    HdrVividTransFunc tf;
    HdrVividColorSpace colorSpace;
    HdrVividColorFormat colorFormat;
};

struct SimpleSetting {
    uint32_t inputMode;
    uint32_t outputMode;
    HdrVividColorSpace outputColorSpace;
    HdrVividColorFormat outputColorFormat;
    uint32_t brightness;
    std::string filePath;
    std::string outputBufferFilePath;
};

void OnUpdateTexImageCallback(JNIEnv *env, void *context, HdrVividRender *render, HdrVividTexImageInfo *texImageInfo);

class SimpleProcessor {
public:
    explicit SimpleProcessor(JavaVM *vm);

    ~SimpleProcessor();

public:
    int32_t StartPlay(const char *decoderName);

    int32_t StopPlay();

    void PausePlay();

    void ResumePlay();

    void SetInputSurface(ANativeWindow *window);

    void SetOutputSurface(ANativeWindow *window);

    void SetConfig(std::string filePath, uint32_t inputMode, uint32_t outputMode, HdrVividColorSpace outputColorSpace,
                   HdrVividColorFormat outputColorFormat, uint32_t brightness);

    void SetBufferOutFilePath(std::string bufferOutFilePath);

    uint32_t GenTextureId() const;

    int32_t UpdateTexture() const;

    void SetStaticMetaData();

    void SetDynamicMetaData(int64_t ptsUs);

    bool IsOutputFilePathValid() const;

    const char *GetOutputFilePath() const;

    HdrVividRender *GetHdrVividRender() const;

    const SimplePlayer *GetSimplePlayer() const;

    const SimpleSetting *GetSimpleSetting() const;

private:
    bool GetVideoInfo();

    int32_t ConfigHdrVividRender() const;

private:
    SimpleCodec m_simpleCodec;
    VideoInfo m_videoInfo{};
    ANativeWindow *m_inputWindow{};
    ANativeWindow *m_outputWindow{};
    HdrVividRender *m_hdrVividRender{};
    SimplePlayer m_simplePlayer;
    SimpleSetting m_simpleSetting{};
};

#endif // SIMPLE_PROCESSOR_H
