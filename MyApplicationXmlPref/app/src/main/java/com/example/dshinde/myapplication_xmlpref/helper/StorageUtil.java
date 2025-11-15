package com.example.dshinde.myapplication_xmlpref.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import com.example.dshinde.myapplication_xmlpref.common.FileType;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StorageUtil {

    public static final String STORAGE_DIR = "MyNotes";
    public static final int NOT_AVAILABLE = 0;
    public static final int PICK_FILE_FOR_IMPORT = 30;
    public static final int PICK_FILE_FOR_VIEW = 31;
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
    public static final String JPG = "jpg";
    public static final String MP3 = "mp3";
    public static final String IMG = "IMG";
    public static final String AUD = "AUD";
    public static final String DOC = "DOC";

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
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

        File externalStorage = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS);
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
        if (null == data || data.isEmpty()) {
            return null;
        }
        File dst = StorageUtil.getInternalStorageFile(fileName + TXT);
        if (dst != null) {
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(dst);
                output.write(data.getBytes());
                filePath = dst.getAbsolutePath();
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
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(new FileInputStream(src));
            return input.readObject();
        } catch (FileNotFoundException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
        return DocumentFile.fromTreeUri(context, url);
    }

    public static String saveAsTextToDocumentFile(Context context, DocumentFile dir, String fileName, String data) {
        try {
            DocumentFile file = dir.createFile(TEXT_FILE, fileName + TXT);
            try (OutputStream out = context.getContentResolver().openOutputStream(file.getUri())) {
                out.write(data.getBytes());
                return file.getUri().getPath();
            }
        } catch (IOException e) {
            return null;
        }
    }

    public static String saveAsObjectToDocumentFile(Context context, DocumentFile dir, String fileName, String data) {
        try {
            DocumentFile file = dir.createFile(JSON_FILE, fileName + JSON);
            try (OutputStream out = context.getContentResolver().openOutputStream(file.getUri())) {
                out.write(data.getBytes());
                return file.getUri().getPath();
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
                extension = JPG;
                prefix = IMG;
                break;
            case MUSIC:
                type = Environment.DIRECTORY_MUSIC;
                extension = MP3;
                prefix = AUD;
                break;
            default:
                type = Environment.DIRECTORY_DOCUMENTS;
                extension = PDF;
                prefix = DOC;
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

    public static File getExternalStorageFile(Context context, String fileName, FileType fileType, String folder) {
        if (!isExternalStorageWritable()) {
            return null;
        }
        FileStorageTypeExtension result = getFileStorageTypeExtension(fileType);
        String dir = folder != null ?result.type + "/" + folder : result.type;
        return new File(context.getExternalFilesDir(dir), fileName);
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

    public static void copyFileToExternalStorage(Context context, Uri uri, FileType fileType, String folder) {
        File destinationFile = getExternalStorageFile(context, getFileName(context, uri), fileType, folder);
        assert destinationFile != null;
        if (!destinationFile.exists()) {
            try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
                 OutputStream outputStream = Files.newOutputStream(destinationFile.toPath())) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.close();
                getUriForFile(context, destinationFile);
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        getUriForFile(context, destinationFile);
    }

    public static void writeKeyValueListToCacheDir(Context context, List<KeyValue> keyValues) {
        // Write to cache
        try {
            File cacheFile = new File(context.getCacheDir(), "keyValues.json");
            FileWriter writer = new FileWriter(cacheFile, false);
            new Gson().toJson(keyValues, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<KeyValue> getKeyValueListFromCacheDir(Context context) {
        try {
            File cacheFile = new File(context.getCacheDir(), "keyValues.json");
            FileReader reader = new FileReader(cacheFile);
            Type listType = new TypeToken<List<KeyValue>>() {
            }.getType();
            List<KeyValue> keyValues = new Gson().fromJson(reader, listType);
            reader.close();
            return keyValues;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
