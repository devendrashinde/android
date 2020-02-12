package com.example.dshinde.myapplication_xmlpref.services;

import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FireDbDataStorageManagerImpl extends DataStorageManager {
    String collectionName=null;
    private DatabaseReference mDatabase;
    private ValueEventListener valueEventListener;
    private FirebaseAuth mAuth;

    public FireDbDataStorageManagerImpl(String collectionName, boolean autoKey) {
        this(collectionName, autoKey, false);
    }

    public FireDbDataStorageManagerImpl(String collectionName, boolean autoKey, boolean descendingOrder) {
        this.collectionName = collectionName;
        this.autoKey = autoKey;
        this.descendingOrder = descendingOrder;
        initialiseDBSupport();
    }

    private void initialiseDBSupport(){
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance()
                .getReference((autoKey ? "" : Constants.DATABASE_PATH_NOTE_DETAILS + "/") + collectionName + "/" + mAuth.getUid());
    }

    @Override
    public void loadData() {
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                loadData(snapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        //adding an event listener to fetch values
        mDatabase.addValueEventListener(valueEventListener);
    }

    private void loadData(DataSnapshot snapshot) {
        data.clear();
        //iterating through all the values in database
        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
            String key = autoKey ? (String) postSnapshot.getValue() : postSnapshot.getKey();
            String value = autoKey ? postSnapshot.getKey() : (String) postSnapshot.getValue();

            KeyValue upload = new KeyValue(key, value);
            data.add(upload);
        }
        Collections.sort(data, keyValueComparator);
        notifyDataLoaded();
    }

    public void remove(String key) {
        Query query;
        if(autoKey) {
            query = mDatabase.orderByValue().equalTo(key);
        } else {
            query = mDatabase.orderByKey().equalTo(key);
        }
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshotRef: dataSnapshot.getChildren()) {
                    snapshotRef.getRef().removeValue();
                    notifyDataSetChanged(key, null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void removeAll() {
        data.clear();
        //TODO remove all records from collection i.e. drop collection
        mDatabase.removeValue();
    }

    private String getNewKey() {
        DatabaseReference newPostRef = mDatabase.push();
        return newPostRef.getKey();
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

    private void updateDB(List<KeyValue> values){
        Map<String, Object> childUpdates = new HashMap<>();
        String key;
        for(KeyValue kv : values) {
            if (autoKey) {
                key = kv.getKey();
                childUpdates.put(kv.getValue(), (key == null || key.isEmpty() ? getNewKey() : key));
            } else {
                childUpdates.put(kv.getKey(), kv.getValue());
            }
        }
        mDatabase.updateChildren(childUpdates);
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
        for (KeyValue keyValue: data) {
            dataMap.put(keyValue.getKey(), keyValue.getValue().toString());
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
        if(valueEventListener != null) {
            mDatabase.removeEventListener(valueEventListener);
        }
    }
}
