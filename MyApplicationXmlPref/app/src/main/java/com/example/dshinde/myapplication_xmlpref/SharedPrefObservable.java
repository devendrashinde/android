package com.example.dshinde.myapplication_xmlpref;

public interface SharedPrefObservable {
    public void add(SharedPrefListener listener);
    public void remove(SharedPrefListener listener);
}
