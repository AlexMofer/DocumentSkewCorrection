package io.github.alexmofer.documentskewcorrection.app.activities.main.hms;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.UUID;

import io.github.alexmofer.android.support.other.StringResourceException;
import io.github.alexmofer.android.support.utils.ContextUtils;
import io.github.alexmofer.documentskewcorrection.app.activities.main.auto.MainAutoViewModel;
import io.github.alexmofer.documentskewcorrection.app.utils.FileProviderUtils;
import io.github.alexmofer.documentskewcorrection.hms.DocumentSkewCorrectionHMS;

/**
 * ViewModel
 * Created by Alex on 2025/5/27.
 */
public class MainHMSViewModel extends MainAutoViewModel {
    @Override
    @NonNull
    protected Uri handleImageInBackground(Context context, @NonNull Uri uri) throws Exception {
        // 可用性检查
        if (!DocumentSkewCorrectionHMS.isEnable(context)) {
            // 设备不支持
            throw new StringResourceException("设备不支持");
        }
        // 检测
        this.notifyDetectStart();
        final float[] points = DocumentSkewCorrectionHMS.detect(context, uri);
        this.notifyDetectEnd();
        if (points == null) {
            throw new StringResourceException("检测不到文档边框，请选择其他图片");
        }
        // 校正
        this.notifyCorrectStart();
        final Bitmap corrected = DocumentSkewCorrectionHMS.correct(context, uri, points);
        this.notifyCorrectEnd();
        if (corrected == null) {
            throw new StringResourceException("文档校正失败");
        }
        // 写入文件
        final File dir = ContextUtils.getExternalCacheDir(context, true);
        final File saved = new File(dir, "Corrected_" + UUID.randomUUID().toString());
        //noinspection IOStreamConstructor
        try (final OutputStream output = new FileOutputStream(saved)) {
            if (corrected.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                return FileProviderUtils.getUriForFile(context, saved);
            } else {
                throw new StringResourceException("保存到文件失败");
            }
        } finally {
            corrected.recycle();
        }
    }
}
