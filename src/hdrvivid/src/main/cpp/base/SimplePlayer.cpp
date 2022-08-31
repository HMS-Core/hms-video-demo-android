/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 * Description: Use OpenGL to render the image returned by the SDK.
 * Create: 2022-05-16
 */

#include "SimplePlayer.h"
#include "SimpleLog.h"

const uint8_t SimplePlayer::coordsOrder[] = {
    0, 1, 2, 3
};

const float SimplePlayer::vertexCoords[] = {
    -1.0f, +1.0f,
    -1.0f, -1.0f,
    +1.0f, -1.0f,
    +1.0f, +1.0f,
};

const float SimplePlayer::textureCoords[] = {
    0.0f, 0.0f,
    0.0f, 1.0f,
    1.0f, 1.0f,
    1.0f, 0.0f,

    0.0f, 1.0f,
    0.0f, 0.0f,
    1.0f, 0.0f,
    1.0f, 1.0f,
};

void *SimpleGLThread(void *argv)
{
    LOGI("%s enter ", __FUNCTION__);
    SimplePlayer *simplePlayer = static_cast<SimplePlayer *>(argv);
    simplePlayer->InitGL();

    while (simplePlayer->status) {
        if (!simplePlayer->isBufferIdle) {
            simplePlayer->Draw();
            simplePlayer->isBufferIdle = true;
        } else {
            usleep(SIMPLE_PLAYER_SLEEP_US);
        }
    }
    simplePlayer->ReleaseGL();
    LOGI("%s end", __FUNCTION__);

    return nullptr;
}


SimplePlayer::SimplePlayer()
{
}

SimplePlayer::~SimplePlayer()
{
}

void SimplePlayer::Init(int32_t width, int32_t height, ANativeWindow *window)
{
    videoWidth = width;
    videoHeight = height;
    outputWindow = window;
    status = true;
    pthread_create(&thread, nullptr, SimpleGLThread, this);
}

void SimplePlayer::Release()
{
    status = false;
}

void SimplePlayer::ReleaseGL()
{
    glContext.Release();
}

void SimplePlayer::InitGL()
{
    glContext.Init(outputWindow);

    eglMakeCurrent(glContext.eglDisplay, glContext.eglSurface, glContext.eglSurface, glContext.eglContext);

    glProgram = CreateProgram();

    vertexCoordHandle = glGetAttribLocation(glProgram, "position");
    textureCoordHandle = glGetAttribLocation(glProgram, "textureCoord");
    rgbaTextureLocation = glGetUniformLocation(glProgram, "rgbaTexture");

    glGenTextures(1, &textureID);
    glBindTexture(GL_TEXTURE_2D, textureID);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glEnable(GL_TEXTURE_2D);
}

void SimplePlayer::OnBufferAvailable(uint8_t *data, uint32_t size)
{
    if (bufferData == nullptr) {
        if (size <= 0 || size > SIMPLE_PLAYER_SIZE_MAX) {
            return;
        }
        bufferData = (uint8_t *) malloc(size);
        bufferSize = size;
    }

    if (bufferSize != size) {
        return;
    }

    // A more reliable buffer mechanism, double buffering or triple buffering, can be used to avoid frame loss.
    if (!isBufferIdle) {
        LOGI("%s drop frame.", __FUNCTION__);
        return;
    }

    for (uint32_t i = 0; i < size; ++i) {
        bufferData[i] = data[i];
    }
    isBufferIdle = false;
}

