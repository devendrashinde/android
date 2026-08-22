package com.example.dshinde.myapplication_xmlpref;

import android.app.Application;
import android.content.Context;

import com.example.dshinde.myapplication_xmlpref.db.AppDatabase;
import com.example.dshinde.myapplication_xmlpref.db.MyNotesRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;

public class MyNotesApplication extends Application {
    private String userId;
    // Singleton instances
    private AppDatabase database;
    private MyNotesRepository repository;

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
        );
        // Room DB initialization will be handled after userId is set
    }

    public void initializeDbRepository(String userId) {
        this.userId = userId;
        // Initialize Room + Repository singletons
        database = AppDatabase.getInstance(this);
        this.repository = new MyNotesRepository(database.myNotesDao(), userId);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String id) {
        this.userId = id;
        initializeDbRepository(id);
    }

    // --------------------------
    // Getters for global use
    // --------------------------
    public AppDatabase getDatabase() {
        return database;
    }

    public MyNotesRepository getRepository() {
        return repository;
    }

}
