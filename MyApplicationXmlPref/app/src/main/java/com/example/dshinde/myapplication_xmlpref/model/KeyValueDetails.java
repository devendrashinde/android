package com.example.dshinde.myapplication_xmlpref.model;

import com.example.dshinde.myapplication_xmlpref.services.SharedPrefManager;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class KeyValueDetails {
    private String key;
    private List<KeyValue> values;

    public KeyValueDetails(){
    }

    public KeyValueDetails(String key, List<KeyValue> values) {
        this.key = key;
        this.values = values;
    }

    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }

    public List<KeyValue> getValues() {
        return values;
    }
    public void setValues(List<KeyValue> values) {
        this.values = values;
    }

    @Exclude
    public static KeyValueDetails getInstance(String baseKey, Map<String, String> items){
        String key = null;
        String value = null;
        List<KeyValue> values = new ArrayList<>();
        for (Map.Entry<String, String> entry : items.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(SharedPrefManager.KEY)){
                key = entry.getValue();
            }
            if (entry.getKey().equalsIgnoreCase(SharedPrefManager.VALUE)){
                value = entry.getValue();
            }
            values.add(new KeyValue(key, value));
        }
        return new KeyValueDetails(baseKey, values);
    }

}
