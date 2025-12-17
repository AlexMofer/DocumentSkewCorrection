package io.github.alexmofer.documentskewcorrection.app.activities.main.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.UUID;

import io.github.alexmofer.android.support.other.StringResource;
import io.github.alexmofer.android.support.other.StringResourceException;
import io.github.alexmofer.android.support.utils.ContextUtils;
import io.github.alexmofer.documentskewcorrection.DocumentSkewCorrectionOpenCV;
import io.github.alexmofer.documentskewcorrection.DocumentSkewCorrectionPoints;
import io.github.alexmofer.documentskewcorrection.app.activities.main.common.MainCommonViewModel;
import io.github.alexmofer.documentskewcorrection.app.concurrent.ListenableFutureHelper;
import io.github.alexmofer.documentskewcorrection.app.utils.FileProviderUtils;

/**
 * ViewModel
 * Created by Alex on 2025/5/26.
 */
public class MainUIViewModel extends MainCommonViewModel {
    private final MutableLiveData<Uri> mOriginal = new MutableLiveData<>();
    private final MutableLiveData<DocumentSkewCorrectionPoints> mPoints = new MutableLiveData<>();
    private final MutableLiveData<Uri> mCorrected = new MutableLiveData<>();
    private final MutableLiveData<StringResource> mFailure = new MutableLiveData<>();

    LiveData<Uri> getOriginal() {
        return mOriginal;
    }

    LiveData<DocumentSkewCorrectionPoints> getPoints() {
        return mPoints;
    }

    LiveData<Uri> getCorrected() {
        return mCorrected;
    }

    void clearCorrected() {
        mCorrected.setValue(null);
    }

    LiveData<StringResource> getFailure() {
        return mFailure;
    }

    void clearFailure() {
        mFailure.setValue(null);
    }

    @Override
    protected final void handleImage(Context context, Uri uri) {
        mOriginal.setValue(uri);
        mPoints.setValue(null);
        setProcessing(true);
        ListenableFutureHelper.submit(() -> {
            final DocumentSkewCorrectionPoints points = new DocumentSkewCorrectionPoints();
            final boolean detected =
                    DocumentSkewCorrectionOpenCV.getInstance().detect(context, uri, points);
            if (!detected) {
                throw new StringResourceException("检测不到文档边框，请选择其他图片");
            }
            return points;
        }, result -> {
            setProcessing(false);
            mPoints.setValue(result);
        }, t -> {
            setProcessing(false);
            mFailure.setValue(StringResourceException.getMessage(t));
        });
    }

    void correct(Context context, DocumentSkewCorrectionPoints points) {
        final Uri uri = mOriginal.getValue();
        if (uri == null) {
            mFailure.setValue(new StringResource("点击右上角菜单选择一张图片进行处理"));
            return;
        }
        if (points == null) {
            mFailure.setValue(new StringResource("校正点为空"));
            return;
        }
        setProcessing(true);
        mCorrected.setValue(null);
        ListenableFutureHelper.submit(() -> {
            final Bitmap corrected =
                    DocumentSkewCorrectionOpenCV.getInstance().correct(context, uri, points);
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
        }, result -> {
            setProcessing(false);
            mCorrected.setValue(result);
        }, t -> {
            setProcessing(false);
            mFailure.setValue(StringResourceException.getMessage(t));
        });
    }
}
