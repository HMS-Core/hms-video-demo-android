/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 * Description: Use OpenGL to render the image returned by the SDK.
 * Create: 2022-05-16
 */

#ifndef VIDEOKITDEMO_SIMPLEPLAYER_H
#define VIDEOKITDEMO_SIMPLEPLAYER_H

#include <unistd.h>

#include <android/native_window.h>
#include "GlContext.h"
#include <mutex>

void *SimpleGLThread(void *argv);

constexpr int32_t SIMPLE_PLAYER_SLEEP_US = 5 * 1000;

class SimplePlayer {
public:
    SimplePlayer();

    ~SimplePlayer();

    void Init(int32_t width, int32_t height, ANativeWindow *window);

    void Draw() const;

    void OnBufferAvailable(uint8_t *data, uint32_t size);

    void InitGL();

    void Release();

    void ReleaseGL();

public:
    bool isBufferIdle{true};
    bool status{false};

private:
    GLuint CreateProgram();

    GLuint LoadShader(GLenum type, const char *source);

    static const int bytesPerFloat = 4;
    static const int vertexSize = 2;
    static const int vertexStride = vertexSize * bytesPerFloat;
    static const int coordsConunt = 4;
    static const uint8_t coordsOrder[];
    static const float vertexCoords[];
    static const float textureCoords[];

private:
    GlContext glContext;
    ANativeWindow *outputWindow;

    int32_t videoWidth;
    int32_t videoHeight;

    GLuint textureID;
    GLuint glProgram;

    GLint vertexCoordHandle;
    GLint textureCoordHandle;
    GLint rgbaTextureLocation;
    pthread_t thread;

    uint8_t *bufferData = nullptr;
    uint32_t bufferSize{0};

    std::mutex lock;
    std::condition_variable cond;
};


#endif
