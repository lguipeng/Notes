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

import android.util.Log;

import com.evernote.client.android.asyncclient.EvernoteUserStoreClient;
import com.evernote.edam.userstore.BootstrapInfo;
import com.evernote.edam.userstore.BootstrapProfile;
import com.evernote.thrift.TException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * A class that provides access to check the current
 * {@link com.evernote.edam.userstore.Constants#EDAM_VERSION_MAJOR} and
 * the {@link com.evernote.edam.userstore.Constants#EDAM_VERSION_MINOR} against the Evernote Web serivice for API
 * Compatibility
 *
 * It provides access to the {@link List} of {@link BootstrapProfile} representing the possible server connections
 * for the user.  This list must be requested from the server on any type of authentication attempt
 *
 * @author @tylersmithnet
 */
@SuppressWarnings({"JavaDoc", "unused"})
/*package*/ class BootstrapManager {

  private static final String LOGTAG = "EvernoteSession";

  /**
   * List of locales that match china.
   */
  public static final List<Locale> CHINA_LOCALES = Collections.unmodifiableList(Arrays.asList(
          Locale.TRADITIONAL_CHINESE,
          Locale.CHINESE,
          Locale.CHINA,
          Locale.SIMPLIFIED_CHINESE
  ));

  private ArrayList<String> mBootstrapServerUrls = new ArrayList<>();
  private Locale mLocale;
  private String mBootstrapServerUsed;
  private final EvernoteSession mEvernoteSession;

  /**
   * package-scope constructor.
   */
  BootstrapManager(EvernoteSession session) {
    this(session.getEvernoteService(), session);
  }

  BootstrapManager(EvernoteSession.EvernoteService service, EvernoteSession session) {
    this(service, session, Locale.getDefault());
  }

  /**
   * package-scope constructor.
   *
   * @param service {@link com.evernote.client.android.EvernoteSession.EvernoteService#PRODUCTION} when using
   * production and {@link com.evernote.client.android.EvernoteSession.EvernoteService#SANDBOX} when using sandbox
   * @param locale Used to detect if the china servers need to be checked
   */
  BootstrapManager(EvernoteSession.EvernoteService service, EvernoteSession session, Locale locale) {
    mEvernoteSession = session;
    mLocale = locale;

    mBootstrapServerUrls.clear();
    switch (service) {
      case PRODUCTION:
        if (CHINA_LOCALES.contains(mLocale)) {
          mBootstrapServerUrls.add(EvernoteSession.HOST_CHINA);
        }
        mBootstrapServerUrls.add(EvernoteSession.HOST_PRODUCTION);
        break;

      case SANDBOX:
        mBootstrapServerUrls.add(EvernoteSession.HOST_SANDBOX);
        break;
    }
  }

  /**
   * Initialized the User Store to check for supported version of the API.
   *
   * @throws ClientUnsupportedException on unsupported version
   * @throws Exception on generic errors
   */
  private void initializeUserStoreAndCheckVersion() throws Exception {

    int i = 0;
    String version = com.evernote.edam.userstore.Constants.EDAM_VERSION_MAJOR + "."
        + com.evernote.edam.userstore.Constants.EDAM_VERSION_MINOR;

    for (String url : mBootstrapServerUrls) {
      i++;
      try {
        EvernoteUserStoreClient userStoreClient = mEvernoteSession.getEvernoteClientFactory().getUserStoreClient(getUserStoreUrl(url), null);

        if (!userStoreClient.checkVersion(EvernoteUtil.generateUserAgentString(mEvernoteSession.getApplicationContext()),
                com.evernote.edam.userstore.Constants.EDAM_VERSION_MAJOR,
                com.evernote.edam.userstore.Constants.EDAM_VERSION_MINOR)) {
          throw new ClientUnsupportedException(version);
        }

        mBootstrapServerUsed = url;
        return;
      } catch (ClientUnsupportedException cue) {

        Log.e(LOGTAG, "Invalid Version", cue);
        throw cue;
      } catch (Exception e) {
        if (i < mBootstrapServerUrls.size()) {
          Log.e(LOGTAG, "Error contacting bootstrap server=" + url, e);
        } else {
          throw e;
        }
      }
    }
  }

  /**
   * Makes a web request to get the latest bootstrap information.
   * This is a requirement during the oauth process
   *
   * @return {@link BootstrapInfoWrapper}
   * @throws Exception
   */
  BootstrapInfoWrapper getBootstrapInfo() throws Exception {
    Log.d(LOGTAG, "getBootstrapInfo()");
    BootstrapInfo bsInfo = null;
    try {
      if (mBootstrapServerUsed == null) {
        initializeUserStoreAndCheckVersion();
      }

      bsInfo = mEvernoteSession.getEvernoteClientFactory().getUserStoreClient(getUserStoreUrl(mBootstrapServerUsed), null).getBootstrapInfo(mLocale.toString());
      printBootstrapInfo(bsInfo);

    } catch (TException e) {
      Log.e(LOGTAG, "error getting bootstrap info", e);
    }

    return new BootstrapInfoWrapper(mBootstrapServerUsed, bsInfo);
  }

  /**
   * Log the {@link BootstrapProfile} list.
   * @param bsInfo
   */
  void printBootstrapInfo(BootstrapInfo bsInfo) {
    if (bsInfo == null) return;

    Log.d(LOGTAG, "printBootstrapInfo");
    List<BootstrapProfile> profiles = bsInfo.getProfiles();
    if (profiles != null) {
      for (BootstrapProfile profile : profiles) {
        Log.d(LOGTAG, profile.toString());
      }
    } else {
      Log.d(LOGTAG, "Profiles are null");
    }
  }

  /**
   * Wrapper class to hold the Evernote API server URL and the {@link BootstrapProfile} object.
   */
  static class BootstrapInfoWrapper {
    private String mServerUrl;
    private BootstrapInfo mBootstrapInfo;

    BootstrapInfoWrapper(String serverUrl, BootstrapInfo info) {
      mServerUrl = serverUrl;
      mBootstrapInfo = info;
    }

    @SuppressWarnings("unused")
    String getServerUrl() {
      return mServerUrl;
    }

    BootstrapInfo getBootstrapInfo() {
      return mBootstrapInfo;
    }
  }

  public static class ClientUnsupportedException extends Exception {
    public ClientUnsupportedException(String version) {
      super("Client version " + version + " not supported.");
    }
  }

  private String getUserStoreUrl(String bootstrapServer) {
    return bootstrapServer + "/edam/user";
  }
}
