/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 * Description: decode H.265 ES by MediaCodec or libavcodec
 * Create: 2022-05-20
 */

#include <unistd.h>
#include "JniUtil.h"
#include "SimpleCommon.h"
#include "SimpleExtractor.h"
#include "SimpleLog.h"
#include "SimpleCodec.h"

void OnReceiveFrameCallback(JNIEnv *jniEnv, int64_t ptsUs);

SimpleCodec::SimpleCodec()
{
    m_dynamicMetaData.data = new uint8_t[maxDmSize];
}

SimpleCodec::~SimpleCodec()
{
    delete m_dynamicMetaData.data;
}

void SimpleCodec::SetHdrVividRender(HdrVividRender *hdrVividRender)
{
    m_hdrVividRender = hdrVividRender;
}

void SimpleCodec::StartMc(std::string filePath, std::string decoderName, ANativeWindow *surface, int32_t width,
                          int32_t height)
{
    std::unique_lock<std::mutex> lck(m_stdMutex);
    if (m_threadRun) {
        LOGV("%s thread already run", __FUNCTION__);
        return;
    }

    m_filePath = std::move(filePath);
    m_decoderName = std::move(decoderName);
    m_surface = surface;
    m_width = width;
    m_height = height;

    m_threadRun = true;
    m_stdThread = std::thread(&SimpleCodec::ThreadMc, this);
}

void SimpleCodec::StopMc()
{
    LOGI("%s begin", __FUNCTION__);

    std::unique_lock<std::mutex> lck(m_stdMutex);
    if (!m_threadRun) {
        LOGI("%s thread already exit", __FUNCTION__);
        return;
    }
    m_threadRun = false;

    if (m_stdThread.joinable()) {
        LOGI("%s notify and will join", __FUNCTION__);
        m_stdThread.join();
    }

    LOGI("%s end", __FUNCTION__);
}

void SimpleCodec::Pause()
{
    m_isPlaying = false;
}

void SimpleCodec::Play()
{
    m_isPlaying = true;
}

void SimpleCodec::SetStaticMetaData()
{
    if (m_staticMetaData.gDisplayPrimariesX == m_lastStaticMetaData.gDisplayPrimariesX &&
        m_staticMetaData.gDisplayPrimariesY == m_lastStaticMetaData.gDisplayPrimariesY &&
        m_staticMetaData.bDisplayPrimariesX == m_lastStaticMetaData.bDisplayPrimariesX &&
        m_staticMetaData.bDisplayPrimariesY == m_lastStaticMetaData.bDisplayPrimariesY &&
        m_staticMetaData.rDisplayPrimariesX == m_lastStaticMetaData.rDisplayPrimariesX &&
        m_staticMetaData.rDisplayPrimariesY == m_lastStaticMetaData.rDisplayPrimariesY &&
        m_staticMetaData.whitePointX == m_lastStaticMetaData.whitePointX &&
        m_staticMetaData.whitePointY == m_lastStaticMetaData.whitePointY &&
        m_staticMetaData.maxDisplayMasteringLum == m_lastStaticMetaData.maxDisplayMasteringLum &&
        m_staticMetaData.minDisplayMasteringLum == m_lastStaticMetaData.minDisplayMasteringLum &&
        m_staticMetaData.maxContentLightLevel == m_lastStaticMetaData.maxContentLightLevel &&
        m_staticMetaData.maxPicAverageLightLevel == m_lastStaticMetaData.maxPicAverageLightLevel) {
        return;
    }

    m_lastStaticMetaData = m_staticMetaData;
    HdrVividRenderSetStaticMetaData(m_hdrVividRender, &m_lastStaticMetaData);
}

void SimpleCodec::SetDynamicMetaData(int64_t ptsUs)
{
    HdrVividBuffer *hdrVividBuffer = GetDynamicMetaData(ptsUs);
    if (hdrVividBuffer == nullptr) {
        LOGW("%s, failed to get dm, ptsUs=%" PRId64, __FUNCTION__, ptsUs);
        return;
    }
    HdrVividRenderSetDynamicMetaData(m_hdrVividRender, ptsUs, hdrVividBuffer);
}

void SimpleCodec::setJavaVm(JavaVM *javaVm)
{
    m_javaVm = javaVm;
}

