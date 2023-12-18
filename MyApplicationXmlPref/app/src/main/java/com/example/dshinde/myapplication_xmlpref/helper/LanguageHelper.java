package com.example.dshinde.myapplication_xmlpref.helper;

import com.example.dshinde.myapplication_xmlpref.common.Constants;

import java.util.Locale;

public class LanguageHelper {

    public static Locale getLanguage(String language){
        String lang = "en";
        switch (language) {
            case Constants.MARATHI:
                lang = "mr";
                break;
            case Constants.HINDI:
                lang = "hi";
                break;
            default:
                lang = "en";
        }
        return new Locale(lang, "IN");
    }
}
