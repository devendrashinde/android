package com.example.dshinde.myapplication_xmlpref.listners;

import com.example.dshinde.myapplication_xmlpref.model.KeyValue;

import java.util.List;

public interface DataStorageListener {
    public void dataChanged(String key, String value);
    public void dataLoaded(List<KeyValue> data);
}
