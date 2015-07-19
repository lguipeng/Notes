package com.lguipeng.notes.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Author: lgp
 * Date: 2014/12/31.
 */
public class FileUtil {

    public final static String BASE_PATH = "/LoveShare";
    public final static String SD_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();
    public final static String APP_DIR = SD_DIR + BASE_PATH ;
    public final static String IMAGE_PATH = APP_DIR + "/image";
    public static String LOG_DIR = FileUtil.APP_DIR + "/log";
    /**
     * 创建APP 的SD卡文件夹
     */
    static {
        if (checkSdcardStatus())
        {
            mkdir(APP_DIR);
            mkdir(IMAGE_PATH);
            mkdir(LOG_DIR);
            DebugLog.d("create app dir ok");
        }else{
            DebugLog.e("sd card not ready");
        }
    }

    public static void mkdir(String dir)
    {
        if (StringUtil.isBlank(dir))
            return;
        File dirFile = new File(dir);
        if (!dirFile.exists())
            dirFile.mkdir();
    }
    public static boolean isFileExist(String filePath)
    {
        if (StringUtil.isBlank(filePath)) {
            return false;
        }
        File file = new File(filePath);
        return (file.exists() && file.isFile());
    }

    public static boolean isFileExistInAppRoot(String fileName)
    {
        return isFileExist(APP_DIR + fileName);
    }

    /**
     *
     * @param filename
     * @return true if create success
     */
    public static boolean createFile(String filename)
    {
       return createFile(APP_DIR, filename);
    }

