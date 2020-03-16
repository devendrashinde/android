package com.example.dshinde.myapplication_xmlpref.helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class JsonHelper {
    public static Object toJSON(Object object) throws JSONException {
        if (object instanceof Map) {
            JSONObject json = new JSONObject();
            Map map = (Map) object;
            for (Object key : map.keySet()) {
                json.put(key.toString(), toJSON(map.get(key)));
            }
            return json;
        } else if (object instanceof Iterable) {
            JSONArray json = new JSONArray();
            for (Object value : ((Iterable) object)) {
                json.put(value);
            }
            return json;
        } else {
            return object;
        }
    }

    public static boolean isEmptyObject(JSONObject object) {
        return object.names() == null;
    }

    public static Map<String, Object> getMap(JSONObject object, String key) throws JSONException {
        return toMap(object.getJSONObject(key));
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap();
        Iterator keys = object.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            map.put(key, fromJson(object.get(key)));
        }
        return map;
    }

    public static List toList(JSONArray array) throws JSONException {
        List list = new ArrayList();
        for (int i = 0; i < array.length(); i++) {
            list.add(fromJson(array.get(i)));
        }
        return list;
    }

    private static Object fromJson(Object json) throws JSONException {
        if (json == JSONObject.NULL) {
            return null;
        } else if (json instanceof JSONObject) {
            return toMap((JSONObject) json);
        } else if (json instanceof JSONArray) {
            return toList((JSONArray) json);
        } else {
            return json;
        }
    }

    public static String formatAsString(String value) {
        return formatAsString(value, false);
    }

    public static String formatAsString(String value, boolean html) {
        String formattedValue = value;
        if(html) {
            formattedValue = formattedValue.replaceAll("\\\\n", "<br>");
            formattedValue = formattedValue.replaceAll("\\\\", "");
            formattedValue = formattedValue.replaceAll("\\{\"", "<b>");
            formattedValue = formattedValue.replaceAll("\\{", "<b>");
            formattedValue = formattedValue.replaceAll("=\\{", "</b>{");
            formattedValue = formattedValue.replaceAll("\":\"", "</b>:<br>");
            formattedValue = formattedValue.replaceAll("\",\"", "<br><b>");
            formattedValue = formattedValue.replaceAll("\"\\},", "<br><br>");
            formattedValue = formattedValue.replaceAll("\"", "");
            formattedValue = formattedValue.replaceAll("\\}", "");
            formattedValue = formattedValue.replaceAll("\\\\u0026", "&amp;");
            formattedValue = formattedValue.replaceAll("=", "</b><br>");
            formattedValue = formattedValue.replaceAll("\\[", "");
            formattedValue = formattedValue.replaceAll("\\]", "");
            formattedValue = formattedValue.replaceAll("\\*\\*\\* ", "<h3>");
            formattedValue = formattedValue.replaceAll(" \\*\\*\\*", "</h3>");
        } else {
            formattedValue = formattedValue.replaceAll("\\\\n", "\n ");
            formattedValue = formattedValue.replaceAll("\",\"", "\n");
            formattedValue = formattedValue.replaceAll("\"", "");
            formattedValue = formattedValue.replaceAll("\\{", "");
            formattedValue = formattedValue.replaceAll(":", ":\n ");
            formattedValue = formattedValue.replaceAll("\\}", "");
        }
        return formattedValue;
    }

    public static boolean isJSONValid(String value) {
        if (value == null) return false;
        try {
            new JSONObject(value);
        } catch (JSONException ex) {
            try {
                new JSONArray(value);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

}