/*
 * Copyright 2012 Evernote Corporation
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, mClient
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    mClient list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.evernote.client.android;

import com.evernote.client.android.asyncclient.EvernoteNoteStoreClient;
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
import com.evernote.thrift.protocol.TProtocol;

import java.util.List;

/**
 * An Async wrapper for {@link NoteStore.Client}.
 * Use these methods with a {@link OnClientCallback} to get make network requests.
 *
 * @author @tylersmithnet
 * @deprecated Use {@link EvernoteNoteStoreClient} instead.
 */
@SuppressWarnings({"UnusedDeclaration", "deprecation", "JavaDoc"})
@Deprecated
public class AsyncNoteStoreClient {

  protected String mAuthenticationToken;
  protected final NoteStore.Client mClient;

  @Deprecated
  AsyncNoteStoreClient(TProtocol prot, String authenticationToken) {
    mClient = new NoteStore.Client(prot);
    mAuthenticationToken = authenticationToken;
  }

  @Deprecated
  AsyncNoteStoreClient(TProtocol iprot, TProtocol oprot, String authenticationToken) {
    mClient = new NoteStore.Client(iprot, oprot);
    mAuthenticationToken = authenticationToken;
  }

  /**
   * If direct access to the Note Store is needed, all of these calls are synchronous.
   * @return {@link NoteStore.Client}
   */
  @Deprecated
  public NoteStore.Client getClient() {
    return mClient;
  }

  /**
   * @return authToken inserted into calls
   */
  @Deprecated
  String getAuthenticationToken() {
    return mAuthenticationToken;
  }

  @Deprecated
  void setAuthToken(String authenticationToken) {
    mAuthenticationToken = authenticationToken;
  }

