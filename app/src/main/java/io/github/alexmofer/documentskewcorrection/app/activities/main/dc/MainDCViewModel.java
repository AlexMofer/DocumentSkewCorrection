package io.github.alexmofer.documentskewcorrection.app.activities.main.dc;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import io.github.alexmofer.android.support.other.StringResource;
import io.github.alexmofer.android.support.other.StringResourceException;
import io.github.alexmofer.documentskewcorrection.app.activities.main.common.MainCommonViewModel;
import io.github.alexmofer.documentskewcorrection.app.concurrent.ListenableFutureHelper;

/**
 * ViewModel
 * Created by Alex on 2025/5/26.
 */
public abstract class MainDCViewModel extends MainCommonViewModel {
    private final MutableLiveData<StringResource> mInfo = new MutableLiveData<>(new StringResource("点击右上角菜单选择一张图片"));
    private final MutableLiveData<Uri> mOriginal = new MutableLiveData<>();
    private final MutableLiveData<Uri> mCorrected = new MutableLiveData<>();
    private final MutableLiveData<StringResource> mFailure = new MutableLiveData<>();
    private long mDetectStart;
    private long mDetectEnd;
    private long mCorrectStart;

    LiveData<StringResource> getInfo() {
        return mInfo;
    }

    LiveData<Uri> getOriginal() {
        return mOriginal;
    }

    LiveData<Uri> getCorrected() {
        return mCorrected;
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
        setProcessing(true);
        ListenableFutureHelper.submit(
                () -> handleImageInBackground(context, uri),
                result -> {
                    setProcessing(false);
                    mCorrected.setValue(result);
                },
                t -> {
                    setProcessing(false);
                    mFailure.setValue(StringResourceException.getMessage(t));
                });
    }

    /**
     * 处理图片
     *
     * @param context Context
     * @param uri     图片Uri
     * @return 处理后的图片Uri
     * @throws Exception 失败信息
     */
    @WorkerThread
    @NonNull
    protected abstract Uri handleImageInBackground(Context context, @NonNull Uri uri) throws Exception;

    protected void notifyDetectStart() {
        mDetectStart = System.currentTimeMillis();
    }

    protected void notifyDetectEnd() {
        mDetectEnd = System.currentTimeMillis();
        final long detect = mDetectEnd - mDetectStart;
        final String info = "检测用时：" + detect + "毫秒。";
        mInfo.postValue(new StringResource(info));
    }

    protected void notifyCorrectStart() {
        mCorrectStart = System.currentTimeMillis();
    }

    protected void notifyCorrectEnd() {
        final long detect = mDetectEnd - mDetectStart;
        final long correct = System.currentTimeMillis() - mCorrectStart;
        final String info = "检测用时：" + detect + "毫秒，校正用时：" + correct + "毫秒。";
        mInfo.postValue(new StringResource(info));
    }
}
