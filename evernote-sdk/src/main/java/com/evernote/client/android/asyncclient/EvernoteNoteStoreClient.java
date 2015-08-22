package com.evernote.client.android.asyncclient;

import android.support.annotation.NonNull;

import com.evernote.client.android.helper.EvernotePreconditions;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.ClientUsageMetrics;
import com.evernote.edam.notestore.NoteCollectionCounts;
import com.evernote.edam.notestore.NoteEmailParameters;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.notestore.NoteVersionId;
import com.evernote.edam.notestore.NotesMetadataList;
import com.evernote.edam.notestore.NotesMetadataResultSpec;
import com.evernote.edam.notestore.RelatedQuery;
import com.evernote.edam.notestore.RelatedResult;
import com.evernote.edam.notestore.RelatedResultSpec;
import com.evernote.edam.notestore.SyncChunk;
import com.evernote.edam.notestore.SyncChunkFilter;
import com.evernote.edam.notestore.SyncState;
import com.evernote.edam.type.LazyMap;
import com.evernote.edam.type.LinkedNotebook;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Resource;
import com.evernote.edam.type.ResourceAttributes;
import com.evernote.edam.type.SavedSearch;
import com.evernote.edam.type.SharedNotebook;
import com.evernote.edam.type.SharedNotebookRecipientSettings;
import com.evernote.edam.type.Tag;
import com.evernote.edam.userstore.AuthenticationResult;
import com.evernote.thrift.TException;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * An async wrapper for {@link NoteStore.Client}. Call {@link EvernoteClientFactory#getNoteStoreClient(String, String)}
 * to get an instance.
 *
 * @author rwondratschek
 * @see NoteStore
 * @see NoteStore.Client
 */
@SuppressWarnings("unused")
public class EvernoteNoteStoreClient extends EvernoteAsyncClient {

    private final NoteStore.Client mClient;
    private final String mAuthenticationToken;

    /*package*/ EvernoteNoteStoreClient(@NonNull NoteStore.Client client, @NonNull String authenticationToken, @NonNull ExecutorService executorService) {
        super(executorService);
        mClient = EvernotePreconditions.checkNotNull(client);
        mAuthenticationToken = EvernotePreconditions.checkNotEmpty(authenticationToken);
    }

    public SyncState getSyncState() throws EDAMUserException, EDAMSystemException, TException {
        return mClient.getSyncState(mAuthenticationToken);
    }

    public Future<SyncState> getSyncStateAsync(EvernoteCallback<SyncState> callback) {
        return submitTask(new Callable<SyncState>() {
            @Override
            public SyncState call() throws Exception {
                return getSyncState();
            }
        }, callback);
    }

    public SyncState getSyncStateWithMetrics(ClientUsageMetrics clientMetrics) throws EDAMUserException, EDAMSystemException, TException {
        return mClient.getSyncStateWithMetrics(mAuthenticationToken, clientMetrics);
    }

    public Future<SyncState> getSyncStateWithMetricsAsync(final ClientUsageMetrics clientMetrics, EvernoteCallback<SyncState> callback) {
        return submitTask(new Callable<SyncState>() {
            @Override
            public SyncState call() throws Exception {
                return getSyncStateWithMetrics(clientMetrics);
            }
        }, callback);
    }

    public SyncChunk getSyncChunk(int afterUSN, int maxEntries, boolean fullSyncOnly) throws EDAMUserException, EDAMSystemException, TException {
        return mClient.getSyncChunk(mAuthenticationToken, afterUSN, maxEntries, fullSyncOnly);
    }

    public Future<SyncChunk> getSyncChunkAsync(final int afterUSN, final int maxEntries, final boolean fullSyncOnly, EvernoteCallback<SyncChunk> callback) {
        return submitTask(new Callable<SyncChunk>() {
            @Override
            public SyncChunk call() throws Exception {
                return getSyncChunk(afterUSN, maxEntries, fullSyncOnly);
            }
        }, callback);
    }

    public SyncChunk getFilteredSyncChunk(int afterUSN, int maxEntries, SyncChunkFilter filter) throws EDAMUserException, EDAMSystemException, TException {
        return mClient.getFilteredSyncChunk(mAuthenticationToken, afterUSN, maxEntries, filter);
    }

    public Future<SyncChunk> getFilteredSyncChunkAsync(final int afterUSN, final int maxEntries, final SyncChunkFilter filter, EvernoteCallback<SyncChunk> callback) {
        return submitTask(new Callable<SyncChunk>() {
            @Override
            public SyncChunk call() throws Exception {
                return getFilteredSyncChunk(afterUSN, maxEntries, filter);
            }
        }, callback);
    }

    public SyncState getLinkedNotebookSyncState(LinkedNotebook linkedNotebook) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.getLinkedNotebookSyncState(mAuthenticationToken, linkedNotebook);
    }

    public Future<SyncState> getLinkedNotebookSyncStateAsync(final LinkedNotebook linkedNotebook, EvernoteCallback<SyncState> callback) {
        return submitTask(new Callable<SyncState>() {
            @Override
            public SyncState call() throws Exception {
                return getLinkedNotebookSyncState(linkedNotebook);
            }
        }, callback);
    }

    public SyncChunk getLinkedNotebookSyncChunk(LinkedNotebook linkedNotebook, int afterUSN, int maxEntries, boolean fullSyncOnly)
            throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {

        return mClient.getLinkedNotebookSyncChunk(mAuthenticationToken, linkedNotebook, afterUSN, maxEntries, fullSyncOnly);
    }

    public Future<SyncChunk> getLinkedNotebookSyncChunkAsync(final LinkedNotebook linkedNotebook, final int afterUSN,
                                                             final int maxEntries, final boolean fullSyncOnly, EvernoteCallback<SyncChunk> callback) {

        return submitTask(new Callable<SyncChunk>() {
            @Override
            public SyncChunk call() throws Exception {
                return getLinkedNotebookSyncChunk(linkedNotebook, afterUSN, maxEntries, fullSyncOnly);
            }
        }, callback);
    }

    public List<Notebook> listNotebooks() throws EDAMUserException, EDAMSystemException, TException {
        return mClient.listNotebooks(mAuthenticationToken);
    }

    public Future<List<Notebook>> listNotebooksAsync(EvernoteCallback<List<Notebook>> callback) {
        return submitTask(new Callable<List<Notebook>>() {
            @Override
            public List<Notebook> call() throws Exception {
                return listNotebooks();
            }
        }, callback);
    }

    public Notebook getNotebook(String guid) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.getNotebook(mAuthenticationToken, guid);
    }

    public Future<Notebook> getNotebookAsync(final String guid, EvernoteCallback<Notebook> callback) {
        return submitTask(new Callable<Notebook>() {
            @Override
            public Notebook call() throws Exception {
                return getNotebook(guid);
            }
        }, callback);
    }

    public Notebook getDefaultNotebook() throws EDAMUserException, EDAMSystemException, TException {
        return mClient.getDefaultNotebook(mAuthenticationToken);
    }

    public Future<Notebook> getDefaultNotebookAsync(EvernoteCallback<Notebook> callback) {
        return submitTask(new Callable<Notebook>() {
            @Override
            public Notebook call() throws Exception {
                return getDefaultNotebook();
            }
        }, callback);
    }

    public Notebook createNotebook(Notebook notebook) throws EDAMUserException, EDAMSystemException, TException {
        return mClient.createNotebook(mAuthenticationToken, notebook);
    }

    public Future<Notebook> createNotebookAsync(final Notebook notebook, EvernoteCallback<Notebook> callback) {
        return submitTask(new Callable<Notebook>() {
            @Override
            public Notebook call() throws Exception {
                return createNotebook(notebook);
            }
        }, callback);
    }

    public int updateNotebook(Notebook notebook) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.updateNotebook(mAuthenticationToken, notebook);
    }

    public Future<Integer> updateNotebookAsync(final Notebook notebook, EvernoteCallback<Integer> callback) {
        return submitTask(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return updateNotebook(notebook);
            }
        }, callback);
    }

    public int expungeNotebook(String guid) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.expungeNotebook(mAuthenticationToken, guid);
    }

    public Future<Integer> expungeNotebookAsync(final String guid, EvernoteCallback<Integer> callback) {
        return submitTask(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return expungeNotebook(guid);
            }
        }, callback);
    }

    public List<Tag> listTags() throws EDAMUserException, EDAMSystemException, TException {
        return mClient.listTags(mAuthenticationToken);
    }

    public Future<List<Tag>> listTagsAsync(EvernoteCallback<List<Tag>> callback) {
        return submitTask(new Callable<List<Tag>>() {
            @Override
            public List<Tag> call() throws Exception {
                return listTags();
            }
        }, callback);
    }

    public List<Tag> listTagsByNotebook(String notebookGuid) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.listTagsByNotebook(mAuthenticationToken, notebookGuid);
    }

    public Future<List<Tag>> listTagsByNotebookAsync(final String notebookGuid, EvernoteCallback<List<Tag>> callback) {
        return submitTask(new Callable<List<Tag>>() {
            @Override
            public List<Tag> call() throws Exception {
                return listTagsByNotebook(notebookGuid);
            }
        }, callback);
    }

    public Tag getTag(String guid) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.getTag(mAuthenticationToken, guid);
    }

    public Future<Tag> getTagAsync(final String guid, EvernoteCallback<Tag> callback) {
        return submitTask(new Callable<Tag>() {
            @Override
            public Tag call() throws Exception {
                return getTag(guid);
            }
        }, callback);
    }

    public Tag createTag(Tag tag) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.createTag(mAuthenticationToken, tag);
    }

    public Future<Tag> createTagAsync(final Tag tag, EvernoteCallback<Tag> callback) {
        return submitTask(new Callable<Tag>() {
            @Override
            public Tag call() throws Exception {
                return createTag(tag);
            }
        }, callback);
    }

    public int updateTag(Tag tag) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.updateTag(mAuthenticationToken, tag);
    }

    public Future<Integer> updateTagAsync(final Tag tag, EvernoteCallback<Integer> callback) {
        return submitTask(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return updateTag(tag);
            }
        }, callback);
    }

    public void untagAll(String guid) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        mClient.untagAll(mAuthenticationToken, guid);
    }

    public Future<Void> untagAllAsync(final String guid, EvernoteCallback<Void> callback) {
        return submitTask(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                untagAll(guid);
                return null;
            }
        }, callback);
    }

    public int expungeTag(String guid) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.expungeTag(mAuthenticationToken, guid);
    }

    public Future<Integer> expungeTagAsync(final String guid, EvernoteCallback<Integer> callback) {
        return submitTask(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return expungeTag(guid);
            }
        }, callback);
    }

    public List<SavedSearch> listSearches() throws EDAMUserException, EDAMSystemException, TException {
        return mClient.listSearches(mAuthenticationToken);
    }

    public Future<List<SavedSearch>> listSearchesAsync(EvernoteCallback<List<SavedSearch>> callback) {
        return submitTask(new Callable<List<SavedSearch>>() {
            @Override
            public List<SavedSearch> call() throws Exception {
                return listSearches();
            }
        }, callback);
    }

    public SavedSearch getSearch(String guid) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.getSearch(mAuthenticationToken, guid);
    }

    public Future<SavedSearch> getSearchAsync(final String guid, EvernoteCallback<SavedSearch> callback) {
        return submitTask(new Callable<SavedSearch>() {
            @Override
            public SavedSearch call() throws Exception {
                return getSearch(guid);
            }
        }, callback);
    }

    public SavedSearch createSearch(SavedSearch search) throws EDAMUserException, EDAMSystemException, TException {
        return mClient.createSearch(mAuthenticationToken, search);
    }

    public Future<SavedSearch> createSearchAsync(final SavedSearch search, EvernoteCallback<SavedSearch> callback) {
        return submitTask(new Callable<SavedSearch>() {
            @Override
            public SavedSearch call() throws Exception {
                return createSearch(search);
            }
        }, callback);
    }

    public int updateSearch(SavedSearch search) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.updateSearch(mAuthenticationToken, search);
    }

    public Future<Integer> updateSearchAsync(final SavedSearch search, EvernoteCallback<Integer> callback) {
        return submitTask(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return updateSearch(search);
            }
        }, callback);
    }

    public int expungeSearch(String guid) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.expungeSearch(mAuthenticationToken, guid);
    }

    public Future<Integer> expungeSearchAsync(final String guid, EvernoteCallback<Integer> callback) {
        return submitTask(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return expungeSearch(guid);
            }
        }, callback);
    }

    public NoteList findNotes(NoteFilter filter, int offset, int maxNotes) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.findNotes(mAuthenticationToken, filter, offset, maxNotes);
    }

    public Future<NoteList> findNotesAsync(final NoteFilter filter, final int offset, final int maxNotes, EvernoteCallback<NoteList> callback) {
        return submitTask(new Callable<NoteList>() {
            @Override
            public NoteList call() throws Exception {
                return findNotes(filter, offset, maxNotes);
            }
        }, callback);
    }

    public int findNoteOffset(NoteFilter filter, String guid) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.findNoteOffset(mAuthenticationToken, filter, guid);
    }

    public Future<Integer> findNoteOffsetAsync(final NoteFilter filter, final String guid, EvernoteCallback<Integer> callback) {
        return submitTask(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return findNoteOffset(filter, guid);
            }
        }, callback);
    }

    public NotesMetadataList findNotesMetadata(NoteFilter filter, int offset, int maxNotes, NotesMetadataResultSpec resultSpec)
            throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {

        return mClient.findNotesMetadata(mAuthenticationToken, filter, offset, maxNotes, resultSpec);
    }

    public Future<NotesMetadataList> findNotesMetadataAsync(final NoteFilter filter, final int offset, final int maxNotes,
                                                            final NotesMetadataResultSpec resultSpec, EvernoteCallback<NotesMetadataList> callback) {

        return submitTask(new Callable<NotesMetadataList>() {
            @Override
            public NotesMetadataList call() throws Exception {
                return findNotesMetadata(filter, offset, maxNotes, resultSpec);
            }
        }, callback);
    }

    public NoteCollectionCounts findNoteCounts(NoteFilter filter, boolean withTrash) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.findNoteCounts(mAuthenticationToken, filter, withTrash);
    }

    public Future<NoteCollectionCounts> findNoteCountsAsync(final NoteFilter filter, final boolean withTrash, EvernoteCallback<NoteCollectionCounts> callback) {
        return submitTask(new Callable<NoteCollectionCounts>() {
            @Override
            public NoteCollectionCounts call() throws Exception {
                return findNoteCounts(filter, withTrash);
            }
        }, callback);
    }

    public Note getNote(String guid, boolean withContent, boolean withResourcesData, boolean withResourcesRecognition,
                        boolean withResourcesAlternateData) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {

        return mClient.getNote(mAuthenticationToken, guid, withContent, withResourcesData, withResourcesRecognition, withResourcesAlternateData);
    }

    public Future<Note> getNoteAsync(final String guid, final boolean withContent, final boolean withResourcesData,
                                     final boolean withResourcesRecognition, final boolean withResourcesAlternateData, EvernoteCallback<Note> callback) {

        return submitTask(new Callable<Note>() {
            @Override
            public Note call() throws Exception {
                return getNote(guid, withContent, withResourcesData, withResourcesRecognition, withResourcesAlternateData);
            }
        }, callback);
    }

    public LazyMap getNoteApplicationData(String guid) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.getNoteApplicationData(mAuthenticationToken, guid);
    }

    public Future<LazyMap> getNoteApplicationDataAsync(final String guid, EvernoteCallback<LazyMap> callback) {
        return submitTask(new Callable<LazyMap>() {
            @Override
            public LazyMap call() throws Exception {
                return getNoteApplicationData(guid);
            }
        }, callback);
    }

    public String getNoteApplicationDataEntry(String guid, String key) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.getNoteApplicationDataEntry(mAuthenticationToken, guid, key);
    }

    public Future<String> getNoteApplicationDataEntryAsync(final String guid, final String key, EvernoteCallback<String> callback) {
        return submitTask(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return getNoteApplicationDataEntry(guid, key);
            }
        }, callback);
    }

    public int setNoteApplicationDataEntry(String guid, String key, String value) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.setNoteApplicationDataEntry(mAuthenticationToken, guid, key, value);
    }

    public Future<Integer> setNoteApplicationDataEntryAsync(final String guid, final String key, final String value, EvernoteCallback<Integer> callback) {
        return submitTask(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return setNoteApplicationDataEntry(guid, key, value);
            }
        }, callback);
    }

    public int unsetNoteApplicationDataEntry(String guid, String key) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.unsetNoteApplicationDataEntry(mAuthenticationToken, guid, key);
    }

    public Future<Integer> unsetNoteApplicationDataEntryAsync(final String guid, final String key, EvernoteCallback<Integer> callback) {
        return submitTask(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return unsetNoteApplicationDataEntry(guid, key);
            }
        }, callback);
    }

    public String getNoteContent(String guid) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.getNoteContent(mAuthenticationToken, guid);
    }

    public Future<String> getNoteContentAsync(final String guid, EvernoteCallback<String> callback) {
        return submitTask(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return getNoteContent(guid);
            }
        }, callback);
    }

    public String getNoteSearchText(String guid, boolean noteOnly, boolean tokenizeForIndexing) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.getNoteSearchText(mAuthenticationToken, guid, noteOnly, tokenizeForIndexing);
    }

    public Future<String> getNoteSearchTextAsync(final String guid, final boolean noteOnly, final boolean tokenizeForIndexing, EvernoteCallback<String> callback) {
        return submitTask(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return getNoteSearchText(guid, noteOnly, tokenizeForIndexing);
            }
        }, callback);
    }

    public String getResourceSearchText(String guid) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.getResourceSearchText(mAuthenticationToken, guid);
    }

    public Future<String> getResourceSearchTextAsync(final String guid, EvernoteCallback<String> callback) {
        return submitTask(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return getResourceSearchText(guid);
            }
        }, callback);
    }

    public List<String> getNoteTagNames(String guid) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.getNoteTagNames(mAuthenticationToken, guid);
    }

    public Future<List<String>> getNoteTagNamesAsync(final String guid, EvernoteCallback<List<String>> callback) {
        return submitTask(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                return getNoteTagNames(guid);
            }
        }, callback);
    }

    public Note createNote(Note note) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.createNote(mAuthenticationToken, note);
    }

    public Future<Note> createNoteAsync(final Note note, EvernoteCallback<Note> callback) {
        return submitTask(new Callable<Note>() {
            @Override
            public Note call() throws Exception {
                return createNote(note);
            }
        }, callback);
    }

    public Note updateNote(Note note) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.updateNote(mAuthenticationToken, note);
    }

    public Future<Note> updateNoteAsync(final Note note, EvernoteCallback<Note> callback) {
        return submitTask(new Callable<Note>() {
            @Override
            public Note call() throws Exception {
                return updateNote(note);
            }
        }, callback);
    }

    public int deleteNote(String guid) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.deleteNote(mAuthenticationToken, guid);
    }

    public Future<Integer> deleteNoteAsync(final String guid, EvernoteCallback<Integer> callback) {
        return submitTask(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return deleteNote(guid);
            }
        }, callback);
    }

    public int expungeNote(String guid) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.expungeNote(mAuthenticationToken, guid);
    }

    public Future<Integer> expungeNoteAsync(final String guid, EvernoteCallback<Integer> callback) {
        return submitTask(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return expungeNote(guid);
            }
        }, callback);
    }

    public int expungeNotes(List<String> noteGuids) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.expungeNotes(mAuthenticationToken, noteGuids);
    }

    public Future<Integer> expungeNotesAsync(final List<String> noteGuids, EvernoteCallback<Integer> callback) {
        return submitTask(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return expungeNotes(noteGuids);
            }
        }, callback);
    }

    public int expungeInactiveNotes() throws EDAMUserException, EDAMSystemException, TException {
        return mClient.expungeInactiveNotes(mAuthenticationToken);
    }

    public Future<Integer> expungeInactiveNotesAsync(EvernoteCallback<Integer> callback) {
        return submitTask(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return expungeInactiveNotes();
            }
        }, callback);
    }

    public Note copyNote(String noteGuid, String toNotebookGuid) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.copyNote(mAuthenticationToken, noteGuid, toNotebookGuid);
    }

    public Future<Note> copyNoteAsync(final String noteGuid, final String toNotebookGuid, EvernoteCallback<Note> callback) {
        return submitTask(new Callable<Note>() {
            @Override
            public Note call() throws Exception {
                return copyNote(noteGuid, toNotebookGuid);
            }
        }, callback);
    }

    public List<NoteVersionId> listNoteVersions(String noteGuid) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.listNoteVersions(mAuthenticationToken, noteGuid);
    }

    public Future<List<NoteVersionId>> listNoteVersionsAsync(final String noteGuid, EvernoteCallback<List<NoteVersionId>> callback) {
        return submitTask(new Callable<List<NoteVersionId>>() {
            @Override
            public List<NoteVersionId> call() throws Exception {
                return listNoteVersions(noteGuid);
            }
        }, callback);
    }

    public Note getNoteVersion(String noteGuid, int updateSequenceNum, boolean withResourcesData, boolean withResourcesRecognition,
                               boolean withResourcesAlternateData) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {

        return mClient.getNoteVersion(mAuthenticationToken, noteGuid, updateSequenceNum, withResourcesData, withResourcesRecognition, withResourcesAlternateData);
    }

    public Future<Note> getNoteVersionAsync(final String noteGuid, final int updateSequenceNum, final boolean withResourcesData,
                                            final boolean withResourcesRecognition, final boolean withResourcesAlternateData, EvernoteCallback<Note> callback) {

        return submitTask(new Callable<Note>() {
            @Override
            public Note call() throws Exception {
                return getNoteVersion(noteGuid, updateSequenceNum, withResourcesData, withResourcesRecognition, withResourcesAlternateData);
            }
        }, callback);
    }

    public Resource getResource(String guid, boolean withData, boolean withRecognition, boolean withAttributes, boolean withAlternateData)
            throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {

        return mClient.getResource(mAuthenticationToken, guid, withData, withRecognition, withAttributes, withAlternateData);
    }

    public Future<Resource> getResourceAsync(final String guid, final boolean withData, final boolean withRecognition, final boolean withAttributes,
                                             final boolean withAlternateData, EvernoteCallback<Resource> callback) {

        return submitTask(new Callable<Resource>() {
            @Override
            public Resource call() throws Exception {
                return getResource(guid, withData, withRecognition, withAttributes, withAlternateData);
            }
        }, callback);
    }

    public LazyMap getResourceApplicationData(String guid) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.getResourceApplicationData(mAuthenticationToken, guid);
    }

    public Future<LazyMap> getResourceApplicationDataAsync(final String guid, EvernoteCallback<LazyMap> callback) {
        return submitTask(new Callable<LazyMap>() {
            @Override
            public LazyMap call() throws Exception {
                return getResourceApplicationData(guid);
            }
        }, callback);
    }

    public String getResourceApplicationDataEntry(String guid, String key) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.getResourceApplicationDataEntry(mAuthenticationToken, guid, key);
    }

    public Future<String> getResourceApplicationDataEntryAsync(final String guid, final String key, EvernoteCallback<String> callback) {
        return submitTask(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return getResourceApplicationDataEntry(guid, key);
            }
        }, callback);
    }

    public int setResourceApplicationDataEntry(String guid, String key, String value) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.setResourceApplicationDataEntry(mAuthenticationToken, guid, key, value);
    }

    public Future<Integer> setResourceApplicationDataEntryAsync(final String guid, final String key, final String value, EvernoteCallback<Integer> callback) {
        return submitTask(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return setResourceApplicationDataEntry(guid, key, value);
            }
        }, callback);
    }

    public int unsetResourceApplicationDataEntry(String guid, String key) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.unsetResourceApplicationDataEntry(mAuthenticationToken, guid, key);
    }

    public Future<Integer> unsetResourceApplicationDataEntryAsync(final String guid, final String key, EvernoteCallback<Integer> callback) {
        return submitTask(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return unsetResourceApplicationDataEntry(guid, key);
            }
        }, callback);
    }

    public int updateResource(Resource resource) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.updateResource(mAuthenticationToken, resource);
    }

    public Future<Integer> updateResourceAsync(final Resource resource, EvernoteCallback<Integer> callback) {
        return submitTask(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return updateResource(resource);
            }
        }, callback);
    }

    public byte[] getResourceData(String guid) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.getResourceData(mAuthenticationToken, guid);
    }

    public Future<byte[]> getResourceDataAsync(final String guid, EvernoteCallback<byte[]> callback) {
        return submitTask(new Callable<byte[]>() {
            @Override
            public byte[] call() throws Exception {
                return getResourceData(guid);
            }
        }, callback);
    }

    public Resource getResourceByHash(String noteGuid, byte[] contentHash, boolean withData, boolean withRecognition, boolean withAlternateData)
            throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {

        return mClient.getResourceByHash(mAuthenticationToken, noteGuid, contentHash, withData, withRecognition, withAlternateData);
    }

    public Future<Resource> getResourceByHashAsync(final String noteGuid, final byte[] contentHash, final boolean withData,
                                                   final boolean withRecognition, final boolean withAlternateData, EvernoteCallback<Resource> callback) {

        return submitTask(new Callable<Resource>() {
            @Override
            public Resource call() throws Exception {
                return getResourceByHash(noteGuid, contentHash, withData, withRecognition, withAlternateData);
            }
        }, callback);
    }

    public byte[] getResourceRecognition(String guid) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.getResourceRecognition(mAuthenticationToken, guid);
    }

    public Future<byte[]> getResourceRecognitionAsync(final String guid, EvernoteCallback<byte[]> callback) {
        return submitTask(new Callable<byte[]>() {
            @Override
            public byte[] call() throws Exception {
                return getResourceRecognition(guid);
            }
        }, callback);
    }

    public byte[] getResourceAlternateData(String guid) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.getResourceAlternateData(mAuthenticationToken, guid);
    }

    public Future<byte[]> getResourceAlternateDataAsync(final String guid, EvernoteCallback<byte[]> callback) {
        return submitTask(new Callable<byte[]>() {
            @Override
            public byte[] call() throws Exception {
                return getResourceAlternateData(guid);
            }
        }, callback);
    }

    public ResourceAttributes getResourceAttributes(String guid) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.getResourceAttributes(mAuthenticationToken, guid);
    }

    public Future<ResourceAttributes> getResourceAttributesAsync(final String guid, EvernoteCallback<ResourceAttributes> callback) {
        return submitTask(new Callable<ResourceAttributes>() {
            @Override
            public ResourceAttributes call() throws Exception {
                return getResourceAttributes(guid);
            }
        }, callback);
    }

    public Notebook getPublicNotebook(int userId, String publicUri) throws EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.getPublicNotebook(userId, publicUri);
    }

    public Future<Notebook> getPublicNotebookAsync(final int userId, final String publicUri, EvernoteCallback<Notebook> callback) {
        return submitTask(new Callable<Notebook>() {
            @Override
            public Notebook call() throws Exception {
                return getPublicNotebook(userId, publicUri);
            }
        }, callback);
    }

    public SharedNotebook createSharedNotebook(SharedNotebook sharedNotebook) throws EDAMUserException, EDAMNotFoundException, EDAMSystemException, TException {
        return mClient.createSharedNotebook(mAuthenticationToken, sharedNotebook);
    }

    public Future<SharedNotebook> createSharedNotebookAsync(final SharedNotebook sharedNotebook, EvernoteCallback<SharedNotebook> callback) {
        return submitTask(new Callable<SharedNotebook>() {
            @Override
            public SharedNotebook call() throws Exception {
                return createSharedNotebook(sharedNotebook);
            }
        }, callback);
    }

    public int updateSharedNotebook(SharedNotebook sharedNotebook) throws EDAMUserException, EDAMNotFoundException, EDAMSystemException, TException {
        return mClient.updateSharedNotebook(mAuthenticationToken, sharedNotebook);
    }

    public Future<Integer> updateSharedNotebookAsync(final SharedNotebook sharedNotebook, EvernoteCallback<Integer> callback) {
        return submitTask(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return updateSharedNotebook(sharedNotebook);
            }
        }, callback);
    }

    public int setSharedNotebookRecipientSettings(long sharedNotebookId, SharedNotebookRecipientSettings recipientSettings)
            throws EDAMUserException, EDAMNotFoundException, EDAMSystemException, TException {

        return mClient.setSharedNotebookRecipientSettings(mAuthenticationToken, sharedNotebookId, recipientSettings);
    }

    public Future<Integer> setSharedNotebookRecipientSettingsAsync(final long sharedNotebookId, final SharedNotebookRecipientSettings recipientSettings,
                                                                   EvernoteCallback<Integer> callback) {

        return submitTask(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return setSharedNotebookRecipientSettings(sharedNotebookId, recipientSettings);
            }
        }, callback);
    }

    public int sendMessageToSharedNotebookMembers(String notebookGuid, String messageText, List<String> recipients) throws EDAMUserException,
            EDAMNotFoundException, EDAMSystemException, TException {

        return mClient.sendMessageToSharedNotebookMembers(mAuthenticationToken, notebookGuid, messageText, recipients);
    }

    public Future<Integer> sendMessageToSharedNotebookMembersAsync(final String notebookGuid, final String messageText,
                                                                   final List<String> recipients, EvernoteCallback<Integer> callback) {

        return submitTask(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return sendMessageToSharedNotebookMembers(notebookGuid, messageText, recipients);
            }
        }, callback);
    }

    public List<SharedNotebook> listSharedNotebooks() throws EDAMUserException, EDAMNotFoundException, EDAMSystemException, TException {
        return mClient.listSharedNotebooks(mAuthenticationToken);
    }

    public Future<List<SharedNotebook>> listSharedNotebooksAsync(EvernoteCallback<List<SharedNotebook>> callback) {
        return submitTask(new Callable<List<SharedNotebook>>() {
            @Override
            public List<SharedNotebook> call() throws Exception {
                return listSharedNotebooks();
            }
        }, callback);
    }

    public int expungeSharedNotebooks(List<Long> sharedNotebookIds) throws EDAMUserException, EDAMNotFoundException, EDAMSystemException, TException {
        return mClient.expungeSharedNotebooks(mAuthenticationToken, sharedNotebookIds);
    }

    public Future<Integer> expungeSharedNotebooksAsync(final List<Long> sharedNotebookIds, EvernoteCallback<Integer> callback) {
        return submitTask(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return expungeSharedNotebooks(sharedNotebookIds);
            }
        }, callback);
    }

    public LinkedNotebook createLinkedNotebook(LinkedNotebook linkedNotebook) throws EDAMUserException, EDAMNotFoundException, EDAMSystemException, TException {
        return mClient.createLinkedNotebook(mAuthenticationToken, linkedNotebook);
    }

    public Future<LinkedNotebook> createLinkedNotebookAsync(final LinkedNotebook linkedNotebook, EvernoteCallback<LinkedNotebook> callback) {
        return submitTask(new Callable<LinkedNotebook>() {
            @Override
            public LinkedNotebook call() throws Exception {
                return createLinkedNotebook(linkedNotebook);
            }
        }, callback);
    }

    public int updateLinkedNotebook(LinkedNotebook linkedNotebook) throws EDAMUserException, EDAMNotFoundException, EDAMSystemException, TException {
        return mClient.updateLinkedNotebook(mAuthenticationToken, linkedNotebook);
    }

    public Future<Integer> updateLinkedNotebookAsync(final LinkedNotebook linkedNotebook, EvernoteCallback<Integer> callback) {
        return submitTask(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return updateLinkedNotebook(linkedNotebook);
            }
        }, callback);
    }

    public List<LinkedNotebook> listLinkedNotebooks() throws EDAMUserException, EDAMNotFoundException, EDAMSystemException, TException {
        return mClient.listLinkedNotebooks(mAuthenticationToken);
    }

    public Future<List<LinkedNotebook>> listLinkedNotebooksAsync(EvernoteCallback<List<LinkedNotebook>> callback) {
        return submitTask(new Callable<List<LinkedNotebook>>() {
            @Override
            public List<LinkedNotebook> call() throws Exception {
                return listLinkedNotebooks();
            }
        }, callback);
    }

    public int expungeLinkedNotebook(String guid) throws EDAMUserException, EDAMNotFoundException, EDAMSystemException, TException {
        return mClient.expungeLinkedNotebook(mAuthenticationToken, guid);
    }

    public Future<Integer> expungeLinkedNotebookAsync(final String guid, EvernoteCallback<Integer> callback) {
        return submitTask(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return expungeLinkedNotebook(guid);
            }
        }, callback);
    }

    public AuthenticationResult authenticateToSharedNotebook(String shareKey) throws EDAMUserException, EDAMNotFoundException, EDAMSystemException, TException {
        return mClient.authenticateToSharedNotebook(shareKey, mAuthenticationToken);
    }

    public Future<AuthenticationResult> authenticateToSharedNotebookAsync(final String shareKey, EvernoteCallback<AuthenticationResult> callback) {
        return submitTask(new Callable<AuthenticationResult>() {
            @Override
            public AuthenticationResult call() throws Exception {
                return authenticateToSharedNotebook(shareKey);
            }
        }, callback);
    }

    public SharedNotebook getSharedNotebookByAuth() throws EDAMUserException, EDAMNotFoundException, EDAMSystemException, TException {
        return mClient.getSharedNotebookByAuth(mAuthenticationToken);
    }

    public Future<SharedNotebook> getSharedNotebookByAuthAsync(EvernoteCallback<SharedNotebook> callback) {
        return submitTask(new Callable<SharedNotebook>() {
            @Override
            public SharedNotebook call() throws Exception {
                return getSharedNotebookByAuth();
            }
        }, callback);
    }

    public void emailNote(NoteEmailParameters parameters) throws EDAMUserException, EDAMNotFoundException, EDAMSystemException, TException {
        mClient.emailNote(mAuthenticationToken, parameters);
    }

    public Future<Void> emailNoteAsync(final NoteEmailParameters parameters, EvernoteCallback<Void> callback) {
        return submitTask(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                emailNote(parameters);
                return null;
            }
        }, callback);
    }

    public String shareNote(String guid) throws EDAMUserException, EDAMNotFoundException, EDAMSystemException, TException {
        return mClient.shareNote(mAuthenticationToken, guid);
    }

    public Future<String> shareNoteAsync(final String guid, EvernoteCallback<String> callback) {
        return submitTask(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return shareNote(guid);
            }
        }, callback);
    }

    public void stopSharingNote(String guid) throws EDAMUserException, EDAMNotFoundException, EDAMSystemException, TException {
        mClient.stopSharingNote(mAuthenticationToken, guid);
    }

    public Future<Void> stopSharingNoteAsync(final String guid, EvernoteCallback<Void> callback) {
        return submitTask(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                stopSharingNote(guid);
                return null;
            }
        }, callback);
    }

    public AuthenticationResult authenticateToSharedNote(String guid, String noteKey) throws EDAMUserException, EDAMNotFoundException, EDAMSystemException, TException {
        return mClient.authenticateToSharedNote(guid, noteKey, mAuthenticationToken);
    }

    public Future<AuthenticationResult> authenticateToSharedNoteAsync(final String guid, final String noteKey, EvernoteCallback<AuthenticationResult> callback) {
        return submitTask(new Callable<AuthenticationResult>() {
            @Override
            public AuthenticationResult call() throws Exception {
                return authenticateToSharedNote(guid, noteKey);
            }
        }, callback);
    }

    public RelatedResult findRelated(RelatedQuery query, RelatedResultSpec resultSpec) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        return mClient.findRelated(mAuthenticationToken, query, resultSpec);
    }

    public Future<RelatedResult> findRelatedAsync(final RelatedQuery query, final RelatedResultSpec resultSpec, EvernoteCallback<RelatedResult> callback) {
        return submitTask(new Callable<RelatedResult>() {
            @Override
            public RelatedResult call() throws Exception {
                return findRelated(query, resultSpec);
            }
        }, callback);
    }
}
