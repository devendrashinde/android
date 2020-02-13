package com.example.dshinde.myapplication_xmlpref.helper;

import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

public class Converter {

    public static String getValuesJsonString(List<KeyValue> data) {
        StringBuilder values = new StringBuilder("[");
        boolean firstTime = true;
        for (KeyValue keyValue: data){
            values.append(firstTime ? "" : ",");
            values.append(keyValue.getValue());
            firstTime = false;
        }
        values.append("]");
        return values.toString();
    }

    public static String getKeysJsonString(List<KeyValue> data) {
        StringBuilder values = new StringBuilder("[");
        boolean firstTime = true;
        for (KeyValue keyValue: data){
            values.append(firstTime ? "" : ",");
            values.append(keyValue.getKey());
            firstTime = false;
        }
        values.append("]");
        return values.toString();
    }

    public static String getJsonString(List<KeyValue> data) {
        return new GsonBuilder().create().toJson(data);
    }
}
