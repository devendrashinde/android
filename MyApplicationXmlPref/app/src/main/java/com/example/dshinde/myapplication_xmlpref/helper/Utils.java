package com.example.dshinde.myapplication_xmlpref.helper;

import android.util.Log;
import android.util.Patterns;

import com.example.dshinde.myapplication_xmlpref.common.ControlType;
import com.example.dshinde.myapplication_xmlpref.model.ScreenControl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Patterns;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;

public class Utils {

    public static final String REGEX = "-?\\d+(\\.\\d+)?";

    public static boolean isNumeric(String text) {
        return text.matches(REGEX);  //match a number with optional '-' and decimal.
    }

    private static boolean isMultiOptionControl(ControlType type) {
        return type == ControlType.CheckBox
                || type == ControlType.RadioButton
                || type == ControlType.DropDownList
                || type == ControlType.Expression;
    }

    public static List<String> extractLinks(String text) {
        List<String> links = new ArrayList<>();
        Matcher m = Patterns.WEB_URL.matcher(text);
        while (m.find()) {
            String url = m.group();
            links.add(url);
        }
        return links;
    }

    public static List<ScreenControl> parseScreenConfig(String screenConfigJson) {
        List<ScreenControl> controls = new ArrayList<>();
        Gson gson = new GsonBuilder().create();

        try {
            JSONArray arr = new JSONArray(screenConfigJson);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                ScreenControl item = gson.fromJson(obj.toString(), ScreenControl.class);
                if(item.getPositionId() == null){
                    item.setPositionId(String.format("%03d", i+1));
                } else{
                    item.setPositionId(String.format("%03d", Integer.valueOf(item.getPositionId())));
                }
                if (isMultiOptionControl(item.getControlType()) &&
                        item.getOptions() != null &&
                        item.getOptions().length() > 0) {
                    item.setOptionValues(item.getOptions().split("\\n"));
                }
                controls.add(item);
            }
            Collections.sort(controls, positionIdComparator);

        } catch (JSONException e) {
            System.out.print(e);
        }
        return controls;
    }

    private static Comparator<ScreenControl> positionIdComparator = new Comparator<ScreenControl>() {
        public int compare(ScreenControl m1, ScreenControl m2) {
            return m1.getPositionId().compareTo(m2.getPositionId());
        }
    };




}
