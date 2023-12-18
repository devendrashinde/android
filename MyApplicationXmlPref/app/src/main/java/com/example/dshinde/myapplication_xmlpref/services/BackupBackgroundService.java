package com.example.dshinde.myapplication_xmlpref.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.helper.Factory;
import com.example.dshinde.myapplication_xmlpref.helper.StorageUtil;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class BackupBackgroundService extends BroadcastReceiver {

    private static final String TAG = BackupBackgroundService.class.getName();
    private ReadWriteOnceDataStorage readWriteOnceDataStorage;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        List<KeyValue> notesToBackup = gson.fromJson(bundle.getString(Constants.PARAM_DATA), new TypeToken<List<KeyValue>>(){}.getType());
        DocumentFile backupFolder = DocumentFile.fromTreeUri(context, Uri.parse(bundle.getString(Constants.PARAM_FOLDER)));
        StorageUtil.saveAsObjectToDocumentFile(context, backupFolder, Constants.DATABASE_PATH_NOTES, gson.toJson(notesToBackup));
        notesToBackup.forEach(keyValue -> doBackup(context, keyValue.getValue(), backupFolder));
    }

    private void doBackup(Context context, String note, DocumentFile backupFolder) {
        Log.d(TAG, note);
        readWriteOnceDataStorage = Factory.getReadOnceFireDataStorageInstance(note,
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
                    }
                    readWriteOnceDataStorage.removeDataStorageListeners();
                }
            });
    }

}
