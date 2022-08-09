/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 * Description: SEI common defines
 * Create: 2022-02-01
 */

#ifndef SEI_H
#define SEI_H

typedef enum {
    SEI_BUFFERING_PERIOD = 0,
    SEI_PIC_TIMING = 1,
    SEI_PAN_SCAN_RECT = 2,
    SEI_FILLER_PAYLOAD = 3,
    SEI_USER_DATA_REGISTERED_ITU_T_T35 = 4,
    SEI_USER_DATA_UNREGISTERED = 5,
    SEI_MASTERING_DISPLAY_COLOUR_VOLUME = 137, // mastering_display_colour_volume
    SEI_CONTENT_LIGHT_LEVEL_INFO = 144, // content_light_level_info
    SEI_MAX_ELEMENTS  // number of maximum syntax elements
} SEI_Type;

#define SEI_PAYLOAD_HEADER_STOP         0xFF

#endif // SEI_H
