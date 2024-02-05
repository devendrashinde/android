package com.example.dshinde.myapplication_xmlpref.services;

import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;

import java.util.ArrayList;
import java.util.List;

public abstract class ReadWriteOnceDataStorageManager implements ReadWriteOnceDataStorage {
    protected List<DataStorageListener> listeners = new ArrayList<>();
    String collectionName=null;
    String pathInCollection = null; // used to read specific record from collection
    List<KeyValue> data = new ArrayList<>();

    ReadWriteOnceDataStorageManager(String collectionName, DataStorageListener dataStorageListener) {
        if(collectionName.contains("/")) {
            this.pathInCollection = collectionName.split("/")[1];
            collectionName = collectionName.split("/")[0];
        }
        this.collectionName = collectionName;
        addDataStorageListener(dataStorageListener);
    }

    public void addDataStorageListener(DataStorageListener listener) {
        listeners.add(listener);
    }

    public void removeDataStorageListener(DataStorageListener listener) {
        listeners.remove(listener);
    }

    public int getKeyIndex(String key) {
        for(KeyValue item : data){
            if(item.getKey().equals(key)){
                return data.indexOf(item);
            }
        }
        return -1;
    }

    public String getValue(String key) {
        int index = getKeyIndex(key);
        if(index >= 0){
            return data.get(index).getValue();
        }
        return "";
    }

    public KeyValue getValue(int index) {
        return data.get(index);
    }

    public List<KeyValue> getValues() {
        return data;
    }

    public void removeDataStorageListeners() {
        listeners.clear();
    }

    void notifyDataLoaded() {
        for (DataStorageListener listener : listeners) {
            listener.dataLoaded(data);
        }
    }
}
