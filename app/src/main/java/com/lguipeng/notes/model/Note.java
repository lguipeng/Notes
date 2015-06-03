package com.lguipeng.notes.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.lguipeng.notes.utils.JsonUtils;

import net.tsz.afinal.annotation.sqlite.OneToMany;
import net.tsz.afinal.annotation.sqlite.Table;
import net.tsz.afinal.db.sqlite.OneToManyLazyLoader;

import java.io.Serializable;

/**
 * Created by lgp on 2015/5/25.
 */
@Table(name = "notes")
public class Note implements Serializable{
    @JSONField(serialize=false, deserialize=false)
    private int id;
    private int type;
    private String label;
    private String content;
    private long lastOprTime;
    @JSONField(serialize=false, deserialize=false)
    @OneToMany(manyColumn = "noteId")
    private OneToManyLazyLoader<Note ,NoteOperateLog> logs;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public OneToManyLazyLoader<Note, NoteOperateLog> getLogs() {
        return logs;
    }

    public void setLogs(OneToManyLazyLoader<Note, NoteOperateLog> logs) {
        this.logs = logs;
    }

    @Override
    public String toString() {
        return JsonUtils.jsonNote(this);
    }
}
