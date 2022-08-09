/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 * Description: simple processor
 * Create: 2022-05-10
 */

#include "SimpleProcessor.h"

static bool OnGetTexImageInfo(JNIEnv *env, void *context, HdrVividRender *render, HdrVividTexImageInfo *texImageInfo)
{
    OnUpdateTexImageCallback(env, context, render, texImageInfo);
    return true;
}

static void OnBufferAvailable(JNIEnv *env, void *context, HdrVividRender *render, HdrVividBuffer *buffer,
                              const HdrVividBufferInfo *info)
{
    if (context != nullptr) {
        SimpleProcessor *simpleProcessor = static_cast<SimpleProcessor *>(context);

        if (simpleProcessor->GetSimpleSetting()->outputMode == OUT_MODE_BUFFER &&
            simpleProcessor->GetSimpleSetting()->outputColorFormat == COLORFORMAT_R8G8B8A8) {
            SimplePlayer *simplePlayer = const_cast<SimplePlayer *>(simpleProcessor->GetSimplePlayer());
            simplePlayer->OnBufferAvailable(buffer->data, buffer->size);
        }

        if (simpleProcessor->IsOutputFilePathValid()) {
            EasyWriteFile(simpleProcessor->GetOutputFilePath(), buffer->data, buffer->size);
        }
        buffer->size = 0;
    }
}

SimpleProcessor::SimpleProcessor(JavaVM *vm)
{
    LOGI("%s begin", __FUNCTION__);
    m_hdrVividRender = HdrVividRenderInit(vm);
    m_simpleCodec.setJavaVm(vm);
    LOGI("%s end", __FUNCTION__);
}

SimpleProcessor::~SimpleProcessor()
{
    LOGI("%s begin", __FUNCTION__);
    HdrVividRenderRelease(m_hdrVividRender);
    LOGI("%s end", __FUNCTION__);
}

int32_t SimpleProcessor::StartPlay(const char *decoderName)
{
    LOGI("%s begin, decoderName=%s", __FUNCTION__, decoderName);

    bool success = GetVideoInfo();
    if (!success) {
        LOGE("%s get video info failed", __FUNCTION__);
        return SIMPLE_ERR_FORMAT_NOT_SUPPORT;
    }

    int ret = ConfigHdrVividRender();
    if (ret != SIMPLE_SUCCESS) {
        LOGE("%s sdk start failed, ret %d", __FUNCTION__, ret);
        return ret;
    }
    if (m_simpleSetting.outputMode == OUT_MODE_BUFFER && m_simpleSetting.outputColorFormat == COLORFORMAT_R8G8B8A8) {
        m_simplePlayer.Init(m_videoInfo.width, m_videoInfo.height, m_outputWindow);
    }

    m_simpleCodec.SetHdrVividRender(m_hdrVividRender);
    if (m_simpleSetting.inputMode == INPUT_MODE_SURFACE) {
        m_simpleCodec.StartMc(m_simpleSetting.filePath, decoderName, m_inputWindow, m_videoInfo.width, m_videoInfo.height);
    }

    LOGI("%s end", __FUNCTION__);
    return SIMPLE_SUCCESS;
}

int32_t SimpleProcessor::StopPlay()
{
    LOGI("%s begin", __FUNCTION__);

    if (m_simpleSetting.inputMode == INPUT_MODE_SURFACE) {
        m_simpleCodec.StopMc();
    }
    HdrVividStatus hdrVividStatus = HdrVividRenderStop(m_hdrVividRender);
    if (m_simpleSetting.outputMode == OUT_MODE_BUFFER && m_simpleSetting.outputColorFormat == COLORFORMAT_R8G8B8A8) {
        m_simplePlayer.Release();
    }
    LOGI("%s end", __FUNCTION__);
    if (hdrVividStatus != HDRVIVID_SUCCESS) {
        LOGE("%s HdrVividRenderStop failed, hdrVividStatus %d", __FUNCTION__, hdrVividStatus);
        return SIMPLE_ERR_SDK_STOP_FAILED;
    }
    return SIMPLE_SUCCESS;
}

void SimpleProcessor::PausePlay()
{
    m_simpleCodec.Pause();
}

void SimpleProcessor::ResumePlay()
{
    m_simpleCodec.Play();
}

