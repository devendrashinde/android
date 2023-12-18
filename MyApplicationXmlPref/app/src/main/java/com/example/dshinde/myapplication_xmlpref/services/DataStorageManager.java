package com.example.dshinde.myapplication_xmlpref.services;

import android.os.Handler;
import android.os.Looper;

import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.common.DataChangeType;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public abstract class DataStorageManager implements DataStorage {
    List<DataStorageListener> listeners = new ArrayList<>();
    List<KeyValue> data = new ArrayList<>();
    boolean autoKey=false;
    boolean descendingOrder=false;
    boolean sortData =true;
    boolean notifyDataChange=true;
    private int lastModifiedIndex=-1;
    private DataChangeType lastDataChangeType = DataChangeType.ALL_DATA;

    public void addDataStorageListener(DataStorageListener listener) {
        if(listener != null) listeners.add(listener);
    }

    public void removeDataStorageListener(DataStorageListener listener) {
        if(listener != null) listeners.remove(listener);
    }

    public void removeDataStorageListeners() {
        listeners.clear();
    }

    public int count() {
        return data.size();
    }

    public String[] getKeys() {
        return new String[]{KEY, VALUE};
    }

    public KeyValue getValue(int index) {
        return data.get(index);
    }

    public List<KeyValue> getValues() {
        return data;
    }

    public int getKeyIndex(String key) {
        return data.indexOf(new KeyValue(key, null));
    }

    public String getValue(String key) {
        int index = getKeyIndex(key);
        if(index >= 0){
            return data.get(index).getValue();
        }
        return "";
    }

    public int getLastModifiedIndex(){
        return lastModifiedIndex;
    }

    public DataChangeType getLastDataChangeType() {
        return lastDataChangeType;
    }

    public void save(String value) {
        save(null, value);
    }

    public void save(List<KeyValue> values) {

    }

    void removeFromDataSource(String key) {
        int keyIndex = getKeyIndex(key);
        if (keyIndex >= 0) {
            data.remove(keyIndex);
            lastModifiedIndex = keyIndex;
            lastDataChangeType = DataChangeType.DELETED;
        }
    }

    void updateDataSource(String key, String value) {
        int keyIndex = getKeyIndex(key);
        if (keyIndex >= 0) {
            data.set(keyIndex, new KeyValue(key, value));
            lastDataChangeType = DataChangeType.MODIFIED;
            lastModifiedIndex = keyIndex;
        } else {
            data.add(new KeyValue(key, value));
            lastDataChangeType = DataChangeType.ADDED;
            lastModifiedIndex = data.size()-1;
        }
    }

    @Override
    public void disableSort(){
        sortData = false;
    }

    @Override
    public void enableSort(){
        sortData = true;
    }

    @Override
    public void disableNotifyDataChange(){
        notifyDataChange = false;
    }

    @Override
    public void enableNotifyDataChange(){
        notifyDataChange = true;
    }

    void notifyDataSetChanged(String key, String value) {
        if(notifyDataChange) {
            if (sortData) Collections.sort(data, keyValueComparator);
            for (DataStorageListener listener : listeners) {
                listener.dataChanged(key, value);
            }
        }
    }

    void notifyDataLoaded() {
        if(notifyDataChange) {
            KeyValue keyValue = null;
            if(lastDataChangeType == DataChangeType.ADDED) keyValue = data.get(lastModifiedIndex);

            if (sortData) Collections.sort(data, keyValueComparator);
            if(keyValue != null) lastModifiedIndex = data.indexOf(keyValue);
            for (DataStorageListener listener : listeners) {
                listener.dataLoaded(data);
            }
        }
    }

    Comparator<Map<String, String>> mapComparator = new Comparator<Map<String, String>>() {
        public int compare(Map<String, String> m1, Map<String, String> m2) {
            return autoKey ?
                    (descendingOrder ? m2.get(VALUE).compareTo(m1.get(VALUE)): m1.get(VALUE).compareTo(m2.get(VALUE))) :
                    (descendingOrder ? m2.get(KEY).compareTo(m1.get(KEY)) : m1.get(KEY).compareTo(m2.get(KEY)));
        }
    };

    Comparator<KeyValue> keyValueComparator = new Comparator<KeyValue>() {
        public int compare(KeyValue m1, KeyValue m2) {
            return autoKey ?
                    (descendingOrder ? m2.getValue().compareTo(m1.getValue()): m1.getValue().compareTo(m2.getValue())) :
                    (descendingOrder ? m2.getKey().compareTo(m1.getKey()) : m1.getKey().compareTo(m2.getKey()));
        }
    };

    public String getDataString(){
        StringBuilder dataString  = new StringBuilder();
        if (!data.isEmpty()) {
            for (KeyValue entry : data) {
                dataString.append(Constants.CR_LF).append(entry.getKey().trim()).append(Constants.CR_LF).append(entry.getValue().trim());
            }
        }
        return dataString.toString();
    }

}
