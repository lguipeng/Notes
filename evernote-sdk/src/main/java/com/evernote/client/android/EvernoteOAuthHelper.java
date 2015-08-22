package com.evernote.client.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.evernote.client.android.helper.Cat;
import com.evernote.client.android.helper.EvernotePreconditions;
import com.evernote.client.oauth.YinxiangApi;
import com.evernote.edam.userstore.BootstrapInfo;
import com.evernote.edam.userstore.BootstrapProfile;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.Api;
import org.scribe.builder.api.EvernoteApi;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.OAuthEncoder;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A helper class to handle OAuth requests.
 *
 * @author rwondratschek
 */
@SuppressWarnings("UnusedDeclaration")
public class EvernoteOAuthHelper {

    /**
     * Server matched name for BootstrapProfile that matches china.
     */
    public static final String CHINA_PROFILE_NAME = "Evernote-China";

    protected static final String CALLBACK_SCHEME = "en-oauth";
    protected static final Cat CAT = new Cat("OAuthHelper");

    protected static final Pattern NOTE_STORE_REGEX = Pattern.compile("edam_noteStoreUrl=([^&]+)");
    protected static final Pattern WEB_API_REGEX = Pattern.compile("edam_webApiUrlPrefix=([^&]+)");
    protected static final Pattern USER_ID_REGEX = Pattern.compile("edam_userId=([^&]+)");

    protected final EvernoteSession mSession;
    protected final String mConsumerKey;
    protected final String mConsumerSecret;
    protected final boolean mSupportAppLinkedNotebooks;
    protected final Locale mLocale;

    protected BootstrapProfile mBootstrapProfile;
    protected OAuthService mOAuthService;

    protected Token mRequestToken;

    public EvernoteOAuthHelper(EvernoteSession session, String consumerKey, String consumerSecret, boolean supportAppLinkedNotebooks) {
        this(session, consumerKey, consumerSecret, supportAppLinkedNotebooks, Locale.getDefault());
    }

    public EvernoteOAuthHelper(EvernoteSession session, String consumerKey, String consumerSecret, boolean supportAppLinkedNotebooks, Locale locale) {
        mSession = EvernotePreconditions.checkNotNull(session);
        mConsumerKey = EvernotePreconditions.checkNotEmpty(consumerKey);
        mConsumerSecret = EvernotePreconditions.checkNotEmpty(consumerSecret);
        mSupportAppLinkedNotebooks = supportAppLinkedNotebooks;
        mLocale = EvernotePreconditions.checkNotNull(locale);
    }

    public List<BootstrapProfile> fetchBootstrapProfiles() throws Exception {
        //Network request
        BootstrapManager.BootstrapInfoWrapper infoWrapper = new BootstrapManager(mSession.getEvernoteService(), mSession, mLocale).getBootstrapInfo();
        if (infoWrapper == null) {
            return null;
        }

        BootstrapInfo info = infoWrapper.getBootstrapInfo();
        if (info == null) {
            return null;
        }

        return info.getProfiles();
    }

    public BootstrapProfile getDefaultBootstrapProfile(List<BootstrapProfile> bootstrapProfiles) {
        EvernotePreconditions.checkCollectionNotEmpty(bootstrapProfiles, "bootstrapProfiles");

        // return the first in the list, this is the preferred profile from the server
        return bootstrapProfiles.get(0);
    }

    public void setBootstrapProfile(BootstrapProfile bootstrapProfile) {
        mBootstrapProfile = EvernotePreconditions.checkNotNull(bootstrapProfile);
    }

    public void initialize() throws Exception {
        if (mBootstrapProfile == null) {
            List<BootstrapProfile> bootstrapProfiles = fetchBootstrapProfiles();
            setBootstrapProfile(getDefaultBootstrapProfile(bootstrapProfiles));
        }

        mOAuthService = createOAuthService(mBootstrapProfile, mConsumerKey, mConsumerSecret);
    }

