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
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.annotation.StyleableRes;

import java.util.function.Consumer;

/**
 * 工具
 * 可使用以下工具库替换：
 * io.github.alexmofer.android.support.formulas.DistanceFormulas
 * Created by Alex on 2025/12/17.
 */
final class ViewUtils {

    /**
     * Retrieve styled attribute information in this Context's theme.  See
     * {@link android.content.res.Resources.Theme#obtainStyledAttributes(AttributeSet, int[], int, int)}
     * for more information.
     *
     * @see android.content.res.Resources.Theme#obtainStyledAttributes(AttributeSet, int[], int, int)
     */
    public static void obtainStyledAttributes(Consumer<TypedArray> consumer,
                                              @NonNull Context context,
                                              @Nullable AttributeSet set,
                                              @NonNull @StyleableRes int[] attrs,
                                              @AttrRes int defStyleAttr,
                                              @StyleRes int defStyleRes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try (final TypedArray custom =
                         context.obtainStyledAttributes(set, attrs, defStyleAttr, defStyleRes)) {
                consumer.accept(custom);
            }
        } else {
            final TypedArray custom =
                    context.obtainStyledAttributes(set, attrs, defStyleAttr, defStyleRes);
            consumer.accept(custom);
            custom.recycle();
        }
    }
}
