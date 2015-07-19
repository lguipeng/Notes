package com.lguipeng.notes.utils;

import android.os.Environment;
import android.text.TextUtils;

import com.lguipeng.notes.model.SNote;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Author: lgp
 * Date: 2014/12/31.
 */
public class FileUtils {

    public final static String SD_ROOT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();
    public final static String APP_DIR = SD_ROOT_DIR + File.separator +"SNotes" ;
    public final static String BACKUP_FILE_NAME = "notes.txt" ;
    /**
     * 创建APP 的SD卡文件夹
     */
    {
        if (checkSdcardStatus()) {
            mkdir(APP_DIR);
            NotesLog.d("create app dir ok");
        }else{
            NotesLog.e("sd card not ready");
        }
    }

    public void mkdir(String dir){
        if (TextUtils.isEmpty(dir))
            return;
        File dirFile = new File(dir);
        if (!dirFile.exists())
            dirFile.mkdir();
    }
    public boolean isFileExist(String filePath){
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        File file = new File(filePath);
        return (file.exists() && file.isFile());
    }

    /**
     *
     * @param filename
     * @return true if create success
     */
    public boolean createFile(String filename) {
       return createFile(APP_DIR, filename);
    }

    public boolean createFile(String dir, String filename) {
        File dirFile = new File(dir);
        if (!dirFile.isDirectory())
            return false;
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
        File newFile = new File(dir + File.separator + filename);
        try {
            if (!newFile.exists())
                newFile.createNewFile();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     *
     * @param filename
     * @return true if delete success
     */
    public boolean deleteFile(String filename) {
        File deleteFile = new File(filename);
        if (deleteFile.exists())
        {
            deleteFile.delete();
            return true;
        }else
        {
            return false;
        }
    }

    public boolean  writeSNotesFile(String content) {
        return writeFile(APP_DIR, BACKUP_FILE_NAME, content, false);
    }

    public boolean  writeFile(String fileName, String content, boolean append) {
        return writeFile(APP_DIR, fileName, content, append);
    }

    public boolean  writeFile(String dir, String fileName, String content, boolean append) {
        if (TextUtils.isEmpty(content)) {
            return false;
        }
        FileWriter fileWriter = null;
        try {
            String filePath = dir + File.separator + fileName;
            fileWriter = new FileWriter(filePath, append);
            fileWriter.write(content + "\n");
            fileWriter.close();
            return true;
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    throw new RuntimeException("IOException occurred. ", e);
                }
            }
        }
    }

    public long getFileSize(String path) {
        if (TextUtils.isEmpty(path)) {
            return -1;
        }
        File file = new File(path);
        return (file.exists() && file.isFile() ? file.length() : -1);
    }

    public boolean checkSdcardStatus() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public void backupSNotes(List<SNote> notes) {
        createFile(BACKUP_FILE_NAME);
        StringBuilder sb = new StringBuilder();
        for (SNote note : notes){
            sb.append("Title:" + note.getLabel() + "\n");
            sb.append("Content:\n" + note.getContent() + "\n\n");
        }
        writeSNotesFile(sb.toString());
    }
}
