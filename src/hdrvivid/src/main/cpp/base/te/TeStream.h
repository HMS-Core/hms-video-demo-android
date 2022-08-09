/**
 * Copyright (c) Huawei Technologies Co., Ltd. 2011-2013. All rights reserved.
 * Description: Bitstream system for synchronization to the input
 * Create: 2011-04-01
 */

#ifndef TE_STREAM_H
#define TE_STREAM_H

#include <stdint.h>

typedef unsigned char te_u8_t;
typedef unsigned int te_u32_t;

#define GET_BITS(X)                 ((X) << 3)
#define GET_BYTES(X)                ((X) >> 3)

#ifdef __cplusplus
extern "C" {
#endif

typedef struct TeStream TeStream;

/**
 * @brief   A single cached aligned bitstream system
 *
 * @note    The single cached system is 1.3 to 1.5 times as fast as a double cached system
 *          according to the testing result.
 */
struct TeStream {
    te_u32_t        cache;          // the single cache
    int             cacheBitCnt;    // remaining bit counts of cache
    const te_u32_t *bufPtr;         // the next buffer address for reading, 4 bytes aligned
    const te_u8_t  *bufStart;       // internal buffer start address, 1 byte aligned
    int             initBitSize;    // total bitsize after init
};

int TE_InitStream(TeStream *stream, const te_u8_t *buf, int bitSize);
te_u32_t TE_ReadBits(TeStream *stream, int n);
te_u32_t TE_ReadBits32(TeStream *stream);
void TE_SkipBits(TeStream *stream, int n);
void TE_SkipBitsLong(TeStream *stream, int n);


// The following are Getters for aligned bitstream.

/**
 * @brief   Get the consumed bytes in bitstream
 *
 * @param   stream  bitstream system
 *
 * @return  consumed bytes
 */
static inline int GetConsumedBytes(const TeStream *stream)
{
    return (int)((te_u8_t *)stream->bufPtr - stream->bufStart) - ((unsigned int)stream->cacheBitCnt >> 3);
}

/**
 * @brief   Get the remaining bit count, including the cache and the part after bufPtr
 *
 * @param   stream  bitstream system
 *
 * @return  remaining bit count
 */
static inline int GetBitCnt(const TeStream *stream)
{
    return stream->cacheBitCnt + stream->initBitSize - ((unsigned int)((te_u8_t *)stream->bufPtr - stream->bufStart) << 3);
}

/**
 * @brief   Get the start address of the remaining part, including the cache and the part after bufPtr
 *
 * @param   stream  bitstream system
 *
 * @return  start address of the remaining part
 */
static inline te_u8_t* GetRemainingStart(const TeStream *stream)
{
    return (te_u8_t *)stream->bufPtr - ((unsigned int)(stream->cacheBitCnt + 7) >> 3);
}

#ifdef __cplusplus
}
#endif

#endif /* TE_STREAM_H */