void SimpleCodec::ThreadMc()
{
    LOGI("%s begin", __FUNCTION__);

    AMediaCodec *mc = InitMc();
    if (mc == nullptr) {
        LOGI("%s init mediacodec failed", __FUNCTION__);
        return;
    }

    auto *se = new SimpleExtractor();
    if (!se->Open(m_filePath.c_str())) {
        LOGW("%s open file failed, filePath=%s", __FUNCTION__, m_filePath.c_str());
        delete se;
        AMediaCodec_delete(mc);
        return;
    }

    bool isEOS = false;
    int64_t firstPtsUs = -1;
    int64_t startMs = SimpleTimeGetBootTimeWithDeepSleepMs();

    while (m_threadRun) {
        if (!m_isPlaying) {
            usleep(threadSleepUs);
            firstPtsUs = -1;
            startMs = SimpleTimeGetBootTimeWithDeepSleepMs();
            continue;
        }

        if (!isEOS) {
            ssize_t idx = AMediaCodec_dequeueInputBuffer(mc, codecInTimeoutUs);
            if (idx >= 0) {
                SimplePacket *simplePacket = se->GetNextSample();
                if (simplePacket == nullptr) {
                    isEOS = true;
                    AMediaCodec_queueInputBuffer(mc, idx, 0, 0, 0, AMEDIACODEC_BUFFER_FLAG_END_OF_STREAM);
                } else {
                    size_t outSize = -1;
                    uint8_t *inBuffer = AMediaCodec_getInputBuffer(mc, idx, &outSize);
                    uint8_t *srcData = simplePacket->GetData();
                    int32_t srcSize = simplePacket->GetSize();
                    for (int32_t i = 0; i < srcSize; ++i) {
                        inBuffer[i] = srcData[i];
                    }
                    AMediaCodec_queueInputBuffer(mc, idx, 0, simplePacket->GetSize(), simplePacket->GetPtsUs(), 0);
                    RefreshStaticMetaData(simplePacket);
                    InsertSimplePacket(simplePacket);
                }
            }
        }

        AMediaCodecBufferInfo bi;
        ssize_t idx = AMediaCodec_dequeueOutputBuffer(mc, &bi, codecOutTimeoutUs);
        switch (idx) {
            case AMEDIACODEC_INFO_OUTPUT_BUFFERS_CHANGED:
                LOGI("%s AMEDIACODEC_INFO_OUTPUT_BUFFERS_CHANGED", __FUNCTION__);
                break;
            case AMEDIACODEC_INFO_OUTPUT_FORMAT_CHANGED:
                LOGI("%s AMEDIACODEC_INFO_OUTPUT_FORMAT_CHANGED", __FUNCTION__);
                {
                    AMediaFormat *mf = AMediaCodec_getOutputFormat(mc);
                    if (mf) {
                        int32_t width = -1;
                        int32_t height = -1;
                        AMediaFormat_getInt32(mf, AMEDIAFORMAT_KEY_WIDTH, &width);
                        AMediaFormat_getInt32(mf, AMEDIAFORMAT_KEY_HEIGHT, &height);
                        LOGI("%s format changed, WxH=%dx%d", __FUNCTION__, width, height);
                    }
                }
                break;
            case AMEDIACODEC_INFO_TRY_AGAIN_LATER:
                LOGI("%s AMEDIACODEC_INFO_TRY_AGAIN_LATER", __FUNCTION__);
                break;
            default:
                if (firstPtsUs == -1) {
                    firstPtsUs = bi.presentationTimeUs;
                }

                while ((bi.presentationTimeUs - firstPtsUs) / MS_2_US >
                       SimpleTimeGetBootTimeWithDeepSleepMs() - startMs) {
                    usleep(renderSleepUs);
                }

                bool render = true;
                if (bi.presentationTimeUs < 0) {
                    render = false;
                }
                AMediaCodec_releaseOutputBuffer(mc, idx, render);
                break;
        }

        if (bi.flags == AMEDIACODEC_BUFFER_FLAG_END_OF_STREAM) {
            LOGI("%s AMEDIACODEC_BUFFER_FLAG_END_OF_STREAM", __FUNCTION__);
            break;
        }
    }

    se->Close();
    delete se;
    AMediaCodec_stop(mc);
    AMediaCodec_delete(mc);

    LOGI("%s end", __FUNCTION__);
}

AMediaCodec *SimpleCodec::InitMc()
{
    AMediaCodec *mc = AMediaCodec_createCodecByName(m_decoderName.c_str());
    if (mc == nullptr) {
        LOGW("%s create mediacodec failed", __FUNCTION__);
        return nullptr;
    }

    media_status_t status = AMEDIA_OK;

    AMediaFormat *mf = AMediaFormat_new();
    AMediaFormat_setString(mf, AMEDIAFORMAT_KEY_MIME, mimeHevc);
    AMediaFormat_setInt32(mf, AMEDIAFORMAT_KEY_WIDTH, m_width);
    AMediaFormat_setInt32(mf, AMEDIAFORMAT_KEY_HEIGHT, m_height);

    status = AMediaCodec_configure(mc, mf, m_surface, nullptr, 0);
    if (status != AMEDIA_OK) {
        LOGW("%s config mediacodec failed", __FUNCTION__);
        AMediaFormat_delete(mf);
        AMediaCodec_delete(mc);
        return nullptr;
    }
    AMediaFormat_delete(mf);

    status = AMediaCodec_start(mc);
    if (status != AMEDIA_OK) {
        LOGW("%s start mediacodec failed", __FUNCTION__);
        AMediaCodec_delete(mc);
        return nullptr;
    }

    return mc;
}

