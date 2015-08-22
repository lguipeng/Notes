package com.evernote.client.android.asyncclient;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.EvernoteUtil;
import com.evernote.client.android.helper.EvernotePreconditions;
import com.evernote.client.conn.mobile.ByteStore;
import com.evernote.client.conn.mobile.DiskBackedByteStore;
import com.evernote.client.conn.mobile.TAndroidTransport;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.type.LinkedNotebook;
import com.evernote.edam.type.User;
import com.evernote.edam.userstore.AuthenticationResult;
import com.evernote.edam.userstore.UserStore;
import com.evernote.thrift.TException;
import com.evernote.thrift.protocol.TBinaryProtocol;
import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * A factory to create async wrappers around a {@link NoteStore.Client}. Use the corresponding
 * {@link EvernoteClientFactory.Builder} to create an instance.
 * <p/>
 * <br>
 * <br>
 * <p/>
 * Try to reuse a created instances. A factory caches created {@link NoteStore.Client}s, their wrappers
 * and internal helper objects like the http client. The easiest way to get access to a factory is to
 * call {@link EvernoteSession#getEvernoteClientFactory()}.
 *
 * @author rwondratschek
 */
@SuppressWarnings("unused")
public class EvernoteClientFactory {

    protected final EvernoteSession mEvernoteSession;
    protected final OkHttpClient mHttpClient;
    protected final ByteStore mByteStore;
    protected final Map<String, String> mHeaders;
    protected final ExecutorService mExecutorService;

    private final Map<String, EvernoteUserStoreClient> mUserStoreClients;
    private final Map<String, EvernoteNoteStoreClient> mNoteStoreClients;
    private final Map<String, EvernoteLinkedNotebookHelper> mLinkedNotebookHelpers;
    private EvernoteBusinessNotebookHelper mBusinessNotebookHelper;

    private EvernoteHtmlHelper mHtmlHelperDefault;
    private final Map<String, EvernoteHtmlHelper> mLinkedHtmlHelper;
    private EvernoteHtmlHelper mHtmlHelperBusiness;

    private EvernoteSearchHelper mEvernoteSearchHelper;

    private final EvernoteAsyncClient mCreateHelperClient;

    private com.evernote.edam.userstore.AuthenticationResult mBusinessAuthenticationResult;

    protected EvernoteClientFactory(EvernoteSession session, OkHttpClient httpClient, ByteStore byteStore, Map<String, String> headers, ExecutorService executorService) {
        mEvernoteSession = EvernotePreconditions.checkNotNull(session);
        mHttpClient = EvernotePreconditions.checkNotNull(httpClient);
        mByteStore = EvernotePreconditions.checkNotNull(byteStore);
        mHeaders = headers;
        mExecutorService = EvernotePreconditions.checkNotNull(executorService);

        mUserStoreClients = new HashMap<>();
        mNoteStoreClients = new HashMap<>();
        mLinkedNotebookHelpers = new HashMap<>();
        mLinkedHtmlHelper = new HashMap<>();

        mCreateHelperClient = new EvernoteAsyncClient(mExecutorService) { };
    }

    /**
     * @return The default client for this session. It references the signed in user's user store.
     * @see UserStore
     * @see UserStore.Client
     */
    public synchronized EvernoteUserStoreClient getUserStoreClient() {
        checkLoggedIn();

        String url = new Uri.Builder()
                .scheme("https")
                .authority(mEvernoteSession.getAuthenticationResult().getEvernoteHost())
                .path("/edam/user")
                .build()
                .toString();

        return getUserStoreClient(url, mEvernoteSession.getAuthToken());
    }

    /**
     * @return An async wrapper for {@link UserStore.Client} with this specific url and authentication
     * token combination.
     * @see UserStore
     * @see UserStore.Client
     */
    public synchronized EvernoteUserStoreClient getUserStoreClient(@NonNull String url, @Nullable String authToken) {
        String key = createKey(url, authToken);
        EvernoteUserStoreClient userStoreClient = mUserStoreClients.get(key);
        if (userStoreClient == null) {
            userStoreClient = createUserStoreClient(url, authToken);
            mUserStoreClients.put(key, userStoreClient);
        }
        return userStoreClient;
    }

    protected EvernoteUserStoreClient createUserStoreClient(String url, String authToken) {
        UserStore.Client client = new UserStore.Client(createBinaryProtocol(url));
        return new EvernoteUserStoreClient(client, authToken, mExecutorService);
    }

    /**
     * @return The default client for this session. It references the user's private note store.
     * @see EvernoteClientFactory#getNoteStoreClient(String, String)
     * @see com.evernote.client.android.AuthenticationResult#getNoteStoreUrl()
     */
    public synchronized EvernoteNoteStoreClient getNoteStoreClient() {
        checkLoggedIn();

        return getNoteStoreClient(mEvernoteSession.getAuthenticationResult().getNoteStoreUrl(), EvernotePreconditions.checkNotEmpty(mEvernoteSession.getAuthToken()));
    }

    /**
     * @param url       The note store URL.
     * @param authToken The authentication token to get access to this note store.
     * @return An async wrapper for {@link NoteStore.Client} with this specific url and authentication
     * token combination.
     * @see NoteStore
     * @see NoteStore.Client
     */
    public synchronized EvernoteNoteStoreClient getNoteStoreClient(@NonNull String url, @NonNull String authToken) {
        String key = createKey(url, authToken);
        EvernoteNoteStoreClient client = mNoteStoreClients.get(key);
        if (client == null) {
            client = createEvernoteNoteStoreClient(url, authToken);
            mNoteStoreClients.put(key, client);
        }

        return client;
    }

    /**
     * Returns an async wrapper providing several helper methods for this {@link LinkedNotebook}. With
     * {@link EvernoteLinkedNotebookHelper#getClient()} you can get access to the underlying {@link EvernoteNoteStoreClient},
     * which references the {@link LinkedNotebook}'s note store URL.
     *
     * @param linkedNotebook The referenced {@link LinkedNotebook}. Its GUID and share key must not be
     *                       {@code null}.
     * @return An async wrapper providing several helper methods.
     */
    public synchronized EvernoteLinkedNotebookHelper getLinkedNotebookHelper(@NonNull LinkedNotebook linkedNotebook)
            throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {

        String key = linkedNotebook.getGuid();
        EvernoteLinkedNotebookHelper notebookHelper = mLinkedNotebookHelpers.get(key);
        if (notebookHelper == null) {
            notebookHelper = createLinkedNotebookHelper(linkedNotebook);
            mLinkedNotebookHelpers.put(key, notebookHelper);
        }

        return notebookHelper;
    }

    /**
     * @see #getLinkedNotebookHelper(LinkedNotebook)
     */
    public Future<EvernoteLinkedNotebookHelper> getLinkedNotebookHelperAsync(@NonNull final LinkedNotebook linkedNotebook,
                                                                             @Nullable EvernoteCallback<EvernoteLinkedNotebookHelper> callback) {

        return mCreateHelperClient.submitTask(new Callable<EvernoteLinkedNotebookHelper>() {
            @Override
            public EvernoteLinkedNotebookHelper call() throws Exception {
                return getLinkedNotebookHelper(linkedNotebook);
            }
        }, callback);
    }

    protected EvernoteLinkedNotebookHelper createLinkedNotebookHelper(@NonNull LinkedNotebook linkedNotebook) throws EDAMUserException,
            EDAMSystemException, EDAMNotFoundException, TException {

        String url = linkedNotebook.getNoteStoreUrl();

        EvernoteNoteStoreClient client = getNoteStoreClient(url, EvernotePreconditions.checkNotEmpty(mEvernoteSession.getAuthToken()));
        AuthenticationResult authenticationResult = client.authenticateToSharedNotebook(linkedNotebook.getShareKey());

        client = getNoteStoreClient(url, authenticationResult.getAuthenticationToken());

        return new EvernoteLinkedNotebookHelper(client, linkedNotebook, mExecutorService);
    }

    /**
     * Returns an async wrapper providing several helper methods for business notebooks. With
     * {@link EvernoteBusinessNotebookHelper#getClient()} you can get access to the underlying {@link EvernoteNoteStoreClient},
     * which references the business note store URL.
     *
     * @return An async wrapper providing several helper methods.
     */
    public synchronized EvernoteBusinessNotebookHelper getBusinessNotebookHelper() throws TException, EDAMUserException, EDAMSystemException {
        if (mBusinessNotebookHelper == null || isBusinessAuthExpired()) {
            mBusinessNotebookHelper = createBusinessNotebookHelper();
        }
        return mBusinessNotebookHelper;
    }

    /**
     * @see #getBusinessNotebookHelper()
     */
    public Future<EvernoteBusinessNotebookHelper> getBusinessNotebookHelperAsync(@Nullable EvernoteCallback<EvernoteBusinessNotebookHelper> callback) {
        return mCreateHelperClient.submitTask(new Callable<EvernoteBusinessNotebookHelper>() {
            @Override
            public EvernoteBusinessNotebookHelper call() throws Exception {
                return getBusinessNotebookHelper();
            }
        }, callback);
    }

    protected EvernoteBusinessNotebookHelper createBusinessNotebookHelper() throws TException, EDAMUserException, EDAMSystemException {
        authenticateToBusiness();

        EvernoteNoteStoreClient client = getNoteStoreClient(mBusinessAuthenticationResult.getNoteStoreUrl(), mBusinessAuthenticationResult.getAuthenticationToken());

        User businessUser = mBusinessAuthenticationResult.getUser();
        return new EvernoteBusinessNotebookHelper(client, mExecutorService, businessUser.getName(), businessUser.getShardId());
    }

    /**
     * Use this method, if you want to download a personal note as HTML.
     *
     * @return An async wrapper to load a note as HTML from the Evernote service.
     */
    public synchronized EvernoteHtmlHelper getHtmlHelperDefault() {
        checkLoggedIn();

        if (mHtmlHelperDefault == null) {
            mHtmlHelperDefault = createHtmlHelper(mEvernoteSession.getAuthToken());
        }
        return mHtmlHelperDefault;
    }

    /**
     * Use this method, if you want to download a linked note as HTML.
     *
     * @param linkedNotebook The referenced {@link LinkedNotebook}. Its GUID and share key must not be
     *                       {@code null}.
     * @return An async wrapper to load a note as HTML from the Evernote service.
     */
    public EvernoteHtmlHelper getLinkedHtmlHelper(@NonNull LinkedNotebook linkedNotebook) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        String key = linkedNotebook.getGuid();

        EvernoteHtmlHelper htmlHelper = mLinkedHtmlHelper.get(key);
        if (htmlHelper == null) {
            String url = linkedNotebook.getNoteStoreUrl();

            EvernoteNoteStoreClient client = getNoteStoreClient(url, EvernotePreconditions.checkNotEmpty(mEvernoteSession.getAuthToken()));
            AuthenticationResult authenticationResult = client.authenticateToSharedNotebook(linkedNotebook.getShareKey());

            htmlHelper = createHtmlHelper(authenticationResult.getAuthenticationToken());

            mLinkedHtmlHelper.put(key, htmlHelper);
        }

        return htmlHelper;
    }

    /**
     * @see #getLinkedNotebookHelper(LinkedNotebook)
     */
    public Future<EvernoteHtmlHelper> getLinkedHtmlHelperAsync(@NonNull final LinkedNotebook linkedNotebook, @Nullable EvernoteCallback<EvernoteHtmlHelper> callback) {
        return mCreateHelperClient.submitTask(new Callable<EvernoteHtmlHelper>() {
            @Override
            public EvernoteHtmlHelper call() throws Exception {
                return getLinkedHtmlHelper(linkedNotebook);
            }
        }, callback);
    }

    /**
     * Use this method, if you want to download a business note as HTML.
     *
     * @return An async wrapper to load a business note as HTML from the Evernote service.
     */
    public synchronized EvernoteHtmlHelper getHtmlHelperBusiness() throws TException, EDAMUserException, EDAMSystemException {
        if (mHtmlHelperBusiness == null) {
            authenticateToBusiness();
            mHtmlHelperBusiness = createHtmlHelper(mBusinessAuthenticationResult.getAuthenticationToken());
        }
        return mHtmlHelperBusiness;
    }

    /**
     * @see #getHtmlHelperBusiness()
     */
    public Future<EvernoteHtmlHelper> getHtmlHelperBusinessAsync(@Nullable EvernoteCallback<EvernoteHtmlHelper> callback) {
        return mCreateHelperClient.submitTask(new Callable<EvernoteHtmlHelper>() {
            @Override
            public EvernoteHtmlHelper call() throws Exception {
                return getHtmlHelperBusiness();
            }
        }, callback);
    }

    protected EvernoteHtmlHelper createHtmlHelper(String authToken) {
        return new EvernoteHtmlHelper(mHttpClient, mEvernoteSession.getAuthenticationResult().getEvernoteHost(), authToken, mExecutorService);
    }

    /**
     * @return An async wrapper to search notes in multiple note stores.
     */
    public EvernoteSearchHelper getEvernoteSearchHelper() {
        checkLoggedIn();

        if (mEvernoteSearchHelper == null) {
            mEvernoteSearchHelper = createEvernoteSearchHelper();
        }
        return mEvernoteSearchHelper;
    }

    protected EvernoteSearchHelper createEvernoteSearchHelper() {
        return new EvernoteSearchHelper(mEvernoteSession, mExecutorService);
    }

    protected TBinaryProtocol createBinaryProtocol(String url) {
        return new TBinaryProtocol(new TAndroidTransport(mHttpClient, mByteStore, url, mHeaders));
    }

    protected NoteStore.Client createNoteStoreClient(String url) {
        return new NoteStore.Client(createBinaryProtocol(url));
    }

    protected synchronized EvernoteNoteStoreClient createEvernoteNoteStoreClient(String url, String authToken) {
        return new EvernoteNoteStoreClient(createNoteStoreClient(url), authToken, mExecutorService);
    }

    protected final String createKey(String url, String authToken) {
        if (url == null && authToken == null) {
            throw new IllegalArgumentException();
        } else if (url == null) {
            return authToken;
        } else if (authToken == null) {
            return url;
        } else {
            return url + authToken;
        }
    }

    protected final void authenticateToBusiness() throws TException, EDAMUserException, EDAMSystemException {
        if (isBusinessAuthExpired()) {
            mBusinessAuthenticationResult = getUserStoreClient().authenticateToBusiness();
        }
    }

    protected final boolean isBusinessAuthExpired() {
        return mBusinessAuthenticationResult == null || mBusinessAuthenticationResult.getExpiration() < System.currentTimeMillis();
    }

    protected void checkLoggedIn() {
        if (!mEvernoteSession.isLoggedIn()) {
            throw new IllegalStateException("user not logged in");
        }
    }

    /**
     * A builder to construct an {@link EvernoteClientFactory}. The recommended approach is to set
     * the builder in the session with {@link EvernoteSession#setEvernoteClientFactoryBuilder(Builder)}
     * and then to call {@link EvernoteSession#getEvernoteClientFactory()}.
     */
    public static class Builder {

        private final EvernoteSession mEvernoteSession;
        private final Map<String, String> mHeaders;

        private OkHttpClient mHttpClient;
        private ByteStore.Factory mByteStoreFactory;
        private ExecutorService mExecutorService;

        /**
         * @param evernoteSession The current session, must not be {@code null}.
         */
        public Builder(EvernoteSession evernoteSession) {
            mEvernoteSession = EvernotePreconditions.checkNotNull(evernoteSession);
            mHeaders = new HashMap<>();
        }

        /**
         * @param httpClient The client executing the HTTP calls.
         */
        public Builder setHttpClient(OkHttpClient httpClient) {
            mHttpClient = httpClient;
            return this;
        }

        /**
         * @param byteStoreFactory Creates the {@link ByteStore} for each Thread. The {@link ByteStore}
         *                         caches the written data, which is later sent to the Evernote service.
         */
        public Builder setByteStoreFactory(ByteStore.Factory byteStoreFactory) {
            mByteStoreFactory = byteStoreFactory;
            return this;
        }

        private Builder addHeader(String name, String value) {
            // maybe set this to public
            mHeaders.put(name, value);
            return this;
        }

        /**
         * @param executorService Runs the background actions.
         */
        public Builder setExecutorService(ExecutorService executorService) {
            mExecutorService = executorService;
            return this;
        }

        public EvernoteClientFactory build() {
            if (mHttpClient == null) {
                mHttpClient = createDefaultHttpClient();
            }
            if (mByteStoreFactory == null) {
                mByteStoreFactory = createDefaultByteStore(mEvernoteSession.getApplicationContext());
            }
            if (mExecutorService == null) {
                mExecutorService = Executors.newSingleThreadExecutor();
            }

            addHeader("Cache-Control", "no-transform");
            addHeader("Accept", "application/x-thrift");
            addHeader("User-Agent", EvernoteUtil.generateUserAgentString(mEvernoteSession.getApplicationContext()));

            return new EvernoteClientFactory(mEvernoteSession, mHttpClient, mByteStoreFactory.create(), mHeaders, mExecutorService);
        }

        private OkHttpClient createDefaultHttpClient() {
            OkHttpClient httpClient = new OkHttpClient();
            httpClient.setConnectTimeout(10, TimeUnit.SECONDS);
            httpClient.setReadTimeout(10, TimeUnit.SECONDS);
            httpClient.setWriteTimeout(20, TimeUnit.SECONDS);
            httpClient.setConnectionPool(new ConnectionPool(20, 2 * 60 * 1000));
            return httpClient;
        }

        private ByteStore.Factory createDefaultByteStore(Context context) {
            int cacheSize = (int) (Runtime.getRuntime().maxMemory() / 32);
            return new DiskBackedByteStore.Factory(new File(context.getCacheDir(), "evernoteCache"), cacheSize);
        }
    }
}