    public static boolean createFile(String dir, String filename)
    {
        File dirFile = new File(dir);
        if (!dirFile.exists())
        {
            dirFile.mkdir();
        }
        if (!dirFile.isDirectory())
            return false;
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
    public static boolean deleteFile(String filename)
    {
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

    public static boolean  writeFile(String filePath, String content, boolean append)
    {
        if (StringUtil.isEmpty(content)) {
            return false;
        }

        FileWriter fileWriter = null;
        try {
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

    public static long getFileSize(String path) {
        if (StringUtil.isBlank(path)) {
            return -1;
        }
        File file = new File(path);
        return (file.exists() && file.isFile() ? file.length() : -1);
    }

    public static int getCompressSize(String imagePath) {
        if(isFileExist(imagePath))
        {
            long fileSize = getFileSize(imagePath) / 1024;
            //分级压缩
            if (fileSize > 800)
            {
                return (int)(fileSize / 800) + 1;
            }
            if (fileSize > 600)
            {
                return (int)(fileSize / 600) + 1;
            }
            if (fileSize > 400)
            {
                return (int)(fileSize / 400) + 1;
            }
            if (fileSize > 200)
            {
                return (int)(fileSize / 200) + 1;
            }
        }
        DebugLog.d("file not exist");
        //else return 1;
        return 1;
    }

    public static String getCacheDir(Context context)
    {
        if (context.getExternalCacheDir() == null)
            return IMAGE_PATH;
        return context.getExternalCacheDir().getAbsolutePath();
    }

    public static String getImageCacheDir(Context context) {
        String dir = getCacheDir(context) + "image" + File.separator;
        File file = new File(dir);
        if (!file.exists()) file.mkdirs();
        return dir;
    }

    public  static int getDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int degree) {
        if (bitmap != null) {
            Matrix m = new Matrix();
            m.postRotate(degree);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
            return bitmap;
        }
        return null;
    }

    public  static int calculateInSampleSize(BitmapFactory.Options options, int rqsW, int rqsH) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (rqsW == 0 || rqsH == 0) return 1;
        if (height > rqsH || width > rqsW) {
            final int heightRatio = Math.round((float) height/ (float) rqsH);
            final int widthRatio = Math.round((float) width / (float) rqsW);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    /**
     * 压缩指定路径的图片，并得到图片对象
     * @return Bitmap {@link Bitmap}
     */
    public  static Bitmap compressBitmap(String path, int rqsW, int rqsH) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = calculateInSampleSize(options, rqsW, rqsH);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }
    /**
     * 压缩指定路径图片，并将其保存在缓存目录中，通过isDelSrc判定是否删除源文件，并获取到缓存后的图片路径
     */
    public  static String compressBitmap(Context context, String srcPath, int rqsW, int rqsH, boolean isDelSrc) {
        Bitmap bitmap = compressBitmap(srcPath, rqsW, rqsH);
        File srcFile = new File(srcPath);
        String desPath = getImageCacheDir(context) + srcFile.getName();
        int degree = getDegree(srcPath);
        try {
            if (degree != 0) bitmap = rotateBitmap(bitmap, degree);
            File file = new File(desPath);
            FileOutputStream  fos = new FileOutputStream(file);
            int compressSize = getCompressSize(srcPath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100 / compressSize, fos);
            fos.close();
            if (isDelSrc) srcFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (bitmap != null)
            {
                bitmap.recycle();
            }
        }
        return desPath;
    }

    public static Uri compressBitmap(Context context, Uri uri, PictureSourceType type, boolean isDelSrc) {
        String srcPath;
        if (type == PictureSourceType.LOCAL)
        {
            srcPath = getFilePathFromContentUri(uri, context.getContentResolver());
        }else{
            srcPath = uri.getPath();
        }
        return Uri.fromFile(new File(compressBitmap(context, srcPath, 480, 800, isDelSrc)));
    }

    public static boolean checkSdcardStatus()
    {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static String getFilePathFromContentUri(Uri uri, ContentResolver contentResolver) {
        String filePath;
        String[] filePathColumn = {MediaStore.MediaColumns.DATA};
        Cursor cursor = contentResolver.query(uri, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        filePath = cursor.getString(columnIndex);
        cursor.close();
        return filePath;
    }

    public static String getLogFileGenerateNameByDate()
    {
        return getFileGenerateNameByDate("log");
    }

    public static String getImageFileGenerateNameByDate()
    {
        return getFileGenerateNameByDate("jpg");
    }

    public static String getFileGenerateNameByDate(String suffix)
    {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "." + suffix;
    }

    @Deprecated()
    public static Uri compressPicture(Uri uri, ContentResolver contentResolver, PictureSourceType type, boolean isDeleteSource)
    {
        Bitmap photo = null;
        OutputStream stream = null;
        try {
            int compressSize = 1;
            if (type == PictureSourceType.LOCAL)
            {
                compressSize = getCompressSize(getFilePathFromContentUri(uri, contentResolver));
                photo = BitmapFactory.decodeFile(getFilePathFromContentUri(uri, contentResolver));
            }else if (type == PictureSourceType.Camera){
                compressSize = getCompressSize(uri.getPath());
                photo = BitmapFactory.decodeFile(uri.getPath());
            }
            if (isDeleteSource)
            {
                if (type == PictureSourceType.LOCAL)
                {
                    FileUtil.deleteFile(getFilePathFromContentUri(uri, contentResolver));
                }else
                {
                    FileUtil.deleteFile(uri.getPath());
                }
            }
            String fileName = getImageFileGenerateNameByDate();
            FileUtil.createFile(IMAGE_PATH, fileName);
            final File newFile = new File(IMAGE_PATH, fileName);
            stream = new FileOutputStream(newFile);
            if (photo != null)
            {
                photo.compress(Bitmap.CompressFormat.JPEG, 100 / compressSize, stream);
                return Uri.fromFile(newFile);
            }else {
                return null;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            if (photo != null)
                photo.recycle();
            if (stream != null)
            {
                try {
                    stream.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Deprecated()
    public static Uri compressPicture(Uri uri, ContentResolver contentResolver, PictureSourceType type)
    {
        return compressPicture(uri, contentResolver, type, false);
    }

    public enum PictureSourceType{
        Camera,
        LOCAL
    }

}
