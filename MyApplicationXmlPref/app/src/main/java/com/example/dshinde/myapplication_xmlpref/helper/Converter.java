package com.example.dshinde.myapplication_xmlpref.helper;

import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Converter {


    public static String getKeyValuesJsonString(List<KeyValue> data) {

        JSONArray jsonArray = new JSONArray();

        for (KeyValue keyValue: data){
            JSONObject formDetailsJson = new JSONObject();
            try {
                formDetailsJson.put(keyValue.getKey(), keyValue.getValue());
                jsonArray.put(formDetailsJson);
            } catch (Exception e) {

            }
        }
        return jsonArray.toString();
    }

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

    public static List<KeyValue> geKeyValueList(Map<String, String> data) {
        List<KeyValue> keyValues = new ArrayList<>();
        for (Map.Entry<String, String> entry: data.entrySet()){
            keyValues.add(new KeyValue(entry.getKey(), entry.getValue()));
        }
        return keyValues;
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
