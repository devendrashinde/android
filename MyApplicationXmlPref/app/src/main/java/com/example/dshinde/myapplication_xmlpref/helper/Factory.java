package com.example.dshinde.myapplication_xmlpref.helper;

import android.content.Context;

import com.example.dshinde.myapplication_xmlpref.MyNotesApplication;
import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.common.DataStorageType;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.listners.FireStorageListener;
import com.example.dshinde.myapplication_xmlpref.services.DataStorage;
import com.example.dshinde.myapplication_xmlpref.services.FileStorage;
import com.example.dshinde.myapplication_xmlpref.services.FireDbDataStorageManagerImpl;
import com.example.dshinde.myapplication_xmlpref.services.FireDbReadWriteOnceDataStorageImpl;
import com.example.dshinde.myapplication_xmlpref.services.FireStorageManager;
import com.example.dshinde.myapplication_xmlpref.services.FireStoreDataStorageManagerImpl;
import com.example.dshinde.myapplication_xmlpref.services.ReadWriteOnceDataStorage;
import com.example.dshinde.myapplication_xmlpref.services.SharedPrefDataStorageManagerImpl;

public class Factory {


    public static FileStorage getFileStorageInstance(Context context){
        return getFileStorageInstance(context, null);
    }

    public static FileStorage getFileStorageInstance(Context context, FireStorageListener fireStorageListener){
        MyNotesApplication app = (MyNotesApplication) context.getApplicationContext();
        String storagePath = Constants.STORAGE_PATH_NOTES + "/" + app.getUserId();
        return new FireStorageManager(context, storagePath, fireStorageListener);
    }

    public static DataStorage getDataStorageInstance(Context context, DataStorageType dataStorageType, String name, boolean autoKey, boolean descendingOrder){
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

    public static DataStorage getDataStorageInstance(Context context, DataStorageType dataStorageType, String name, boolean autoKey, boolean descendingOrder, DataStorageListener dataStorageListener){
        DataStorage dataStorageManager=null;
        if (dataStorageType == DataStorageType.SHARED_PREFERENCES) {
            dataStorageManager = new SharedPrefDataStorageManagerImpl(context, name, autoKey, descendingOrder, dataStorageListener);
        } else if (dataStorageType == DataStorageType.FIREBASE_DB) {
            dataStorageManager = new FireDbDataStorageManagerImpl(name, autoKey, descendingOrder, dataStorageListener);
        } else if (dataStorageType == DataStorageType.FIREBASE_STORE) {
            dataStorageManager = new FireStoreDataStorageManagerImpl(name, autoKey, descendingOrder);
        }
        return dataStorageManager;
    }

    public static ReadWriteOnceDataStorage getReadOnceFireDataStorageInstance(String name, DataStorageListener dataStorageListener){
        return new FireDbReadWriteOnceDataStorageImpl(name, dataStorageListener);
    }

}
