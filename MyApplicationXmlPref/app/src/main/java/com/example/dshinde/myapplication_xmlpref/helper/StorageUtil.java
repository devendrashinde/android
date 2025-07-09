package com.example.dshinde.myapplication_xmlpref.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;
import androidx.loader.content.CursorLoader;

import com.example.dshinde.myapplication_xmlpref.common.FileType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StorageUtil {

    public static final String STORAGE_DIR = "MyNotes";
    public static final int NOT_AVAILABLE = 0;
    public static final int PICK_FILE_FOR_IMPORT = 30;
    public static final int PICK_FILE_FOR_VIEW = 31;
    public static final int CREATE_REQUEST_CODE = 40;
    public static final int OPEN_REQUEST_CODE = 41;
    public static final int SAVE_REQUEST_CODE = 42;
    public static final int PICK_DOCUMENT_FOLDER_FOR_EXPORT = 43;
    public static final int PICK_DOCUMENT_FOLDER_FOR_BACKUP = 53;
    public static final String TXT = ".txt";
    private static final String TEXT_FILE = "text/plain; charset=utf-8";
    private static final String JSON_FILE = "application/json; charset=utf-8";
    public static final String JPEG_FILE = "image/jpeg";
    public static final String OBJ = ".obj";
    public static final String JSON = ".json";
    public static final String DSHINDE_FILEPROVIDER = "com.example.dshinde.fileprovider";
    public static final String PDF = ".pdf";
    public static final String YYYY_MMDD_HHMMSS = "yyyyMMdd_HHmmss";

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static File getInternalStorageFile(String fileName) {
        if (isExternalStorageWritable()) {
            File dir = getStorage(STORAGE_DIR);
            if (dir != null) {
                return new File(dir, fileName);
            }
        }
        return null;
    }

    public static File getStorage(String fileName) {

        File externalStorage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File mStorage = new File(externalStorage, fileName);

        if (!mStorage.exists()) {
            if (!mStorage.mkdirs()) {
                return null;
            }
        }
        return mStorage;
    }

    public static String saveAsTextToFile(String fileName, String data) {
        String filePath = null;
        if (null == data || data.length() == 0) {
            return null;
        }
        File dst = StorageUtil.getInternalStorageFile(fileName + TXT);
        if (dst != null) {
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(dst);
                output.write(data.toString().getBytes());
                filePath = dst.getAbsolutePath();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (output != null) {
                        output.flush();
                        output.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

        }
        return filePath;
    }

    public static String saveAsObjectToFile(String fileName, Object object) {
        if (null == object) {
            return null;
        }
        String filePath = null;
        File dst = StorageUtil.getInternalStorageFile(fileName + JSON);
        if (null != dst) {
            ObjectOutputStream output = null;
            try {
                output = new ObjectOutputStream(new FileOutputStream(dst));
                output.writeObject(object);
                filePath = dst.getAbsolutePath();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (output != null) {
                        output.flush();
                        output.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return filePath;
    }

    public static Object getObjectFromFile(File src) {
        boolean res = false;
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(new FileInputStream(src));
            return input.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public static File[] getObjectFiles() {
        File dir = getStorage(STORAGE_DIR);
        return dir.listFiles(
                new FileFilter() {
                    public boolean accept(File file) {
                        return file.getName().endsWith(OBJ);
                    }
                });
    }

    public static DocumentFile[] getDocumentFiles(DocumentFile dir) {
        return dir.listFiles();
    }


    public static DocumentFile getDocumentDir(Context context, Uri url) {
        DocumentFile pickedDir = DocumentFile.fromTreeUri(context, url);
        /*
        DocumentFile dir = pickedDir.findFile(STORAGE_DIR);
        if (dir == null) {
            pickedDir = pickedDir.createDirectory(STORAGE_DIR);
        } else {
            pickedDir = dir;
        }
        */
        return pickedDir;
    }

    public static String saveAsTextToDocumentFile(Context context, DocumentFile dir, String fileName, String data) {
        try {
            DocumentFile file = dir.createFile(TEXT_FILE, fileName + TXT);
            OutputStream out = context.getContentResolver().openOutputStream(file.getUri());
            try {
                out.write(data.getBytes());
                return file.getUri().getPath();
            } finally {
                out.close();
            }
        } catch (IOException e) {
            return null;
        }
    }

    public static String saveAsObjectToDocumentFile(Context context, DocumentFile dir, String fileName, String data) {
        try {
            DocumentFile file = dir.createFile(JSON_FILE, fileName + JSON);
            OutputStream out = context.getContentResolver().openOutputStream(file.getUri());
            try {
                out.write(data.getBytes());
                return file.getUri().getPath();
            } finally {
                out.close();
            }
        } catch (IOException e) {
            return null;
        }
    }

    public static JSONObject getObjectFromDocumentFile(Context context, Uri fileUri) {
        try {
            return new JSONObject(getTextFromDocumentFile(context, fileUri));
        } catch (JSONException e) {
            return null;
        }
    }

    public static String getTextFromDocumentFile(Context context, Uri fileUri) {
        try {
            InputStream input = context.getContentResolver().openInputStream(fileUri);
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));

            StringBuilder responseStrBuilder = new StringBuilder();
            String inputStr;
            try {
                while ((inputStr = streamReader.readLine()) != null)
                    responseStrBuilder.append(inputStr).append("\n");
            } finally {
                streamReader.close();
                input.close();
            }
            return responseStrBuilder.toString();
        } catch (IOException e) {
            return null;
        }
    }


    @SuppressLint("Range")
    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static String getAudioPath(Context context, Uri uri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(context, uri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    public static StorageSelectionResult getStorageSelectionResult(Context context, int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri fileUri = data.getData();
            DocumentFile dir;
            switch (requestCode) {
                case StorageUtil.PICK_DOCUMENT_FOLDER_FOR_EXPORT:
                case StorageUtil.PICK_DOCUMENT_FOLDER_FOR_BACKUP:
                    dir = StorageUtil.getDocumentDir(context, fileUri);
                    return new StorageSelectionResult(requestCode, dir);
                case StorageUtil.PICK_FILE_FOR_IMPORT:
                case StorageUtil.PICK_FILE_FOR_VIEW:
                    String fileName = StorageUtil.getFileName(context, fileUri);
                    return new StorageSelectionResult(requestCode, fileName);
                default:
                    break;
            }
        }
        return new StorageSelectionResult();
    }

    public static String getFileNameWithOutExtension(String filename) {
        return filename.replaceFirst("[.][^.]+$", "");
    }

    public static File createTempImageFileOnExternalStorage(Context context) {
        return createTempFileOnExternalStorage(context, FileType.PICTURE);
    }

    public static File createTempDocumentFileOnExternalStorage(Context context) {
        return createTempFileOnExternalStorage(context, FileType.DOCUMENT);
    }

    public static File createTempAudioFileOnExternalStorage(Context context) {
        return createTempFileOnExternalStorage(context, FileType.MUSIC);
    }

    public static File createTempFileOnExternalStorage(Context context, FileType fileType) {
        return createTempFileOnExternalStorage(context, fileType, null);
    }
    public static File createTempFileOnExternalStorage(Context context, FileType fileType, String fileExtension) {
        // Create an image file name
        String timeStamp = new SimpleDateFormat(YYYY_MMDD_HHMMSS).format(new Date());
        FileStorageTypeExtension result = getFileStorageTypeExtension(fileType);
        String fileName = result.prefix + timeStamp;

        try {
            if(fileExtension == null || fileExtension.isEmpty()){
                fileExtension = result.extension;
            }
            File storageDir = context.getExternalFilesDir(result.type);
            return File.createTempFile(
                    fileName,                   /* prefix */
                    "." + fileExtension,        /* suffix */
                    storageDir                  /* directory */
            );
        } catch(IOException e){
            return null;
        }
    }

    private static @NonNull FileStorageTypeExtension getFileStorageTypeExtension(FileType fileType) {
        String type = null;
        String extension = null;
        String prefix = null;
        switch(fileType) {
            case PICTURE:
                type = Environment.DIRECTORY_PICTURES;
                extension = "jpg";
                prefix = "IMG";
                break;
            case MUSIC:
                type = Environment.DIRECTORY_MUSIC;
                extension = "mp3";
                prefix = "AUD";
                break;
            default:
                type = Environment.DIRECTORY_DOCUMENTS;
                extension = "pdf";
                prefix = "DOC";
                break;

        }
        return new FileStorageTypeExtension(type, extension, prefix);
    }

    private static class FileStorageTypeExtension {
        public final String type;
        public final String extension;
        public final String prefix;

        public FileStorageTypeExtension(String type, String extension, String prefix) {
            this.type = type;
            this.extension = extension;
            this.prefix = prefix;
        }
    }

    public static File getInternalStorageFile(Context context, String fileName) {
        return new File(context.getFilesDir(), fileName);
    }

    public static File getExternalStorageFile(Context context, String fileName, FileType fileType) {
        if (!isExternalStorageWritable()) {
            return null;
        }
        FileStorageTypeExtension result = getFileStorageTypeExtension(fileType);
        return new File(context.getExternalFilesDir(result.type), fileName);
    }

    public static Uri createUriForImageFileOnExternalStorage(Context context) {
        return getUriForFile(context, createTempImageFileOnExternalStorage(context));
    }

    public static Uri getUriForFile(Context context, File file) {
        return FileProvider.getUriForFile(context,
                DSHINDE_FILEPROVIDER,
                file);
    }

    public static byte[] readBytesFromFile(File file) {
        FileInputStream fis = null;
        byte[] bytesArray = null;

        try {
            fis = new FileInputStream(file);
            bytesArray = new byte[(int) file.length()];
            fis.read(bytesArray);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return bytesArray;
    }
    public static void writeBytesToFile(byte[] data, File file) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(data);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        return ""; // No extension found
    }


}