GLuint SimplePlayer::CreateProgram()
{
    const char *vertexSource =
        "attribute vec4 position;\n"
        "attribute vec2 textureCoord;\n"
        "varying vec2 vTextureCoord;\n"

        "void main() {\n"
        "    gl_Position = position;\n"
        "    vTextureCoord = textureCoord;\n"
        "}";

    const char *fragmentSource =
        "precision mediump float;\n"
        "uniform sampler2D rgbaTexture;\n"
        "varying vec2 vTextureCoord;\n"
        "void main()\n"
        "{\n"
        "    gl_FragColor = texture2D(rgbaTexture, vTextureCoord);\n"
        "}\n";

    GLuint vertexShader = LoadShader(GL_VERTEX_SHADER, vertexSource);
    if (vertexShader == GL_NONE) {
        LOGE("LoadShader(GL_VERTEX_SHADER) failed");
        return GL_NONE;
    }
    GLuint fragmentShader = LoadShader(GL_FRAGMENT_SHADER, fragmentSource);
    if (fragmentShader == GL_NONE) {
        LOGE("LoadShader(GL_FRAGMENT_SHADER) failed");
        return GL_NONE;
    }

    GLuint program = glCreateProgram();
    if (program == GL_NONE) {
        return GL_NONE;
    }
    glAttachShader(program, vertexShader);
    glAttachShader(program, fragmentShader);
    glLinkProgram(program);

    glDeleteShader(vertexShader);
    glDeleteShader(fragmentShader);

    GLint linkStatus = GL_FALSE;
    glGetProgramiv(program, GL_LINK_STATUS, &linkStatus);
    if (linkStatus == GL_FALSE) {
        LOGE("glGetProgramiv()=0x%X", eglGetError());
        glDeleteProgram(program);
        return GL_NONE;
    }

    return program;
}

void SimplePlayer::Draw() const
{
    LOGI("%s enter", __FUNCTION__);

    if (outputWindow == nullptr) {
        LOGI("%s outputWindow is nullptr", __FUNCTION__);
        return;
    }

    int32_t width = ANativeWindow_getWidth(outputWindow);
    int32_t height = ANativeWindow_getHeight(outputWindow);

    // Set the viewport
    glViewport(0, 0, width, height);
    // Clear the color buffer
    glClear(GL_COLOR_BUFFER_BIT);

    // Use the program object
    glUseProgram(glProgram);

    glEnableVertexAttribArray(vertexCoordHandle);
    glVertexAttribPointer(vertexCoordHandle, vertexSize, GL_FLOAT, false, vertexStride, vertexCoords);
    glEnableVertexAttribArray(textureCoordHandle);
    glVertexAttribPointer(textureCoordHandle, vertexSize, GL_FLOAT, false, vertexStride, textureCoords);

    // draw
    glActiveTexture(textureID);
    glBindTexture(GL_TEXTURE_2D, textureID);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, videoWidth, videoHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, bufferData);
    glUniform1i(rgbaTextureLocation, 0);
    glDrawElements(GL_TRIANGLE_FAN, coordsConunt, GL_UNSIGNED_BYTE, coordsOrder);

    glDisableVertexAttribArray(vertexCoordHandle);
    glDisableVertexAttribArray(textureCoordHandle);
    glUseProgram(0);

    eglSwapBuffers(glContext.eglDisplay, glContext.eglSurface);

    LOGI("%s end width %d, height %d", __FUNCTION__, width, height);
}

GLuint SimplePlayer::LoadShader(GLenum type, const char *source)
{
    GLuint shader = glCreateShader(type);
    if (shader == GL_NONE) {
        LOGE("glCreateShader(%d)=0x%X", type, eglGetError());
        return GL_NONE;
    }
    glShaderSource(shader, 1, &source, nullptr);
    glCompileShader(shader);

    GLint compiled = GL_FALSE;
    glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
    if (compiled == GL_FALSE) {
        static const int bufLen = 256;
        GLchar infoLog[bufLen] = {0};
        GLsizei length = bufLen;
        glGetShaderInfoLog(shader, bufLen, &length, infoLog);
        LOGE("ShaderError: %s", infoLog);
        LOGE("glGetShaderiv(0x%04X)=0x%04X", type, eglGetError());
        glDeleteShader(shader);
        shader = GL_NONE;
    }
    return shader;
}
