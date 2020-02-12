package com.example.dshinde.myapplication_xmlpref.services;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static android.content.Context.MODE_PRIVATE;

public class SharedPrefDataStorageManagerImpl extends DataStorageManager {
    Context context;
    SharedPreferences sharedpreferences;
    String sharedPreferenceName;
    int lastKeyValue =-1;

    public SharedPrefDataStorageManagerImpl(Context context, String sharedPreferenceName, boolean autoKey) {
        this(context, sharedPreferenceName, autoKey, false);
    }

    public SharedPrefDataStorageManagerImpl(Context context, String sharedPreferenceName, boolean autoKey, boolean descendingOrder) {
        this.sharedPreferenceName = sharedPreferenceName;
        this.autoKey = autoKey;
        this.descendingOrder = descendingOrder;
        this.context = context;
        this.sharedpreferences = context.getSharedPreferences(sharedPreferenceName, MODE_PRIVATE);
        loadData();
    }

    public void remove(String key) {
        removeFromDataSource(key);
        sharedpreferences.edit().remove(key).apply();
        notifyDataSetChanged(key, null);
    }

    public void removeAll() {
        data.clear();
        sharedpreferences.edit().clear().apply();
    }

    private String getNewKey() {
        return String.valueOf(++lastKeyValue);
    }

    public void save(String value) {
        save(null, value);
    }

    public void save(String key, String value) {
        updateDB(Collections.singletonList(new KeyValue(key, value)));
    }

    private void updateDB(List<KeyValue> values) {
        String key = null;
        String value = null;
        for (KeyValue kv : values) {
            key = kv.getKey();
            value = kv.getValue();
            if (autoKey && (key == null || key.isEmpty())) {
                key = getNewKey();
            }
            updateDataSource(key, value);
            sharedpreferences.edit().putString(key, value).apply();
        }
        Collections.sort(data, keyValueComparator);
        notifyDataSetChanged(key, value);

    }

    public List<KeyValue> getValues() {
        return data;
    }

    public void loadData() {
        data.clear();
        Map<String, ?> allEntries = sharedpreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (autoKey && entry.getKey().compareToIgnoreCase("LastKey") != 0) {
                if (lastKeyValue < Integer.valueOf(entry.getKey()))
                    lastKeyValue = Integer.valueOf(entry.getKey());
            }
            data.add(new KeyValue(entry.getKey(), entry.getValue().toString()));
        }
        Collections.sort(data, keyValueComparator);
        notifyDataLoaded();
    }

    public String getDataString(String fileName){
        return getDataMap(fileName).toString();
    }

    public Map<String,String> getDataMap(){
        return getDataMap(sharedpreferences);
    }

    public Map<String,String> getDataMap(String fileName){
        return getDataMap(getSharedPreferences(fileName));
    }

    public void loadData(Map<String,String> data, boolean removeExistingData){
        if(removeExistingData) {
            removeAll();
        }
        for (Map.Entry<String, String> entry : data.entrySet()) {
            save(entry.getKey(), entry.getValue());
        }
    }

    private Map<String,String> getDataMap(SharedPreferences sharedpreferences){
        return new TreeMap((Map<String,String>) sharedpreferences.getAll());
    }

    private SharedPreferences getSharedPreferences(String fileName){
        return context.getSharedPreferences(fileName, MODE_PRIVATE);
    }
}
