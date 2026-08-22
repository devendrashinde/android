package com.example.dshinde.myapplication_xmlpref.services;

import static com.example.dshinde.myapplication_xmlpref.common.Constants.BACKUP_OPERATION;

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

import java.util.ArrayList;
import java.util.List;

public class BackupBackgroundService extends BroadcastReceiver {

    private static final String TAG = BackupBackgroundService.class.getName();

    private ReadWriteOnceDataStorage readWriteOnceDataStorage;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;

        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        String operation = bundle.getString(BACKUP_OPERATION);
        if (Constants.SYNC.equals(operation)) {
            syncFirebaseToRoom(context);
            return;
        }

        List<KeyValue> notesToBackup = gson.fromJson(
            bundle.getString(Constants.PARAM_DATA),
            new TypeToken<List<KeyValue>>() {}.getType()
        );
        if (notesToBackup == null) notesToBackup = new ArrayList<>();

        DocumentFile backupFolder = DocumentFile.fromTreeUri(
            context, Uri.parse(bundle.getString(Constants.PARAM_FOLDER))
        );
        if (backupFolder == null) return;

        StorageUtil.saveAsObjectToDocumentFile(
            context, backupFolder, Constants.DATABASE_PATH_NOTES, gson.toJson(notesToBackup)
        );

        for (KeyValue keyValue : notesToBackup) {
            if (keyValue != null && keyValue.getValue() != null) {
                doBackup(context, keyValue.getValue(), backupFolder);
            }
        }
    }

    private void doBackup(Context context, String note, DocumentFile backupFolder) {
        Log.d(TAG, note);
        readWriteOnceDataStorage = Factory.getReadWriteOnceDataStorageInstance(
            context, note, new DataStorageListener() {
                @Override
                public void dataChanged(String key, String value) {
                }

                @Override
                public void dataLoaded(List<KeyValue> data) {
                    if (data != null && !data.isEmpty()) {
                        String path = StorageUtil.saveAsObjectToDocumentFile(
                            context, backupFolder, note, gson.toJson(data)
                        );
                        if (path != null) {
                            Log.d(TAG, note + " saved");
                        }
                    }
                    if (readWriteOnceDataStorage != null) {
                        readWriteOnceDataStorage.removeDataStorageListeners();
                    }
                }
            }
        );
    }

    private void syncFirebaseToRoom(Context context) {
        Log.d(TAG, "Starting Firebase->Room one-way sync");
        readWriteOnceDataStorage = Factory.getReadOnceFireDataStorageInstance(
            Constants.DATABASE_PATH_NOTES,
            new DataStorageListener() {
                @Override
                public void dataChanged(String key, String value) {
                }

                @Override
                public void dataLoaded(List<KeyValue> notes) {
                    if (notes == null || notes.isEmpty()) {
                        Log.d(TAG, "No notes found to sync");
                        return;
                    }
                    Log.d(TAG, "Notes list size from Firebase: " + notes.size());
                    DataStorage roomWriter = Factory.getRoomLocalOnlyDataStorageInstance(
                            context, Constants.DATABASE_PATH_NOTES, true, false, null
                    );

                    roomWriter.disableNotifyDataChange();
                    roomWriter.disableSort();
                    // Keep sync as silent bulk write and always restore flags.
                    for (KeyValue note : notes) {
                        if (note != null && note.getKey() != null) {
                            try {
                                roomWriter.save(null, note.getKey());
                                Log.d(TAG, "Synced " + note.getKey() + " to Room");
                            } catch (Throwable t) {
                                Log.e(TAG, "Failed Room sync for " + Constants.DATABASE_PATH_NOTES, t);
                            }
                            syncSingleNoteDetailsToRoom(context, note.getKey());
                            // backup screenDesign
                            syncSingleNoteDetailsToRoom(context, Constants.SCREEN_DESIGN_NOTE_PREFIX + note.getKey());
                            // backup mediaNote
                            syncSingleNoteDetailsToRoom(context, Constants.MEDIA_NOTE_PREFIX + note.getKey());
                        }
                    }
                }
            }
        );
    }

    // ...existing code...
    private void syncSingleNoteDetailsToRoom(Context context, String noteId) {
        Log.d(TAG, "Syncing details of note " + noteId + " from Firebase to Room");
        Factory.getReadOnceFireDataStorageInstance(noteId, new DataStorageListener() {
            @Override
            public void dataChanged(String key, String value) {
            }

            @Override
            public void dataLoaded(List<KeyValue> remoteData) {
                if (remoteData == null || remoteData.isEmpty()) {
                    Log.d(TAG, "No notes found to sync");
                    return;
                }
                DataStorage roomWriter = Factory.getRoomLocalOnlyDataStorageInstance(
                    context, noteId, false, false, null
                );
                // Keep sync as silent bulk write and always restore flags.
                roomWriter.disableNotifyDataChange();
                roomWriter.disableSort();
                try {
                    roomWriter.save(remoteData);
                    Log.d(TAG, "Synced " + remoteData.size() + " rows to Room for note " + noteId);
                } catch (Throwable t) {
                    Log.e(TAG, "Failed Room sync for note " + noteId, t);
                } finally {
                    roomWriter.enableSort();
                    roomWriter.enableNotifyDataChange();
                }
            }
        });
    }
}