    public Token createRequestToken() {
        mRequestToken = mOAuthService.getRequestToken();
        return mRequestToken;
    }

    public String createAuthorizationUrl(Token requestToken) {
        String url = mOAuthService.getAuthorizationUrl(requestToken);
        if (mSupportAppLinkedNotebooks) {
            url += "&supportLinkedSandbox=true";
        }

        return url;
    }

    public Intent startAuthorization(Activity activity) {
        try {
            initialize();
        } catch (Exception e) {
            CAT.e(e);
            return null;
        }

        createRequestToken();
        String authorizationUrl = createAuthorizationUrl(mRequestToken);
        return EvernoteUtil.createAuthorizationIntent(activity, authorizationUrl, mSession.isForceAuthenticationInThirdPartyApp());
    }

    public boolean finishAuthorization(Activity activity, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK || data == null) {
            return false;
        }

        String url = data.getStringExtra(EvernoteUtil.EXTRA_OAUTH_CALLBACK_URL);
        if (TextUtils.isEmpty(url)) {
            return false;
        }

        Uri uri = Uri.parse(url);

        String verifierString = uri.getQueryParameter("oauth_verifier");
        String appLnbString = uri.getQueryParameter("sandbox_lnb");
        boolean isAppLinkedNotebook = !TextUtils.isEmpty(appLnbString) && "true".equalsIgnoreCase(appLnbString);

        if (TextUtils.isEmpty(verifierString)) {
            CAT.i("User did not authorize access");
            return false;
        }

        Verifier verifier = new Verifier(verifierString);
        try {
            Token accessToken = mOAuthService.getAccessToken(mRequestToken, verifier);
            String rawResponse = accessToken.getRawResponse();

            String authToken = accessToken.getToken();
            String noteStoreUrl = extract(rawResponse, NOTE_STORE_REGEX);
            String webApiUrlPrefix = extract(rawResponse, WEB_API_REGEX);
            int userId = Integer.parseInt(extract(rawResponse, USER_ID_REGEX));

            String evernoteHost = mBootstrapProfile.getSettings().getServiceHost();

            AuthenticationResult authenticationResult = new AuthenticationResult(authToken, noteStoreUrl, webApiUrlPrefix, evernoteHost, userId, isAppLinkedNotebook);
            authenticationResult.persist();
            mSession.setAuthenticationResult(authenticationResult);
            return true;

        } catch (Exception e) {
            CAT.e("Failed to obtain OAuth access token", e);
        }

        return false;
    }

    protected static OAuthService createOAuthService(BootstrapProfile bootstrapProfile, String consumerKey, String consumerSecret) {
        String host = bootstrapProfile.getSettings().getServiceHost();
        if (host == null) {
            return null;
        }

        Uri uri = new Uri.Builder()
            .authority(host)
            .scheme("https")
            .build();

        Class<? extends Api> apiClass;
        switch (uri.toString()) {
            case EvernoteSession.HOST_SANDBOX:
                apiClass = EvernoteApi.Sandbox.class;
                break;

            case EvernoteSession.HOST_PRODUCTION:
                apiClass = EvernoteApi.class;
                break;

            case EvernoteSession.HOST_CHINA:
                apiClass = YinxiangApi.class;
                break;

            default:
                throw new IllegalArgumentException("Unsupported Evernote host: " + host);
        }

        return new ServiceBuilder()
            .provider(apiClass)
            .apiKey(consumerKey)
            .apiSecret(consumerSecret)
            .callback(CALLBACK_SCHEME + "://callback")
            .build();
    }

    private static String extract(String response, Pattern p) {
        Matcher matcher = p.matcher(response);
        if (matcher.find() && matcher.groupCount() >= 1) {
            return OAuthEncoder.decode(matcher.group(1));
        } else {
            throw new OAuthException("Response body is incorrect. Can't extract token and secret from this: " + response);
        }
    }
}
