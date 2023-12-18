package com.example.dshinde.myapplication_xmlpref.services;

import com.example.dshinde.myapplication_xmlpref.common.DataChangeType;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageObservable;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public interface DataStorage extends DataStorageObservable {

    public static final String KEY = "key";
    public static final String VALUE = "value";

    int count();

    void loadData();

    String[] getKeys();

    void remove(String key);

    void removeAll();

    void save(String value);

    void disableSort();

    void enableSort();

    void disableNotifyDataChange();

    void enableNotifyDataChange();

    void save(String key, String value);

    void save(List<KeyValue> values);

    int getKeyIndex(String key);

    int getLastModifiedIndex();

    DataChangeType getLastDataChangeType();

    KeyValue getValue(int index);

    String getValue(String key);

    List<KeyValue> getValues();

    String getDataString();

    String getDataString(String collectionName);

    Map<String, String> getDataMap(String collectionName);

    void loadData(Map<String, String> data, boolean removeExistingData);

}