void SimpleCodec::RefreshStaticMetaData(SimplePacket *simplePacket)
{
    m_staticMetaData.gDisplayPrimariesX = simplePacket->GetHdrMetaData()->GetGx();
    m_staticMetaData.gDisplayPrimariesY = simplePacket->GetHdrMetaData()->GetGy();
    m_staticMetaData.bDisplayPrimariesX = simplePacket->GetHdrMetaData()->GetBx();
    m_staticMetaData.bDisplayPrimariesY = simplePacket->GetHdrMetaData()->GetBy();
    m_staticMetaData.rDisplayPrimariesX = simplePacket->GetHdrMetaData()->GetRx();
    m_staticMetaData.rDisplayPrimariesY = simplePacket->GetHdrMetaData()->GetRy();
    m_staticMetaData.whitePointX = simplePacket->GetHdrMetaData()->GetWhitePointX();
    m_staticMetaData.whitePointY = simplePacket->GetHdrMetaData()->GetWhitePointY();
    m_staticMetaData.maxDisplayMasteringLum = simplePacket->GetHdrMetaData()->GetMaxDisplayMasteringLum();
    m_staticMetaData.minDisplayMasteringLum = simplePacket->GetHdrMetaData()->GetMinDisplayMasteringLum();
    m_staticMetaData.maxContentLightLevel = simplePacket->GetHdrMetaData()->GetMaxContentLightLevel();
    m_staticMetaData.maxPicAverageLightLevel = simplePacket->GetHdrMetaData()->GetMaxPicAverageLightLevel();
}

void SimpleCodec::InsertSimplePacket(SimplePacket *srcPacket)
{
    std::lock_guard<std::mutex> lk(m_metaDataMutex);
    SimplePacket *copyPacket = CloneSimplePacket(srcPacket);
    m_simplePacketMap.insert(std::make_pair(copyPacket->GetPtsUs(), copyPacket));
}

SimplePacket *SimpleCodec::CloneSimplePacket(SimplePacket *srcPacket)
{
    SimplePacket *copyPacket = new SimplePacket();
    copyPacket->SetPtsUs(srcPacket->GetPtsUs());

    copyPacket->GetHdrMetaData()->SetGx(srcPacket->GetHdrMetaData()->GetGx());
    copyPacket->GetHdrMetaData()->SetGy(srcPacket->GetHdrMetaData()->GetGx());
    copyPacket->GetHdrMetaData()->SetBx(srcPacket->GetHdrMetaData()->GetBx());
    copyPacket->GetHdrMetaData()->SetBy(srcPacket->GetHdrMetaData()->GetBy());
    copyPacket->GetHdrMetaData()->SetRx(srcPacket->GetHdrMetaData()->GetRx());
    copyPacket->GetHdrMetaData()->SetRy(srcPacket->GetHdrMetaData()->GetRy());
    copyPacket->GetHdrMetaData()->SetWhitePointX(srcPacket->GetHdrMetaData()->GetWhitePointX());
    copyPacket->GetHdrMetaData()->SetWhitePointY(srcPacket->GetHdrMetaData()->GetWhitePointY());
    copyPacket->GetHdrMetaData()->SetMaxDisplayMasteringLum(srcPacket->GetHdrMetaData()->GetMaxDisplayMasteringLum());
    copyPacket->GetHdrMetaData()->SetMinDisplayMasteringLum(srcPacket->GetHdrMetaData()->GetMinDisplayMasteringLum());
    copyPacket->GetHdrMetaData()->SetMaxContentLightLevel(srcPacket->GetHdrMetaData()->GetMaxContentLightLevel());
    copyPacket->GetHdrMetaData()->SetMaxPicAverageLightLevel(srcPacket->GetHdrMetaData()->GetMaxPicAverageLightLevel());

    if (srcPacket->GetHdrMetaData()->GetDmSize() > 0) {
        copyPacket->GetHdrMetaData()->SetDm(srcPacket->GetHdrMetaData()->GetDmData(), srcPacket->GetHdrMetaData()->GetDmSize());
    }

    return copyPacket;
}

HdrVividBuffer *SimpleCodec::GetDynamicMetaData(int64_t ptsUs)
{
    std::lock_guard<std::mutex> lk(m_metaDataMutex);
    auto iter = m_simplePacketMap.find(ptsUs);
    if (iter == m_simplePacketMap.end()) {
        return nullptr;
    }
    SimplePacket *simplePacket = iter->second;
    const uint8_t *srcData = simplePacket->GetHdrMetaData()->GetDmData();
    int32_t srcSize = simplePacket->GetHdrMetaData()->GetDmSize();
    for (int32_t i = 0; i < srcSize; ++i) {
        m_dynamicMetaData.data[i] = srcData[i];
    }
    m_dynamicMetaData.size = simplePacket->GetHdrMetaData()->GetDmSize();
    m_simplePacketMap.erase(iter);
    delete simplePacket;
    return &m_dynamicMetaData;
}
