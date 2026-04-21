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
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.exifinterface.media.ExifInterface;

import java.io.InputStream;

/**
 * 位图工具
 * 可使用以下工具库替换：
 * io.github.alexmofer.android.support.utils.BitmapUtils
 * Created by Alex on 2025/12/17.
 */
final class BitmapUtils {

    private BitmapUtils() {
        //no instance
    }

    /**
     * 判断是否为位图
     *
     * @param context Context
     * @param uri     Uri
     * @return 为位图时返回 true
     * @throws Exception Uri打开异常
     * @noinspection BooleanMethodIsAlwaysInverted
     */
    public static boolean isBitmap(Context context, @NonNull Uri uri) throws Exception {
        try (final InputStream input = context.getContentResolver().openInputStream(uri)) {
            if (input == null) {
                throw new Exception("Cannot open uri.");
            }
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, options);
            return options.outWidth > 0 && options.outHeight > 0;
        }
    }

    private static int getExifOrientation(Context context, @NonNull Uri uri) throws Exception {
        try (final InputStream input = context.getContentResolver().openInputStream(uri)) {
            if (input == null) {
                throw new Exception("Cannot open uri.");
            }
            final ExifInterface exif = new ExifInterface(input);
            return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
        }
    }

    /**
     * 从 Uri 获取位图
     *
     * @param context       Context
     * @param uri           Uri
     * @param mutable       是否可修改
     * @param config        格式
     * @param premultiplied 是否预乘，未预乘的位图不可用于显示
     * @return 位图
     */
    public static Bitmap fromUri(Context context, @NonNull Uri uri,
                                 boolean mutable, Bitmap.Config config, boolean premultiplied) throws Exception {
        if (!isBitmap(context, uri)) {
            throw new Exception("Not a bitmap uri.");
        }
        final int orientation = getExifOrientation(context, uri);
        try (final InputStream input = context.getContentResolver().openInputStream(uri)) {
            if (input == null) {
                throw new Exception("Cannot get bitmap from uri.");
            }
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = mutable;
            options.inPreferredConfig = config;
            options.inPremultiplied = premultiplied;
            final Bitmap original = BitmapFactory.decodeStream(input, null, options);
            if (original == null) {
                throw new Exception("Cannot get bitmap from uri.");
            }
            // 处理 EXIF ORIENTATION
            if (orientation == ExifInterface.ORIENTATION_FLIP_HORIZONTAL) {
                // 水平翻转
                final Matrix matrix = new Matrix();
                matrix.setScale(-1, 1);
                final Bitmap handled = Bitmap.createBitmap(original, 0, 0,
                        original.getWidth(), original.getHeight(), matrix, true);
                original.recycle();
                return handled;
            }
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                // 180度旋转
                final Matrix matrix = new Matrix();
                matrix.setRotate(180);
                final Bitmap handled = Bitmap.createBitmap(original, 0, 0,
                        original.getWidth(), original.getHeight(), matrix, true);
                original.recycle();
                return handled;
            }
            if (orientation == ExifInterface.ORIENTATION_FLIP_VERTICAL) {
                // 垂直翻转
                final Matrix matrix = new Matrix();
                matrix.setScale(1, -1);
                final Bitmap handled = Bitmap.createBitmap(original, 0, 0,
                        original.getWidth(), original.getHeight(), matrix, true);
                original.recycle();
                return handled;
            }
            if (orientation == ExifInterface.ORIENTATION_TRANSPOSE) {
                // 垂直翻转再旋转90度
                final Matrix matrix = new Matrix();
                matrix.setScale(1, -1);
                matrix.postRotate(90);
                final Bitmap handled = Bitmap.createBitmap(original, 0, 0,
                        original.getWidth(), original.getHeight(), matrix, true);
                original.recycle();
                return handled;
            }
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                // 旋转90度
                final Matrix matrix = new Matrix();
                matrix.setRotate(90);
                final Bitmap handled = Bitmap.createBitmap(original, 0, 0,
                        original.getWidth(), original.getHeight(), matrix, true);
                original.recycle();
                return handled;
            }
            if (orientation == ExifInterface.ORIENTATION_TRANSVERSE) {
                // 旋转90度再垂直翻转
                final Matrix matrix = new Matrix();
                matrix.setRotate(90);
                matrix.postScale(1, -1);
                final Bitmap handled = Bitmap.createBitmap(original, 0, 0,
                        original.getWidth(), original.getHeight(), matrix, true);
                original.recycle();
                return handled;
            }
            if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                // 旋转270度
                final Matrix matrix = new Matrix();
                matrix.setRotate(270);
                final Bitmap handled = Bitmap.createBitmap(original, 0, 0,
                        original.getWidth(), original.getHeight(), matrix, true);
                original.recycle();
                return handled;
            }
            // 无需处理
            return original;
        }
    }
}
