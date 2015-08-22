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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Looper;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;

import com.evernote.client.android.helper.Cat;
import com.evernote.edam.type.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("unused")
public final class EvernoteUtil {

    private static final Cat CAT = new Cat("EvernoteUtil");

    private EvernoteUtil() {

    }

    /**
     * Action for an {@link Intent} to authorize this app.
     */
    public static final String ACTION_AUTHORIZE = "com.evernote.action.AUTHORIZE";

    /**
     * Action for an {@link Intent} to receive the bootstrap profile name from the main Evernote app.
     */
    public static final String ACTION_GET_BOOTSTRAP_PROFILE_NAME = "com.evernote.action.GET_BOOTSTRAP_PROFILE_NAME";

    /**
     * Extra URL to authorize this app.
     */
    public static final String EXTRA_AUTHORIZATION_URL = "authorization_url";

    /**
     * Returned OAuth callback from the main Evernote app.
     */
    public static final String EXTRA_OAUTH_CALLBACK_URL = "oauth_callback_url";

    /**
     * Returned bootstrap profile name from the main Evernote app.
     */
    public static final String EXTRA_BOOTSTRAP_PROFILE_NAME = "bootstrap_profile_name";

    /**
     * The ENML preamble to every Evernote note.
     * Note content goes between <en-note> and </en-note>
     */
    public static final String NOTE_PREFIX =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">" + "<en-note>";

    /**
     * The ENML postamble to every Evernote note.
     */
    public static final String NOTE_SUFFIX = "</en-note>";

    /**
     * One-way hashing function used for providing a checksum of EDAM data.
     */
    private static final String EDAM_HASH_ALGORITHM = "MD5";

    private static final MessageDigest HASH_DIGEST;

    private static final String PACKAGE_NAME = "com.evernote";