  /*
   * Async wrappers for NoteStore.Client Methods.
   */

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code.
   * @see NoteStore.Client#getSyncState(String)
   */
  @Deprecated
  public void getSyncState(OnClientCallback<SyncState> callback) {
    AsyncReflector.execute(mClient, callback, "getSyncState", mAuthenticationToken);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client##getSyncStateWithMetrics(com.evernote.edam.notestore.ClientUsageMetrics, OnClientCallback)
   */
  @Deprecated
  public void getSyncStateWithMetrics(ClientUsageMetrics clientMetrics, OnClientCallback<SyncState> callback) {
    AsyncReflector.execute(mClient, callback, "getSyncStateWithMetrics", mAuthenticationToken, clientMetrics);
  }


  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#getSyncChunk(String, int, int, boolean)
   */
  @Deprecated
  public void getSyncChunk(int afterUSN, int maxEntries, boolean fullSyncOnly, OnClientCallback<SyncChunk> callback) {
    AsyncReflector.execute(mClient, callback, "getSyncChunk", mAuthenticationToken, afterUSN, maxEntries, fullSyncOnly);
  }


  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#getFilteredSyncChunk(String, int, int, com.evernote.edam.notestore.SyncChunkFilter)
   */
  @Deprecated public void getFilteredSyncChunk(int afterUSN, int maxEntries, SyncChunkFilter filter, OnClientCallback<SyncChunk> callback) {
    AsyncReflector.execute(mClient, callback, "getFilteredSyncChunk", mAuthenticationToken, afterUSN, maxEntries, filter);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#getLinkedNotebookSyncState(String, com.evernote.edam.type.LinkedNotebook)
   */
  @Deprecated public void getLinkedNotebookSyncState(LinkedNotebook linkedNotebook, OnClientCallback<SyncState> callback) {
    AsyncReflector.execute(mClient, callback, "getLinkedNotebookSyncState", mAuthenticationToken, linkedNotebook);
  }


  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#getLinkedNotebookSyncChunk(String, com.evernote.edam.type.LinkedNotebook, int, int, boolean)
   */
  @Deprecated public void getLinkedNotebookSyncChunk(LinkedNotebook linkedNotebook, int afterUSN, int maxEntries, boolean fullSyncOnly, OnClientCallback<SyncChunk> callback) {
    AsyncReflector.execute(mClient, callback, "getLinkedNotebookSyncChunk", mAuthenticationToken, linkedNotebook, afterUSN, maxEntries, fullSyncOnly);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#listNotebooks(String)
   */
  @Deprecated public void listNotebooks(OnClientCallback<List<Notebook>> callback) {
    AsyncReflector.execute(mClient, callback, "listNotebooks", mAuthenticationToken);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#getNotebook(String, String)
   */
  @Deprecated public void getNotebook(String guid, OnClientCallback<Notebook> callback) {
    AsyncReflector.execute(mClient, callback, "getNotebook", mAuthenticationToken, guid);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#getDefaultNotebook(String)
   */
  @Deprecated public void getDefaultNotebook(OnClientCallback<Notebook> callback) {
    AsyncReflector.execute(mClient, callback, "getDefaultNotebook", mAuthenticationToken);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#createNotebook(String, com.evernote.edam.type.Notebook)
   */
  @Deprecated public void createNotebook(Notebook notebook, OnClientCallback<Notebook> callback) {
    AsyncReflector.execute(mClient, callback, "createNotebook", mAuthenticationToken, notebook);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#updateNotebook(String, com.evernote.edam.type.Notebook)
   */
  @Deprecated public void updateNotebook(Notebook notebook, OnClientCallback<Integer> callback) {
    AsyncReflector.execute(mClient, callback, "updateNotebook", mAuthenticationToken, notebook);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#expungeNotebook(String, String)
   */
  @Deprecated public void expungeNotebook(String guid, OnClientCallback<Integer> callback) {
    AsyncReflector.execute(mClient, callback, "expungeNotebook", mAuthenticationToken, guid);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#listTags(String)
   */
  @Deprecated public void listTags(OnClientCallback<List<Tag>> callback) {
    AsyncReflector.execute(mClient, callback, "listTags", mAuthenticationToken);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#listTagsByNotebook(String, String)
   */
  @Deprecated public void listTagsByNotebook(String notebookGuid, OnClientCallback<List<Tag>> callback) {
    AsyncReflector.execute(mClient, callback, "listTagsByNotebook", mAuthenticationToken, notebookGuid);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#getTag(String, String)
   */
  @Deprecated public void getTag(String guid, OnClientCallback<Tag> callback) {
    AsyncReflector.execute(mClient, callback, "getTag", mAuthenticationToken, guid);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#createTag(String, com.evernote.edam.type.Tag)
   */
  @Deprecated public void createTag(Tag tag, OnClientCallback<Tag> callback) {
    AsyncReflector.execute(mClient, callback, "createTag", mAuthenticationToken, tag);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#updateTag(String, com.evernote.edam.type.Tag)
   */
  @Deprecated public void updateTag(Tag tag, OnClientCallback<Integer> callback) {
    AsyncReflector.execute(mClient, callback, "updateTag", mAuthenticationToken, tag);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#untagAll(String, String)
   */
  @Deprecated public void untagAll(String guid, OnClientCallback<Integer> callback) {
    AsyncReflector.execute(mClient, callback, "untagAll", mAuthenticationToken, guid);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#expungeTag(String, String)
   */
  @Deprecated public void expungeTag(String guid, OnClientCallback<Integer> callback) {
    AsyncReflector.execute(mClient, callback, "expungeTag", mAuthenticationToken, guid);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#listSearches(String)
   */
  @Deprecated public void listSearches(OnClientCallback<List<SavedSearch>> callback) {
    AsyncReflector.execute(mClient, callback, "listSearches", mAuthenticationToken);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#getSearch(String, String)
   */
  @Deprecated public void getSearch(String guid, OnClientCallback<SavedSearch> callback) {
    AsyncReflector.execute(mClient, callback, "getSearch", mAuthenticationToken, guid);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#createSearch(String, com.evernote.edam.type.SavedSearch)
   */
  @Deprecated public void createSearch(SavedSearch search, OnClientCallback<SavedSearch> callback) {
    AsyncReflector.execute(mClient, callback, "createSearch", mAuthenticationToken, search)
    ;
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#updateSearch(String, com.evernote.edam.type.SavedSearch)
   */
  @Deprecated public void updateSearch(SavedSearch search, OnClientCallback<Integer> callback) {
    AsyncReflector.execute(mClient, callback, "updateSearch", mAuthenticationToken, search);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#expungeSearch(String, String)
   */
  @Deprecated public void expungeSearch(String guid, OnClientCallback<Integer> callback) {
    AsyncReflector.execute(mClient, callback, "expungeSearch", mAuthenticationToken, guid);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#findNotes(String, com.evernote.edam.notestore.NoteFilter, int, int)
   */
  @Deprecated public void findNotes(NoteFilter filter, int offset, int maxNotes, OnClientCallback<NoteList> callback) {
    AsyncReflector.execute(mClient, callback, "findNotes", mAuthenticationToken, filter, offset, maxNotes);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#findNoteOffset(String, com.evernote.edam.notestore.NoteFilter, String)
   */
  @Deprecated public void findNoteOffset(NoteFilter filter, String guid, OnClientCallback<Integer> callback) {
    AsyncReflector.execute(mClient, callback, "findNoteOffset", mAuthenticationToken, filter, guid);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#findNotesMetadata(String, com.evernote.edam.notestore.NoteFilter, int, int, com.evernote.edam.notestore.NotesMetadataResultSpec)
   */
  @Deprecated public void findNotesMetadata(NoteFilter filter, int offset, int maxNotes, NotesMetadataResultSpec resultSpec, OnClientCallback<NotesMetadataList> callback) {
    AsyncReflector.execute(mClient, callback, "findNotesMetadata", mAuthenticationToken, filter, offset, maxNotes, resultSpec);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#findNoteCounts(String, com.evernote.edam.notestore.NoteFilter, boolean)
   */
  @Deprecated public void findNoteCounts(NoteFilter filter, boolean withTrash, OnClientCallback<NoteCollectionCounts> callback) {
    AsyncReflector.execute(mClient, callback, "findNoteCounts", mAuthenticationToken, filter, withTrash);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#getNote(String, String, boolean, boolean, boolean, boolean)
   */
  @Deprecated public void getNote(String guid, boolean withContent, boolean withResourcesData, boolean withResourcesRecognition,
                      boolean withResourcesAlternateData, OnClientCallback<Note> callback) {
    AsyncReflector.execute(mClient, callback, "getNote", mAuthenticationToken, guid, withContent, withResourcesData, withResourcesRecognition, withResourcesAlternateData);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#getNoteApplicationData(String, String)
   */
  @Deprecated public void getNoteApplicationData(String guid, OnClientCallback<LazyMap> callback) {
    AsyncReflector.execute(mClient, callback, "getNoteApplicationData", mAuthenticationToken, guid);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#getNoteApplicationDataEntry(String, String, String)
   */
  @Deprecated public void getNoteApplicationDataEntry(String guid, String key, OnClientCallback<String> callback) {
    AsyncReflector.execute(mClient, callback, "getNoteApplicationDataEntry", mAuthenticationToken, guid, key);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#setNoteApplicationDataEntry(String, String, String, String)
   */
  @Deprecated public void setNoteApplicationDataEntry(String guid, String key, String value, OnClientCallback<Integer> callback) {
    AsyncReflector.execute(mClient, callback, "setNoteApplicationDataEntry", mAuthenticationToken, guid, key, value);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#unsetNoteApplicationDataEntry(String, String, String)
   */
  @Deprecated public void unsetNoteApplicationDataEntry(String guid, String key, OnClientCallback<Integer> callback) {
    AsyncReflector.execute(mClient, callback, "unsetNoteApplicationDataEntry", mAuthenticationToken, guid, key);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#getNoteContent(String, String)
   */
  @Deprecated public void getNoteContent(String guid, OnClientCallback<String> callback) {
    AsyncReflector.execute(mClient, callback, "getNoteContent", mAuthenticationToken, guid);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#getNoteSearchText(String, String, boolean, boolean)
   */
  @Deprecated public void getNoteSearchText(String guid, boolean noteOnly, boolean tokenizeForIndexing, OnClientCallback<String> callback) {
    AsyncReflector.execute(mClient, callback, "getNoteSearchText", mAuthenticationToken, guid, noteOnly, tokenizeForIndexing);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#getResourceSearchText(String, String)
   */
  @Deprecated public void getResourceSearchText(String guid, OnClientCallback<String> callback) {
    AsyncReflector.execute(mClient, callback, "getResourceSearchText", mAuthenticationToken, guid);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#getNoteTagNames(String, String)
   */
  @Deprecated public void getNoteTagNames(String guid, OnClientCallback<List<String>> callback) {
    AsyncReflector.execute(mClient, callback, "getNoteTagNames", mAuthenticationToken, guid);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#createNote(String, com.evernote.edam.type.Note)
   */
  @Deprecated public void createNote(Note note, OnClientCallback<Note> callback) {
    AsyncReflector.execute(mClient, callback, "createNote", mAuthenticationToken, note);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#updateNote(String, com.evernote.edam.type.Note)
   */
  @Deprecated public void updateNote(Note note, OnClientCallback<Note> callback) {
    AsyncReflector.execute(mClient, callback, "updateNote", mAuthenticationToken, note);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#deleteNote(String, String)
   */
  @Deprecated public void deleteNote(String guid, OnClientCallback<Integer> callback) {
    AsyncReflector.execute(mClient, callback, "deleteNote", mAuthenticationToken, guid);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#expungeNote(String, String)
   */
  @Deprecated public void expungeNote(String guid, OnClientCallback<Integer> callback) {
    AsyncReflector.execute(mClient, callback, "expungeNote", mAuthenticationToken, guid);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#expungeNotes(String, java.util.List)
   */
  @Deprecated public void expungeNotes(List<String> noteGuids, OnClientCallback<Integer> callback) {
    AsyncReflector.execute(mClient, callback, "expungeNotes", mAuthenticationToken, noteGuids)
    ;
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#expungeInactiveNotes(String)
   */
  @Deprecated public void expungeInactiveNotes(OnClientCallback<Integer> callback) {
    AsyncReflector.execute(mClient, callback, "expungeInactiveNotes", mAuthenticationToken);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#copyNote(String, String, String)
   */
  @Deprecated public void copyNote(String noteGuid, String toNotebookGuid, OnClientCallback<Note> callback) {
    AsyncReflector.execute(mClient, callback, "copyNote", mAuthenticationToken, noteGuid, toNotebookGuid);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#listNoteVersions(String, String)
   */
  @Deprecated public void listNoteVersions(String noteGuid, OnClientCallback<List<NoteVersionId>> callback) {
    AsyncReflector.execute(mClient, callback, "listNoteVersions", mAuthenticationToken, noteGuid);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#getNoteVersion(String, String, int, boolean, boolean, boolean)
   */
  @Deprecated public void getNoteVersion(String noteGuid, int updateSequenceNum, boolean withResourcesData, boolean withResourcesRecognition,
                             boolean withResourcesAlternateData, OnClientCallback<Note> callback) {
    AsyncReflector.execute(mClient, callback, "getNoteVersion", mAuthenticationToken, noteGuid, updateSequenceNum, withResourcesData, withResourcesRecognition,
        withResourcesAlternateData);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#getResource(String, String, boolean, boolean, boolean, boolean)
   */
  @Deprecated public void getResource(String guid, boolean withData, boolean withRecognition, boolean withAttributes, boolean withAlternateData,
                          OnClientCallback<Resource> callback) {
    AsyncReflector.execute(mClient, callback, "getResource", mAuthenticationToken, guid, withData, withRecognition, withAttributes, withAlternateData)
    ;
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#getResourceApplicationData(String, String)
   */
  @Deprecated public void getResourceApplicationData(String guid, OnClientCallback<LazyMap> callback) {
    AsyncReflector.execute(mClient, callback, "getResourceApplicationData", mAuthenticationToken, guid);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#getResourceApplicationDataEntry(String, String, String)
   */
  @Deprecated public void getResourceApplicationDataEntry(String guid, String key, OnClientCallback<String> callback) {
    AsyncReflector.execute(mClient, callback, "getResourceApplicationDataEntry", mAuthenticationToken, guid, key);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#setResourceApplicationDataEntry(String, String, String, String)
   */
  @Deprecated public void setResourceApplicationDataEntry(String guid, String key, String value, OnClientCallback<Integer> callback) {
    AsyncReflector.execute(mClient, callback, "setResourceApplicationDataEntry", mAuthenticationToken, guid, key, value);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#unsetResourceApplicationDataEntry(String, String, String)
   */
  @Deprecated public void unsetResourceApplicationDataEntry(String guid, String key, OnClientCallback<Integer> callback) {
    AsyncReflector.execute(mClient, callback, "unsetResourceApplicationDataEntry", mAuthenticationToken, guid, key);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#updateResource(String, com.evernote.edam.type.Resource)
   */
  @Deprecated public void updateResource(Resource resource, OnClientCallback<Integer> callback) {
    AsyncReflector.execute(mClient, callback, "updateResource", mAuthenticationToken, resource);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#getResourceData(String, String)
   */
  @Deprecated public void getResourceData(String guid, OnClientCallback<byte[]> callback) {
    AsyncReflector.execute(mClient, callback, "getResourceData", mAuthenticationToken, guid);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#getResourceByHash(String, String, byte[], boolean, boolean, boolean)
   */
  @Deprecated public void getResourceByHash(String noteGuid, byte[] contentHash, boolean withData, boolean withRecognition,
                                            boolean withAlternateData, OnClientCallback<Resource> callback) {
    AsyncReflector.execute(mClient, callback, "getResourceByHash", mAuthenticationToken, noteGuid, contentHash, withData, withRecognition, withAlternateData);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#getResourceRecognition(String, String)
   */
  @Deprecated public void getResourceRecognition(String guid, OnClientCallback<byte[]> callback) {
    AsyncReflector.execute(mClient, callback, "getResourceRecognition", mAuthenticationToken);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#getResourceAlternateData(String, String)
   */
  @Deprecated public void getResourceAlternateData(String guid, OnClientCallback<byte[]> callback) {
    AsyncReflector.execute(mClient, callback, "getResourceAlternateData", mAuthenticationToken, guid)
    ;
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#getResourceAttributes(String, String)
   */
  @Deprecated public void getResourceAttributes(String guid, OnClientCallback<ResourceAttributes> callback) {
    AsyncReflector.execute(mClient, callback, "getResourceAttributes", mAuthenticationToken, guid);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#getPublicNotebook(int, String)
   */
  @Deprecated public void getPublicNotebook(int userId, String publicUri, OnClientCallback<Notebook> callback) {
    AsyncReflector.execute(mClient, callback, "getPublicNotebook", mAuthenticationToken, userId, publicUri);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#createSharedNotebook(String, com.evernote.edam.type.SharedNotebook)
   */
  @Deprecated public void createSharedNotebook(SharedNotebook sharedNotebook, OnClientCallback<SharedNotebook> callback) {
    AsyncReflector.execute(mClient, callback, "createSharedNotebook", mAuthenticationToken, sharedNotebook);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#updateSharedNotebook(String, com.evernote.edam.type.SharedNotebook)
   */
  @Deprecated public void updateSharedNotebook(SharedNotebook sharedNotebook, OnClientCallback<Integer> callback) {
    AsyncReflector.execute(mClient, callback, "updateSharedNotebook", mAuthenticationToken, sharedNotebook);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#sendMessageToSharedNotebookMembers(String, String, String, java.util.List)
   */
  @Deprecated public void sendMessageToSharedNotebookMembers(String notebookGuid, String messageText, List<String> recipients, OnClientCallback<Integer> callback) {
    AsyncReflector.execute(mClient, callback, "sendMessageToSharedNotebookMembers", mAuthenticationToken, notebookGuid, messageText, recipients);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#listSharedNotebooks(String)
   */
  @Deprecated public void listSharedNotebooks(OnClientCallback<List<SharedNotebook>> callback) {
    AsyncReflector.execute(mClient, callback, "listSharedNotebooks", mAuthenticationToken);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#expungeSharedNotebooks(String, java.util.List)
   */
  @Deprecated public void expungeSharedNotebooks(List<Long> sharedNotebookIds, OnClientCallback<Integer> callback) {
    AsyncReflector.execute(mClient, callback, "expungeSharedNotebooks", mAuthenticationToken, sharedNotebookIds);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#createLinkedNotebook(String, com.evernote.edam.type.LinkedNotebook)
   */
  @Deprecated public void createLinkedNotebook(LinkedNotebook linkedNotebook, OnClientCallback<LinkedNotebook> callback) {
    AsyncReflector.execute(mClient, callback, "createLinkedNotebook", mAuthenticationToken, linkedNotebook);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#updateLinkedNotebook(String, com.evernote.edam.type.LinkedNotebook)
   */
  @Deprecated public void updateLinkedNotebook(LinkedNotebook linkedNotebook, OnClientCallback<Integer> callback) {
    AsyncReflector.execute(mClient, callback, "updateLinkedNotebook", mAuthenticationToken, linkedNotebook);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#listLinkedNotebooks(String)
   */
  @Deprecated public void listLinkedNotebooks(OnClientCallback<List<LinkedNotebook>> callback) {
    AsyncReflector.execute(mClient, callback, "listLinkedNotebooks", mAuthenticationToken);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#expungeLinkedNotebook(String, String)
   */
  @Deprecated public void expungeLinkedNotebook(String guid, OnClientCallback<Integer> callback) {
    AsyncReflector.execute(mClient, callback, "expungeLinkedNotebook", mAuthenticationToken);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#authenticateToSharedNotebook(String, String)
   */
  @Deprecated public void authenticateToSharedNotebook(String shareKey, OnClientCallback<AuthenticationResult> callback) {
    AsyncReflector.execute(mClient, callback, "authenticateToSharedNotebook", mAuthenticationToken, shareKey);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#getSharedNotebookByAuth(String)
   */
  @Deprecated public void getSharedNotebookByAuth(OnClientCallback<SharedNotebook> callback) {
    AsyncReflector.execute(mClient, callback, "getSharedNotebookByAuth", mAuthenticationToken);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#emailNote(String, com.evernote.edam.notestore.NoteEmailParameters)
   */
  @Deprecated public void emailNote(NoteEmailParameters parameters, OnClientCallback<Void> callback) {
    AsyncReflector.execute(mClient, callback, "emailNote", mAuthenticationToken, parameters);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#shareNote(String, String)
   */
  @Deprecated public void shareNote(String guid, OnClientCallback<String> callback) {
    AsyncReflector.execute(mClient, callback, "shareNote", mAuthenticationToken, guid);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#stopSharingNote(String, String)
   */
  @Deprecated public void stopSharingNote(String guid, OnClientCallback<Void> callback) {
    AsyncReflector.execute(mClient, callback, "stopSharingNote", mAuthenticationToken, guid);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#authenticateToSharedNote(String, String, String)
   */
  @Deprecated public void authenticateToSharedNote(String guid, String noteKey, String authToken, OnClientCallback<AuthenticationResult> callback) {
    AsyncReflector.execute(mClient, callback, "authenticateToSharedNote", guid, noteKey, authToken);
  }

  /**
   * Asynchronous wrapper.
   *
   * @param callback {@link OnClientCallback} providing an interface to the calling code
   * @see NoteStore.Client#findRelated(String, com.evernote.edam.notestore.RelatedQuery, com.evernote.edam.notestore.RelatedResultSpec)
   */
  @Deprecated public void findRelated(RelatedQuery query, RelatedResultSpec resultSpec, OnClientCallback<RelatedResult> callback) {
    AsyncReflector.execute(mClient, callback, "findRelated", mAuthenticationToken, query, resultSpec);
  }

  /**
   * Asynchronous wrapper.
   *
   * @see NoteStore.Client#setSharedNotebookRecipientSettings(String, long,
   *      SharedNotebookRecipientSettings)
   */
  @Deprecated public void setSharedNotebookRecipientSettings(final long sharedNotebookId,
      final SharedNotebookRecipientSettings recipientSettings, OnClientCallback<Integer> callback)
      throws EDAMUserException, EDAMNotFoundException, EDAMSystemException,
      TException {
    AsyncReflector.execute(mClient, callback, "setSharedNotebookRecipientSettings", mAuthenticationToken, sharedNotebookId, recipientSettings);
  }


}
