package com.example.dshinde.myapplication_xmlpref.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;

import androidx.documentfile.provider.DocumentFile;

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
        DocumentFile dir = pickedDir.findFile(STORAGE_DIR);
        if (dir == null) {
            pickedDir = pickedDir.createDirectory(STORAGE_DIR);
        } else {
            pickedDir = dir;
        }

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
                cursor.close();
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



}
