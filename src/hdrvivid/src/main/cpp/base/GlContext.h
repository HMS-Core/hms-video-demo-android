/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 * Description: VideoKit HDR Vivid
 * Create: 2022-05-12
 */

#ifndef HDRVIVID_GLCONTEXT_H
#define HDRVIVID_GLCONTEXT_H

#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <GLES3/gl3.h>
#include <GLES3/gl3ext.h>

class GlContext {
public:
    GlContext();

    ~GlContext();

    bool Init(ANativeWindow *outputWindow);

    void Release();

public:
    EGLDisplay eglDisplay = EGL_NO_DISPLAY;
    EGLConfig eglConfig = nullptr;
    EGLContext eglContext = EGL_NO_CONTEXT;
    EGLSurface eglSurface = nullptr;
private:
    static const int configAttrs[];
    static const int contextAttrs[];
};

#endif // HDRVIVID_GLCONTEXT_H
