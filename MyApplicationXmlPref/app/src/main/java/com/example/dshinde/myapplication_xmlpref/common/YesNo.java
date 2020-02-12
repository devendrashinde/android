package com.example.dshinde.myapplication_xmlpref.common;

import java.util.HashMap;
import java.util.Map;

public enum YesNo {
    YES("Yes", true),
    NO("No", false);

    private String value;
    private boolean asBoolean;

    private static Map<String, YesNo> stringToYesNo = new HashMap<>();
    private static Map<Boolean, YesNo> booleanToYesNo = new HashMap<>();

    YesNo(String value, boolean asBoolean) {
        this.value = value;
        this.asBoolean = asBoolean;
    }

    static {
        for (YesNo yesNo : values()) {
            stringToYesNo.put(yesNo.value, yesNo);
            stringToYesNo.put(yesNo.value.toLowerCase(), yesNo);
            booleanToYesNo.put(yesNo.asBoolean, yesNo);
        }
    }

    public String getValue() {
        return value;
    }

    public static boolean mapYesNoToBoolean(String yesNo) {

        YesNo yesNoValue = YesNo.fromString(yesNo);
        return yesNoValue == null ? false : yesNoValue.asBoolean;
    }

    public Boolean toBoolean() {
        return asBoolean;
    }

    public static YesNo fromString(String text) {

        return stringToYesNo.get(text);
    }

    public static YesNo fromBoolean(boolean value) {

        return booleanToYesNo.get(value);
    }
}