void SimpleProcessor::SetInputSurface(ANativeWindow *window)
{
    m_inputWindow = window;
    if (window != nullptr) {
        LOGI("%s, WxH=%dx%d", __FUNCTION__, ANativeWindow_getWidth(window), ANativeWindow_getHeight(window));
    }
}

void SimpleProcessor::SetOutputSurface(ANativeWindow *window)
{
    m_outputWindow = window;
    if (window != nullptr) {
        if (m_hdrVividRender != nullptr) {
            HdrVividRenderSetOutputSurfaceSize(m_hdrVividRender, ANativeWindow_getWidth(window), ANativeWindow_getHeight(window));
        }
        LOGI("%s, WxH=%dx%d", __FUNCTION__, ANativeWindow_getWidth(window), ANativeWindow_getHeight(window));
    }
}

void SimpleProcessor::SetBufferOutFilePath(std::string bufferOutFilePath)
{
    m_simpleSetting.outputBufferFilePath = std::move(bufferOutFilePath);
}

void SimpleProcessor::SetConfig(std::string filePath, uint32_t inputMode, uint32_t outputMode,
                                HdrVividColorSpace outputColorSpace, HdrVividColorFormat outputColorFormat,
                                uint32_t brightness)
{
    m_simpleSetting.filePath = std::move(filePath);

    m_simpleSetting.inputMode = inputMode;
    m_simpleSetting.outputMode = outputMode;
    m_simpleSetting.outputColorSpace = outputColorSpace;
    m_simpleSetting.outputColorFormat = outputColorFormat;
    m_simpleSetting.brightness = brightness;

    LOGI("%s: filePath=%s, ioMode=%d/%d, output[cs/cf]=%d/%d, brightness=%d",
         __FUNCTION__, m_simpleSetting.filePath.c_str(), m_simpleSetting.inputMode, m_simpleSetting.outputMode,
         m_simpleSetting.outputColorSpace, m_simpleSetting.outputColorFormat, m_simpleSetting.brightness);
}

uint32_t SimpleProcessor::GenTextureId() const
{
    uint32_t textureId;
    HdrVividRenderGenInputTexture(m_hdrVividRender, &textureId);
    return textureId;
}

int32_t SimpleProcessor::UpdateTexture() const
{
    HdrVividStatus hdrVividStatus = HdrVividRenderUpdateInputTexImage(m_hdrVividRender);
    if (hdrVividStatus != HDRVIVID_SUCCESS) {
        LOGI("%s: HdrVividRenderUpdateInputTexImage failed, hdrVividStatus %d", __FUNCTION__, hdrVividStatus);
        return SIMPLE_ERR_SDK_SET_FAILED;
    }
    return SIMPLE_SUCCESS;
}

void SimpleProcessor::SetStaticMetaData()
{
    m_simpleCodec.SetStaticMetaData();
}

void SimpleProcessor::SetDynamicMetaData(int64_t ptsUs)
{
    m_simpleCodec.SetDynamicMetaData(ptsUs);
}

bool SimpleProcessor::IsOutputFilePathValid() const
{
    return m_simpleSetting.outputBufferFilePath.length() != 0;
}

const char *SimpleProcessor::GetOutputFilePath() const
{
    return m_simpleSetting.outputBufferFilePath.data();
}

HdrVividRender *SimpleProcessor::GetHdrVividRender() const
{
    return m_hdrVividRender;
}

const SimplePlayer *SimpleProcessor::GetSimplePlayer() const
{
    return &m_simplePlayer;
}

const SimpleSetting *SimpleProcessor::GetSimpleSetting() const
{
    return &m_simpleSetting;
}

bool SimpleProcessor::GetVideoInfo()
{
    auto *se = new SimpleExtractor();
    if (!se->Open(m_simpleSetting.filePath.c_str())) {
        se->Close();
        delete se;
        return false;
    }

    SimplePacket *simplePacket = se->GetNextSample();
    LOGI("%s width %d height %d tf %d colorSpace %d colorFormat %d", __FUNCTION__,
         simplePacket->GetWidth(), simplePacket->GetHeight(),
         simplePacket->GetTf(), simplePacket->GetColorSpace(), simplePacket->GetColorFormat());

    m_videoInfo.width = simplePacket->GetWidth();
    m_videoInfo.height = simplePacket->GetHeight();
    m_videoInfo.tf = simplePacket->GetTf();
    m_videoInfo.colorSpace = simplePacket->GetColorSpace();
    m_videoInfo.colorFormat = simplePacket->GetColorFormat();
    se->Close();
    delete se;
    return true;
}

