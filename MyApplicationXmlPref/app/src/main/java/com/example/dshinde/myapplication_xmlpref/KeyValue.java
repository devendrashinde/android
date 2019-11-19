package com.example.dshinde.myapplication_xmlpref;

import java.util.Map;

public class KeyValue {
    private String key;
    private String value;

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

    public static KeyValue getInstance(Map<String, String> item){
        String key = null;
        String value = null;
        for (Map.Entry<String, String> entry : item.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(SharedPrefManager.KEY)){
                key = entry.getValue();
            }
            if (entry.getKey().equalsIgnoreCase(SharedPrefManager.VALUE)){
                value = entry.getValue();
            }
        }
        return new KeyValue(key, value);

    }


}
