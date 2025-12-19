/*
 * Copyright (C) 2025 AlexMofer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.alexmofer.documentskewcorrection;

import android.graphics.Bitmap;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * 基于 OpenCV 实现的文档检测与斜切形变
 * Created by Alex on 2025/12/16.
 */
@Keep
final class OpenCVUtils {
    private static boolean ENABLE;

    static {
        try {
            System.loadLibrary("core");
            ENABLE = true;
        } catch (Throwable e) {
            ENABLE = false;
        }
    }

    private OpenCVUtils() {
        //no instance
    }

    /**
     * 检测
     *
     * @param image   位图
     * @param maxSize 最大尺寸，小于等于0时不做缩小
     * @param points  保存检测到的检测点
     * @return 检测到文档时返回 true
     */
    public static boolean detect(@NonNull Bitmap image, int maxSize,
                                 @NonNull DocumentSkewCorrectionPoints points) {
        if (!ENABLE) {
            return false;
        }
        if (Objects.isNull(image)) {
            return false;
        }
        if (image.isRecycled()) {
            return false;
        }
        if (image.getConfig() != Bitmap.Config.ARGB_8888
                && image.getConfig() != Bitmap.Config.RGB_565) {
            // 注意 ARGB_8888 格式的位图会强制作为未预乘的位图处理，传入带透明度的位图会检测不准确。
            // 如果确定位图不带透明度，那么预乘与不预乘是无区别的。
            // 因实际处理都会转为灰度图，如果位图带有透明度，请外部处理好是底部叠加黑色还是白色。
            // 此处不做预乘限制是因为，从相机获取的位图，虽然预乘，但其实没有透明度，可作为未预乘的位图处理。
            return false;
        }
        final int originalWidth = image.getWidth();
        final int originalHeight = image.getHeight();
        if (maxSize <= 0 || (originalWidth <= maxSize && originalHeight <= maxSize)) {
            // 不做缩放
            final int[] ps = new int[8];
            if (DocumentSkewCorrection_OpenCVUtils_detect(image, ps)) {
                points.set(originalWidth, originalHeight, ps);
                return true;
            }
        } else {
            // 缩小位图到限定尺寸
            final float scale = Math.min(1f * maxSize / originalWidth, 1f * maxSize / originalHeight);
            final Bitmap scaled;
            try {
                scaled = Bitmap.createScaledBitmap(image,
                        Math.round(scale * originalWidth),
                        Math.round(scale * originalHeight), true);
            } catch (Throwable t) {
                return false;
            }
            final int scaledWidth = scaled.getWidth();
            final int scaledHeight = scaled.getHeight();
            final int[] ps = new int[8];
            final boolean detected = DocumentSkewCorrection_OpenCVUtils_detect(scaled, ps);
            if (scaled != image) {
                scaled.recycle();
            }
            if (detected) {
                points.set(originalWidth, originalHeight, scaledWidth, scaledHeight, ps);
                return true;
            }
        }
        return false;
    }

    /**
     * 检测
     *
     * @param originalWidth  位图原始宽度
     * @param originalHeight 位图原始高度
     * @param width          位图宽度
     * @param height         位图高度
     * @param pixels         临界处理后的位图
     * @param points         保存检测到的检测点
     * @return 检测到文档时返回 true
     */
    public static boolean detect(int originalWidth, int originalHeight,
                                 int width, int height, byte[] pixels,
                                 @NonNull DocumentSkewCorrectionPoints points) {
        if (!ENABLE) {
            return false;
        }
        if (width <= 0 || height <= 0 || pixels == null || pixels.length == 0) {
            return false;
        }
        final int[] ps = new int[8];
        if (DocumentSkewCorrection_OpenCVUtils_detectThreshold(width, height, pixels, ps)) {
            points.set(originalWidth, originalHeight, width, height, ps);
            return true;
        }
        return false;
    }

    /**
     * 校正（此处不进行点的位置校验，请确保点不交叉）
     *
     * @param src 图片源
     * @param ltx 左上X
     * @param lty 左上Y
     * @param rtx 右上X
     * @param rty 右上Y
     * @param lbx 左下X
     * @param lby 左下Y
     * @param rbx 右下X
     * @param rby 右下Y
     * @return 校正后的位图，校正失败时返回空
     */
    @Nullable
    public static Bitmap correct(@NonNull Bitmap src,
                                 float ltx, float lty, float rtx, float rty,
                                 float lbx, float lby, float rbx, float rby) {
        if (!ENABLE) {
            return null;
        }
        if (Objects.isNull(src)) {
            return null;
        }
        if (src.isRecycled()) {
            return null;
        }
        if (src.getConfig() != Bitmap.Config.ARGB_8888) {
            // 请使用 ARGB_8888 格式
            return null;
        }
        final int width = (int) Math.round(
                (Utils.calculatePointToPoint(ltx, lty, rtx, rty)
                        + Utils.calculatePointToPoint(lbx, lby, rbx, rby)) * 0.5f);
        final int height = (int) Math.round(
                (Utils.calculatePointToPoint(ltx, lty, lbx, lby)
                        + Utils.calculatePointToPoint(rtx, rty, rbx, rby)) * 0.5f);
        if (width <= 0 || height <= 0) {
            return null;
        }
        final Bitmap dst;
        try {
            dst = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        } catch (Throwable t) {
            return null;
        }
        dst.setPremultiplied(false);
        if (DocumentSkewCorrection_OpenCVUtils_correct(src, dst,
                ltx, lty, rtx, rty, lbx, lby, rbx, rby)) {
            return dst;
        }
        dst.recycle();
        return null;
    }

    /**
     * 校正
     *
     * @param src    图片源
     * @param points 校正点
     * @return 校正后的位图，校正失败时返回空
     */
    @Nullable
    public static Bitmap correct(@NonNull Bitmap src,
                                 @NonNull DocumentSkewCorrectionPoints points) {
        final int width = src.getWidth();
        final int height = src.getHeight();
        return correct(src,
                points.getLTX(width), points.getLTY(height),
                points.getRTX(width), points.getRTY(height),
                points.getLBX(width), points.getLBY(height),
                points.getRBX(width), points.getRBY(height));
    }

    private static native boolean DocumentSkewCorrection_OpenCVUtils_detect(Object src, int[] points);

    private static native boolean DocumentSkewCorrection_OpenCVUtils_detectThreshold(int width, int height, byte[] pixels, int[] points);

    private static native boolean DocumentSkewCorrection_OpenCVUtils_correct(Object src, Object dst,
                                                                             float ltx, float lty, float rtx, float rty,
                                                                             float lbx, float lby, float rbx, float rby);
}