    static {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // notify in hash method
            messageDigest = null;
        }
        HASH_DIGEST = messageDigest;
    }

    /**
     * Create an ENML &lt;en-media&gt; tag for the specified Resource object.
     */
    public static String createEnMediaTag(Resource resource) {
        return "<en-media hash=\"" + bytesToHex(resource.getData().getBodyHash()) + "\" type=\"" + resource.getMime() + "\"/>";
    }

    /**
     * Returns an MD5 checksum of the provided array of bytes.
     */
    public static byte[] hash(byte[] body) {
        if (HASH_DIGEST != null) {
            return HASH_DIGEST.digest(body);
        } else {
            throw new EvernoteUtilException(EDAM_HASH_ALGORITHM + " not supported", new NoSuchAlgorithmException(EDAM_HASH_ALGORITHM));
        }
    }

    /**
     * Returns an MD5 checksum of the contents of the provided InputStream.
     */
    public static byte[] hash(InputStream in) throws IOException {
        if (HASH_DIGEST == null) {
            throw new EvernoteUtilException(EDAM_HASH_ALGORITHM + " not supported", new NoSuchAlgorithmException(EDAM_HASH_ALGORITHM));
        }

        byte[] buf = new byte[1024];
        int n;
        while ((n = in.read(buf)) != -1) {
            HASH_DIGEST.update(buf, 0, n);
        }
        return HASH_DIGEST.digest();
    }

    /**
     * Converts the provided byte array into a hexadecimal string
     * with two characters per byte.
     */
    public static String bytesToHex(byte[] bytes) {
        return bytesToHex(bytes, false);
    }

    /**
     * Takes the provided byte array and converts it into a hexadecimal string
     * with two characters per byte.
     *
     * @param withSpaces if true, include a space character between each hex-rendered
     *                   byte for readability.
     */
    public static String bytesToHex(byte[] bytes, boolean withSpaces) {
        StringBuilder sb = new StringBuilder();
        for (byte hashByte : bytes) {
            int intVal = 0xff & hashByte;
            if (intVal < 0x10) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(intVal));
            if (withSpaces) {
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    /**
     * Takes a string in hexadecimal format and converts it to a binary byte
     * array. This does no checking of the format of the input, so this should
     * only be used after confirming the format or origin of the string. The input
     * string should only contain the hex data, two characters per byte.
     */
    public static byte[] hexToBytes(String hexString) {
        byte[] result = new byte[hexString.length() / 2];
        for (int i = 0; i < result.length; ++i) {
            int offset = i * 2;
            result[i] = (byte) Integer.parseInt(hexString.substring(offset,
                offset + 2), 16);
        }
        return result;
    }

    /**
     * Removes all cookies for this application.
     */
    public static void removeAllCookies(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            removeAllCookiesV21();
        } else {
            removeAllCookiesV14(context.getApplicationContext());
        }
    }

    @SuppressWarnings("deprecation")
    private static void removeAllCookiesV14(Context context) {
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void removeAllCookiesV21() {
        final CookieManager cookieManager = CookieManager.getInstance();

        Looper looper = Looper.myLooper();
        boolean prepared = false;
        if (looper == null) {
            Looper.prepare();
            prepared = true;
        }

        // requires a looper
        cookieManager.removeAllCookies(new ValueCallback<Boolean>() {
            @Override
            public void onReceiveValue(Boolean value) {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        // is synchronous, run in background
                        cookieManager.flush();
                    }
                };
                thread.start();
            }
        });

        if (prepared) {
            looper = Looper.myLooper();
            if (looper != null) {
                looper.quit();
            }
        }
    }

    /**
     * Checks if Evernote is installed and if the app can resolve this action.
     */
    public static EvernoteInstallStatus getEvernoteInstallStatus(Context context, String action) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(action).setPackage(PACKAGE_NAME);

        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (!resolveInfos.isEmpty()) {
            return EvernoteInstallStatus.INSTALLED;
        }

        try {
            // authentication feature not available, yet
            packageManager.getPackageInfo(PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            return EvernoteInstallStatus.OLD_VERSION;

        } catch (Exception e) {
            return EvernoteInstallStatus.NOT_INSTALLED;
        }
    }

    /**
     * Creates an {@link Intent} to authorize this app by the main Evernote app.
     *
     * @param context The {@link Context} starting the {@link Intent}.
     * @param authorizationUrl The OAuth authorization URL.
     * @param forceThirdPartyApp If {@code true}, never use Evernote app to authenticate user.
     * @return The {@link Intent}.
     */
    public static Intent createAuthorizationIntent(Context context, String authorizationUrl, boolean forceThirdPartyApp) {
        Intent intent;

        if (!forceThirdPartyApp && EvernoteInstallStatus.INSTALLED.equals(getEvernoteInstallStatus(context, ACTION_AUTHORIZE))) {
            intent = new Intent(ACTION_AUTHORIZE);
            intent.setPackage(PACKAGE_NAME);
        } else {
            intent = new Intent(context, EvernoteOAuthActivity.class);
        }

        intent.putExtra(EXTRA_AUTHORIZATION_URL, authorizationUrl);
        return intent;
    }

    /**
     * Returns an Intent to query the bootstrap profile name from the main Evernote app. This is useful
     * if you want to use the main app to authenticate the user and he is already signed in.
     *
     * @param context The {@link Context} starting the {@link Intent}.
     * @param evernoteSession The current session.
     * @return An Intent to query the bootstrap profile name. Returns {@code null}, if the main app
     * is not installed, not up to date or you do not want to use the main app to authenticate the
     * user.
     */
    public static Intent createGetBootstrapProfileNameIntent(Context context, EvernoteSession evernoteSession) {
        if (evernoteSession.isForceAuthenticationInThirdPartyApp()) {
            // we don't want to use the main app, return null
            return null;
        }

        EvernoteUtil.EvernoteInstallStatus installStatus = EvernoteUtil.getEvernoteInstallStatus(context, EvernoteUtil.ACTION_GET_BOOTSTRAP_PROFILE_NAME);
        if (!EvernoteUtil.EvernoteInstallStatus.INSTALLED.equals(installStatus)) {
            return null;
        }

        return new Intent(EvernoteUtil.ACTION_GET_BOOTSTRAP_PROFILE_NAME).setPackage(PACKAGE_NAME);
    }

    /**
     * Construct a user-agent string based on the running application and
     * the device and operating system information. This information is
     * included in HTTP requests made to the Evernote service and assists
     * in measuring traffic and diagnosing problems.
     */
    public static String generateUserAgentString(Context ctx) {
        String packageName = null;
        int packageVersion = 0;
        try {
            packageName = ctx.getPackageName();
            packageVersion = ctx.getPackageManager().getPackageInfo(packageName, 0).versionCode;

        } catch (PackageManager.NameNotFoundException e) {
            CAT.e(e.getMessage());
        }

        String userAgent = packageName + " Android/" + packageVersion;

        Locale locale = java.util.Locale.getDefault();
        if (locale == null) {
            userAgent += " (" + Locale.US + ");";
        } else {
            userAgent += " (" + locale.toString() + "); ";
        }
        userAgent += "Android/" + Build.VERSION.RELEASE + "; ";
        userAgent += Build.MODEL + "/" + Build.VERSION.SDK_INT + ";";
        return userAgent;
    }

    public enum EvernoteInstallStatus {
        INSTALLED,
        OLD_VERSION,
        NOT_INSTALLED
    }

    /**
     * A runtime exception that will be thrown when we hit an error that should
     * "never" occur ... e.g. if the JVM doesn't know about UTF-8 or MD5.
     */
    @SuppressWarnings("serial")
    private static final class EvernoteUtilException extends RuntimeException {
        public EvernoteUtilException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
