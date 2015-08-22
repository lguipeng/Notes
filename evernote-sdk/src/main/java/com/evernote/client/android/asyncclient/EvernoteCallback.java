package com.evernote.client.android.asyncclient;

/**
 * A callback which methods are always invoked on the UI thread.
 *
 * @author rwondratschek
 */
@SuppressWarnings("UnnecessaryInterfaceModifier")
public interface EvernoteCallback<T> {

    /**
     * Invoked when the async operation has completed successfully.
     *
     * @param result The result, which the async operation returned.
     */
    public void onSuccess(final T result);

    /**
     * Invoked when the async operation has completed with an exception.
     *
     * @param exception The error from the async operation.
     */
    public void onException(final Exception exception);
}
