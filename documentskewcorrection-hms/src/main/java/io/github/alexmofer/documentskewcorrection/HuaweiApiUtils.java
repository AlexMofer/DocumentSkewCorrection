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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.huawei.hms.api.HuaweiApiAvailability;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionAnalyzer;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionAnalyzerFactory;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionAnalyzerSetting;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionConstant;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionCoordinateInput;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionResult;
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewDetectResult;

import java.util.ArrayList;

/**
 * 工具类
 * Created by Alex on 2025/12/16.
 */
final class HuaweiApiUtils {

    /**
     * 判断 HMS Core 是否可用
     *
     * @return HMS Core 可用时返回true
     */
    public static boolean isEnable(Context context) {
        return HuaweiApiAvailability.getInstance().isHuaweiMobileServicesAvailable(context) == 0;
    }

    @Nullable
    private static MLDocumentSkewDetectResult detect(@NonNull Bitmap image) {
        try {
            final MLFrame frame = MLFrame.fromBitmap(image);
            final MLDocumentSkewCorrectionAnalyzer analyzer =
                    MLDocumentSkewCorrectionAnalyzerFactory.getInstance()
                            .getDocumentSkewCorrectionAnalyzer(
                                    new MLDocumentSkewCorrectionAnalyzerSetting.Factory().create());
            final SparseArray<MLDocumentSkewDetectResult> result = analyzer.analyseFrame(frame);
            if (result == null || result.size() <= 0) {
                // 失败。
                analyzer.stop();
                return null;
            }
            final MLDocumentSkewDetectResult detected = result.get(0);
            if (detected == null ||
                    detected.getResultCode() != MLDocumentSkewCorrectionConstant.SUCCESS) {
                // 失败。
                analyzer.stop();
                return null;
            }
            analyzer.stop();
            return detected;
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * 检测
     *
     * @param image   位图
     * @param maxSize 最大尺寸
     * @return 检测到的文档边框，返回空表示未检测到文档边框
     */
    public static boolean detect(@NonNull Bitmap image, float maxSize,
                                 @NonNull DocumentSkewCorrectionPoints points) {
        maxSize = Math.min(1920, maxSize);
        final int originalWidth = image.getWidth();
        final int originalHeight = image.getHeight();
        if (originalWidth > maxSize || originalHeight > maxSize) {
            // 图片大小超限，缩小到限定尺寸
            final float scale = Math.min(maxSize / originalWidth, maxSize / originalHeight);
            final Bitmap scaled;
            try {
                scaled = Bitmap.createScaledBitmap(image,
                        Math.round(scale * originalWidth),
                        Math.round(scale * originalHeight), true);
            } catch (Throwable t) {
                return false;
            }
            if (scaled == image) {
                // 没有进行缩放
                return false;
            }
            final int scaledWidth = scaled.getWidth();
            final int scaledHeight = scaled.getHeight();
            final MLDocumentSkewDetectResult result = detect(scaled);
            scaled.recycle();
            if (result == null) {
                return false;
            }
            final Point lt = result.getLeftTopPosition();
            final Point rt = result.getRightTopPosition();
            final Point lb = result.getLeftBottomPosition();
            final Point rb = result.getRightBottomPosition();
            points.set(originalWidth, originalHeight, scaledWidth, scaledHeight, lt, rt, lb, rb);
            return true;
        } else {
            final MLDocumentSkewDetectResult result = detect(image);
            if (result == null) {
                return false;
            }
            final Point lt = result.getLeftTopPosition();
            final Point rt = result.getRightTopPosition();
            final Point lb = result.getLeftBottomPosition();
            final Point rb = result.getRightBottomPosition();
            points.set(originalWidth, originalHeight, lt, rt, lb, rb);
            return true;
        }
    }

    /**
     * 校正文档
     *
     * @param src  位图
     * @param points 校正点
     * @return 校正后的位图
     */
    @Nullable
    public static Bitmap correct(@NonNull Bitmap src,
                                 @NonNull DocumentSkewCorrectionPoints points) {
        try {
            final int width = src.getWidth();
            final int height = src.getHeight();
            final MLFrame frame = MLFrame.fromBitmap(src);
            final ArrayList<Point> coordinates = new ArrayList<>();
            coordinates.add(new Point(
                    Math.max(0, Math.min(width, Math.round(points.getLTX(width)))),
                    Math.max(0, Math.min(height, Math.round(points.getLTY(height))))));
            coordinates.add(new Point(
                    Math.max(0, Math.min(width, Math.round(points.getRTX(width)))),
                    Math.max(0, Math.min(height, Math.round(points.getRTY(height))))));
            coordinates.add(new Point(
                    Math.max(0, Math.min(width, Math.round(points.getRBX(width)))),
                    Math.max(0, Math.min(height, Math.round(points.getRBY(height))))));
            coordinates.add(new Point(
                    Math.max(0, Math.min(width, Math.round(points.getLBX(width)))),
                    Math.max(0, Math.min(height, Math.round(points.getLBY(height))))));
            final MLDocumentSkewCorrectionAnalyzer analyzer =
                    MLDocumentSkewCorrectionAnalyzerFactory.getInstance()
                            .getDocumentSkewCorrectionAnalyzer(
                                    new MLDocumentSkewCorrectionAnalyzerSetting.Factory().create());
            final SparseArray<MLDocumentSkewCorrectionResult> result =
                    analyzer.syncDocumentSkewCorrect(frame,
                            new MLDocumentSkewCorrectionCoordinateInput(coordinates));
            if (result == null || result.size() <= 0) {
                // 失败。
                analyzer.stop();
                return null;
            }
            final MLDocumentSkewCorrectionResult corrected = result.get(0);
            if (corrected == null ||
                    corrected.getResultCode() != MLDocumentSkewCorrectionConstant.SUCCESS) {
                // 失败。
                analyzer.stop();
                return null;
            }
            analyzer.stop();
            return corrected.getCorrected();
        } catch (Throwable t) {
            return null;
        }
    }
}
