/*
 * Copyright 2012 Evernote Corporation.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.evernote.client.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;

import com.evernote.client.android.asyncclient.EvernoteClientFactory;
import com.evernote.client.android.helper.Cat;
import com.evernote.client.android.helper.EvernotePreconditions;
import com.evernote.client.android.login.EvernoteLoginActivity;
import com.evernote.client.android.login.EvernoteLoginFragment;

import java.io.File;
import java.util.Locale;

/**
 * Represents a session with the Evernote web service API. Used to authenticate
 * to the service via OAuth and obtain NoteStore.Client objects, which are used
 * to make authenticated API calls.
 *
 * To use EvernoteSession, first initialize the EvernoteSession singleton with the
 * {@link EvernoteSession.Builder} class and call {@link EvernoteSession#asSingleton()}. After that
 * initiate authentication at an appropriate time:
 * <pre>
 * EvernoteSession evernoteSession = new EvernoteSession.Builder(this)
 *      .setEvernoteService(EvernoteSession.EvernoteService.PRODUCTION)
 *      .setSupportAppLinkedNotebooks(SUPPORT_APP_LINKED_NOTEBOOKS)
 *      .build(consumerKey, consumerSecret)
 *      .asSingleton();
 *
 * if (!session.isLoggedIn()) {
 *      session.authenticate(...);
 * }
 * </pre>
 *
 * Later, you can make any Evernote API calls that you need by obtaining a
 * NoteStore.Client from the session and using the session's auth token:
 * <pre>
 *   NoteStore.client noteStore = session.createNoteStoreClient();
 *   Notebook notebook = noteStore.getDefaultNotebook(session.getAuthToken());
 * </pre>
 *
 * @author tsmith
 * @author rwondratschek
 */
@SuppressWarnings("UnusedDeclaration")
public final class EvernoteSession {

    // Standard hostnames for bootstrap detection
    public static final String HOST_SANDBOX = "https://sandbox.evernote.com";
    public static final String HOST_PRODUCTION = "https://www.evernote.com";
    public static final String HOST_CHINA = "https://app.yinxiang.com";

    public static final String SCREEN_NAME_YXBIJI = "印象笔记";
    public static final String SCREEN_NAME_INTERNATIONAL = "Evernote International";

    /**
     * @deprecated Use {@link EvernoteSession#REQUEST_CODE_LOGIN} instead.
     */
    @Deprecated
    public static final int REQUEST_CODE_OAUTH = 14390;

    /**
     * The used request code when you launch authentication process from a {@link Activity}. Override
     * {@link Activity#onActivityResult(int, int, Intent)} to receive the result.
     */
    public static final int REQUEST_CODE_LOGIN = 14390;

    private static final Cat CAT = new Cat("EvernoteSession");

    private static EvernoteSession sInstance = null;

    public static EvernoteSession getInstance() {
        return sInstance;
    }

    /**
     * Use to acquire a singleton instance of the EvernoteSession for authentication.
     * If the singleton has already been initialized, the existing instance will
     * be returned (and the parameters passed to this method will be ignored).
     *
     * @param ctx                       Application Context or activity
     * @param consumerKey               The consumer key portion of your application's API key.
     * @param consumerSecret            The consumer secret portion of your application's API key.
     * @param evernoteService           The enum of the Evernote service instance that you wish
     *                                  to use. Development and testing is typically performed against {@link EvernoteService#SANDBOX}.
     *                                  The production Evernote service is {@link EvernoteService#HOST_PRODUCTION}
     * @param supportAppLinkedNotebooks true if you want to allow linked notebooks for
     *                                  applications which can only access a single notebook.
     * @return The EvernoteSession singleton instance.
     * @throws IllegalArgumentException
     * @deprecated Use the {@link Builder} instead and call {@link EvernoteSession#asSingleton()}.
     */
    @Deprecated
    public static EvernoteSession getInstance(Context ctx,
                                              String consumerKey,
                                              String consumerSecret,
                                              EvernoteService evernoteService,
                                              boolean supportAppLinkedNotebooks) {

        if (sInstance == null) {
            synchronized (EvernoteSession.class) {
                if (sInstance == null) {
                    new Builder(ctx)
                        .setEvernoteService(evernoteService)
                        .setSupportAppLinkedNotebooks(supportAppLinkedNotebooks)
                        .build(consumerKey, consumerSecret)
                        .asSingleton();
                }
            }
        }

        return sInstance;
    }

