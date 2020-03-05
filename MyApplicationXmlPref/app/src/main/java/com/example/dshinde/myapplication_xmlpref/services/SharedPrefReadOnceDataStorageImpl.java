package com.example.dshinde.myapplication_xmlpref.services;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class SharedPrefReadOnceDataStorageImpl extends ReadOnceDataStorageManager {
    Context context;
    SharedPreferences sharedpreferences;

    public SharedPrefReadOnceDataStorageImpl(Context context, String collectionName, DataStorageListener dataStorageListener) {
        super(collectionName, dataStorageListener);
        this.context = context;
        initialiseDBSupport();
    }

    private void initialiseDBSupport(){
        new Thread() {
            @Override
            public void run() {
                sharedpreferences = context.getSharedPreferences(collectionName, MODE_PRIVATE);
                loadData();
            }
        }.start();
    }

    private void loadData() {
        data.clear();
        Map<String, ?> allEntries = sharedpreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            data.add(new KeyValue(entry.getKey(), entry.getValue().toString()));
        }
        notifyDataLoaded();
        removeDataStorageListeners();
    }

}
