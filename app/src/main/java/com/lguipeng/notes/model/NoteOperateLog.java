package com.lguipeng.notes.model;

import net.tsz.afinal.annotation.sqlite.ManyToOne;
import net.tsz.afinal.annotation.sqlite.Table;

import java.io.Serializable;

/**
 * Created by lgp on 2015/5/25.
 */
@Table(name = "note_opr_log")
public class NoteOperateLog implements Serializable {
    private int id;
    private int type;
    private long time;
    @ManyToOne(column = "noteId")
    private Note note;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }
}
