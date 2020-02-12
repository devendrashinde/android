package com.example.dshinde.myapplication_xmlpref.listners;

public interface SharedPrefObservable {
    public void add(SharedPrefListener listener);
    public void remove(SharedPrefListener listener);
}
