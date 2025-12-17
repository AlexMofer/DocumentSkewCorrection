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
import android.net.Uri;

import androidx.annotation.NonNull;

/**
 * 检测器
 * Created by Alex on 2025/12/16.
 */
public interface DocumentSkewCorrectionDetector {

    /**
     * 检测
     *
     * @param image   位图
     * @param maxSize 最大尺寸，小于等于0时不做缩小
     * @param points  用于承载检测结果的检测点
     * @return 检测成功时返回 true
     */
    boolean detect(@NonNull Bitmap image, int maxSize,
                   @NonNull DocumentSkewCorrectionPoints points);

    /**
     * 检测
     *
     * @param image  位图
     * @param points 用于承载检测结果的检测点
     * @return 检测成功时返回 true
     */
    default boolean detect(@NonNull Bitmap image, @NonNull DocumentSkewCorrectionPoints points) {
        return detect(image, 500, points);
    }

    /**
     * 检测
     *
     * @param context Context
     * @param uri     位图
     * @param maxSize 最大尺寸，小于等于0时不做缩小
     * @param points  用于承载检测结果的检测点
     * @return 检测成功时返回 true
     */
    default boolean detect(@NonNull Context context, @NonNull Uri uri,
                           int maxSize, @NonNull DocumentSkewCorrectionPoints points) {
        Bitmap image = null;
        try {
            image = BitmapUtils.fromUri(context, uri, false,
                    Bitmap.Config.RGB_565, false);
            return detect(image, maxSize, points);
        } catch (Throwable t) {
            return false;
        } finally {
            if (image != null) {
                image.recycle();
            }
        }
    }


    /**
     * 检测
     *
     * @param context Context
     * @param uri     位图
     * @param points  用于承载检测结果的检测点
     * @return 检测成功时返回 true
     */
    default boolean detect(@NonNull Context context, @NonNull Uri uri,
                           @NonNull DocumentSkewCorrectionPoints points) {
        return detect(context, uri, 500, points);
    }
}
