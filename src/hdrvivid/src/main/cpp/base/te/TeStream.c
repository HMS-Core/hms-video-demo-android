/**
 * Copyright (c) Huawei Technologies Co., Ltd. 2011-2012. All rights reserved.
 * Description: Bitstream system for synchronization to the input
 * Create: 2011-04-01
 */

#include "TeStream.h"

static inline te_u32_t ByteSwap32(te_u32_t v)
{
    te_u32_t t = ~0x0000FF00;
    te_u32_t r;
    r = v ^ ((v >> 16) | (v << 16));
    r = t & (r >> 8);
    v = r ^ ((v >> 8) | (v << 24));
    return v;
}

/**
 * @defgroup    TeStream    Bitstream system for synchronization to the input
 * @ingroup     te
 * @brief   Bitstream initializaton
 *
 * @param   stream  bitstream system
 * @param   buf     input buffer address
 * @param   bitSize input buffer size in bits
 *
 * @return  0 if successful, otherwise -1
 */
int TE_InitStream(TeStream *stream, const te_u8_t *buf, int bitSize)
{
    te_u32_t cache;
    int cacheBitCnt;
    const te_u32_t *bufPtr = NULL;
    int i;

    if (bitSize <= 0) {
        return -1;
    }

    stream->bufStart = buf;
    stream->initBitSize = bitSize;

    // Put the unaligned part or aligned 4 bytes into cache and
    // don't read any dangerous data beyond the buffer boundary.
    cacheBitCnt = 32 - (((uintptr_t)buf & 3) << 3);
    cache = 0;
    for (i = cacheBitCnt; i != 0; i -= 8) {
        cache <<= 8;
        cache |= *buf++;
    }
    cache <<= (32 - cacheBitCnt);

    // bufPtr now points to a 4 bytes aligned address
    bufPtr = (const te_u32_t *)buf;

    stream->cache = cache;
    stream->cacheBitCnt = cacheBitCnt;
    stream->bufPtr = bufPtr;

    return 0;
}

/**
 * @brief   Read n bits from the bitstream
 *
 * @param   stream  bitstream system
 * @param   n       number of bits to get
 *
 * @return  n bits of data from bitstream
 *
 * @note    n is between [0, 31], n < 0 or n > 31 is generally a mistake. Though it
 *          can be protected by using a few instructions, but the requested n is either
 *          meaningless for n <= 0 or can't be achieved due to the range of return
 *          value for n > 32, and it can be lack of efficiency. The result of v << 32
 *          varies from compilers, so for n == 32, ReadBits32 should be used.
 *          This bitstream system is written for internel usage by this library.
 */
te_u32_t TE_ReadBits(TeStream *stream, int n)
{
    te_u32_t res;
    te_u32_t cache = stream->cache;
    int cacheBitCnt = stream->cacheBitCnt;

    // The result of v >> 32 varies from compilers.
    // Though we could use a ReadBits0 for the case when n is 0, we still need to check
    // the value of n outside this function.
    res = cache >> 1;
    res >>= (31 - n);
    cacheBitCnt -= n;

    if (cacheBitCnt >= 0) {
        cache <<= (te_u32_t)n;
    } else { // When cache is insufficient, read 32 bits buffer and shift the cache properly.
        const te_u32_t *bufPtr = stream->bufPtr;
        cacheBitCnt += 32;
        cache = ByteSwap32(*bufPtr);
        res |= cache >> cacheBitCnt;
        cache <<= (32 - cacheBitCnt);
        stream->bufPtr = ++bufPtr;
    }

    // Update stream.
    stream->cache = cache;
    stream->cacheBitCnt = cacheBitCnt;

    return res;
}

/**
 * @brief   Read 32 bits from the bitstream
 *
 * @param   stream  bitstream system
 *
 * @return  32 bits of data from bitstream
 */
te_u32_t TE_ReadBits32(TeStream *stream)
{
    te_u32_t res;
    te_u32_t cache = stream->cache;
    int cacheBitCnt = stream->cacheBitCnt;

    res = cache;
    cacheBitCnt -= 32;

    if (cacheBitCnt >= 0) {
        cache = 0;
    } else { // When cache is insufficient, read 32 bits buffer and shift the cache properly.
        const te_u32_t *bufPtr = stream->bufPtr;
        cacheBitCnt += 32;
        cache = ByteSwap32(*bufPtr);
        res |= cache >> (te_u32_t)cacheBitCnt;
        // n << 32 yields 0 in some compilers, but yields n in some others.
        if (cacheBitCnt == 0) {
            cache = 0;
        } else {
            cache <<= ((unsigned int)(32 - cacheBitCnt));
        }
        stream->bufPtr = ++bufPtr;
    }

    // Update stream.
    stream->cache = cache;
    stream->cacheBitCnt = cacheBitCnt;

    return res;
}

/**
 * @brief   Skip n bits from the bitstream
 *
 * @param   stream  bitstream system
 * @param   n       number of bits to skip
 *
 * @return  none
 *
 * @note    The same thing as ReadBits.
 */
void TE_SkipBits(TeStream *stream, int n)
{
    te_u32_t cache = stream->cache;
    int cacheBitCnt = stream->cacheBitCnt;

    cacheBitCnt -= n;

    if (cacheBitCnt >= 0) {
        cache <<= (te_u32_t)(n);
    } else { // When cache is insufficient, read 32 bits buffer and shift the cache properly.
        const te_u32_t *bufPtr = stream->bufPtr;
        cacheBitCnt += 32;
        cache = ByteSwap32(*bufPtr);
        cache <<= ((unsigned int)(32 - cacheBitCnt));
        stream->bufPtr = ++bufPtr;
    }

    // Update stream.
    stream->cache = cache;
    stream->cacheBitCnt = cacheBitCnt;
}

/**
 * @brief   Skip n bits from the bitstream
 *
 * @param   stream  bitstream system
 * @param   n       number of bits to skip
 *
 * @return  none
 */
void TE_SkipBitsLong(TeStream *stream, int n)
{
    const te_u32_t *bufPtr;
    te_u32_t cache;
    int cacheBitCnt;
    int bitsOffset;

    bufPtr = stream->bufPtr;
    cacheBitCnt = stream->cacheBitCnt;

    // Any value of bitsOffset is correct in the following process, inlucding the negative ones.
    bitsOffset = n - cacheBitCnt;

    bufPtr += (bitsOffset >> 5);
    cacheBitCnt = bitsOffset & 31;

    cache = *bufPtr++;
    cache = ByteSwap32(cache);
    cache <<= ((te_u32_t)(cacheBitCnt));
    cacheBitCnt = 32 - cacheBitCnt;

    stream->cache = cache;
    stream->cacheBitCnt = cacheBitCnt;
    stream->bufPtr = bufPtr;
}
