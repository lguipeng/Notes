package com.evernote.client.android.asyncclient;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.helper.EvernotePreconditions;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.type.LinkedNotebook;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.SharedNotebook;
import com.evernote.thrift.TException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Provides several helper methods for business notebooks. The easiest way to create an instance
 * is to call {@link EvernoteClientFactory#getBusinessNotebookHelper()}.
 *
 * @author rwondratschek
 */
@SuppressWarnings("unused")
public class EvernoteBusinessNotebookHelper extends EvernoteAsyncClient {

    private final EvernoteNoteStoreClient mClient;
    private final String mBusinessUserName;
    private final String mBusinessUserShardId;

    /**
     * @param client The note store client referencing the business note store url.
     * @param executorService The executor running the actions in the background.
     * @param businessUserName The name of the business user.
     * @param businessUserShardId The shard ID of the business user.
     */
    public EvernoteBusinessNotebookHelper(@NonNull EvernoteNoteStoreClient client, @NonNull ExecutorService executorService,
                                          @NonNull String businessUserName, @NonNull String businessUserShardId) {

        super(executorService);
        mClient = EvernotePreconditions.checkNotNull(client);
        mBusinessUserName = EvernotePreconditions.checkNotEmpty(businessUserName);
        mBusinessUserShardId = EvernotePreconditions.checkNotEmpty(businessUserShardId);
    }

    /**
     * @return The note store client referencing the business note store.
     */
    public EvernoteNoteStoreClient getClient() {
        return mClient;
    }

    /**
     * @return The business user name.
     */
    public String getBusinessUserName() {
        return mBusinessUserName;
    }

    /**
     * @return The shard ID for this business user.
     */
    public String getBusinessUserShardId() {
        return mBusinessUserShardId;
    }

    /**
     * @param session The current valid session.
     * @return A list of {@link LinkedNotebook}s, which all have a business ID.
     */
    public List<LinkedNotebook> listBusinessNotebooks(@NonNull EvernoteSession session) throws EDAMUserException, EDAMSystemException, TException, EDAMNotFoundException {
        return listBusinessNotebooks(session.getEvernoteClientFactory().getNoteStoreClient());
    }

    /**
     * @param defaultClient The note store client, which references the user's note store.
     * @return A list of {@link LinkedNotebook}s, which all have a business ID.
     */
    public List<LinkedNotebook> listBusinessNotebooks(@NonNull EvernoteNoteStoreClient defaultClient) throws EDAMUserException,
            EDAMSystemException, TException, EDAMNotFoundException {

        List<LinkedNotebook> businessNotebooks = new ArrayList<>(defaultClient.listLinkedNotebooks());

        Iterator<LinkedNotebook> iterator = businessNotebooks.iterator();
        while (iterator.hasNext()) {
            if (!isBusinessNotebook(iterator.next())) {
                iterator.remove();
            }
        }

        return businessNotebooks;
    }

    /**
     * @see #listBusinessNotebooks(EvernoteSession)
     */
    public Future<List<LinkedNotebook>> listBusinessNotebooksAsync(@NonNull final EvernoteSession session, @Nullable EvernoteCallback<List<LinkedNotebook>> callback) {
        return listBusinessNotebooksAsync(session.getEvernoteClientFactory().getNoteStoreClient(), callback);
    }

    /**
     * @see #listBusinessNotebooks(EvernoteNoteStoreClient)
     */
    public Future<List<LinkedNotebook>> listBusinessNotebooksAsync(@NonNull final EvernoteNoteStoreClient defaultClient,
                                                                   @Nullable EvernoteCallback<List<LinkedNotebook>> callback) {

        return submitTask(new Callable<List<LinkedNotebook>>() {
            @Override
            public List<LinkedNotebook> call() throws Exception {
                return listBusinessNotebooks(defaultClient);
            }
        }, callback);
    }

    /**
     * @param notebook The new business notebook.
     * @param session The current valid session.
     * @return The new created {@link LinkedNotebook}, which has an business ID.
     */
    public LinkedNotebook createBusinessNotebook(@NonNull Notebook notebook, @NonNull EvernoteSession session) throws TException,
            EDAMUserException, EDAMSystemException, EDAMNotFoundException {

        return createBusinessNotebook(notebook, session.getEvernoteClientFactory().getNoteStoreClient());
    }

    /**
     * @param notebook The new business notebook.
     * @param defaultClient The note store client, which references the user's note store.
     * @return The new created {@link LinkedNotebook}, which has an business ID.
     */
    public LinkedNotebook createBusinessNotebook(@NonNull Notebook notebook, @NonNull EvernoteNoteStoreClient defaultClient)
            throws TException, EDAMUserException, EDAMSystemException, EDAMNotFoundException {

        Notebook originalNotebook = mClient.createNotebook(notebook);

        List<SharedNotebook> sharedNotebooks = originalNotebook.getSharedNotebooks();
        SharedNotebook sharedNotebook = sharedNotebooks.get(0);

        LinkedNotebook linkedNotebook = new LinkedNotebook();
        linkedNotebook.setShareKey(sharedNotebook.getShareKey());
        linkedNotebook.setShareName(originalNotebook.getName());
        linkedNotebook.setUsername(mBusinessUserName);
        linkedNotebook.setShardId(mBusinessUserShardId);

        return defaultClient.createLinkedNotebook(linkedNotebook);
    }

    /**
     * @see #createBusinessNotebook(Notebook, EvernoteSession)
     */
    public Future<LinkedNotebook> createBusinessNotebookAsync(@NonNull final Notebook notebook, @NonNull final EvernoteSession session,
                                                              @Nullable EvernoteCallback<LinkedNotebook> callback) {

        return createBusinessNotebookAsync(notebook, session.getEvernoteClientFactory().getNoteStoreClient(), callback);
    }

    /**
     * @see #createBusinessNotebook(Notebook, EvernoteNoteStoreClient)
     */
    public Future<LinkedNotebook> createBusinessNotebookAsync(@NonNull final Notebook notebook, @NonNull final EvernoteNoteStoreClient defaultClient,
                                                              @Nullable EvernoteCallback<LinkedNotebook> callback) {

        return submitTask(new Callable<LinkedNotebook>() {
            @Override
            public LinkedNotebook call() throws Exception {
                return createBusinessNotebook(notebook, defaultClient);
            }
        }, callback);
    }

    /**
     * @return {@code true} if this linked notebook has a business ID.
     */
    public static boolean isBusinessNotebook(LinkedNotebook linkedNotebook) {
        return linkedNotebook.isSetBusinessId();
    }
}
