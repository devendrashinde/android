package com.example.dshinde.myapplication_xmlpref.services;

import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageObservable;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;

import java.util.List;
import java.util.Map;

public interface ReadWriteOnceDataStorage extends DataStorageObservable {

    int getKeyIndex(String key);

    KeyValue getValue(int index);

    String getValue(String key);

    List<KeyValue> getValues();

}
