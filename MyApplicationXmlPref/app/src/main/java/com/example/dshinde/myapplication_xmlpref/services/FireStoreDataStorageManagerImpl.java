package com.example.dshinde.myapplication_xmlpref.services;

import androidx.annotation.Nullable;

import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.common.DataChangeType;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FireStoreDataStorageManagerImpl extends DataStorageManager {
    String collectionName=null;
    private CollectionReference mDatabase;
    ListenerRegistration registration;
    private FirebaseAuth mAuth;

    public FireStoreDataStorageManagerImpl(String collectionName, boolean autoKey) {
        this(collectionName, autoKey, false);
    }

    public FireStoreDataStorageManagerImpl(String collectionName, boolean autoKey, boolean descendingOrder) {
        this.collectionName = collectionName;
        this.autoKey = autoKey;
        this.descendingOrder = descendingOrder;
        initialiseDBSupport();
    }

    private void initialiseDBSupport(){
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance().collection((autoKey ? "" : Constants.DATABASE_PATH_NOTES + "/") + collectionName + (autoKey ? "" : "/" + mAuth.getUid()));
    }

    @Override
    public void loadData() {
        registration = mDatabase.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    System.err.println("Listen failed:" + e);
                    return;
                }
                loadData(queryDocumentSnapshots);
            }

        });
    }

    private void loadData(QuerySnapshot snapshot) {
        data.clear();
        //iterating through all the values in database
        for (DocumentSnapshot postSnapshot : snapshot) {
            KeyValue upload;
            if(autoKey) {
                String key = postSnapshot.getString("id");
                String value = postSnapshot.getId();
                upload = new KeyValue(key, value);
                data.add(upload);
            } else {
                String key = postSnapshot.getId();
                String value = postSnapshot.getString("value");
                upload = new KeyValue(key, value);
                data.add(upload);
            }
        }
        Collections.sort(data, keyValueComparator);
        notifyDataLoaded();
    }

    public void remove(String key) {
    }

    public void removeAll() {
        data.clear();
        //TODO remove all records from collection i.e. drop collection
    }

    private String getNewKey() {
        return mDatabase.document().getId();
    }

    public void save(String key, String value) {
        if (autoKey && (key == null || key.isEmpty())) {
            key = getNewKey();
        }
        updateDB(Collections.singletonList(new KeyValue(key, value)));
    }

    public void save(List<KeyValue> values) {
        updateDB( values);
    }

    @Override
    public DataChangeType getLastDataChangeType() {
        return null;
    }

    private void updateDB(List<KeyValue> values){
        new Thread() {
            @Override
            public void run() {
                Map<String, Object> data = new HashMap<>();
                String key;
                for (KeyValue kv : values) {
                    if (!autoKey) {
                        data.put("value", kv.getValue());
                    }
                    mDatabase.document(autoKey ? kv.getValue() : kv.getKey()).set(data);
                }
            }
        }.start();
    }

    public KeyValue getValue(int index) {
        return data.get(index);
    }

    public List<KeyValue> getValues() {
        return data;
    }

    public String getDataString(){

        return "";
    }

    public String getDataString(String collectionName){
        return getDataMap(collectionName).toString();
    }

    public Map<String,String> getDataMap(String collectionName){
        final Map<String, String> dataMap = new HashMap<>();
        for (KeyValue snapshotRef: data) {
            dataMap.put(snapshotRef.getKey(), snapshotRef.getValue().toString());
        }
        return dataMap;
    }

    public void loadData(Map<String,String> data, boolean removeExistingData){
        if(removeExistingData) {
            removeAll();
        }
        List<KeyValue> values = new ArrayList<>();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            values.add(new KeyValue(entry.getKey(), entry.getValue()));
        }
        save(values);
    }

    public void removeDataStorageListeners() {
        super.removeDataStorageListeners();
        if(registration != null) {
            registration.remove();
        }
    }
}
