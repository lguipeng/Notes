package com.evernote.client.android.login;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.evernote.androidsdk.R;
import com.evernote.client.android.EvernoteOAuthHelper;
import com.evernote.client.android.EvernoteSession;

import net.vrallev.android.task.TaskExecutor;
import net.vrallev.android.task.TaskResult;

import java.util.Locale;

/**
 * Used if you call {@link EvernoteSession#authenticate(FragmentActivity)}. This class is the
 * recommended authentication process.
 *
 * <p/>
 *
 * You can either extend this class and override {@link EvernoteLoginFragment#onLoginFinished(boolean)} method
 * to get notified about the authentication result or the parent {@link FragmentActivity} can implement the
 * {@link ResultCallback} interface to receive the result.
 *
 * @author rwondratschek
 */
public class EvernoteLoginFragment extends DialogFragment implements EvernoteLoginTask.LoginTaskCallback {

    public static final String TAG = "EvernoteDialogFragment";

    private static final String ARG_CONSUMER_KEY = "consumerKey";
    private static final String ARG_CONSUMER_SECRET = "consumerSecret";
    private static final String ARG_SUPPORT_APP_LINKED_NOTEBOOKS = "supportAppLinkedNotebooks";
    private static final String ARG_LOCALE = "ARG_LOCALE";

    private static final String KEY_TASK = "KEY_TASK";
    private static final String KEY_RESULT_POSTED = "KEY_RESULT_POSTED";

    public static EvernoteLoginFragment create(String consumerKey, String consumerSecret, boolean supportAppLinkedNotebooks, Locale locale) {
        return create(EvernoteLoginFragment.class, consumerKey, consumerSecret, supportAppLinkedNotebooks, locale);
    }

    public static <T extends EvernoteLoginFragment> T create(Class<T> subClass, String consumerKey, String consumerSecret, boolean supportAppLinkedNotebooks, Locale locale) {
        T fragment;
        try {
            fragment = subClass.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

        Bundle args = new Bundle();
        args.putString(ARG_CONSUMER_KEY, consumerKey);
        args.putString(ARG_CONSUMER_SECRET, consumerSecret);
        args.putBoolean(ARG_SUPPORT_APP_LINKED_NOTEBOOKS, supportAppLinkedNotebooks);
        args.putSerializable(ARG_LOCALE, locale);
        fragment.setArguments(args);

        return fragment;
    }

    private int mTaskKey = -1;
    private boolean mResultPosted;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            Bundle args = getArguments();

            EvernoteOAuthHelper helper = new EvernoteOAuthHelper(EvernoteSession.getInstance(), args.getString(ARG_CONSUMER_KEY),
                    args.getString(ARG_CONSUMER_SECRET), args.getBoolean(ARG_SUPPORT_APP_LINKED_NOTEBOOKS, true),
                    (Locale) args.getSerializable(ARG_LOCALE));

            mTaskKey = TaskExecutor.getInstance().execute(new EvernoteLoginTask(helper, true), this);

        } else {
            mTaskKey = savedInstanceState.getInt(KEY_TASK, -1);
            mResultPosted = savedInstanceState.getBoolean(KEY_RESULT_POSTED, false);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(false);

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

        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.esdk_loading));
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel), onClickListener);
        progressDialog.setCancelable(isCancelable());

        return progressDialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_TASK, mTaskKey);
        outState.putBoolean(KEY_RESULT_POSTED, mResultPosted);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
    public final synchronized void onResult(Boolean result, EvernoteLoginTask task) {
        if (mResultPosted || (task != null && task.getKey() != mTaskKey)) {
            return;
        }

        mResultPosted = true;

        dismiss();

        FragmentActivity activity = getActivity();
        if (activity instanceof ResultCallback) {
            ((ResultCallback) activity).onLoginFinished(result);
        } else {
            onLoginFinished(result);
        }
    }

    protected void onLoginFinished(@SuppressWarnings("UnusedParameters") boolean success) {
        // override me
    }

    @Override
    public void show(final String bootstrapScreenName) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ProgressDialog dialog = (ProgressDialog) getDialog();
                Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);

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

    public interface ResultCallback {
        void onLoginFinished(boolean successful);
    }
}
