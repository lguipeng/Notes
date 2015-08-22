package com.lguipeng.notes.model;

import android.text.Html;
import android.text.TextUtils;

import com.evernote.client.android.EvernoteUtil;
import com.evernote.edam.type.Note;
import com.lguipeng.notes.utils.NotesLog;

import net.tsz.afinal.annotation.sqlite.Table;

import java.io.Serializable;

/**
 * Created by lgp on 2015/5/25.
 */
@Table(name = "notes")
public class SNote implements Serializable{
    private int id;
    private String guid;
    private int status;
    private int type;
    private String label;
    private String content;
    private long createTime;
    private long lastOprTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public int getStatus() {
        return status;
    }

    public Status getStatusEnum() {
        return Status.mapValueToStatus(status);
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setStatus(Status status) {
        setStatus(status.getValue());
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getLastOprTime() {
        return lastOprTime;
    }

    public void setLastOprTime(long lastOprTime) {
        this.lastOprTime = lastOprTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setType(NoteType type) {
        setType(type.getValue());
    }

    public NoteType getNoteType() {
        return NoteType.mapValueToStatus(type);
    }

    public boolean hasReadyRemove(){
        return getStatusEnum() == Status.NEED_REMOVE;
    }

    private boolean hasReadyPush(){
        return getStatusEnum() == Status.NEED_PUSH;
    }

    public boolean hasReadyNewPush(){
        if (!hasReadyPush())
            return false;
        if (TextUtils.isEmpty(getGuid())){
            return true;
        }
        return false;
    }

    public boolean hasReadyUpdatePush(){
        if (!hasReadyPush())
            return false;
        if (!TextUtils.isEmpty(getGuid())){
            return true;
        }
        return false;
    }

    public Note parseToNote(){
        Note note = new Note();
        note.setTitle(label);
        note.setContent(convertContentToEvernote());
        return note;
    }

    public void parseFromNote(Note note){
        setCreateTime(note.getCreated());
        setGuid(note.getGuid());
        setStatus(Status.IDLE.getValue());
        setLastOprTime(note.getUpdated());
        setLabel(note.getTitle());
        setContent(convertContentToSnote(note.getContent()));
    }

    private String convertContentToEvernote() {
        String evernoteContent = EvernoteUtil.NOTE_PREFIX
                + getContent().replace("\n", "<br/>")
                + EvernoteUtil.NOTE_SUFFIX;
        NotesLog.d(evernoteContent);
        return evernoteContent;
    }

    private String convertContentToSnote(String content) {
        NotesLog.d(content);
        String snoteContent = Html.fromHtml(content).toString().trim();
        snoteContent = snoteContent.replace("\n\n", "\n");
        NotesLog.d(snoteContent);
        return snoteContent;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[guid:" + guid + ",");
        sb.append("label:" + label + ",");
        sb.append("content:" + content + ",");
        sb.append("type:" + type + "]");
        return sb.toString();
    }

    public enum Status{
        NEED_PUSH(0x00),
        NEED_REMOVE(0x01),
        IDLE(0x02);
        private int mValue;

        Status(int value){
            this.mValue = value;
        }

        public static Status mapValueToStatus(final int value) {
            for (Status status : Status.values()) {
                if (value == status.getValue()) {
                    return status;
                }
            }
            // If run here, return default
            return IDLE;
        }

        public static Status getDefault() {
            return IDLE;
        }

        public int getValue() {
            return mValue;
        }
    }

    public enum NoteType{
        NORMAL(0x00),
        TRASH(0x01);
        private int mValue;

        NoteType(int value){
            this.mValue = value;
        }

        public static NoteType mapValueToStatus(final int value) {
            for (NoteType status : NoteType.values()) {
                if (value == status.getValue()) {
                    return status;
                }
            }
            // If run here, return default
            return NORMAL;
        }

        public static NoteType getDefault() {
            return NORMAL;
        }

        public int getValue() {
            return mValue;
        }
    }
}
