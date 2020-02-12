package com.example.dshinde.myapplication_xmlpref.helper;

import android.content.Context;

import com.example.dshinde.myapplication_xmlpref.common.DataStorageType;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.services.DataStorage;
import com.example.dshinde.myapplication_xmlpref.services.FireDbDataStorageManagerImpl;
import com.example.dshinde.myapplication_xmlpref.services.FireDbReadOnceDataStorageImpl;
import com.example.dshinde.myapplication_xmlpref.services.FireStoreDataStorageManagerImpl;
import com.example.dshinde.myapplication_xmlpref.services.ReadOnceDataStorage;
import com.example.dshinde.myapplication_xmlpref.services.SharedPrefDataStorageManagerImpl;
import com.example.dshinde.myapplication_xmlpref.services.SharedPrefReadOnceDataStorageImpl;

public class Factory {

    public static DataStorage getDataStorageIntsance(Context context, DataStorageType dataStorageType, String name, boolean autoKey, boolean descendingOrder){
        DataStorage dataStorageManager=null;
        if (dataStorageType == DataStorageType.SHARED_PREFERENCES) {
            dataStorageManager = new SharedPrefDataStorageManagerImpl(context, name, autoKey, descendingOrder);
        } else if (dataStorageType == DataStorageType.FIREBASE_DB) {
            dataStorageManager = new FireDbDataStorageManagerImpl(name, autoKey, descendingOrder);
        } else if (dataStorageType == DataStorageType.FIREBASE_STORE) {
            dataStorageManager = new FireStoreDataStorageManagerImpl(name, autoKey, descendingOrder);
        }
        return dataStorageManager;
    }

    public static ReadOnceDataStorage getReadOnceDataStorageIntsance(Context context, DataStorageType dataStorageType, String name, DataStorageListener dataStorageListener){
        ReadOnceDataStorage dataStorageManager=null;
        if (dataStorageType == DataStorageType.SHARED_PREFERENCES) {
            dataStorageManager = new SharedPrefReadOnceDataStorageImpl(context, name, dataStorageListener);
        } else if (dataStorageType == DataStorageType.FIREBASE_DB) {
            dataStorageManager = new FireDbReadOnceDataStorageImpl(name, dataStorageListener);
        }
        return dataStorageManager;
    }

}