    private Context mApplicationContext;
    private String mConsumerKey;
    private String mConsumerSecret;
    private EvernoteService mEvernoteService;
    @SuppressWarnings("deprecation")private ClientFactory mClientFactory;
    private AuthenticationResult mAuthenticationResult;
    private boolean mSupportAppLinkedNotebooks;
    private boolean mForceAuthenticationInThirdPartyApp;
    private Locale mLocale;

    private EvernoteClientFactory.Builder mEvernoteClientFactoryBuilder;
    private ThreadLocal<EvernoteClientFactory> mFactoryThreadLocal;

    private EvernoteSession() {
        // do nothing, builder sets up everything
    }

    /**
     * @return the Bootstrap object to check for server host urls
     */
    protected EvernoteService getEvernoteService() {
        return mEvernoteService;
    }

    /**
     * Use this to create {@link AsyncNoteStoreClient} and {@link AsyncUserStoreClient}.
     * @deprecated Use {@link #getEvernoteClientFactory()} instead.
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    public ClientFactory getClientFactory() {
        return mClientFactory;
    }

    /**
     * Returns a factory to create various clients and helper objects to get access to the Evernote API.
     *
     * <br>
     * <br>
     *
     * The returned factory is <b>not thread safe</b> itself, however, the cached factory is a thread local
     * object. That means a new factory is created for each thread calling this method. The recommended
     * approach is to reuse worker threads to keep the number of created factories small.
     *
     * <br>
     * <br>
     *
     * With {@link #setEvernoteClientFactoryBuilder(EvernoteClientFactory.Builder)} you can exchange
     * the builder.
     *
     * @return A factory for this thread.
     */
    public synchronized EvernoteClientFactory getEvernoteClientFactory() {
        if (mFactoryThreadLocal == null) {
            mFactoryThreadLocal = new ThreadLocal<>();
        }
        if (mEvernoteClientFactoryBuilder == null) {
            mEvernoteClientFactoryBuilder = new EvernoteClientFactory.Builder(this);
        }

        EvernoteClientFactory factory = mFactoryThreadLocal.get();
        if (factory == null) {
            factory = mEvernoteClientFactoryBuilder.build();
            mFactoryThreadLocal.set(factory);
        }
        return factory;
    }

    /**
     * @param builder The new builder returning {@link EvernoteClientFactory}s in {@link #getEvernoteClientFactory()}.
     */
    public synchronized void setEvernoteClientFactoryBuilder(EvernoteClientFactory.Builder builder) {
        mEvernoteClientFactoryBuilder = EvernotePreconditions.checkNotNull(builder);
        mFactoryThreadLocal = null; // invalidate
    }

    /**
     * @return The application context for the running app.
     */
    public Context getApplicationContext() {
        return mApplicationContext;
    }

    /**
     * Get the authentication token that is used to make API calls
     * though a NoteStore.Client.
     *
     * @return the authentication token, or null if {@link #isLoggedIn()}
     * is false.
     */
    public String getAuthToken() {
        if (mAuthenticationResult != null) {
            return mAuthenticationResult.getAuthToken();
        } else {
            return null;
        }
    }

    /**
     * Get the authentication information returned by a successful
     * OAuth authentication to the Evernote web service.
     */
    public AuthenticationResult getAuthenticationResult() {
        return mAuthenticationResult;
    }

    /**
     * Recommended approach to authenticate the user. If the main Evernote app is installed and up to date,
     * the app is launched and authenticates the user. Otherwise the old OAuth process is launched and
     * the user needs to enter his credentials.
     *
     * <p/>
     *
     * Your {@link FragmentActivity} should implement {@link EvernoteLoginFragment.ResultCallback} to receive
     * the authentication result. Alternatively you can extend {@link EvernoteLoginFragment} and override
     * {@link EvernoteLoginFragment#onLoginFinished(boolean)}.
     *
     * @param activity The {@link FragmentActivity} holding the progress dialog.
     */
    public void authenticate(FragmentActivity activity) {
        authenticate(activity, EvernoteLoginFragment.create(mConsumerKey, mConsumerSecret, mSupportAppLinkedNotebooks, mLocale));
    }

