# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

cmake_minimum_required (VERSION 3.10.2)

project(SimpleJni)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++17 -fPIC -Wall -W -Werror -Wno-unused-parameter -Wno-unused-function -fstack-protector-all -ffunction-sections -fdata-sections -fvisibility=hidden")

set(CMAKE_LD_FLAGS "${CMAKE_LD_FLAGS} -Wl,-z,relro,-z,now,-z,noexecstack -Wl,--gc-sections")

link_directories(${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})

add_library(SimpleJni SHARED
        SimpleJni.cpp
        SimpleProcessor.cpp
        SimpleExtractor.cpp
        SimpleCodec.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/base/te/TeStream.c
        ${CMAKE_CURRENT_SOURCE_DIR}/base/SimpleCommon.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/base/SimplePacket.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/base/SimpleLog.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/base/SimplePlayer.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/base/GlContext.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/utils/JniUtil.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/hdr/HdrMetaData.cpp)

target_include_directories(SimpleJni PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}/include
        ${CMAKE_CURRENT_SOURCE_DIR}/base
        ${CMAKE_CURRENT_SOURCE_DIR}/base/te
        ${CMAKE_CURRENT_SOURCE_DIR}/utils
        ${CMAKE_CURRENT_SOURCE_DIR}/hdr)

target_link_libraries(SimpleJni
        android
        log
        mediandk
        videokit_hdrvivid
        EGL
        GLESv3)
