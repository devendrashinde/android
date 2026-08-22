package com.example.dshinde.myapplication_xmlpref.helper;

import android.content.Context;

import com.example.dshinde.myapplication_xmlpref.common.DataStorageConfig;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.listners.FireStorageListener;
import com.example.dshinde.myapplication_xmlpref.services.DataStorage;
import com.example.dshinde.myapplication_xmlpref.services.FileStorage;
import com.example.dshinde.myapplication_xmlpref.services.FireDbDataStorageManagerImpl;
import com.example.dshinde.myapplication_xmlpref.services.FireDbReadWriteOnceDataStorageImpl;
import com.example.dshinde.myapplication_xmlpref.services.FireStorageManager;
import com.example.dshinde.myapplication_xmlpref.services.FireStoreDataStorageManagerImpl;
import com.example.dshinde.myapplication_xmlpref.services.ReadWriteOnceDataStorage;
import com.example.dshinde.myapplication_xmlpref.services.RoomDbDataStorageManagerImpl;
import com.example.dshinde.myapplication_xmlpref.services.RoomReadWriteOnceDataStorageImpl;

public class Factory {
    public static FileStorage getFileStorageInstance(Context context){
        return getFileStorageInstance(context, null, null);
    }
    public static FileStorage getFileStorageInstance(Context context, FireStorageListener fireStorageListener){
        return getFileStorageInstance(context, null, fireStorageListener);
    }
    public static FileStorage getFileStorageInstance(Context context, String collectionName){
        return getFileStorageInstance(context, collectionName, null);
    }
    public static FileStorage getFileStorageInstance(Context context, String collectionName, FireStorageListener fireStorageListener){
        return new FireStorageManager(context, collectionName, fireStorageListener);
    }
    public static DataStorage getDataStorageInstance(Context context, String name, boolean autoKey, boolean descendingOrder){
        return getDataStorageInstance(context, name, autoKey, descendingOrder, null);
    }
    public static DataStorage getDataStorageInstance(Context context, String name, boolean autoKey, boolean descendingOrder, DataStorageListener dataStorageListener){
        switch (DataStorageConfig.getDefaultType()) {
            case FIREBASE_DB:
                return dataStorageListener == null ?
                    new FireDbDataStorageManagerImpl(name, autoKey, descendingOrder) :
                    new FireDbDataStorageManagerImpl(name, autoKey, descendingOrder, dataStorageListener);
            case FIREBASE_STORE:
                return new FireStoreDataStorageManagerImpl(name, autoKey, descendingOrder);
            case ROOM_DB:
                return dataStorageListener == null ?
                    new RoomDbDataStorageManagerImpl(context, name, autoKey, descendingOrder) :
                    new RoomDbDataStorageManagerImpl(context, name, autoKey, descendingOrder, dataStorageListener);
            default:
                throw new IllegalStateException("Unsupported DataStorageType");
        }
    }

    public static DataStorage getRoomDataStorageInstance(
            Context context, String name, boolean autoKey, boolean descendingOrder, DataStorageListener listener) {
        return listener == null
            ? new RoomDbDataStorageManagerImpl(context, name, autoKey, descendingOrder)
            : new RoomDbDataStorageManagerImpl(context, name, autoKey, descendingOrder, listener);
    }

    public static DataStorage getRoomLocalOnlyDataStorageInstance(
            Context context, String name, boolean autoKey, boolean descendingOrder, DataStorageListener listener) {
        return listener == null
            ? new RoomDbDataStorageManagerImpl(context, name, autoKey, descendingOrder, null, false)
            : new RoomDbDataStorageManagerImpl(context, name, autoKey, descendingOrder, listener, false);
    }

    public static ReadWriteOnceDataStorage getReadWriteOnceDataStorageInstance(Context context, String noteId, DataStorageListener dataStorageListener) {
        switch (DataStorageConfig.getDefaultType()) {
            case ROOM_DB:
                return new RoomReadWriteOnceDataStorageImpl(context, noteId, dataStorageListener);
            case FIREBASE_DB:
            case FIREBASE_STORE:
            default:
                return new FireDbReadWriteOnceDataStorageImpl(noteId, dataStorageListener);
        }
    }
    public static ReadWriteOnceDataStorage getReadOnceFireDataStorageInstance(String name, DataStorageListener dataStorageListener){
        return new FireDbReadWriteOnceDataStorageImpl(name, dataStorageListener);
    }

    public static ReadWriteOnceDataStorage getReadWriteOnceRoomDataStorageInstance(
            Context context, String noteId, DataStorageListener dataStorageListener) {
        return new RoomReadWriteOnceDataStorageImpl(context, noteId, dataStorageListener);
    }
}
