package com.evernote.client.android.login;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.evernote.androidsdk.R;
import com.evernote.client.android.EvernoteOAuthHelper;
import com.evernote.client.android.EvernoteSession;
import com.evernote.client.view.ProgressDialog;

import net.vrallev.android.task.TaskExecutor;
import net.vrallev.android.task.TaskResult;

import java.util.Locale;

/**
 * Used if you call {@link EvernoteSession#authenticate(Activity)}. You shouldn't need interact
 * with this class directly.
 *
 * @author rwondratschek
 */
public class EvernoteLoginActivity extends Activity implements EvernoteLoginTask.LoginTaskCallback {

    private static final String EXTRA_CONSUMER_KEY = "EXTRA_CONSUMER_KEY";
    private static final String EXTRA_CONSUMER_SECRET = "EXTRA_CONSUMER_SECRET";
    private static final String EXTRA_SUPPORT_APP_LINKED_NOTEBOOKS = "EXTRA_SUPPORT_APP_LINKED_NOTEBOOKS";
    private static final String EXTRA_LOCALE = "EXTRA_LOCALE";

    private static final String KEY_TASK = "KEY_TASK";
    private static final String KEY_RESULT_POSTED = "KEY_RESULT_POSTED";

    public static Intent createIntent(Context context, String consumerKey, String consumerSecret, boolean supportAppLinkedNotebooks, Locale locale) {
        Intent intent = new Intent(context, EvernoteLoginActivity.class);
        intent.putExtra(EXTRA_CONSUMER_KEY, consumerKey);
        intent.putExtra(EXTRA_CONSUMER_SECRET, consumerSecret);
        intent.putExtra(EXTRA_SUPPORT_APP_LINKED_NOTEBOOKS, supportAppLinkedNotebooks);
        intent.putExtra(EXTRA_LOCALE, locale);
        return intent;
    }

    private int mTaskKey;
    private boolean mResultPosted;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            Bundle args = getIntent().getExtras();
            EvernoteOAuthHelper helper = new EvernoteOAuthHelper(EvernoteSession.getInstance(), args.getString(EXTRA_CONSUMER_KEY),
                    args.getString(EXTRA_CONSUMER_SECRET), args.getBoolean(EXTRA_SUPPORT_APP_LINKED_NOTEBOOKS, true),
                    (Locale) args.getSerializable(EXTRA_LOCALE));

            mTaskKey = TaskExecutor.getInstance().execute(new EvernoteLoginTask(helper, false), this);

        } else {
            mTaskKey = savedInstanceState.getInt(KEY_TASK, -1);
            mResultPosted = savedInstanceState.getBoolean(KEY_RESULT_POSTED, false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mProgressDialog == null || !mProgressDialog.isShowing()) {
            showDialog();
        }
    }

    @Override
    protected void onStop() {
        mProgressDialog.dismiss();
        mProgressDialog = null;
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_TASK, mTaskKey);
        outState.putBoolean(KEY_RESULT_POSTED, mResultPosted);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EvernoteLoginTask.REQUEST_AUTH || requestCode == EvernoteLoginTask.REQUEST_PROFILE_NAME) {
            EvernoteLoginTask task = (EvernoteLoginTask) TaskExecutor.getInstance().getTask(mTaskKey);
            if (task != null) {
                task.onActivityResult(resultCode, data);
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @SuppressWarnings("unused")
    @TaskResult
    public final void onResult(Boolean result, EvernoteLoginTask task) {
        if (mResultPosted || (task != null && task.getKey() != mTaskKey)) {
            return;
        }

        mResultPosted = true;

        setResult(result ? RESULT_OK : RESULT_CANCELED);
        if (mProgressDialog != null && mProgressDialog.isShowing()){
            mProgressDialog.hide();
        }
        finish();
    }

    protected void showDialog() {
        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EvernoteLoginTask task = (EvernoteLoginTask) TaskExecutor.getInstance().getTask(mTaskKey);
                if (task != null) {
                    task.cancel();
                }

                onResult(false, task);
            }
        };

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel), onClickListener);
        mProgressDialog.setCancelable(false);

        mProgressDialog.show();
    }

    @Override
    public void show(final String bootstrapScreenName) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Button button = mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE);

                if (!TextUtils.isEmpty(bootstrapScreenName)) {
                    button.setText(getString(R.string.esdk_switch_to, bootstrapScreenName));
                    button.setVisibility(View.VISIBLE);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            switchBootstrapProfile();
                        }
                    });

                } else {
                    button.setVisibility(View.GONE);
                    button.setOnClickListener(null);
                }
                //disable this button
                button.setVisibility(View.GONE);
            }
        });
    }

    protected void switchBootstrapProfile() {
        EvernoteLoginTask task = (EvernoteLoginTask) TaskExecutor.getInstance().getTask(mTaskKey);
        if (task != null) {
            task.switchBootstrapProfile();
        }
    }
}