    /**
     * @see EvernoteSession#authenticate(FragmentActivity)
     */
    public void authenticate(FragmentActivity activity, EvernoteLoginFragment fragment) {
        fragment.show(activity.getSupportFragmentManager(), EvernoteLoginFragment.TAG);
    }

    /**
     * Similar to {@link EvernoteSession#authenticate(FragmentActivity)}, but instead of opening a dialog
     * this method launches a separate {@link Activity}.
     *
     * <p/>
     *
     * The calling {@code activity} should override {@link Activity#onActivityResult(int, int, android.content.Intent)}. The {@code requestCode}
     * is {@link EvernoteSession#REQUEST_CODE_LOGIN}. The {@code resultCode} is either {@link Activity#RESULT_OK} or
     * {@link Activity#RESULT_CANCELED}.
     *
     * @param activity The {@link Activity} launching the {@link EvernoteLoginActivity}.
     */
    public void authenticate(Activity activity) {
        activity.startActivityForResult(EvernoteLoginActivity.createIntent(activity, mConsumerKey, mConsumerSecret, mSupportAppLinkedNotebooks, mLocale), REQUEST_CODE_LOGIN);
    }

    /**
     * Sets this session instance as singleton. After that you can use {@link #getInstance()} to get
     * this session.
     *
     * @return The same instance.
     */
    public EvernoteSession asSingleton() {
        sInstance = this;
        return this;
    }

    protected synchronized void setAuthenticationResult(AuthenticationResult authenticationResult) {
        mAuthenticationResult = authenticationResult;
    }

    /**
     * Check whether the session has valid authentication information
     * that will allow successful API calls to be made.
     */
    public synchronized boolean isLoggedIn() {
        return mAuthenticationResult != null;
    }

    /**
     * Clears all stored session information. If the user is not logged in, then this is a no-op.
     *
     * @return {@code true} if the user successfully logged out, {@code false} if the user wasn't
     * logged in.
     * @see #isLoggedIn()
     */
    public synchronized boolean logOut() {
        if (!isLoggedIn()) {
            return false;
        }

        mAuthenticationResult.clear();
        mAuthenticationResult = null;

        EvernoteUtil.removeAllCookies(getApplicationContext());
        return true;
    }

    /*package*/ boolean isForceAuthenticationInThirdPartyApp() {
        return mForceAuthenticationInThirdPartyApp;
    }

    /**
     * Evernote Service to use with the bootstrap profile detection.
     * Sandbox will return profiles referencing sandbox.evernote.com
     * Production will return evernote.com and app.yinxiang.com
     */
    public enum EvernoteService implements Parcelable {
        /**
         * References sandbox.evernote.com.
         */
        SANDBOX,

        /**
         * References evernote.com and app.yinxiang.com.
         */
        PRODUCTION;


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(final Parcel dest, final int flags) {
            dest.writeInt(ordinal());
        }

        public static final Creator<EvernoteService> CREATOR = new Creator<EvernoteService>() {
            @Override
            public EvernoteService createFromParcel(final Parcel source) {
                return EvernoteService.values()[source.readInt()];
            }

            @Override
            public EvernoteService[] newArray(final int size) {
                return new EvernoteService[size];
            }
        };
    }

    /**
     * Builder class to construct an {@link EvernoteSession}.
     */
    public static class Builder {

        private final Context mContext;

        private EvernoteService mEvernoteService;
        private boolean mSupportAppLinkedNotebooks;
        private Locale mLocale;

        @Deprecated
        private String mUserAgent;
        @Deprecated
        private File mMessageCacheDir;

        private boolean mForceAuthenticationInThirdPartyApp;

        /**
         * @param context Any context. The session caches the application context.
         */
        public Builder(Context context) {
            EvernotePreconditions.checkNotNull(context);

            mContext = context.getApplicationContext();
            mSupportAppLinkedNotebooks = true;
            mEvernoteService = EvernoteService.SANDBOX;
            //noinspection deprecation
            mUserAgent = EvernoteUtil.generateUserAgentString(mContext);
            //noinspection deprecation
            mMessageCacheDir = mContext.getFilesDir();

            mLocale = Locale.getDefault();
        }

