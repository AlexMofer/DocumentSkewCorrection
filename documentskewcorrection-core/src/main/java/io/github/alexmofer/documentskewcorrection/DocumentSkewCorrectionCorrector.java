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
import androidx.annotation.Nullable;

/**
 * 检测器
 * Created by Alex on 2025/12/16.
 */
public interface DocumentSkewCorrectionCorrector {

    /**
     * 校正
     *
     * @param src    图片源
     * @param points 校正点
     * @return 校正后的位图，校正失败时返回空
     */
    @Nullable
    Bitmap correct(@NonNull Bitmap src, @NonNull DocumentSkewCorrectionPoints points);

    /**
     * 校正
     *
     * @param context Context
     * @param uri     图片源
     * @param points  校正点
     * @return 校正后的位图，校正失败时返回空
     */
    @Nullable
    default Bitmap correct(@NonNull Context context, @NonNull Uri uri,
                           @NonNull DocumentSkewCorrectionPoints points) {
        Bitmap image = null;
        try {
            image = BitmapUtils.fromUri(context, uri, false,
                    Bitmap.Config.ARGB_8888, false);
            return correct(image, points);
        } catch (Throwable t) {
            return null;
        } finally {
            if (image != null) {
                image.recycle();
            }
        }
    }
}
