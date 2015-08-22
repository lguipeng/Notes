package com.evernote.client.android.asyncclient;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.client.android.helper.EvernotePreconditions;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * An async client executes actions in the background and returns the result on the UI thread.
 *
 * @author rwondratschek
 */
public abstract class EvernoteAsyncClient {

    private static final Handler UI_HANDLER = new Handler(Looper.getMainLooper());
    private static final Thread UI_THREAD = Looper.getMainLooper().getThread();

    private final ExecutorService mExecutorService;

    protected EvernoteAsyncClient(@NonNull ExecutorService executorService) {
        mExecutorService = EvernotePreconditions.checkNotNull(executorService);
    }

    protected <T> Future<T> submitTask(@NonNull final Callable<T> callable, @Nullable final EvernoteCallback<T> callback) {
        return mExecutorService.submit(new Callable<T>() {
            @Override
            public T call() throws Exception {
                try {
                    T result = callable.call();
                    onResult(result, callback);
                    return result;

                } catch (Exception e) {
                    onException(e, callback);
                    return null;
                }
            }
        });
    }

    private <T> void onResult(final T result, final EvernoteCallback<T> callback) {
        if (callback != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.onSuccess(result);
                }
            });
        }
    }

    private <T> void onException(final Exception e, final EvernoteCallback<T> callback) {
        if (callback != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.onException(e);
                }
            });
        }
    }

    protected final void runOnUiThread(@NonNull Runnable runnable) {
        if (Thread.currentThread() != UI_THREAD) {
            UI_HANDLER.post(runnable);
        } else {
            runnable.run();
        }
    }
}
