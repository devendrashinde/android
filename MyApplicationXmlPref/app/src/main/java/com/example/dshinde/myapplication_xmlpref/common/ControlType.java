package com.example.dshinde.myapplication_xmlpref.common;

import java.util.HashMap;
import java.util.Map;

public enum ControlType {
    Text("Text"),
    EditText("EditText"),
    MultiLineEditText("MultiLineEditText"),
    CheckBox("CheckBox"),
    RadioButton("RadioButton"),
    TimePicker("TimePicker"),
    DatePicker("DatePicker"),
    DropDownList("DropDownList"),
    NextButton("NextButton"),
    SaveButton("SaveButton"),
    BackButton("BackButton"),
    CancelButton("CancelButton"),
    Photo("Photo"),
    Document("Document");

    private String value;

    private static Map<String, ControlType> values = new HashMap<>();

    ControlType(String value) {
        this.value = value;
    }

    static {
        for (ControlType controlType : values()) {
            values.put(controlType.value, controlType);
            values.put(controlType.value.toLowerCase(), controlType);
        }
    }

    public String getValue() {
        return value;
    }

    public static ControlType fromString(String text) {

        return values.get(text);
    }
}
