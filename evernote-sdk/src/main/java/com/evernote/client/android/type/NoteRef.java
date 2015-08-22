package com.evernote.client.android.type;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.client.android.asyncclient.EvernoteLinkedNotebookHelper;
import com.evernote.client.android.asyncclient.EvernoteNoteStoreClient;
import com.evernote.client.android.asyncclient.EvernoteSearchHelper;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.notestore.NotesMetadataResultSpec;
import com.evernote.edam.type.LinkedNotebook;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
import com.evernote.thrift.TException;

import java.util.List;

/**
 * A reference to a note on the server. This class provides several helper methods to receive the note
 * itself, its notebook and content.
 *
 * <br>
 * <br>
 *
 * Use the {@link DefaultFactory} class to instantiate objects. The easiest way to get a reference
 * is to use the {@link EvernoteSearchHelper} to find notes.
 *
 * @author rwondratschek
 */
@SuppressWarnings("unused")
public class NoteRef implements Parcelable {

    private final String mNoteGuid;
    private final String mNotebookGuid;
    private final String mTitle;
    private final boolean mLinked;

    public NoteRef(@NonNull String noteGuid, @Nullable String notebookGuid, @NonNull String title, boolean linked) {
        mNoteGuid = noteGuid;
        mNotebookGuid = notebookGuid;
        mTitle = title;
        mLinked = linked;
    }

    /**
     * @return The note's GUID. This value is never {@code null}.
     */
    @NonNull
    public String getGuid() {
        return mNoteGuid;
    }

    /**
     * @return The note's notebook GUID. This GUID is either from a personal or linked notebook. May
     * return {@code null}, e.g. if the {@link NotesMetadataResultSpec} did not include the notebook
     * GUID. It's recommend to include the notebook GUID always.
     *
     * @see #isLinked()
     */
    public String getNotebookGuid() {
        return mNotebookGuid;
    }

    /**
     * @return The note's title. This value is never {@code null}.
     */
    @NonNull
    public String getTitle() {
        return mTitle;
    }

    /**
     * @return {@code true} if this note exists in linked notebook.
     */
    public boolean isLinked() {
        return mLinked;
    }

    /**
     * Loads the concrete note from the server.
     *
     * @param withContent If {@code true} the returned note contains its content.
     * @param withResourcesData If {@code true} the returned note contains its resources.
     * @param withResourcesRecognition If {@code true} the returned note contains in its resources
     *                                 the recognition data.
     * @param withResourcesAlternateData If {@code true} the returned note contains in its resources
     *                                   the alternate data.
     * @return The note from the server.
     */
    public Note loadNote(boolean withContent, boolean withResourcesData, boolean withResourcesRecognition,
                         boolean withResourcesAlternateData) throws TException, EDAMUserException, EDAMSystemException, EDAMNotFoundException {

        EvernoteNoteStoreClient noteStore = NoteRefHelper.getNoteStore(this);
        if (noteStore == null) {
            return null;
        }

        return noteStore.getNote(mNoteGuid, withContent, withResourcesData, withResourcesRecognition, withResourcesAlternateData);
    }

    /**
     * @return The note from the server without its content or resources.
     * @see #loadNote(boolean, boolean, boolean, boolean)
     */
    public Note loadNotePartial() throws EDAMUserException, TException, EDAMSystemException, EDAMNotFoundException {
        return loadNote(false, false, false, false);
    }

    /**
     * @return The note from the server with its content, resources, recognition data and alternate data.
     * @see #loadNote(boolean, boolean, boolean, boolean)
     */
    public Note loadNoteFully() throws EDAMUserException, TException, EDAMSystemException, EDAMNotFoundException {
        return loadNote(true, true, true, true);
    }

    /**
     * If this note is linked, then it loads the corresponding notebook for the linked notebook. Use
     * {@link #loadLinkedNotebook()} to get the linked notebook.
     *
     * @return The note's notebook from server.
     * @see #isLinked()
     * @see EvernoteLinkedNotebookHelper#getCorrespondingNotebook()
     */
    public Notebook loadNotebook() throws EDAMUserException, EDAMSystemException, TException, EDAMNotFoundException {
        if (mNotebookGuid == null) {
            return null;
        }

        if (mLinked) {
            LinkedNotebook linkedNotebook = NoteRefHelper.getLinkedNotebook(mNotebookGuid);
            return NoteRefHelper.getSession().getEvernoteClientFactory().getLinkedNotebookHelper(linkedNotebook).getCorrespondingNotebook();
        }

        EvernoteNoteStoreClient noteStore = NoteRefHelper.getNoteStore(this);
        if (noteStore == null) {
            return null;
        }

        return noteStore.getNotebook(mNotebookGuid);
    }

    /**
     * @return The linked notebook if this is a linked note.
     * @see #isLinked()
     */
    public LinkedNotebook loadLinkedNotebook() throws EDAMUserException, EDAMSystemException, TException, EDAMNotFoundException {
        if (!mLinked) {
            return null;
        }

        EvernoteNoteStoreClient noteStore = NoteRefHelper.getSession().getEvernoteClientFactory().getNoteStoreClient();
        if (noteStore == null) {
            return null;
        }

        List<LinkedNotebook> linkedNotebooks = noteStore.listLinkedNotebooks();
        for (LinkedNotebook linkedNotebook : linkedNotebooks) {
            if (linkedNotebook.getGuid().equals(mNotebookGuid)) {
                return linkedNotebook;
            }
        }

        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mNoteGuid);
        dest.writeString(mNotebookGuid);
        dest.writeString(mTitle);
        dest.writeInt(mLinked ? 1 : 0);
    }

    public static final Creator<NoteRef> CREATOR = new Creator<NoteRef>() {
        @Override
        public NoteRef createFromParcel(final Parcel source) {
            return new NoteRef(source.readString(), source.readString(), source.readString(), source.readInt() == 1);
        }

        @Override
        public NoteRef[] newArray(final int size) {
            return new NoteRef[size];
        }
    };

    /**
     * A factory to construct {@link NoteRef} instances.
     */
    public interface Factory {
        /**
         * @param personalNoteMetadata The meta data from the personal note from the server.
         * @return A new reference, which is not linked.
         */
        NoteRef fromPersonal(NoteMetadata personalNoteMetadata);

        /**
         * @param linkedNoteMetadata The meta data from a linked note from the server.
         * @param linkedNotebook The corresponding linked notebook from this note meta data.
         * @return A new reference, which is linked.
         */
        NoteRef fromLinked(NoteMetadata linkedNoteMetadata, LinkedNotebook linkedNotebook);
    }

    /**
     * The default factory to create {@link NoteRef} instances.
     */
    public static class DefaultFactory implements Factory {

        @Override
        public NoteRef fromPersonal(NoteMetadata personalNoteMetadata) {
            return new NoteRef(personalNoteMetadata.getGuid(), personalNoteMetadata.getNotebookGuid(), personalNoteMetadata.getTitle(), false);
        }

        @Override
        public NoteRef fromLinked(NoteMetadata linkedNoteMetadata, LinkedNotebook linkedNotebook) {
            return new NoteRef(linkedNoteMetadata.getGuid(), linkedNotebook.getGuid(), linkedNoteMetadata.getTitle(), true);
        }
    }
}
