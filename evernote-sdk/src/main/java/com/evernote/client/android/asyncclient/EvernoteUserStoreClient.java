package com.evernote.client.android.asyncclient;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.client.android.helper.EvernotePreconditions;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.type.PremiumInfo;
import com.evernote.edam.type.User;
import com.evernote.edam.userstore.AuthenticationResult;
import com.evernote.edam.userstore.BootstrapInfo;
import com.evernote.edam.userstore.PublicUserInfo;
import com.evernote.edam.userstore.UserStore;
import com.evernote.thrift.TException;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * An async wrapper for {@link UserStore.Client}. Call {@link EvernoteClientFactory#getUserStoreClient()}
 * to get an instance.
 *
 * @author rwondratschek
 * @see UserStore
 * @see UserStore.Client
 */
@SuppressWarnings("unused")
public class EvernoteUserStoreClient extends EvernoteAsyncClient {

    private final UserStore.Client mClient;
    private final String mAuthenticationToken;

    /*package*/ EvernoteUserStoreClient(@NonNull UserStore.Client client, @Nullable String authenticationToken, @NonNull ExecutorService executorService) {
        super(executorService);
        mClient = EvernotePreconditions.checkNotNull(client);
        mAuthenticationToken = authenticationToken;
    }

    public boolean checkVersion(String clientName, short edamVersionMajor, short edamVersionMinor) throws TException {
        return mClient.checkVersion(clientName, edamVersionMajor, edamVersionMinor);
    }

    public Future<Boolean> checkVersionAsync(final String clientName, final short edamVersionMajor, final short edamVersionMinor, EvernoteCallback<Boolean> callback) {
        return submitTask(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return checkVersion(clientName, edamVersionMajor, edamVersionMinor);
            }
        }, callback);
    }

    public BootstrapInfo getBootstrapInfo(String locale) throws TException {
        return mClient.getBootstrapInfo(locale);
    }

    public Future<BootstrapInfo> getBootstrapInfoAsync(final String locale, EvernoteCallback<BootstrapInfo> callback) {
        return submitTask(new Callable<BootstrapInfo>() {
            @Override
            public BootstrapInfo call() throws Exception {
                return getBootstrapInfo(locale);
            }
        }, callback);
    }

    public AuthenticationResult authenticate(String username, String password, String consumerKey, String consumerSecret,
                                             boolean supportsTwoFactor) throws EDAMUserException, EDAMSystemException, TException {

        return mClient.authenticate(username, password, consumerKey, consumerSecret, supportsTwoFactor);
    }

    public Future<AuthenticationResult> authenticateAsync(final String username, final String password, final String consumerKey,
                                                          final String consumerSecret, final boolean supportsTwoFactor, EvernoteCallback<AuthenticationResult> callback) {

        return submitTask(new Callable<AuthenticationResult>() {
            @Override
            public AuthenticationResult call() throws Exception {
                return authenticate(username, password, consumerKey, consumerSecret, supportsTwoFactor);
            }
        }, callback);
    }

    public AuthenticationResult authenticateLongSession(String username, String password, String consumerKey, String consumerSecret, String deviceIdentifier,
                                                        String deviceDescription, boolean supportsTwoFactor)
            throws EDAMUserException, EDAMSystemException, TException {

        return mClient.authenticateLongSession(username, password, consumerKey, consumerSecret, deviceIdentifier, deviceDescription, supportsTwoFactor);
    }

    public Future<AuthenticationResult> authenticateLongSessionAsync(final String username, final String password, final String consumerKey, final String consumerSecret,
                                                                     final String deviceIdentifier, final String deviceDescription, final boolean supportsTwoFactor,
                                                                     EvernoteCallback<AuthenticationResult> callback) {

        return submitTask(new Callable<AuthenticationResult>() {
            @Override
            public AuthenticationResult call() throws Exception {
                return authenticateLongSession(username, password, consumerKey, consumerSecret, deviceIdentifier, deviceDescription, supportsTwoFactor);
            }
        }, callback);
    }

    public AuthenticationResult completeTwoFactorAuthentication(String authenticationToken, String oneTimeCode, String deviceIdentifier,
                                                                String deviceDescription) throws EDAMUserException, EDAMSystemException, TException {

        return mClient.completeTwoFactorAuthentication(authenticationToken, oneTimeCode, deviceIdentifier, deviceDescription);
    }

    public Future<AuthenticationResult> completeTwoFactorAuthenticationAsync(final String authenticationToken, final String oneTimeCode,
                                                                             final String deviceIdentifier, final String deviceDescription,
                                                                             EvernoteCallback<AuthenticationResult> callback) {

        return submitTask(new Callable<AuthenticationResult>() {
            @Override
            public AuthenticationResult call() throws Exception {
                return completeTwoFactorAuthentication(authenticationToken, oneTimeCode, deviceIdentifier, deviceDescription);
            }
        }, callback);
    }

    public void revokeLongSession() throws EDAMUserException, EDAMSystemException, TException {
        mClient.revokeLongSession(mAuthenticationToken);
    }

    public Future<Void> revokeLongSessionAsync(EvernoteCallback<Void> evernoteCallback) {
        return submitTask(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                revokeLongSession();
                return null;
            }
        }, evernoteCallback);
    }

    public AuthenticationResult authenticateToBusiness() throws EDAMUserException, EDAMSystemException, TException {
        return mClient.authenticateToBusiness(mAuthenticationToken);
    }

    public Future<AuthenticationResult> authenticateToBusinessAsync(EvernoteCallback<AuthenticationResult> callback) {
        return submitTask(new Callable<AuthenticationResult>() {
            @Override
            public AuthenticationResult call() throws Exception {
                return authenticateToBusiness();
            }
        }, callback);
    }

    public AuthenticationResult refreshAuthentication() throws EDAMUserException, EDAMSystemException, TException {
        return mClient.refreshAuthentication(mAuthenticationToken);
    }

    public Future<AuthenticationResult> refreshAuthenticationAsync(EvernoteCallback<AuthenticationResult> callback) {
        return submitTask(new Callable<AuthenticationResult>() {
            @Override
            public AuthenticationResult call() throws Exception {
                return refreshAuthentication();
            }
        }, callback);
    }

    public User getUser() throws EDAMUserException, EDAMSystemException, TException {
        return mClient.getUser(mAuthenticationToken);
    }

    public Future<User> getUserAsync(EvernoteCallback<User> callback) {
        return submitTask(new Callable<User>() {
            @Override
            public User call() throws Exception {
                return getUser();
            }
        }, callback);
    }

    public PublicUserInfo getPublicUserInfo(String username) throws EDAMNotFoundException, EDAMSystemException, EDAMUserException, TException {
        return mClient.getPublicUserInfo(username);
    }

    public Future<PublicUserInfo> getPublicUserInfoAsync(final String username, EvernoteCallback<PublicUserInfo> callback) {
        return submitTask(new Callable<PublicUserInfo>() {
            @Override
            public PublicUserInfo call() throws Exception {
                return getPublicUserInfo(username);
            }
        }, callback);
    }

    public PremiumInfo getPremiumInfo() throws EDAMUserException, EDAMSystemException, TException {
        return mClient.getPremiumInfo(mAuthenticationToken);
    }

    public Future<PremiumInfo> getPremiumInfoAsync(EvernoteCallback<PremiumInfo> callback) {
        return submitTask(new Callable<PremiumInfo>() {
            @Override
            public PremiumInfo call() throws Exception {
                return getPremiumInfo();
            }
        }, callback);
    }

    public String getNoteStoreUrl() throws EDAMUserException, EDAMSystemException, TException {
        return mClient.getNoteStoreUrl(mAuthenticationToken);
    }

    public Future<String> getNoteStoreUrlAsync(EvernoteCallback<String> callback) {
        return submitTask(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return getNoteStoreUrl();
            }
        }, callback);
    }

    public boolean isBusinessUser() throws TException, EDAMUserException, EDAMSystemException {
        return getUser().getAccounting().isSetBusinessId();
    }

    public Future<Boolean> isBusinessUserAsync(EvernoteCallback<Boolean> callback) {
        return submitTask(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return isBusinessUser();
            }
        }, callback);
    }
}
