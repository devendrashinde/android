package com.example.dshinde.myapplication_xmlpref.listners;

public interface DataStorageObservable {
    public void addDataStorageListener(DataStorageListener listener);
    public void removeDataStorageListener(DataStorageListener listener);
    public void removeDataStorageListeners();
}
