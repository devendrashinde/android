package com.example.dshinde.myapplication_xmlpref.services;

import android.os.Process;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.common.DataChangeType;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageTransactionWorker;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FireDbDataStorageManagerImpl extends DataStorageManager {
    String collectionName = null;
    String collectionItemId = null; // used to read specific record from collection
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ValueEventListener valueEventListener;
    private ChildEventListener childEventListener;
    private static final String CLASS_TAG = "FireDataStorageManager";
    private static FirebaseDatabase firebaseDatabase;

    public FireDbDataStorageManagerImpl(String collectionName, boolean autoKey) {
        this(collectionName, autoKey, false);
    }

    public FireDbDataStorageManagerImpl(String collectionName, boolean autoKey, boolean descendingOrder) {
        this(collectionName, autoKey, descendingOrder, null);
    }

    public FireDbDataStorageManagerImpl(String collectionName, boolean autoKey, boolean descendingOrder, DataStorageListener dataStorageListener) {
        if(collectionName.contains("/")) {
            this.collectionItemId = collectionName.split("/")[1];
            collectionName = collectionName.split("/")[0];
        }
        this.collectionName = collectionName;
        this.autoKey = autoKey;
        this.descendingOrder = descendingOrder;
        this.addDataStorageListener(dataStorageListener);
        getDatabaseCollectionReference();
    }

    private void getDatabaseCollectionReference() {
        Log.d(CLASS_TAG, "getDatabaseCollectionReference");
        if(firebaseDatabase == null) {
            firebaseDatabase = FirebaseDatabase.getInstance();
            firebaseDatabase.setPersistenceEnabled(true);
        }
        if(mDatabase == null || mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
            getDatabaseReference();
        }
    }

    private void getDatabaseReference() {
        mDatabase = firebaseDatabase.getReference(
                (autoKey ? "" :
                        Constants.DATABASE_PATH_NOTE_DETAILS + "/") +
                        collectionName + "/" + mAuth.getUid() +
                        (collectionItemId == null ? "" : "/" + collectionItemId));
        mDatabase.keepSynced(true);
    }

    @Override
    public void loadData() {
        Log.d(CLASS_TAG, "loadData");
        new Thread(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            addReadDataOnce();
            //addChangedDataListener();
            //addAllDataListener();
        }).start();
    }

    private void addReadDataOnce(){
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.d(CLASS_TAG, "addReadDataOnce->onDataChange");
                loadAllData(snapshot);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(CLASS_TAG, "addReadDataOnce->error occurred " + databaseError);
            }
        };
        mDatabase.addListenerForSingleValueEvent(valueEventListener);
    }

    private void addChangedDataListener() {
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                Log.d(CLASS_TAG, "data added");
                loadItem(dataSnapshot, DataChangeType.ADDED, true);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                Log.d(CLASS_TAG, "data modified");
                loadItem(dataSnapshot, DataChangeType.MODIFIED, true);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d(CLASS_TAG, "data removed");
                loadItem(dataSnapshot, DataChangeType.DELETED, true);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(CLASS_TAG, "addChangedDataListener->error occurred " + databaseError);
            }
        };
        //adding an event listener to fetch values
        mDatabase.addChildEventListener(childEventListener);
    }

    private void addAllDataListener() {
        Log.d(CLASS_TAG, "addAllDataListener");
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.d(CLASS_TAG, "addAllDataListener->onDataChange");
                loadAllData(snapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(CLASS_TAG, "addAllDataListener->error occurred " + databaseError);
            }
        };
        //adding an event listener to fetch values
        mDatabase.addValueEventListener(valueEventListener);
    }

    private void loadAllData(DataSnapshot collection) {
        Log.d(CLASS_TAG, "loadAllData received " + collection.getChildrenCount() + " records");
        data.clear();
        if(collectionItemId != null && !collectionItemId.isEmpty()){
            loadItem(collection,DataChangeType.ALL_DATA,false);
            collectionItemId = null;
            getDatabaseReference();
        } else {
            for (DataSnapshot dataSnapshot : collection.getChildren()) {
                loadItem(dataSnapshot, DataChangeType.ALL_DATA, false);
            }
        }
        notifyDataChanged();
    }

    private void loadItem(DataSnapshot item, DataChangeType dataChangeType, boolean notify) {
        try {
            String key = autoKey ? (String) item.getValue() : item.getKey();
            String value = autoKey ? item.getKey() : (String) item.getValue();
            KeyValue keyValue = new KeyValue(key, value);
            switch (dataChangeType) {
                case ADDED:
                case ALL_DATA:
                    data.add(keyValue);
                    break;
                case DELETED:
                    data.remove(getKeyIndex(key));
                    break;
                case MODIFIED:
                    data.set(getKeyIndex(key), keyValue);
                    break;
                default:
                    break;
            }
            if (notify) notifyDataChanged();
        } catch (Exception e) {
            Log.d(CLASS_TAG, e.getMessage());
        }
    }

    private void notifyDataChanged() {
        notifyDataLoaded();
    }

    public void remove(String key) {
        Log.d(CLASS_TAG, "remove record");
        new Thread(() -> {
        removeFromDataSource(key);
        notifyDataChanged();
        Query query;
        if (autoKey) {
            query = mDatabase.orderByValue().equalTo(key);
        } else {
            query = mDatabase.orderByKey().equalTo(key);
        }
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshotRef : dataSnapshot.getChildren()) {
                    Log.d(CLASS_TAG, "removing record " + snapshotRef.getRef());
                    snapshotRef.getRef().removeValue();
                }
            }
            @Override
            @NonNull
            public void onCancelled(DatabaseError databaseError) {
                Log.d(CLASS_TAG, "remove->error occurred " + databaseError);

            }
        });
        }).start();
    }

    public void removeAll() {
        data.clear();
        //TODO remove all records from collection i.e. drop collection
        mDatabase.setValue(null);
    }

    private String getNewKey() {
        DatabaseReference newPostRef = mDatabase.push();
        return newPostRef.getKey();
    }

    public void save(String key, String value) {
        updateDB(Collections.singletonList(new KeyValue(key, value)));
    }

    @Override
    public void saveTransaction(String key, String value, DataStorageTransactionWorker dataStorageTransactionWorker) {
        mDatabase.child(key).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                String value = mutableData.getValue(String.class);
                // Set value and report transaction success
                mutableData.setValue(dataStorageTransactionWorker.updateTransactionData(value));
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed,
                                   DataSnapshot currentData) {
                // Transaction completed
                Log.d(CLASS_TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    public void save(List<KeyValue> values) {
        updateDB(values);
    }

    private void updateDB(List<KeyValue> values) {
        Log.d(CLASS_TAG, "updating DB");
        new Thread(() -> {
            Map<String, Object> childUpdates = new HashMap<>();
            for (KeyValue kv : values) {
                if (autoKey) {
                    if(kv.getKey() == null || kv.getKey().isEmpty()) {
                        kv.setKey(getNewKey());
                    }
                    childUpdates.put(kv.getValue(), kv.getKey());
                } else {
                    childUpdates.put(kv.getKey(), kv.getValue());
                }
                updateDataSource(kv.getKey(),kv.getValue());
            }
            notifyDataChanged();
            mDatabase.updateChildren(childUpdates, (databaseError, databaseReference) -> {
                if(databaseError == null) {
                    Log.d(CLASS_TAG, "DB updated successfully");
                } else {
                    Log.d(CLASS_TAG, "DB update failed, error " + databaseError);
                }
            });
        }).start();
    }

    public KeyValue getValue(int index) {
        return data.get(index);
    }

    public List<KeyValue> getValues() {
        return data;
    }

    public String getDataString(String collectionName) {
        return getDataMap(collectionName).toString();
    }

    public Map<String, String> getDataMap(String collectionName) {
        final Map<String, String> dataMap = new HashMap<>();
        for (KeyValue keyValue : data) {
            dataMap.put(keyValue.getKey(), keyValue.getValue());
        }
        return dataMap;
    }

    public void loadData(Map<String, String> data, boolean removeExistingData) {
        if (removeExistingData) {
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
        if (valueEventListener != null) {
            mDatabase.removeEventListener(valueEventListener);
        }
        if (childEventListener != null) {
            mDatabase.removeEventListener(childEventListener);
        }
    }
}

