/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 * Description: VideoKit HDR Vivid
 * Create: 2022-05-12
 */

#include "SimpleLog.h"
#include "GlContext.h"

const int GlContext::configAttrs[] = {
    EGL_RED_SIZE, 8,
    EGL_GREEN_SIZE, 8,
    EGL_BLUE_SIZE, 8,
    EGL_ALPHA_SIZE, 8,
    EGL_RENDERABLE_TYPE, EGL_OPENGL_ES3_BIT,
    EGL_NONE
};

const int GlContext::contextAttrs[] = {
    EGL_CONTEXT_CLIENT_VERSION, 3,
    EGL_NONE
};

GlContext::GlContext()
{
    eglDisplay = EGL_NO_DISPLAY;
    eglConfig = nullptr;
    eglContext = EGL_NO_CONTEXT;
}

GlContext::~GlContext()
{
    eglDisplay = EGL_NO_DISPLAY;
    eglConfig = nullptr;
    eglContext = EGL_NO_CONTEXT;
}

bool GlContext::Init(ANativeWindow *outputWindow)
{
    LOGI("->GlContext::Init");

    eglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (eglDisplay == EGL_NO_DISPLAY) {
        LOGE("eglGetDisplay failed");
        return false;
    }
    EGLint major;
    EGLint minor;
    if (!eglInitialize(eglDisplay, &major, &minor)) {
        eglDisplay = EGL_NO_DISPLAY;
        LOGE("eglInitialize failed");
        return false;
    }

    auto *configs = new EGLConfig[1];
    EGLint numConfigs = 0;
    if (!eglChooseConfig(eglDisplay, configAttrs, configs, 1, &numConfigs)) {
        LOGE("eglChooseConfig failed");
        return false;
    }
    eglConfig = configs[0];
    eglContext = eglCreateContext(eglDisplay, eglConfig, EGL_NO_CONTEXT, contextAttrs);
    if (eglContext == EGL_NO_CONTEXT) {
        LOGE("eglCreateContext failed");
        return false;
    }

    int attrs[] = {EGL_NONE};
    eglSurface = eglCreateWindowSurface(eglDisplay, eglConfig, outputWindow, attrs);
    if (eglSurface == EGL_NO_SURFACE || NULL == eglSurface) {
        LOGE("eglCreateWindowSurface failed");
        return false;
    }

    LOGI("<-GlContext::Init:eglContext=%p", eglContext);
    return true;
}

void GlContext::Release()
{
    LOGI("->GlContext::Release:eglContext=%p", eglContext);

    EGLBoolean success = eglDestroySurface(eglDisplay, eglSurface);
    if (!success) {
        LOGV("eglDestroySurface failure.");
    }

    if (eglDisplay != EGL_NO_DISPLAY) {
        eglMakeCurrent(eglDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
    }

    if (eglContext != EGL_NO_CONTEXT) {
        eglDestroyContext(eglDisplay, eglContext);
        eglContext = EGL_NO_CONTEXT;
    }

    if (eglDisplay != EGL_NO_DISPLAY) {
        eglReleaseThread();
        eglTerminate(eglDisplay);
        eglDisplay = EGL_NO_DISPLAY;
    }

    LOGI("<-GlContext::Release");
}
