package com.evernote.client.android.type;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.asyncclient.EvernoteNoteStoreClient;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.type.LinkedNotebook;
import com.evernote.thrift.TException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author rwondratschek
 */
/*package*/ final class NoteRefHelper {

    private static final Map<String, LinkedNotebook> LINKED_NOTEBOOK_CACHE = new HashMap<>();

    private NoteRefHelper() {
        // no op
    }

    public static EvernoteNoteStoreClient getNoteStore(NoteRef noteRef) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        EvernoteSession session = getSession();

        if (!noteRef.isLinked()) {
            return getSession().getEvernoteClientFactory().getNoteStoreClient();
        }

        LinkedNotebook linkedNotebook = getLinkedNotebook(noteRef.getNotebookGuid());
        if (linkedNotebook == null) {
            return null;
        }

        return session.getEvernoteClientFactory().getLinkedNotebookHelper(linkedNotebook).getClient();
    }

    public static LinkedNotebook getLinkedNotebook(String notebookGuid) throws EDAMUserException, EDAMSystemException, TException, EDAMNotFoundException {
        if (LINKED_NOTEBOOK_CACHE.containsKey(notebookGuid)) {
            return LINKED_NOTEBOOK_CACHE.get(notebookGuid);
        }

        List<LinkedNotebook> linkedNotebooks = getSession().getEvernoteClientFactory().getNoteStoreClient().listLinkedNotebooks();
        for (LinkedNotebook linkedNotebook : linkedNotebooks) {
            LINKED_NOTEBOOK_CACHE.put(linkedNotebook.getGuid(), linkedNotebook);
        }

        return LINKED_NOTEBOOK_CACHE.get(notebookGuid);
    }

    public static EvernoteSession getSession() {
        EvernoteSession session = EvernoteSession.getInstance();

        if (!session.isLoggedIn()) {
            throw new IllegalArgumentException("session not logged in");
        }

        return session;
    }
}
