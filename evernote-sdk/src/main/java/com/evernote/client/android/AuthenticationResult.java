/*
 * Copyright 2012 Evernote Corporation
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

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;

/**
 * A container class for the results of a successful OAuth authorization with
 * the Evernote service.
 *
 * @author @tylersmithnet
 * @author rwondratschek
 */
@SuppressWarnings("unused")
public class AuthenticationResult {

    private static final String KEY_AUTH_TOKEN = "evernote.mAuthToken";
    private static final String KEY_NOTESTORE_URL = "evernote.notestoreUrl";
    private static final String KEY_WEB_API_URL_PREFIX = "evernote.webApiUrlPrefix";
    private static final String KEY_USER_ID = "evernote.userId";
    private static final String KEY_EVERNOTE_HOST = "evernote.mEvernoteHost";
    private static final String KEY_IS_APP_LINKED_NOTEBOOK = "evernote.isAppLinkedNotebook";

    private static final String PREFERENCE_NAME = "evernote.preferences";

    /*package*/ static AuthenticationResult fromPreferences(Context context) {
        SharedPreferences prefs = getPreferences(context);
        String authToken = prefs.getString(KEY_AUTH_TOKEN, null);
        String noteStoreUrl = prefs.getString(KEY_NOTESTORE_URL, null);

        if (TextUtils.isEmpty(authToken) || TextUtils.isEmpty(noteStoreUrl)) {
            return null;
        }

        return new AuthenticationResult(authToken, noteStoreUrl,
                prefs.getString(KEY_WEB_API_URL_PREFIX, null),
                prefs.getString(KEY_EVERNOTE_HOST, null),
                prefs.getInt(KEY_USER_ID, -1),
                prefs.getBoolean(KEY_IS_APP_LINKED_NOTEBOOK, false));
    }

    private String mAuthToken;
    private String mNoteStoreUrl;
    private String mWebApiUrlPrefix;
    private String mEvernoteHost;

    private int mUserId;
    private boolean mIsAppLinkedNotebook;

    /*package*/ AuthenticationResult(String authToken, String noteStoreUrl, boolean isAppLinkedNotebook) {
        this(authToken, noteStoreUrl, parseWebApiUrlPrefix(noteStoreUrl), parseHost(noteStoreUrl), -1, isAppLinkedNotebook);
    }

    /*package*/ AuthenticationResult(String authToken, String noteStoreUrl, String webApiUrlPrefix, String evernoteHost, int userId, boolean isAppLinkedNotebook) {
        mAuthToken = authToken;
        mNoteStoreUrl = noteStoreUrl;
        mWebApiUrlPrefix = webApiUrlPrefix;
        mEvernoteHost = evernoteHost;
        mUserId = userId;
        mIsAppLinkedNotebook = isAppLinkedNotebook;
    }

    /*package */ void persist() {
        getPreferences(EvernoteSession.getInstance().getApplicationContext())
                .edit()
                .putString(KEY_AUTH_TOKEN, mAuthToken)
                .putString(KEY_NOTESTORE_URL, mNoteStoreUrl)
                .putString(KEY_WEB_API_URL_PREFIX, mWebApiUrlPrefix)
                .putString(KEY_EVERNOTE_HOST, mEvernoteHost)
                .putInt(KEY_USER_ID, mUserId)
                .putBoolean(KEY_IS_APP_LINKED_NOTEBOOK, mIsAppLinkedNotebook)
                .apply();
    }

    /*package*/ void clear() {
        getPreferences(EvernoteSession.getInstance().getApplicationContext())
                .edit()
                .remove(KEY_AUTH_TOKEN)
                .remove(KEY_NOTESTORE_URL)
                .remove(KEY_WEB_API_URL_PREFIX)
                .remove(KEY_EVERNOTE_HOST)
                .remove(KEY_USER_ID)
                .remove(KEY_IS_APP_LINKED_NOTEBOOK)
                .apply();
    }

    /**
     * @return The authentication token that will be used to make authenticated API requests.
     */
    public String getAuthToken() {
        return mAuthToken;
    }

    /**
     * @return The URL that will be used to access the NoteStore service.
     */
    public String getNoteStoreUrl() {
        return mNoteStoreUrl;
    }

    /**
     * @return The URL prefix that can be used to access non-Thrift API endpoints.
     */
    public String getWebApiUrlPrefix() {
        return mWebApiUrlPrefix;
    }

    /**
     * @return The Evernote Web URL provided from the bootstrap process
     */
    public String getEvernoteHost() {
        return mEvernoteHost;
    }

    /**
     * @return The numeric user ID of the user who authorized access to their Evernote account.
     */
    public int getUserId() {
        return mUserId;
    }

    /**
     * @return Indicates whether this account is limited to accessing a single notebook, and
     * that notebook is a linked notebook
     */
    public boolean isAppLinkedNotebook() {
        return mIsAppLinkedNotebook;
    }

    protected static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    private static String parseWebApiUrlPrefix(String noteStoreUrl) {
        int index = noteStoreUrl.indexOf("notestore");
        if (index > 0) {
            return noteStoreUrl.substring(0, index);
        } else {
            return noteStoreUrl;
        }
    }

    private static String parseHost(String noteStoreUrl) {
        return Uri.parse(noteStoreUrl).getHost();
    }
}
