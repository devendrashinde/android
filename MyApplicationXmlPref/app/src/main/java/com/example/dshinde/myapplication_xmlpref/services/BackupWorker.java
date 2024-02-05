package com.example.dshinde.myapplication_xmlpref.services;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.helper.Factory;
import com.example.dshinde.myapplication_xmlpref.helper.StorageUtil;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

public class BackupWorker extends Worker {

    private static final String TAG = BackupWorker.class.getName();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public BackupWorker(
            @NonNull Context appContext,
            @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        DocumentFile backupFolder = DocumentFile.fromTreeUri(context, Uri.parse(getInputData().getString(Constants.PARAM_FOLDER)));
        doBackup(context, Constants.DATABASE_PATH_NOTES, backupFolder);
        return Result.success();
    }

    private void doBackup(Context context, String note, DocumentFile backupFolder) {
        ReadWriteOnceDataStorage readWriteOnceDataStorage = Factory.getReadOnceFireDataStorageInstance(note,
            new DataStorageListener() {
                @Override
                public void dataChanged(String key, String value) {
                }

                @Override
                public void dataLoaded(List<KeyValue> data) {
                    if (data.size() > 0) {
                        String path = StorageUtil.saveAsObjectToDocumentFile(context, backupFolder, note, gson.toJson(data));
                        if(path != null ) {
                            Log.d(TAG, note + " saved");
                        }
                        if(note.equals(Constants.DATABASE_PATH_NOTES)) {
                            // backup note details
                            data.forEach(keyValue -> {
                                doBackup(context, keyValue.getKey(), backupFolder);
                                // backup screenDesign
                                doBackup(context, Constants.SCREEN_DESIGN_NOTE_PREFIX + keyValue.getKey(), backupFolder);
                                // backup mediaNote
                                doBackup(context, Constants.MEDIA_NOTE_PREFIX + keyValue.getKey(), backupFolder);
                            });
                        }
                    }
                }
            });
    }

}
