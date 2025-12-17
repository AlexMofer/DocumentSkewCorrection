#include <jni.h>
#include <android/bitmap.h>
#include "OpenCVUtils.hpp"

static jboolean DocumentSkewCorrection_OpenCVUtils_detect(JNIEnv *env, jclass /*clazz*/,
                                                          jobject src, jintArray points) {
    // 注意 ANDROID_BITMAP_FORMAT_RGBA_8888 格式的位图会强制作为未预乘的位图处理，传入带透明度的位图会检测不准确。
    AndroidBitmapInfo info;
    if (AndroidBitmap_getInfo(env, src, &info) != ANDROID_BITMAP_RESULT_SUCCESS) {
        // 无法获取位图信息
        return JNI_FALSE;
    }
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888
        && info.format != ANDROID_BITMAP_FORMAT_RGB_565) {
        // 不支持的格式
        return JNI_FALSE;
    }
    void *pixels = nullptr;
    if (AndroidBitmap_lockPixels(env, src, &pixels) != ANDROID_BITMAP_RESULT_SUCCESS) {
        // 无法锁定像素
        return JNI_FALSE;
    }
    int ltx, lty, rtx, rty, lbx, lby, rbx, rby;
    const bool result = DocumentSkewCorrection::OpenCVUtils::detect(
            (int) info.width, (int) info.height,
            pixels, info.format == ANDROID_BITMAP_FORMAT_RGBA_8888,
            ltx, lty, rtx, rty, lbx, lby, rbx, rby);
    AndroidBitmap_unlockPixels(env, src);
    if (result) {
        jint *ps = env->GetIntArrayElements(points, JNI_FALSE);
        ps[0] = ltx;
        ps[1] = lty;
        ps[2] = rtx;
        ps[3] = rty;
        ps[4] = lbx;
        ps[5] = lby;
        ps[6] = rbx;
        ps[7] = rby;
        env->ReleaseIntArrayElements(points, ps, 0);
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

static jboolean DocumentSkewCorrection_OpenCVUtils_detectGary(JNIEnv *env, jclass /*clazz*/,
                                                              jint width, jint height,
                                                              jbyteArray pixels,
                                                              jintArray points) {
    jbyte *data = env->GetByteArrayElements(pixels, JNI_FALSE);
    int ltx, lty, rtx, rty, lbx, lby, rbx, rby;
    const bool result = DocumentSkewCorrection::OpenCVUtils::detect(
            width, height, data, ltx, lty, rtx, rty, lbx, lby, rbx, rby);
    env->ReleaseByteArrayElements(pixels, data, 0);
    if (result) {
        jint *ps = env->GetIntArrayElements(points, JNI_FALSE);
        ps[0] = ltx;
        ps[1] = lty;
        ps[2] = rtx;
        ps[3] = rty;
        ps[4] = lbx;
        ps[5] = lby;
        ps[6] = rbx;
        ps[7] = rby;
        env->ReleaseIntArrayElements(points, ps, 0);
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

static jboolean DocumentSkewCorrection_OpenCVUtils_correct(JNIEnv *env, jclass /*clazz*/,
                                                           jobject src, jobject dst,
                                                           jfloat ltx, jfloat lty, jfloat rtx,
                                                           jfloat rty,
                                                           jfloat lbx, jfloat lby, jfloat rbx,
                                                           jfloat rby) {
    AndroidBitmapInfo src_info;
    if (AndroidBitmap_getInfo(env, src, &src_info) != ANDROID_BITMAP_RESULT_SUCCESS) {
        // 无法获取位图信息
        return JNI_FALSE;
    }
    if (src_info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        return JNI_FALSE;
    }
    void *src_pixels = nullptr;
    if (AndroidBitmap_lockPixels(env, src, &src_pixels) != ANDROID_BITMAP_RESULT_SUCCESS) {
        // 无法锁定像素
        return JNI_FALSE;
    }
    AndroidBitmapInfo dst_info;
    if (AndroidBitmap_getInfo(env, dst, &dst_info) != ANDROID_BITMAP_RESULT_SUCCESS) {
        // 无法获取位图信息
        AndroidBitmap_unlockPixels(env, src);
        return JNI_FALSE;
    }
    if (dst_info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        AndroidBitmap_unlockPixels(env, src);
        return JNI_FALSE;
    }
    void *dst_pixels = nullptr;
    if (AndroidBitmap_lockPixels(env, dst, &dst_pixels) != ANDROID_BITMAP_RESULT_SUCCESS) {
        // 无法锁定像素
        AndroidBitmap_unlockPixels(env, src);
        return JNI_FALSE;
    }
    DocumentSkewCorrection::OpenCVUtils::correct(
            (int) src_info.width, (int) src_info.height, src_pixels,
            (int) dst_info.width, (int) dst_info.height, dst_pixels,
            ltx, lty, rtx, rty, lbx, lby, rbx, rby);
    if (AndroidBitmap_unlockPixels(env, dst) != ANDROID_BITMAP_RESULT_SUCCESS) {
        // 解锁失败
        AndroidBitmap_unlockPixels(env, src);
        return JNI_FALSE;
    }
    if (AndroidBitmap_unlockPixels(env, src) != ANDROID_BITMAP_RESULT_SUCCESS) {
        // 解锁失败
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

jint DocumentSkewCorrection_OpenCVUtils_RegisterNatives(JNIEnv *env) {
    jclass clazz = env->FindClass(
            "io/github/alexmofer/documentskewcorrection/OpenCVUtils");
    if (nullptr == clazz) {
        return JNI_ERR;
    }
    JNINativeMethod methods[] = {
            {"DocumentSkewCorrection_OpenCVUtils_detect",     "(Landroid/graphics/Bitmap;[I)Z",                                (void *) (DocumentSkewCorrection_OpenCVUtils_detect)},
            {"DocumentSkewCorrection_OpenCVUtils_detectGary", "(II[B[I)Z",                                                     (void *) (DocumentSkewCorrection_OpenCVUtils_detectGary)},
            {"DocumentSkewCorrection_OpenCVUtils_correct",    "(Landroid/graphics/Bitmap;Landroid/graphics/Bitmap;FFFFFFFF)Z", (void *) (DocumentSkewCorrection_OpenCVUtils_correct)}
    };
    const jint result = env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0]));
    env->DeleteLocalRef(clazz);
    if (result != JNI_OK) {
        return result;
    }
    return JNI_OK;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void */*reversed*/) {
    JNIEnv *env = nullptr;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    auto register_result = DocumentSkewCorrection_OpenCVUtils_RegisterNatives(env);
    if (register_result != JNI_OK) {
        return register_result;
    }
    return JNI_VERSION_1_6;
}