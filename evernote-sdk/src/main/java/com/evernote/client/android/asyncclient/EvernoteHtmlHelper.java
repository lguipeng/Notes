package com.evernote.client.android.asyncclient;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.edam.type.Note;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Provides helper methods to receive a {@link Note} as HTML instead of ENML. The easiest way to
 * create an instance is to call {@link EvernoteClientFactory#getHtmlHelperDefault()} for private and
 * linked notes or call {@link EvernoteClientFactory#getHtmlHelperBusiness()} for business notes.
 *
 * @author rwondratschek
 */
@SuppressWarnings("unused")
public class EvernoteHtmlHelper extends EvernoteAsyncClient {

    protected final OkHttpClient mHttpClient;
    protected final String mHost;
    protected final String mAuthToken;

    private final String mAuthHeader;
    private final String mBaseUrl;

    /**
     * @param httpClient The HTTP client executing the GET call.
     * @param host The current host.
     * @param authToken Either the default authentication token or for the business authentication token
     *                  for business notes.
     * @param executorService The executor running the actions in the background.
     */
    public EvernoteHtmlHelper(@NonNull OkHttpClient httpClient, @NonNull String host, @NonNull String authToken, @NonNull ExecutorService executorService) {
        super(executorService);
        mHttpClient = httpClient;
        mHost = host;
        mAuthToken = authToken;

        mAuthHeader = "auth=" + mAuthToken;
        mBaseUrl = createBaseUrl();
    }

    protected String createBaseUrl() {
        return new Uri.Builder()
                .scheme("https")
                .authority(mHost)
                .path("/note")
                .build()
                .toString();
    }

    /**
     * Makes a GET request to download the note content as HTML. Call {@link #parseBody(Response)}
     * to get the note content from the returned response.
     *
     * @param noteGuid The desired note.
     * @return The server response. You can check the status code if the request was successful.
     */
    public Response downloadNote(@NonNull String noteGuid) throws IOException {
        String url = mBaseUrl + '/' + noteGuid;
        return fetchEvernoteUrl(url);
    }

    /**
     * @see #downloadNote(String)
     */
    public Future<Response> downloadNoteAsync(@NonNull final String noteGuid, @Nullable EvernoteCallback<Response> callback) throws IOException {
        return submitTask(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                return downloadNote(noteGuid);
            }
        }, callback);
    }

    /**
     * @param response The returned server response.
     * @return The note content if the server returned {@code 200} as status code, otherwise {@code null}.
     * @throws IOException
     */
    public String parseBody(@NonNull Response response) throws IOException {
        if (response.code() == 200) {
            return response.body().string();
        } else {
            return null;
        }
    }

    /**
     * Fetches the URL with the current authentication token as cookie in the header.
     *
     * <br>
     * <br>
     *
     * <b>Pay attention</b> to which URLs you are sending the authentication token. It's better to
     * verify the host first.
     *
     * @param url The URL which should be opened.
     * @return The raw response.
     */
    public Response fetchEvernoteUrl(String url) throws IOException {
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .addHeader("Cookie", mAuthHeader)
                .get();

        return mHttpClient.newCall(requestBuilder.build()).execute();
    }
}
