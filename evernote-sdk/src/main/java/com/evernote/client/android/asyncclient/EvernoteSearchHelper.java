package com.evernote.client.android.asyncclient;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.helper.EvernotePreconditions;
import com.evernote.client.android.type.NoteRef;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.notestore.NotesMetadataList;
import com.evernote.edam.notestore.NotesMetadataResultSpec;
import com.evernote.edam.type.LinkedNotebook;
import com.evernote.edam.type.NoteSortOrder;
import com.evernote.edam.type.Notebook;
import com.evernote.thrift.TException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Provides an unified search method to look for notes in multiple note stores.
 *
 * <br>
 * <br>
 *
 * <b>Be careful</b> using this class. A query may be very intense and could require multiple requests.
 *
 * <br>
 * <br>
 *
 * The easiest way to create an instance is to call {@link EvernoteClientFactory#getEvernoteSearchHelper()}.
 *
 * @author rwondratschek
 */
@SuppressWarnings("unused")
public class EvernoteSearchHelper extends EvernoteAsyncClient {

    private final EvernoteSession mSession;
    private final EvernoteClientFactory mClientFactory;
    private final EvernoteNoteStoreClient mPrivateClient;

    /**
     * @param session The current valid session.
     * @param executorService The executor running the actions in the background.
     */
    public EvernoteSearchHelper(@NonNull EvernoteSession session, @NonNull ExecutorService executorService) {
        super(executorService);
        mSession = EvernotePreconditions.checkNotNull(session);
        mClientFactory = mSession.getEvernoteClientFactory();
        mPrivateClient = mClientFactory.getNoteStoreClient();
    }

    /**
     * Submits a search.
     *
     * @param search The desired search with its parameters.
     * @return The result containing multiple {@link NotesMetadataList}s.
     */
    public Result execute(@NonNull Search search) throws Exception {
        if (search.getOffset() >= search.getMaxNotes()) {
            throw new IllegalArgumentException("offset must be less than max notes");
        }

        Result result = new Result(search.getScopes());

        for (Scope scope : search.getScopes()) {
            switch (scope) {
                case PERSONAL_NOTES:
                    try {
                        result.setPersonalResults(findPersonalNotes(search));
                    } catch (Exception e) {
                        maybeRethrow(search, e);
                    }
                    break;

                case LINKED_NOTEBOOKS:
                    List<LinkedNotebook> linkedNotebooks = getLinkedNotebooks(search, false);
                    for (LinkedNotebook linkedNotebook : linkedNotebooks) {
                        try {
                            result.addLinkedNotebookResult(linkedNotebook, findNotesInLinkedNotebook(search, linkedNotebook));
                        } catch (Exception e) {
                            maybeRethrow(search, e);
                        }
                    }
                    break;

                case BUSINESS:
                    linkedNotebooks = getLinkedNotebooks(search, true);
                    for (LinkedNotebook linkedNotebook : linkedNotebooks) {
                        try {
                            result.addBusinessResult(linkedNotebook, findNotesInBusinessNotebook(search, linkedNotebook));
                        } catch (Exception e) {
                            maybeRethrow(search, e);
                        }
                    }
                    break;
            }
        }

        return result;
    }

    /**
     * @see #execute(Search)
     */
    public Future<Result> executeAsync(@NonNull final Search search, @Nullable EvernoteCallback<Result> callback) {
        return submitTask(new Callable<Result>() {
            @Override
            public Result call() throws Exception {
                return execute(search);
            }
        }, callback);
    }

    protected List<NotesMetadataList> findPersonalNotes(Search search) throws Exception {
        return findAllNotes(search, mPrivateClient, search.getNoteFilter());
    }

    protected List<NotesMetadataList> findNotesInLinkedNotebook(Search search, LinkedNotebook linkedNotebook) throws Exception {
        EvernoteLinkedNotebookHelper linkedNotebookHelper = mClientFactory.getLinkedNotebookHelper(linkedNotebook);
        Notebook correspondingNotebook = linkedNotebookHelper.getCorrespondingNotebook();

        // create a deep copy so that we don't touch the initial search request values
        NoteFilter noteFilter = new NoteFilter(search.getNoteFilter());
        noteFilter.setNotebookGuid(correspondingNotebook.getGuid());

        return findAllNotes(search, linkedNotebookHelper.getClient(), noteFilter);
    }

    protected List<NotesMetadataList> findNotesInBusinessNotebook(Search search, LinkedNotebook linkedNotebook) throws Exception {
        EvernoteBusinessNotebookHelper businessNotebookHelper = mClientFactory.getBusinessNotebookHelper();

        EvernoteLinkedNotebookHelper linkedNotebookHelper = mClientFactory.getLinkedNotebookHelper(linkedNotebook);
        Notebook correspondingNotebook = linkedNotebookHelper.getCorrespondingNotebook();

        // create a deep copy so that we don't touch the initial search request values
        NoteFilter noteFilter = new NoteFilter(search.getNoteFilter());
        noteFilter.setNotebookGuid(correspondingNotebook.getGuid());

        return findAllNotes(search, businessNotebookHelper.getClient(), noteFilter);
    }

    protected List<NotesMetadataList> findAllNotes(Search search, EvernoteNoteStoreClient client, NoteFilter filter) throws Exception {
        List<NotesMetadataList> result = new ArrayList<>();

        final int maxNotes = search.getMaxNotes();
        int offset = search.getOffset();

        int remaining = maxNotes - offset;

        while (remaining > 0) {
            try {
                NotesMetadataList notesMetadata = client.findNotesMetadata(filter, offset, maxNotes, search.getResultSpec());
                remaining = notesMetadata.getTotalNotes() - (notesMetadata.getStartIndex() + notesMetadata.getNotesSize());

                result.add(notesMetadata);
            } catch (EDAMUserException | EDAMSystemException | TException | EDAMNotFoundException e) {
                maybeRethrow(search, e);
                remaining -= search.getPageSize();
            }

            offset += search.getPageSize();
        }

        return result;
    }

    protected List<LinkedNotebook> getLinkedNotebooks(Search search, boolean business) throws Exception {
        if (business) {
            if (search.mBusinessNotebooks.isEmpty()) {
                try {
                    return mClientFactory.getBusinessNotebookHelper().listBusinessNotebooks(mSession);
                } catch (EDAMUserException | EDAMSystemException | EDAMNotFoundException | TException e) {
                    maybeRethrow(search, e);
                    return Collections.emptyList();
                }
            } else {
                return search.mBusinessNotebooks;
            }

        } else {
            if (search.mLinkedNotebooks.isEmpty()) {
                try {
                    return mPrivateClient.listLinkedNotebooks();
                } catch (EDAMUserException | EDAMNotFoundException | TException | EDAMSystemException e) {
                    maybeRethrow(search, e);
                    return Collections.emptyList();
                }
            } else {
                return search.mLinkedNotebooks;
            }
        }
    }

    private void maybeRethrow(Search search, Exception e) throws Exception {
        if (!search.isIgnoreExceptions()) {
            throw e;
        }
    }

    /**
     * Defines from where the notes are queried.
     */
    public enum Scope {
        PERSONAL_NOTES,
        LINKED_NOTEBOOKS,
        BUSINESS
    }

    public static class Search {

        private final EnumSet<Scope> mScopes;
        private final List<LinkedNotebook> mLinkedNotebooks;
        private final List<LinkedNotebook> mBusinessNotebooks;
        private NoteFilter mNoteFilter;
        private NotesMetadataResultSpec mResultSpec;
        private int mOffset;
        private int mMaxNotes;
        private int mPageSize;
        private boolean mIgnoreExceptions;

        public Search() {
            mScopes = EnumSet.noneOf(Scope.class);
            mLinkedNotebooks = new ArrayList<>();
            mBusinessNotebooks = new ArrayList<>();
            mOffset = -1;
            mMaxNotes = -1;
            mPageSize = -1;
        }

        /**
         * If no scope is specified, {@link Scope#PERSONAL_NOTES} is the default value.
         *
         * <br>
         * <br>
         *
         * <b>Attention:</b> If you add {@link Scope#LINKED_NOTEBOOKS} or {@link Scope#BUSINESS} and
         * don't add a specific {@link LinkedNotebook}, then this search may be very intense.
         *
         * @param scope Add this scope to the search.
         * @see #addLinkedNotebook(LinkedNotebook)
         */
        public Search addScope(Scope scope) {
            mScopes.add(scope);
            return this;
        }

        /**
         * Specify in which notebooks notes are queried. If this linked notebook is a business notebook,
         * {@link Scope#BUSINESS} is automatically added, otherwise {@link Scope#LINKED_NOTEBOOKS} is
         * added.
         *
         * <br>
         * <br>
         *
         * By default no specific linked notebook is defined.
         *
         * @param linkedNotebook The desired linked notebook.
         * @see #addScope(Scope)
         */
        public Search addLinkedNotebook(LinkedNotebook linkedNotebook) {
            if (EvernoteBusinessNotebookHelper.isBusinessNotebook(linkedNotebook)) {
                addScope(Scope.BUSINESS);
                mBusinessNotebooks.add(linkedNotebook);
            } else {
                addScope(Scope.LINKED_NOTEBOOKS);
                mLinkedNotebooks.add(linkedNotebook);
            }
            return this;
        }

        /**
         * If not filter is set, then the default value only sets {@link NoteSortOrder#UPDATED}.
         *
         * @param noteFilter The used filter for all queries.
         */
        public Search setNoteFilter(NoteFilter noteFilter) {
            mNoteFilter = EvernotePreconditions.checkNotNull(noteFilter);
            return this;
        }

        /**
         * If no spec is set, then the default value will include the note's title and notebook GUID.
         *
         * @param resultSpec The used result spec for all queries.
         */
        public Search setResultSpec(NotesMetadataResultSpec resultSpec) {
            mResultSpec = EvernotePreconditions.checkNotNull(resultSpec);
            return this;
        }

        /**
         * The default value is {@code 0}.
         *
         * @param offset The beginning offset for all queries.
         */
        public Search setOffset(int offset) {
            mOffset = EvernotePreconditions.checkArgumentNonnegative(offset, "negative value now allowed");
            return this;
        }

        /**
         * The default value is {@code 10}. Set this value to {@link Integer#MAX_VALUE}, if you want to query
         * all notes. The higher this value the more intense is the whole search.
         *
         * @param maxNotes The maximum note count for all queries.
         */
        public Search setMaxNotes(int maxNotes) {
            mMaxNotes = EvernotePreconditions.checkArgumentPositive(maxNotes, "maxNotes must be greater or equal 1");
            return this;
        }

        /**
         * The default value is {@code 10}.
         *
         * @param pageSize The page size for a single search.
         */
        public Search setPageSize(int pageSize) {
            mPageSize = EvernotePreconditions.checkArgumentPositive(pageSize, "pageSize must be greater or equal 1");
            return this;
        }

        /**
         * The default value is {@code false}.
         *
         * @param ignoreExceptions If {@code true} then most exceptions while running the search are caught and ignored.
         */
        public Search setIgnoreExceptions(boolean ignoreExceptions) {
            mIgnoreExceptions = ignoreExceptions;
            return this;
        }

        private EnumSet<Scope> getScopes() {
            if (mScopes.isEmpty()) {
                mScopes.add(Scope.PERSONAL_NOTES);
            }
            return mScopes;
        }

        private NoteFilter getNoteFilter() {
            if (mNoteFilter == null) {
                mNoteFilter = new NoteFilter();
                mNoteFilter.setOrder(NoteSortOrder.UPDATED.getValue());
            }
            return mNoteFilter;
        }

        private NotesMetadataResultSpec getResultSpec() {
            if (mResultSpec == null) {
                mResultSpec = new NotesMetadataResultSpec();
                mResultSpec.setIncludeTitle(true);
                mResultSpec.setIncludeNotebookGuid(true);
            }
            return mResultSpec;
        }

        // for all scopes
        private int getOffset() {
            if (mOffset < 0) {
                return 0;
            }
            return mOffset;
        }

        private int getMaxNotes() {
            if (mMaxNotes < 0) {
                return 10;
            }
            return mMaxNotes;
        }

        private int getPageSize() {
            if (mPageSize < 0) {
                return 10;
            }
            return mPageSize;
        }

        public boolean isIgnoreExceptions() {
            return mIgnoreExceptions;
        }
    }

    /**
     * A search result.
     */
    public static final class Result {

        private final List<NotesMetadataList> mPersonalResults;
        private final Map<Pair<String, LinkedNotebook>, List<NotesMetadataList>> mLinkedNotebookResults;
        private final Map<Pair<String, LinkedNotebook>, List<NotesMetadataList>> mBusinessResults;

        private NoteRef.Factory mNoteRefFactory;

        private Result(Set<Scope> scopes) {
            mPersonalResults = scopes.contains(Scope.PERSONAL_NOTES) ? new ArrayList<NotesMetadataList>() : null;
            mLinkedNotebookResults = scopes.contains(Scope.LINKED_NOTEBOOKS) ? new HashMap<Pair<String, LinkedNotebook>, List<NotesMetadataList>>() : null;
            mBusinessResults = scopes.contains(Scope.BUSINESS) ? new HashMap<Pair<String, LinkedNotebook>, List<NotesMetadataList>>() : null;

            mNoteRefFactory = new NoteRef.DefaultFactory();
        }

        /**
         * Exchange the factory to control how the {@link NoteRef} instances are created if you receive
         * the results as NoteRef.
         *
         * @param noteRefFactory The new factory to construct the {@link NoteRef} instances.
         */
        public void setNoteRefFactory(@NonNull NoteRef.Factory noteRefFactory) {
            mNoteRefFactory = EvernotePreconditions.checkNotNull(noteRefFactory);
        }

        private void setPersonalResults(List<NotesMetadataList> personalResults) {
            mPersonalResults.addAll(personalResults);
        }

        private void addLinkedNotebookResult(LinkedNotebook linkedNotebook, List<NotesMetadataList> linkedNotebookResult) {
            Pair<String, LinkedNotebook> key = new Pair<>(linkedNotebook.getGuid(), linkedNotebook);
            mLinkedNotebookResults.put(key, linkedNotebookResult);
        }

        private void addBusinessResult(LinkedNotebook linkedNotebook, List<NotesMetadataList> linkedNotebookResult) {
            Pair<String, LinkedNotebook> key = new Pair<>(linkedNotebook.getGuid(), linkedNotebook);
            mBusinessResults.put(key, linkedNotebookResult);
        }

        /**
         * @return All paginated {@link NotesMetadataList}s containing the personal notes. Returns
         * {@code null}, if {@link Scope#PERSONAL_NOTES} wasn't set.
         */
        public List<NotesMetadataList> getPersonalResults() {
            return mPersonalResults;
        }

        /**
         * @return All personal notes. Returns {@code null}, if {@link Scope#PERSONAL_NOTES} wasn't set.
         */
        public List<NoteRef> getPersonalResultsAsNoteRef() {
            if (mPersonalResults == null) {
                return null;
            }

            List<NoteRef> result = new ArrayList<>();
            fillNoteRef(mPersonalResults, result, null);
            return result;
        }

        /**
         * @return All linked notebooks with their paginated search result. The key in the returned
         * map consists of the {@link LinkedNotebook} and its GUID. Returns {@code null}, if
         * {@link Scope#LINKED_NOTEBOOKS} wasn't set.
         */
        public Map<Pair<String, LinkedNotebook>, List<NotesMetadataList>> getLinkedNotebookResults() {
            return mLinkedNotebookResults;
        }

        /**
         * @return All linked notes. Returns {@code null}, if {@link Scope#LINKED_NOTEBOOKS} wasn't set.
         */
        public List<NoteRef> getLinkedNotebookResultsAsNoteRef() {
            if (mLinkedNotebookResults == null) {
                return null;
            }

            List<NoteRef> result = new ArrayList<>();

            for (Pair<String, LinkedNotebook> key : mLinkedNotebookResults.keySet()) {
                List<NotesMetadataList> notesMetadataLists = mLinkedNotebookResults.get(key);
                fillNoteRef(notesMetadataLists, result, key.second);
            }

            return result;
        }

        /**
         * @return All business notebooks with their paginated search result. The key in the returned
         * map consists of the business notebook and its GUID. Returns {@code null}, if
         * {@link Scope#BUSINESS} wasn't set.
         */
        public Map<Pair<String, LinkedNotebook>, List<NotesMetadataList>> getBusinessResults() {
            return mBusinessResults;
        }

        /**
         * @return All business notes. Returns {@code null}, if {@link Scope#BUSINESS} wasn't set.
         */
        public List<NoteRef> getBusinessResultsAsNoteRef() {
            if (mBusinessResults == null) {
                return null;
            }

            List<NoteRef> result = new ArrayList<>();

            for (Pair<String, LinkedNotebook> key : mBusinessResults.keySet()) {
                List<NotesMetadataList> notesMetadataLists = mBusinessResults.get(key);
                fillNoteRef(notesMetadataLists, result, key.second);
            }

            return result;
        }

        /**
         * @return All personal, linked and business notes. Never returns {@code null}, if no results
         * were found then the list is empty.
         */
        public List<NoteRef> getAllAsNoteRef() {
            List<NoteRef> result = new ArrayList<>();

            List<NoteRef> part = getPersonalResultsAsNoteRef();
            if (part != null) {
                result.addAll(part);
            }

            part = getLinkedNotebookResultsAsNoteRef();
            if (part != null) {
                result.addAll(part);
            }

            part = getBusinessResultsAsNoteRef();
            if (part != null) {
                result.addAll(part);
            }

            return result;
        }

        protected void fillNoteRef(final List<NotesMetadataList> notesMetadataList, final List<NoteRef> result, LinkedNotebook linkedNotebook) {
            for (NotesMetadataList notesMetadataListEntry : notesMetadataList) {
                List<NoteMetadata> notes = notesMetadataListEntry.getNotes();
                for (NoteMetadata note : notes) {
                    NoteRef ref = linkedNotebook == null ? mNoteRefFactory.fromPersonal(note) : mNoteRefFactory.fromLinked(note, linkedNotebook);
                    result.add(ref);
                }
            }
        }
    }
}
