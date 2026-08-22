package com.example.dshinde.myapplication_xmlpref.common;

public class DataStorageConfig {
    private static DataStorageType defaultType = DataStorageType.ROOM_DB; // Set your default here

    public static DataStorageType getDefaultType() {
        return defaultType;
    }

    public static void setDefaultType(DataStorageType type) {
        defaultType = type;
    }
}