int32_t SimpleProcessor::ConfigHdrVividRender() const
{
    HdrVividStatus hdrVividStatus;

    hdrVividStatus = HdrVividRenderSetInputVideoSize(m_hdrVividRender, m_videoInfo.width, m_videoInfo.height);
    if (hdrVividStatus != HDRVIVID_SUCCESS) {
        LOGE("%s hdr vivid render set input video size failed", __FUNCTION__);
        return SIMPLE_ERR_SDK_START_FAILED;
    }

    hdrVividStatus = HdrVividRenderSetTransFunc(m_hdrVividRender, m_videoInfo.tf);
    if (hdrVividStatus != HDRVIVID_SUCCESS) {
        LOGE("%s hdr vivid render set trans func failed", __FUNCTION__);
        return SIMPLE_ERR_SDK_START_FAILED;
    }

    hdrVividStatus = HdrVividRenderSetBrightness(m_hdrVividRender, m_simpleSetting.brightness);
    if (hdrVividStatus != HDRVIVID_SUCCESS) {
        LOGE("%s hdr vivid render set brightness failed", __FUNCTION__);
        return SIMPLE_ERR_SDK_START_FAILED;
    }

    HdrVividInputCallback inputCallback;
    inputCallback.context = (void *) this;

    ANativeWindow *inputWindowPtr = nullptr;
    HdrVividInputCallback *inputCallbackPtr = nullptr;
    if (m_simpleSetting.inputMode == INPUT_MODE_SURFACE) {
        inputCallback.onGetTexImageInfo = OnGetTexImageInfo;
        inputWindowPtr = m_inputWindow;
        inputCallbackPtr = &inputCallback;
    }

    HdrVividOutputCallback outputCallback;
    outputCallback.context = (void *) this;
    ANativeWindow *outputWindowPtr = nullptr;
    HdrVividOutputCallback *outputCallbackPtr = nullptr;
    if (m_simpleSetting.outputMode == OUT_MODE_SURFACE) {
        outputWindowPtr = m_outputWindow;

        hdrVividStatus = HdrVividRenderSetOutputSurfaceSize(m_hdrVividRender, ANativeWindow_getWidth(m_outputWindow), ANativeWindow_getHeight(m_outputWindow));
        if (hdrVividStatus != HDRVIVID_SUCCESS) {
            LOGE("%s hdr vivid render set output surface size failed", __FUNCTION__);
            return SIMPLE_ERR_SDK_START_FAILED;
        }
    } else if (m_simpleSetting.outputMode == OUT_MODE_BUFFER) {
        outputCallback.onBufferAvailable = OnBufferAvailable;
        outputCallbackPtr = &outputCallback;

        hdrVividStatus = HdrVividRenderSetColorSpace(m_hdrVividRender, m_simpleSetting.outputColorSpace);
        if (hdrVividStatus != HDRVIVID_SUCCESS) {
            LOGE("%s hdr vivid render set color space failed", __FUNCTION__);
            return SIMPLE_ERR_SDK_START_FAILED;
        }

        hdrVividStatus = HdrVividRenderSetColorFormat(m_hdrVividRender, m_simpleSetting.outputColorFormat);
        if (hdrVividStatus != HDRVIVID_SUCCESS) {
            LOGE("%s hdr vivid render set color space failed", __FUNCTION__);
            return SIMPLE_ERR_SDK_START_FAILED;
        }
    }
    hdrVividStatus = HdrVividRenderConfigure(m_hdrVividRender, inputWindowPtr, inputCallbackPtr, outputWindowPtr, outputCallbackPtr);
    if (hdrVividStatus != HDRVIVID_SUCCESS) {
        LOGE("%s hdr vivid render configure failed", __FUNCTION__);
        return SIMPLE_ERR_SDK_START_FAILED;
    }

    hdrVividStatus = HdrVividRenderStart(m_hdrVividRender);
    if (hdrVividStatus != HDRVIVID_SUCCESS) {
        LOGE("%s hdr vivid render start failed", __FUNCTION__);
        return SIMPLE_ERR_SDK_START_FAILED;
    }
    return SIMPLE_SUCCESS;
}
