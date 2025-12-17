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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 基于 OpenCV 实现的文档检测与斜切形变
 * Created by Alex on 2025/12/16.
 */
public class DocumentSkewCorrectionOpenCV implements DocumentSkewCorrectionDetector,
        DocumentSkewCorrectionCorrector {
    private static DocumentSkewCorrectionOpenCV sInstance;

    protected DocumentSkewCorrectionOpenCV() {
    }

    public static DocumentSkewCorrectionOpenCV getInstance() {
        if (sInstance == null) {
            sInstance = new DocumentSkewCorrectionOpenCV();
        }
        return sInstance;
    }

    @Override
    public boolean detect(@NonNull Bitmap image, int maxSize,
                          @NonNull DocumentSkewCorrectionPoints points) {
        return OpenCVUtils.detect(image, maxSize, points);
    }

    @Nullable
    @Override
    public Bitmap correct(@NonNull Bitmap src, @NonNull DocumentSkewCorrectionPoints points) {
        return OpenCVUtils.correct(src, points);
    }
}
