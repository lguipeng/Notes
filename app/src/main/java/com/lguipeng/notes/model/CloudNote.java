package com.lguipeng.notes.model;

import com.lguipeng.notes.utils.JsonUtils;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobObject;

/**
 * Created by lgp on 2015/5/28.
 */
public class CloudNote extends BmobObject {

    public CloudNote() {
        this("CloudNote");
    }

    public CloudNote(String theClassName) {
        super(theClassName);
    }

    private String email;

    private List<String> noteList = new ArrayList<>();

    private String noteType;

    private long version;

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getNoteType() {
        return noteType;
    }

    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getNoteList() {
        return noteList;
    }

    public void setNoteList(List<String> noteList) {
        this.noteList = noteList;
    }

    public void addNote(Note note) {
        noteList.add(JsonUtils.jsonNote(note));
    }

    public void clearNotes() {
        noteList.clear();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        //sb.append(super.toString());
        //sb.append("\n");
        for (String note : noteList){
            sb.append(note);
            sb.append("\n");
        }
        return sb.toString();
    }
}
