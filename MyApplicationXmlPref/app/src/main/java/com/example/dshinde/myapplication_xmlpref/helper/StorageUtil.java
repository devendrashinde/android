package com.example.dshinde.myapplication_xmlpref.helper;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;
import androidx.loader.content.CursorLoader;

import com.example.dshinde.myapplication_xmlpref.common.Constants;

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
import java.util.Arrays;
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

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static File getFile(String fileName) {
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
        File dst = StorageUtil.getFile(fileName + TXT);
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
        File dst = StorageUtil.getFile(fileName + JSON);
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

    public static File createTempFile(Context context, String fileType) {
        return createTempFile(context, fileType, null);
    }
    public static File createTempFile(Context context, String fileType, String fileExtension) {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "IMG" + timeStamp;
        try {
            String type = null;
            String extension = null;
            switch(fileType) {
                case Constants.IMAGE_FILE:
                    type = Environment.DIRECTORY_PICTURES;
                    extension = "jpg";
                    break;
                case Constants.AUDIO_FILE:
                    type = Environment.DIRECTORY_MUSIC;
                    extension = "mp3";
                    break;
                default:
                    type = Environment.DIRECTORY_DOCUMENTS;
                    extension = "pdf";
                    break;

            }
            if(fileExtension == null || fileExtension.isEmpty()){
                fileExtension = extension;
            }
            File storageDir = context.getExternalFilesDir(type);
            return File.createTempFile(
                    fileName,                   /* prefix */
                    "." + fileExtension,        /* suffix */
                    storageDir                  /* directory */
            );
        } catch(IOException e){
            return null;
        }
    }
    public static File createTempImageFile(Context context) {
        return createTempFile(context, Constants.IMAGE_FILE);
    }

    public static File createTempDocumentFile(Context context) {
        return createTempFile(context, Constants.PDF_FILE);
    }

    public static File createTempAudioFile(Context context) {
        return createTempFile(context, Constants.AUDIO_FILE);
    }

    public static File createImageFile(Context context, String fileName) {
        return getFile(context, fileName.concat(".jpg"));
    }

    public static File getFile(Context context, String fileName) {
        return new File(context.getFilesDir(), fileName);
    }

    public static File createDocumentFile(Context context, String fileName) {
        return getFile(context, fileName.concat(".pdf"));
    }

    public static File createTempDocumentFile(Context context, String fileName) {
        try {
            File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            return File.createTempFile(
                    fileName,       /* prefix */
                    ".pdf",         /* suffix */
                    storageDir      /* directory */
            );
        } catch(IOException e){
            return null;
        }
    }

    public static Uri createImageFileUri(Context context) {
        return getUriForFile(context, createTempImageFile(context));
    }

    public static Uri getUriForFile(Context context, File file) {
        return FileProvider.getUriForFile(context,
                DSHINDE_FILEPROVIDER,
                file);
    }

    public static Uri getAudioUriFromDisplayName(Context context, String displayName) {
        Uri mediaUri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;

        String[] projection = new String[] {
                MediaStore.Audio.Media._ID
        };

        String selection = MediaStore.Audio.Media.DISPLAY_NAME + "=?";
        String[] selectionArgs = new String[] { displayName };

        Cursor cursor = context.getContentResolver().query(
                mediaUri,
                projection,
                selection,
                selectionArgs,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
            cursor.close();

            // Build and return the content URI
            return ContentUris.withAppendedId(mediaUri, id);
        }

        if (cursor != null) cursor.close();
        return null;
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
