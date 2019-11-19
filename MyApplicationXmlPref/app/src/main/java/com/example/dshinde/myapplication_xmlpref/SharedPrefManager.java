package com.example.dshinde.myapplication_xmlpref;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static android.content.Context.MODE_PRIVATE;

public class SharedPrefManager implements SharedPrefObservable {
    SharedPreferences sharedpreferences;
    List<KeyValue> data = new ArrayList<>();
    List<SharedPrefListener> listeners = new ArrayList<SharedPrefListener>();
    String sharedPreferenceName;
    public static final String KEY = "key";
    public static final String VALUE = "value";
    boolean autoKey=false;
    boolean descendingOrder=false;
    int lastKeyValue =-1;

    public SharedPrefManager(Context context, String sharedPreferenceName, boolean autoKey) {
        this(context, sharedPreferenceName, autoKey, false);
    }

    public SharedPrefManager(Context context, String sharedPreferenceName, boolean autoKey, boolean descendingOrder) {
        this.sharedPreferenceName = sharedPreferenceName;
        this.autoKey = autoKey;
        this.descendingOrder = descendingOrder;
        sharedpreferences = context.getSharedPreferences(sharedPreferenceName, MODE_PRIVATE);
        loadData();
    }

    @Override
    public void add(SharedPrefListener listener) {
        listeners.add(listener);
    }

    public int count() {
        return data.size();
    }

    public String[] getKeys() {
        return new String[]{KEY, VALUE};
    }

    @Override
    public void remove(SharedPrefListener listener) {
        listeners.remove(listener);
    }

    public void remove(String key) {
        removeFromDataSource(key);
        sharedpreferences.edit().remove(key).apply();
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
        if (key == null || key.isEmpty()) {
            key = getNewKey();
        }
        updateDataSource(key, value);
        sharedpreferences.edit().putString(key, value).apply();
    }

    public String getValue(String key) {
        if (!key.isEmpty() && sharedpreferences.contains(key)) {
            return sharedpreferences.getString(key, "");
        }
        return "";
    }

    public KeyValue getValue(int index) {
        return data.get(index);
    }

    public List<KeyValue> getValues() {
        return data;
    }

    private void loadData() {
        Map<String, ?> allEntries = sharedpreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if(autoKey && entry.getKey().compareToIgnoreCase("LastKey") !=0) {
                if(lastKeyValue < Integer.valueOf(entry.getKey()))
                lastKeyValue = Integer.valueOf(entry.getKey());
            }
            data.add(new KeyValue(entry.getKey(), entry.getValue().toString()));
        }
        Collections.sort(data, keyValueComparator);

    }

    private int getKeyIndex(String key) {
        for(KeyValue item : data){
            if(item.getKey().equals(key)){
                return data.indexOf(item);
            }
        }
        return -1;
    }

    private void updateDataSource(String key, String value) {
        int keyIndex = getKeyIndex(key);
        if (keyIndex >= 0) {
            data.set(keyIndex, new KeyValue(key, value));
        } else {
            data.add(new KeyValue(key, value));
        }
        Collections.sort(data, keyValueComparator);
        notifyDataSetChanged(key, value);
    }

    private void removeFromDataSource(String key) {
        int keyIndex = getKeyIndex(key);
        if (keyIndex >= 0) {
            data.remove(keyIndex);
            notifyDataSetChanged(key, null);
        }
    }

    private void notifyDataSetChanged(String key, String value) {
        for (SharedPrefListener listener : listeners) {
            listener.sharedPrefChanged(key, value);
        }
    }

    private Comparator<Map<String, String>> mapComparator = new Comparator<Map<String, String>>() {
        public int compare(Map<String, String> m1, Map<String, String> m2) {
            return autoKey ?
                    (descendingOrder ? m2.get(VALUE).compareTo(m1.get(VALUE)): m1.get(VALUE).compareTo(m2.get(VALUE))) :
                    (descendingOrder ? m2.get(KEY).compareTo(m1.get(KEY)) : m1.get(KEY).compareTo(m2.get(KEY)));
        }
    };

    private Comparator<KeyValue> keyValueComparator = new Comparator<KeyValue>() {
        public int compare(KeyValue m1, KeyValue m2) {
            return autoKey ?
                    (descendingOrder ? m2.getValue().compareTo(m1.getValue()): m1.getValue().compareTo(m2.getValue())) :
                    (descendingOrder ? m2.getKey().compareTo(m1.getKey()) : m1.getKey().compareTo(m2.getKey()));
        }
    };

    public static boolean copy(Context context, String src, String dest) {
        Map<String, String> allEntries = getDataMap(getSharedPreferences(context, src));
        if (!allEntries.isEmpty()) {
            SharedPreferences sharedpreferences = getSharedPreferences(context, dest);
            for (Map.Entry<String, String> entry : allEntries.entrySet()) {
                sharedpreferences.edit().putString(entry.getKey(), entry.getValue()).apply();
            }
            return true;
        }
        return false;
    }

    public String getDataString(){
        return getDataString(sharedpreferences);
    }

    public Map<String,String> getDataMap(){
        return getDataMap(sharedpreferences);
    }

    public void loadData(Map<String,String> data, boolean removeExistingData){
        if(removeExistingData) {
            removeAll();
        }
        for (Map.Entry<String, String> entry : data.entrySet()) {
            save(entry.getKey(), entry.getValue());
        }
    }

    public static String getDataString(Context context, String fileName){
        return getDataString(getSharedPreferences(context, fileName));
    }

    public static Map<String,String> getDataMap(Context context, String fileName){
        return getDataMap(getSharedPreferences(context, fileName));
    }

    public static boolean loadData(Context context, String fileName, Map<String,Object> data, boolean removeExistingData){
        if(!data.isEmpty()) {
            SharedPreferences sharedPreferences = getSharedPreferences(context, fileName);
            if (removeExistingData) {
                sharedPreferences.edit().clear().apply();
            }
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                sharedPreferences.edit().putString(entry.getKey(), entry.getValue().toString()).apply();
            }
            return true;
        }
        return false;
    }

    private static String getDataString(SharedPreferences sharedpreferences){
        Map<String, String> allEntries = getDataMap(sharedpreferences);
        StringBuilder data  = new StringBuilder();
        String crLf = "\r\n";
        if (!allEntries.isEmpty()) {
            for (Map.Entry<String, String> entry : allEntries.entrySet()) {
                data.append(crLf).append(entry.getKey().trim()).append(crLf).append(entry.getValue().trim()).append(crLf);
            }
        }
        return data.toString();
    }

    private static Map<String,String> getDataMap(SharedPreferences sharedpreferences){
        return new TreeMap((Map<String,String>) sharedpreferences.getAll());
    }

    private static SharedPreferences getSharedPreferences(Context context, String fileName){
        return context.getSharedPreferences(fileName, MODE_PRIVATE);
    }
}
