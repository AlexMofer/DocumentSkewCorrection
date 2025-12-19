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
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import org.tensorflow.lite.Interpreter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Objects;

/**
 * 文档校正
 * 使用 Tensorflow 替代图像二值化，计算边框及斜切依然为 OpenCV
 * Created by Alex on 2025/5/26.
 */
public final class DocumentSkewCorrectionTensorflow extends DocumentSkewCorrectionOpenCV {
    private static DocumentSkewCorrectionTensorflow sDefault;
    private final Interpreter mInterpreter;
    private final int mWidth;
    private final int mHeight;

    public DocumentSkewCorrectionTensorflow(Interpreter interpreter, int width, int height) {
        mInterpreter = interpreter;
        mWidth = width;
        mHeight = height;
    }

    /**
     * 获取默认模型
     * 该模型强制要求输入位图为256*256的位图，其他尺寸均报：java.lang.IllegalArgumentException: Cannot copy to a TensorFlowLite tensor (hed_input) with 786432 bytes from a Java Buffer with *** bytes.
     * 因此缺点也很明显：精度降低到256，且强行变形到256*256，输出要进行按比例变回去，其精度丢失明显大很多。
     *
     * @param context Context
     * @return 默认模型
     * @throws IOException 输出异常
     */
    @NonNull
    public static DocumentSkewCorrectionTensorflow getDefault(Context context) throws IOException {
        if (sDefault == null) {
            sDefault = new DocumentSkewCorrectionTensorflow(newInterpreter(context.getAssets(),
                    "hed_lite_model_quantize.tflite"), 256, 256);
        }
        return sDefault;
    }

    /**
     * 新建 Interpreter
     *
     * @param manager  AssetManager
     * @param fileName 文件名
     * @return Interpreter
     * @throws IOException 文件读写异常
     */
    public static Interpreter newInterpreter(AssetManager manager, String fileName)
            throws IOException {
        try (final AssetFileDescriptor fd = manager.openFd(fileName)) {
            return new Interpreter(fd.createInputStream().getChannel().map(
                    FileChannel.MapMode.READ_ONLY, fd.getStartOffset(), fd.getLength()));
        }
    }

    @Override
    public boolean detect(@NonNull Bitmap image, int maxSize,
                          @NonNull DocumentSkewCorrectionPoints points) {
        if (Objects.isNull(image)) {
            return false;
        }
        if (image.isRecycled()) {
            return false;
        }
        final int originalWidth = image.getWidth();
        final int originalHeight = image.getHeight();
        final int scaledWidth = mWidth;
        final int scaledHeight = mHeight;
        final Bitmap scaled;
        try {
            scaled = Bitmap.createScaledBitmap(image, scaledWidth, scaledHeight, true);
        } catch (Throwable t) {
            return false;
        }
        // 创建像素值
        final int[] pixels = new int[scaledWidth * scaledHeight];
        // 读取位图像素值（空间换时间，一次性拿出会比每次获取快）
        scaled.getPixels(pixels, 0, scaledWidth, 0, 0, scaledWidth, scaledHeight);
        if (scaled != image) {
            scaled.recycle();
        }
        // 创建输入
        final ByteBuffer input = ByteBuffer.allocateDirect(
                3 * Float.SIZE / Byte.SIZE * scaledWidth * scaledHeight);
        input.order(ByteOrder.nativeOrder());
        input.clear();
        input.rewind();
        // 写入输入
        for (int pixel : pixels) {
            input.putFloat(((pixel >> 16) & 0xFF));
            input.putFloat(((pixel >> 8) & 0xFF));
            input.putFloat((pixel & 0xFF));
        }
        // 创建输出
        final ByteBuffer output = ByteBuffer.allocateDirect(
                Float.SIZE / Byte.SIZE * scaledWidth * scaledHeight);
        output.order(ByteOrder.nativeOrder());
        output.clear();
        // 处理为灰度图
        mInterpreter.run(input, output);
        output.rewind();
        // 读取输出，并进行二值化
        final byte[] ps = new byte[scaledWidth * scaledHeight];
        for (int i = 0; i < ps.length; i++) {
            if (output.getFloat() > 0.2) {
                ps[i] = (byte) 255;
            } else {
                ps[i] = 0;
            }
        }
        final boolean result = OpenCVUtils.detect(originalWidth, originalHeight,
                scaledWidth, scaledHeight, ps, points);
        // 主动调用一下gc
        System.gc();
        return result;
    }
}
