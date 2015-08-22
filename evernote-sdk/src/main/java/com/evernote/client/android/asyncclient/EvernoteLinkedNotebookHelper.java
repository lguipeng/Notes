package com.evernote.client.android.asyncclient;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.helper.EvernotePreconditions;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.type.LinkedNotebook;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.SharedNotebook;
import com.evernote.thrift.TException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Provides several helper methods for {@link LinkedNotebook}s. The easiest way to create an instance
 * is to call {@link EvernoteClientFactory#getLinkedNotebookHelper(LinkedNotebook)}.
 *
 * @author rwondratschek
 */
@SuppressWarnings("unused")
public class EvernoteLinkedNotebookHelper extends EvernoteAsyncClient {

    protected final EvernoteNoteStoreClient mClient;
    protected final LinkedNotebook mLinkedNotebook;

    /**
     * @param client The note store client referencing the linked notebook note store url.
     * @param linkedNotebook The desired linked notebook.
     * @param executorService The executor running the actions in the background.
     */
    public EvernoteLinkedNotebookHelper(@NonNull EvernoteNoteStoreClient client, @NonNull LinkedNotebook linkedNotebook, @NonNull ExecutorService executorService) {
        super(executorService);
        mClient = EvernotePreconditions.checkNotNull(client);
        mLinkedNotebook = EvernotePreconditions.checkNotNull(linkedNotebook);
    }

    /**
     * @return The note store client referencing the linked notebook's note store.
     */
    public EvernoteNoteStoreClient getClient() {
        return mClient;
    }

    public LinkedNotebook getLinkedNotebook() {
        return mLinkedNotebook;
    }

    /**
     * @param note The new note.
     * @return The new created note from the server.
     */
    public Note createNoteInLinkedNotebook(@NonNull Note note) throws EDAMUserException, EDAMSystemException, TException, EDAMNotFoundException {
        SharedNotebook sharedNotebook = mClient.getSharedNotebookByAuth();
        note.setNotebookGuid(sharedNotebook.getNotebookGuid());
        return mClient.createNote(note);
    }

    /**
     * @see #createNoteInLinkedNotebook(Note)
     */
    public Future<Note> createNoteInLinkedNotebookAsync(@NonNull final Note note, @Nullable EvernoteCallback<Note> callback) {
        return submitTask(new Callable<Note>() {
            @Override
            public Note call() throws Exception {
                return createNoteInLinkedNotebook(note);
            }
        }, callback);
    }

    /**
     * @param session The current valid session.
     * @return A flag indicating if the action was successful.
     * @see EvernoteNoteStoreClient#expungeLinkedNotebook(String)
     */
    public int deleteLinkedNotebook(@NonNull EvernoteSession session) throws TException, EDAMUserException, EDAMSystemException, EDAMNotFoundException {
        return deleteLinkedNotebook(session.getEvernoteClientFactory().getNoteStoreClient());
    }

    /**
     * @param defaultClient The note store client, which references the user's note store.
     * @return A flag indicating if the action was successful.
     * @see EvernoteNoteStoreClient#expungeLinkedNotebook(String)
     */
    public int deleteLinkedNotebook(@NonNull EvernoteNoteStoreClient defaultClient) throws TException, EDAMUserException, EDAMSystemException, EDAMNotFoundException {
        SharedNotebook sharedNotebook = mClient.getSharedNotebookByAuth();
        List<Long> sharedNotebookIds = new ArrayList<>();
        sharedNotebookIds.add(sharedNotebook.getId());

        mClient.expungeSharedNotebooks(sharedNotebookIds);

        return defaultClient.expungeLinkedNotebook(mLinkedNotebook.getGuid());
    }

    /**
     * @see #deleteLinkedNotebook(EvernoteSession)
     */
    public Future<Integer> deleteLinkedNotebookAsync(@NonNull final EvernoteSession session, @Nullable EvernoteCallback<Integer> callback) {
        return deleteLinkedNotebookAsync(session.getEvernoteClientFactory().getNoteStoreClient(), callback);
    }

    /**
     * @see #deleteLinkedNotebook(EvernoteNoteStoreClient)
     */
    public Future<Integer> deleteLinkedNotebookAsync(@NonNull final EvernoteNoteStoreClient defaultClient, @Nullable EvernoteCallback<Integer> callback) {
        return submitTask(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return deleteLinkedNotebook(defaultClient);
            }
        }, callback);
    }

    /**
     * @return The notebook for this linked notebook.
     */
    public Notebook getCorrespondingNotebook() throws TException, EDAMUserException, EDAMSystemException, EDAMNotFoundException {
        SharedNotebook sharedNotebook = mClient.getSharedNotebookByAuth();
        return mClient.getNotebook(sharedNotebook.getNotebookGuid());
    }

    /**
     * @see #getCorrespondingNotebook()
     */
    public Future<Notebook> getCorrespondingNotebookAsync(@Nullable EvernoteCallback<Notebook> callback) {
        return submitTask(new Callable<Notebook>() {
            @Override
            public Notebook call() throws Exception {
                return getCorrespondingNotebook();
            }
        }, callback);
    }

    /**
     * @return {@code true} if this linked notebook is writable.
     */
    public boolean isNotebookWritable() throws EDAMUserException, TException, EDAMSystemException, EDAMNotFoundException {
        Notebook notebook = getCorrespondingNotebook();
        return !notebook.getRestrictions().isNoCreateNotes();
    }

    /**
     * @see #isNotebookWritable()
     */
    public Future<Boolean> isNotebookWritableAsync(@Nullable EvernoteCallback<Boolean> callback) {
        return submitTask(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return isNotebookWritable();
            }
        }, callback);
    }
}
