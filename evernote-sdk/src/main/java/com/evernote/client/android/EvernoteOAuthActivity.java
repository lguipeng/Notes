package com.evernote.client.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.evernote.client.android.helper.Cat;


/**
 * Alternative if Evernote is not installed.
 *
 * @author rwondratschek
 */
@SuppressWarnings("UnusedDeclaration")
public class EvernoteOAuthActivity extends FragmentActivity {

    private static final Cat CAT = new Cat("EvernoteOAuthActivity");

    private static final String HOST_EVERNOTE = "www.evernote.com";
    private static final String HOST_SANDBOX = "sandbox.evernote.com";
    private static final String HOST_CHINA = "app.yinxiang.com";

    public static Intent createIntent(Context context, String url) {
        Intent intent = new Intent(context, EvernoteOAuthActivity.class);
        intent.putExtra(EvernoteUtil.EXTRA_AUTHORIZATION_URL, url);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setResultUri(null);

        String url = getIntent().getStringExtra(EvernoteUtil.EXTRA_AUTHORIZATION_URL);
        if (TextUtils.isEmpty(url)) {
            CAT.w("no uri passed, return cancelled");
            finish();
            return;
        }

        Uri uri = Uri.parse(url);
        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            CAT.w("https required, return cancelled");
            finish();
            return;
        }

        String host = uri.getHost();
        if (!HOST_EVERNOTE.equalsIgnoreCase(host) && !HOST_SANDBOX.equalsIgnoreCase(host) && !HOST_CHINA.equalsIgnoreCase(host)) {
            CAT.w("unacceptable host, return cancelled");
            finish();
            return;
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, new WebViewFragment())
                .commit();
        }
    }

    private void setResultUri(String clickedOAuthUri) {
        Intent data = new Intent();
        data.putExtra(EvernoteUtil.EXTRA_OAUTH_CALLBACK_URL, clickedOAuthUri);
        setResult(TextUtils.isEmpty(clickedOAuthUri) ? RESULT_CANCELED : RESULT_OK, data);
    }

    public static class WebViewFragment extends Fragment {

        private static final String INTENT_KEY = "IntentKey";

        public static WebViewFragment createInstance() {
            WebViewFragment fragment = new WebViewFragment();
            fragment.setRetainInstance(true);
            return fragment;
        }

        private WebView mWebView;
        private boolean mIsWebViewAvailable;

        private String mUrl;

        @Override
        public void onAttach(Activity activity) {
            if (!(activity instanceof EvernoteOAuthActivity)) {
                throw new IllegalArgumentException();
            }

            super.onAttach(activity);

            mUrl = activity.getIntent().getStringExtra(EvernoteUtil.EXTRA_AUTHORIZATION_URL);
        }

        @SuppressLint("SetJavaScriptEnabled")
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            destroyWebView();

            mWebView = new WebView(getActivity());
            mWebView.setWebViewClient(mWebViewClient);
            mWebView.getSettings().setJavaScriptEnabled(true);

            if (savedInstanceState == null) {
                mWebView.loadUrl(mUrl);
            } else {
                mWebView.restoreState(savedInstanceState);
            }

            mIsWebViewAvailable = true;
            return mWebView;
        }

        @Override
        public void onPause() {
            super.onPause();
            mWebView.onPause();
        }

        @Override
        public void onResume() {
            mWebView.onResume();
            super.onResume();
        }

        @Override
        public void onDestroyView() {
            mIsWebViewAvailable = false;
            super.onDestroyView();
        }

        @Override
        public void onDestroy() {
            destroyWebView();
            super.onDestroy();
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            mWebView.saveState(outState);
        }

        public WebView getWebView() {
            return mIsWebViewAvailable ? mWebView : null;
        }

        private void destroyWebView() {
            if (mWebView != null) {
                ViewGroup viewGroup = (ViewGroup) mWebView.getParent();
                if (viewGroup != null) {
                    viewGroup.removeView(mWebView);
                }

                mWebView.destroy();
                mWebView = null;
            }
        }

        private WebViewClient mWebViewClient = new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Uri uri = Uri.parse(url);
                if (EvernoteOAuthHelper.CALLBACK_SCHEME.equals(uri.getScheme())) {
                    ((EvernoteOAuthActivity) getActivity()).setResultUri(url);
                    getActivity().finish();
                    return true;
                }

                return super.shouldOverrideUrlLoading(view, url);
            }
        };
    }
}
