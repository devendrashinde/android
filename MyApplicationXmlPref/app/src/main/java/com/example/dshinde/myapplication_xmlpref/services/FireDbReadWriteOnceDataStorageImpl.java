package com.example.dshinde.myapplication_xmlpref.services;

import androidx.annotation.NonNull;

import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FireDbReadWriteOnceDataStorageImpl extends ReadWriteOnceDataStorageManager {
    private DatabaseReference mDatabase;
    private ValueEventListener valueEventListener = null;
    private FirebaseAuth mAuth;

    public FireDbReadWriteOnceDataStorageImpl(@NonNull String collectionName, DataStorageListener dataStorageListener) {
        super(collectionName, dataStorageListener);
        if(collectionName.equals(Constants.DATABASE_PATH_NOTES)) {
            initialiseDBSupport(collectionName);
        } else {
            initialiseDBSupport(Constants.DATABASE_PATH_NOTE_DETAILS + "/" + collectionName);
        }
    }

    private void initialiseDBSupport(String path){
        new Thread() {
            @Override
            public void run() {
                mAuth = FirebaseAuth.getInstance();
                mDatabase = FirebaseDatabase.getInstance()
                        .getReference(path + "/" + mAuth.getUid());
                readDataOnce();
            }
        }.start();
    }

    private void readDataOnce(){
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(!listeners.isEmpty()) {
                    loadData(snapshot);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabase.addListenerForSingleValueEvent(valueEventListener);
    }

    private void loadData(DataSnapshot snapshot) {
        data.clear();
        //iterating through all the values in database
        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
            String key = postSnapshot.getKey();
            String value = postSnapshot.getValue().toString();

            KeyValue upload = new KeyValue(key, value);
            data.add(upload);
        }
        notifyDataLoaded();
        removeDataStorageListeners();
        mDatabase.removeEventListener(valueEventListener);
    }
}
