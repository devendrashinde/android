package com.example.dshinde.myapplication_xmlpref.model;

import androidx.annotation.Nullable;

import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.services.SharedPrefManager;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class KeyValue {
    private String key;
    private String value;

    public KeyValue(){
    }

    public KeyValue(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }

    @Exclude
    public static KeyValue getInstance(Map<String, String> item){
        String key = null;
        String value = null;
        for (Map.Entry<String, String> entry : item.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(Constants.KEY)){
                key = entry.getValue();
            }
            if (entry.getKey().equalsIgnoreCase(Constants.VALUE)){
                value = entry.getValue();
            }
        }
        return new KeyValue(key, value);
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(Constants.KEY, key);
        result.put(Constants.VALUE, value);
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj == null) return false;
        if(this.key == null) return false;
        KeyValue kv = (KeyValue) obj;
        if(kv.getKey() == null) return false;
        return this.key.equals(kv.getKey());
    }
}