        /**
         * Default is {@link EvernoteService#SANDBOX}. You need to exchange the value for your
         * production app.
         *
         * @param evernoteService The desired service.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setEvernoteService(EvernoteService evernoteService) {
            mEvernoteService = EvernotePreconditions.checkNotNull(evernoteService);
            return this;
        }

        /**
         * Default is {@code true}.
         *
         * @param supportAppLinkedNotebooks {@code true} if app linked notebooks are supported.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setSupportAppLinkedNotebooks(boolean supportAppLinkedNotebooks) {
            mSupportAppLinkedNotebooks = supportAppLinkedNotebooks;
            return this;
        }

        /**
         * Default is {@code false}.
         *
         * @param forceAuthenticationInThirdPartyApp {@code true} if the authentication should be
         *                                           launched in the third party app and not in the
         *                                           main Evernote app.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setForceAuthenticationInThirdPartyApp(boolean forceAuthenticationInThirdPartyApp) {
            mForceAuthenticationInThirdPartyApp = forceAuthenticationInThirdPartyApp;
            return this;
        }

        /**
         * The parameter is used to find the appropriate Evernote server, which can be with
         * {@link EvernoteService#PRODUCTION} either {@link #HOST_PRODUCTION} or {@link #HOST_CHINA}.
         * China is only used if the locale is Chinese, e.g. {@link Locale#SIMPLIFIED_CHINESE}.
         *
         * <br>
         * <br>
         *
         * Usually you don't want change this value. But for testing purposes it makes sense to switch
         * to a Chinese locale and to test that your app works for Chinese users.
         *
         * <br>
         * <br>
         *
         * The default value is {@link Locale#getDefault()}.
         *
         * @param locale The new locale used the fetch the bootstrap profiles. {@code null} is not allowed.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setLocale(Locale locale) {
            mLocale = EvernotePreconditions.checkNotNull(locale);
            return this;
        }

        @SuppressWarnings("deprecation")
        @Deprecated
        private Builder setUserAgent(String userAgent) {
            // maybe set this to public
            mUserAgent = userAgent;
            return this;
        }

        @SuppressWarnings("deprecation")
        @Deprecated
        private Builder setMessageCacheDir(File messageCacheDir) {
            mMessageCacheDir = messageCacheDir;
            return this;
        }

        /**
         * Creates a new instance with this consumer key and secret pair.
         *
         * @param consumerKey Your consumer key.
         * @param consumerSecret Your consumer secret.
         * @return The new created session. Call {@link #asSingleton()} to make reuse the session in the SDK.
         */
        public EvernoteSession build(String consumerKey, String consumerSecret) {
            EvernoteSession evernoteSession = new EvernoteSession();
            evernoteSession.mConsumerKey = EvernotePreconditions.checkNotEmpty(consumerKey);
            evernoteSession.mConsumerSecret = EvernotePreconditions.checkNotEmpty(consumerSecret);
            evernoteSession.mAuthenticationResult = AuthenticationResult.fromPreferences(mContext);

            return build(evernoteSession);
        }

        /**
         * Creates a session only for your personal account. Use this with the production environment.
         *
         * @param developerToken Your personal developer token.
         * @param noteStoreUrl The note store url of your Evernote account.
         * @return The new created session. Call {@link #asSingleton()} to make reuse the session in the SDK.
         */
        public EvernoteSession buildForSingleUser(String developerToken, String noteStoreUrl) {
            EvernoteSession evernoteSession = new EvernoteSession();
            evernoteSession.mAuthenticationResult = new AuthenticationResult(EvernotePreconditions.checkNotEmpty(developerToken),
                EvernotePreconditions.checkNotEmpty(noteStoreUrl), mSupportAppLinkedNotebooks);

            return build(evernoteSession);
        }

        private EvernoteSession build(EvernoteSession session) {
            session.mApplicationContext = mContext;
            session.mLocale = mLocale;
            session.mSupportAppLinkedNotebooks = mSupportAppLinkedNotebooks;
            //noinspection deprecation
            session.mClientFactory = new ClientFactory(mUserAgent, mMessageCacheDir);
            session.mEvernoteService = mEvernoteService;
            session.mForceAuthenticationInThirdPartyApp = mForceAuthenticationInThirdPartyApp;
            return session;
        }
    }
}
