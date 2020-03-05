package com.example.dshinde.myapplication_xmlpref.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static android.content.Context.MODE_PRIVATE;

public class SharedPrefDataStorageManagerImpl extends DataStorageManager {
    Context context;
    SharedPreferences sharedpreferences;
    String sharedPreferenceName;
    int lastKeyValue =-1;
    private static final String CLASS_TAG = "SharedPrefStorageMgr";

    public SharedPrefDataStorageManagerImpl(Context context, String sharedPreferenceName, boolean autoKey) {
        this(context, sharedPreferenceName, autoKey, false);
    }

    public SharedPrefDataStorageManagerImpl(Context context, String sharedPreferenceName, boolean autoKey, boolean descendingOrder) {
        this(context, sharedPreferenceName, autoKey, descendingOrder, null);
    }

    public SharedPrefDataStorageManagerImpl(Context context, String sharedPreferenceName, boolean autoKey, boolean descendingOrder, DataStorageListener dataStorageListener) {
        this.sharedPreferenceName = sharedPreferenceName;
        this.autoKey = autoKey;
        this.descendingOrder = descendingOrder;
        this.context = context;
        this.addDataStorageListener(dataStorageListener);
        getDatabaseCollectionReference();
    }

    public void remove(String key) {
        Log.d(CLASS_TAG, "remove");
        new Thread() {
            @Override
            public void run() {
                removeFromDataSource(key);
                notifyDataChanged();
                sharedpreferences.edit().remove(key).apply();
            }
        }.start();
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
        Log.d(CLASS_TAG, "update DB");
        new Thread() {
            @Override
            public void run() {
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
                Log.d(CLASS_TAG, "DB updated");
                notifyDataChanged();
            }
        }.start();
    }

    private void notifyDataChanged(){
        Log.d(CLASS_TAG, "notifyDataChanged");
        Collections.sort(data, keyValueComparator);
        notifyDataLoaded();
    }

    private void getDatabaseCollectionReference() {
        Log.d(CLASS_TAG, "getDatabaseCollectionReference");
        if(sharedpreferences == null) {
            sharedpreferences = context.getSharedPreferences(sharedPreferenceName, MODE_PRIVATE);
        }
    }
    public List<KeyValue> getValues() {
        return data;
    }

    public void loadData() {
        Log.d(CLASS_TAG, "loadData");
        new Thread() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                data.clear();
                Map<String, ?> allEntries = sharedpreferences.getAll();
                for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                    if (autoKey && entry.getKey().compareToIgnoreCase("LastKey") != 0) {
                        if (lastKeyValue < Integer.valueOf(entry.getKey()))
                            lastKeyValue = Integer.valueOf(entry.getKey());
                    }
                    data.add(new KeyValue(entry.getKey(), entry.getValue().toString()));
                }
                notifyDataChanged();
            }
        }.start();
    }

    public String getDataString(String collectionName){
        return getDataMap(collectionName).toString();
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
