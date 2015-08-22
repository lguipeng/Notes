package com.evernote.client.android.login;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.evernote.client.android.EvernoteOAuthHelper;
import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.EvernoteUtil;
import com.evernote.client.android.helper.Cat;
import com.evernote.edam.userstore.BootstrapProfile;

import net.vrallev.android.task.Task;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author rwondratschek
 */
/*package*/ class EvernoteLoginTask extends Task<Boolean> {

    private static final Cat CAT = new Cat("EvernoteLoginTask");

    public static final int REQUEST_AUTH = 858;
    public static final int REQUEST_PROFILE_NAME = 859;

    private final EvernoteOAuthHelper mOAuthHelper;
    private List<BootstrapProfile> mBootstrapProfiles;

    private BootstrapProfile mBootstrapProfile;
    private int mBootstrapIndex;

    private CountDownLatch mBootstrapCountDownLatch;
    private CountDownLatch mResultCountDownLatch;

    private int mResultCode;
    private Intent mData;

    private final boolean mIsFragment;

    public EvernoteLoginTask(EvernoteOAuthHelper helper, boolean isFragment) {
        mOAuthHelper = helper;
        mIsFragment = isFragment;
    }

    @Override
    public Boolean execute() {
        boolean intentFired = startAuthorization();
        if (!intentFired) {
            return false;
        }

        if (!canContinue()) {
            return false;
        }

        mResultCountDownLatch = new CountDownLatch(1);
        try {
            mResultCountDownLatch.await();
        } catch (InterruptedException e) {
            return false;
        }

        return finishAuthorization();
    }

    public void switchBootstrapProfile() {
        mBootstrapIndex = (mBootstrapIndex + 1) % mBootstrapProfiles.size();
        mBootstrapProfile = mBootstrapProfiles.get(mBootstrapIndex);

        if (mBootstrapCountDownLatch != null) {
            mBootstrapCountDownLatch.countDown();
        }
    }

    public void onActivityResult(int resultCode, Intent data) {
        if (mResultCountDownLatch != null) {
            mResultCountDownLatch.countDown();
        }

        mResultCode = resultCode;
        mData = data;
    }

    private boolean startAuthorization() {
        if (!canContinue()) {
            return false;
        }

        try {
            mBootstrapProfiles = mOAuthHelper.fetchBootstrapProfiles();
            mBootstrapProfile = mOAuthHelper.getDefaultBootstrapProfile(mBootstrapProfiles);

            if (!canContinue()) {
                return false;
            }

            if (mBootstrapProfiles != null && mBootstrapProfiles.size() > 1) {
                String mainAppBootstrapName = getBootstrapProfileNameFromMainApp();
                if (!canContinue()) {
                    return false;
                }

                boolean showBootstrapOption = true;

                if (!TextUtils.isEmpty(mainAppBootstrapName)) {
                    for (BootstrapProfile bootstrapProfile : mBootstrapProfiles) {
                        if (mainAppBootstrapName.equals(bootstrapProfile.getName())) {
                            mBootstrapProfile = bootstrapProfile;
                            showBootstrapOption = false;
                            break;
                        }
                    }
                }

                if (showBootstrapOption) {
                    for (int i = 0; i < mBootstrapProfiles.size(); i++) {
                        if (mBootstrapProfile.equals(mBootstrapProfiles.get(i))) {
                            mBootstrapIndex = i;
                            break;
                        }
                    }

                    // waits to give user option to change bootstrap profile
                    showBootstrapOption();
                }
            }

        } catch (Exception e) {
            CAT.e(e);
        }

        if (mBootstrapProfile != null) {
            mOAuthHelper.setBootstrapProfile(mBootstrapProfile);
        }

        if (!canContinue()) {
            return false;
        }

        Intent intent = mOAuthHelper.startAuthorization(getActivity());

        if (!canContinue() || intent == null) {
            return false;
        }

        LoginTaskCallback callback = getLoginTaskCallback();
        if (callback != null) {
            callback.startActivityForResult(intent, REQUEST_AUTH);
            return true;
        }

        return false;
    }

    private boolean finishAuthorization() {
        return canContinue() && mOAuthHelper.finishAuthorization(getActivity(), mResultCode, mData);
    }

    private boolean canContinue() {
        return !isCancelled() && getActivity() != null;
    }

    private LoginTaskCallback getLoginTaskCallback() {
        if (mIsFragment) {
            Fragment fragment = getFragment();
            if (fragment instanceof LoginTaskCallback) {
                return (LoginTaskCallback) fragment;
            } else {
                return null;
            }

        } else {
            Activity activity = getActivity();
            if (activity instanceof LoginTaskCallback) {
                return (LoginTaskCallback) activity;
            } else {
                return null;
            }
        }
    }

    private void showBootstrapOption() {
        LoginTaskCallback loginTaskCallback = getLoginTaskCallback();
        if (loginTaskCallback == null) {
            return;
        }

        loginTaskCallback.show(getScreenName(getNextBootstrapProfile()));
        //noinspection UnusedAssignment
        loginTaskCallback = null; // free reference

        mBootstrapCountDownLatch = new CountDownLatch(1);
        try {
            if (mBootstrapCountDownLatch.await(3, TimeUnit.SECONDS)) {
                // user changed bootstrap profile, give him another chance to change it for 3 seconds
                showBootstrapOption();

            } else {
                // hide button
                loginTaskCallback = getLoginTaskCallback();
                if (loginTaskCallback != null) {
                    loginTaskCallback.show(null);
                }
            }

        } catch (InterruptedException e) {
            CAT.e(e);
        }
    }

    private BootstrapProfile getNextBootstrapProfile() {
        int nextIndex = (mBootstrapIndex + 1) % mBootstrapProfiles.size();
        return mBootstrapProfiles.get(nextIndex);
    }

    private String getScreenName(BootstrapProfile profile) {
        if (EvernoteOAuthHelper.CHINA_PROFILE_NAME.equals(profile.getName())) {
            return EvernoteSession.SCREEN_NAME_YXBIJI;

        } else if (EvernoteSession.HOST_PRODUCTION.contains(profile.getSettings().getServiceHost())) {
            return EvernoteSession.SCREEN_NAME_INTERNATIONAL;

        } else {
            return profile.getName();
        }
    }

    private String getBootstrapProfileNameFromMainApp() {
        Activity activity = getActivity();
        if (activity == null) {
            return null;
        }


        LoginTaskCallback callback = getLoginTaskCallback();
        if (callback == null) {
            return null;
        }

        Intent intent = EvernoteUtil.createGetBootstrapProfileNameIntent(activity, EvernoteSession.getInstance());
        if (intent == null) {
            return null;
        }

        callback.startActivityForResult(intent, REQUEST_PROFILE_NAME);

        mResultCountDownLatch = new CountDownLatch(1);
        try {
            mResultCountDownLatch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return null;
        }

        if (mData == null) {
            return null;
        }

        return mData.getStringExtra(EvernoteUtil.EXTRA_BOOTSTRAP_PROFILE_NAME);
    }

    public interface LoginTaskCallback {
        void startActivityForResult(Intent intent, int requestCode);

        void show(String bootstrapScreenName);
    }
}
