package com.lguipeng.notes.utils;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.lguipeng.notes.model.Note;
import com.lguipeng.notes.model.NoteType;

import java.util.List;

/**
 * Created by lgp on 2015/5/28.
 */
public class JsonUtils {

    public static <T> String json(T note){
        if (note == null)
            return "";
        try {
            return JSON.toJSONString(note);
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    public static <T> T parse(String json, Class<T> clazz){
        if (TextUtils.isEmpty(json))
            return null;
        try {
            return JSON.parseObject(json, clazz);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static Note parseNote(String json){
        return parse(json, Note.class);
    }

    public static String jsonNote(Note note){
        return json(note);
    }

    public static List<String> parseNoteType(String json){
        NoteType type = parse(json, NoteType.class);
        if (type == null)
            return null;
        return type.getTypes();
    }

    public static String jsonNoteType(NoteType type){
        return json(type);
    }

}
