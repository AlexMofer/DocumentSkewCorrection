package io.github.alexmofer.documentskewcorrection.app.concurrent;

import android.os.Handler;
import android.os.Looper;

import androidx.core.os.HandlerCompat;

import java.util.concurrent.Executor;

/**
 * UI线程执行者
 * Created by Alex on 2025/5/26.
 */
public class UIThreadExecutor implements Executor {
    private final Handler mHandler;

    public UIThreadExecutor() {
        mHandler = HandlerCompat.createAsync(Looper.getMainLooper());
    }

    @Override
    public void execute(Runnable command) {
        mHandler.post(command);
    }

    /**
     * 获取 Handler
     *
     * @return Handler
     */
    public Handler getHandler() {
        return mHandler;
    }
}