/*
 * Copyright 2011, Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "common.h"

#include <string.h>
#include <android/bitmap.h>

#ifdef __cplusplus
extern "C" {
#endif  /* __cplusplus */

/***************
 * AdaptiveMap *
 ***************/

jint Java_com_googlecode_leptonica_android_AdaptiveMap_nativeBackgroundNormMorph(JNIEnv *env,
                                                                                 jclass clazz,
                                                                                 jint nativePix,
                                                                                 jint reduction,
                                                                                 jint size,
                                                                                 jint bgval) {
  // Normalizes the background of each element in pixa.

  PIX *pixs = (PIX *) nativePix;
  PIX *pixd = pixBackgroundNormMorph(pixs, NULL, (l_int32) reduction, (l_int32) size,
                                     (l_int32) bgval);

  return (jint) pixd;
}

/************
 * Binarize *
 ************/

jint Java_com_googlecode_leptonica_android_Binarize_nativeOtsuAdaptiveThreshold(JNIEnv *env,
                                                                                jclass clazz,
                                                                                jint nativePix,
                                                                                jint sizeX,
                                                                                jint sizeY,
                                                                                jint smoothX,
                                                                                jint smoothY,
                                                                                jfloat scoreFract) {

  PIX *pixs = (PIX *) nativePix;
  PIX *pixd;

  if (pixOtsuAdaptiveThreshold(pixs, (l_int32) sizeX, (l_int32) sizeY, (l_int32) smoothX,
                               (l_int32) smoothY, (l_float32) scoreFract, NULL, &pixd)) {
    return (jint) 0;
  }

  return (jint) pixd;
}

jint Java_com_googlecode_leptonica_android_Binarize_nativeSauvolaBinarizeTiled(JNIEnv *env,
                                                                                jclass clazz,
                                                                                jint nativePix,
                                                                                jint whsize,
                                                                                jfloat factor,
                                                                                jint nx,
                                                                                jint ny) {

  PIX *pixs = (PIX *) nativePix;
  PIX *pixd;

  if (pixSauvolaBinarizeTiled(pixs, (l_int32) whsize, (l_float32) factor, (l_int32) nx,
                               (l_int32) ny, NULL, &pixd)) {
    return (jint) 0;
  }

  return (jint) pixd;
}

/***********
 * Convert *
 ***********/

jint Java_com_googlecode_leptonica_android_Convert_nativeConvertTo8(JNIEnv *env, jclass clazz,
                                                                    jint nativePix) {
  PIX *pixs = (PIX *) nativePix;
  PIX *pixd = pixConvertTo8(pixs, FALSE);

  return (jint) pixd;
}

/***********
 * Enhance *
 ***********/

jint Java_com_googlecode_leptonica_android_Enhance_nativeUnsharpMasking(JNIEnv *env, jclass clazz,
                                                                        jint nativePix,
                                                                        jint halfwidth,
                                                                        jfloat fract) {
  PIX *pixs = (PIX *) nativePix;
  PIX *pixd = pixUnsharpMasking(pixs, (l_int32) halfwidth, (l_float32) fract);

  return (jint) pixd;
}

/**********
 * JpegIO *
 **********/

jbyteArray Java_com_googlecode_leptonica_android_JpegIO_nativeCompressToJpeg(JNIEnv *env,
                                                                             jclass clazz,
                                                                             jint nativePix,
                                                                             jint quality,
                                                                             jboolean progressive) {
  PIX *pix = (PIX *) nativePix;

  l_uint8 *data;
  size_t size;

  if (pixWriteMemJpeg(&data, &size, pix, (l_int32) quality, progressive == JNI_TRUE ? 1 : 0)) {
    LOGE("Failed to write JPEG data");

    return NULL;
  }

  // TODO Can we just use the byte array directly?
  jbyteArray array = env->NewByteArray(size);
  env->SetByteArrayRegion(array, 0, size, (jbyte *) data);

  free(data);

  return array;
}

/*********
 * Scale *
 *********/

jint Java_com_googlecode_leptonica_android_Scale_nativeScale(JNIEnv *env, jclass clazz,
                                                             jint nativePix, jfloat scaleX,
                                                             jfloat scaleY) {
  PIX *pixs = (PIX *) nativePix;
  PIX *pixd = pixScale(pixs, (l_float32) scaleX, (l_float32) scaleY);

  return (jint) pixd;
}

/********
 * Skew *
 ********/

jfloat Java_com_googlecode_leptonica_android_Skew_nativeFindSkew(JNIEnv *env, jclass clazz,
                                                                 jint nativePix, jfloat sweepRange,
                                                                 jfloat sweepDelta,
                                                                 jint sweepReduction,
                                                                 jint searchReduction,
                                                                 jfloat searchMinDelta) {
  // Corrects the rotation of each element in pixa to 0 degrees.

  PIX *pixs = (PIX *) nativePix;

  l_float32 angle, conf;

  if (!pixFindSkewSweepAndSearch(pixs, &angle, &conf, (l_int32) sweepReduction,
                                 (l_int32) searchReduction, (l_float32) sweepRange,
                                 (l_int32) sweepDelta, (l_float32) searchMinDelta)) {
    if (conf <= 0) {
      return (jfloat) 0;
    }

    return (jfloat) angle;
  }

  return (jfloat) 0;
}

/**********
 * Rotate *
 **********/

jint Java_com_googlecode_leptonica_android_Rotate_nativeRotate(JNIEnv *env, jclass clazz,
                                                               jint nativePix, jfloat degrees,
                                                               jboolean quality, jboolean resize) {
  PIX *pixd;
  PIX *pixs = (PIX *) nativePix;

  l_float32 deg2rad = 3.1415926535 / 180.0;
  l_float32 radians = degrees * deg2rad;
  l_int32 w, h, bpp, type;

  pixGetDimensions(pixs, &w, &h, &bpp);

  if (bpp == 1 && quality == JNI_TRUE) {
    pixd = pixRotateBinaryNice(pixs, radians, L_BRING_IN_WHITE);
  } else {
    type = quality == JNI_TRUE ? L_ROTATE_AREA_MAP : L_ROTATE_SAMPLING;
    w = (resize == JNI_TRUE) ? w : 0;
    h = (resize == JNI_TRUE) ? h : 0;
    pixd = pixRotate(pixs, radians, type, L_BRING_IN_WHITE, w, h);
  }

  return (jint) pixd;
}

#ifdef __cplusplus
}
#endif  /* __cplusplus */
