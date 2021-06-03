package com.example.dshinde.myapplication_xmlpref.helper;

public class Utils {

    public static final String REGEX = "-?\\d+(\\.\\d+)?";

    public static boolean isNumeric(String text) {
        return text.matches(REGEX);  //match a number with optional '-' and decimal.
    }

}
