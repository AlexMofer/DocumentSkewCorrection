package io.github.alexmofer.documentskewcorrection.app.concurrent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import io.github.alexmofer.android.support.concurrent.UIThreadExecutor;

/**
 * ListenableFuture 辅助
 * Created by Alex on 2025/5/9.
 */
public final class ListenableFutureHelper {

    private static ExecutorService sJobThreadPool;
    private static ListeningExecutorService sService;
    private static UIThreadExecutor sUIThreadExecutor;

    private ListenableFutureHelper() {
        //no instance
    }

    public static ExecutorService getTaskThreadPool() {
        if (sJobThreadPool == null) {
            final int CPU = Runtime.getRuntime().availableProcessors();
            sJobThreadPool = new ThreadPoolExecutor(
                    Math.max(2, Math.min(CPU - 1, 4)),
                    CPU * 2 + 1,
                    1, TimeUnit.SECONDS,
                    new PriorityBlockingQueue<>(3, new InnerComparator()),
                    new InnerThreadFactory());
        }
        return sJobThreadPool;
    }

    public static ListeningExecutorService getListeningExecutorService() {
        if (sService == null) {
            sService = MoreExecutors.listeningDecorator(getTaskThreadPool());
        }
        return sService;
    }

    public static UIThreadExecutor getUIThreadExecutor() {
        if (sUIThreadExecutor == null) {
            sUIThreadExecutor = new UIThreadExecutor();
        }
        return sUIThreadExecutor;
    }

    /**
     * 提交异步任务
     *
     * @param task    任务执行回调
     * @param success 任务成功回调
     * @param failure 任务失败回调
     * @param <T>     返回类型
     * @return 异步任务
     * @noinspection UnusedReturnValue
     */
    public static <T> ListenableFuture<T> submit(Callable<T> task, Consumer<T> success,
                                                 @Nullable Consumer<Throwable> failure) {
        final ListenableFuture<T> future = getListeningExecutorService().submit(task);
        Futures.addCallback(
                future,
                new FutureCallback<>() {

                    public void onSuccess(T result) {
                        success.accept(result);
                    }

                    public void onFailure(@NonNull Throwable t) {
                        if (failure != null) {
                            failure.accept(t);
                        }
                    }
                },
                getUIThreadExecutor());
        return future;
    }

    /**
     * 提交异步任务
     *
     * @param task    任务执行回调
     * @param success 任务成功回调
     * @param <T>     返回类型
     * @return 异步任务
     * @noinspection UnusedReturnValue
     */
    public static <T> ListenableFuture<T> submit(Callable<T> task, Consumer<T> success) {
        return submit(task, success, null);
    }

    /**
     * 提交异步任务
     *
     * @param task 任务执行回调
     * @param <T>  返回类型
     * @return 异步任务
     * @noinspection UnusedReturnValue
     */
    public static <T> ListenableFuture<T> submit(Callable<T> task) {
        return getListeningExecutorService().submit(task);
    }

    /**
     * 提交异步任务
     *
     * @param task    任务执行回调
     * @param success 任务成功回调
     * @param failure 任务失败回调
     * @return 异步任务
     * @noinspection UnusedReturnValue
     */
    public static ListenableFuture<?> submit(ThrowableRunnable task, Runnable success,
                                             @Nullable Consumer<Throwable> failure) {
        return submit(() -> {
            task.run();
            return null;
        }, unused -> success.run(), failure);
    }

    /**
     * 提交异步任务
     *
     * @param task    任务执行回调
     * @param success 任务成功回调
     * @return 异步任务
     * @noinspection UnusedReturnValue
     */
    public static ListenableFuture<?> submit(ThrowableRunnable task, Runnable success) {
        return submit(task, success, null);
    }

    /**
     * 提交异步任务
     *
     * @param task 任务执行回调
     * @return 异步任务
     * @noinspection UnusedReturnValue
     */
    public static ListenableFuture<?> submit(ThrowableRunnable task) {
        return getListeningExecutorService().submit(() -> {
            task.run();
            return null;
        });
    }

    /**
     * 主线程延迟执行
     *
     * @param r           可执行的
     * @param delayMillis 延迟时间
     * @return 发起成功时返回true
     */
    public static boolean postDelayed(@NonNull Runnable r, long delayMillis) {
        return getUIThreadExecutor().getHandler().postDelayed(r, delayMillis);
    }

    @FunctionalInterface
    public interface ThrowableRunnable {
        /**
         * When an object implementing interface {@code Runnable} is used
         * to create a thread, starting the thread causes the object's
         * {@code run} method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method {@code run} is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        void run() throws Exception;
    }

    private static class InnerComparator implements Comparator<Runnable> {

        @Override
        public int compare(Runnable o1, Runnable o2) {
            if (o1 instanceof Comparable && o2 instanceof Comparable) {
                //noinspection unchecked,rawtypes
                return ((Comparable) o1).compareTo(o2);
            }
            return 0;
        }
    }

    private static class InnerThreadFactory implements ThreadFactory {

        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "Job #" + mCount.getAndIncrement()) {

                @Override
                public void run() {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                    super.run();
                }
            };
        }
    }
}
