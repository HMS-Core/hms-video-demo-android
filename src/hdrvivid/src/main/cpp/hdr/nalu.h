/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 * Description: nalu common defines
 * Create: 2022-02-01
 */

#ifndef NALU_H
#define NALU_H

#define NALU_SIZE                   4

#define NALU_HEADER_AVC             1

#define NALU_HEADER_HEVC            2       // | F(1) | Type(6) | LayerId(6) | TID(3) |
#define NALU_HEADER_HEVC_F          1
#define NALU_HEADER_HEVC_TYPE       6
#define NALU_HEADER_HEVC_LAYER      6
#define NALU_HEADER_HEVC_TID        3

#define NALU_TYPE_HEVC_VPS          32
#define NALU_TYPE_HEVC_SPS          33
#define NALU_TYPE_HEVC_PPS          34
#define NALU_TYPE_HEVC_PRE_SEI      39
#define NALU_TYPE_HEVC_SUF_SEI      40

#define NALU_UNIT_HEADER_SIZE       2
#define NALU_UNIT_TYPE_MASK         0x7F
#define NALU_STARTCODE_SIZE_3       3
#define NALU_STARTCODE_SIZE_4       4
#define NALU_STARTCODE_FIRST        0
#define NALU_STARTCODE_SECOND       1
#define NALU_STARTCODE_THIRD        2
#define NALU_STARTCODE_FOURTH       3

#endif // NALU_H
