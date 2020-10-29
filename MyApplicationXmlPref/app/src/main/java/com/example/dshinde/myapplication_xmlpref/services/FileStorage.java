package com.example.dshinde.myapplication_xmlpref.services;

import android.net.Uri;

import com.example.dshinde.myapplication_xmlpref.listners.FireStorageListener;

public interface FileStorage {
    void getDownloadUrl(String mediaId, String mediaName);
    void getDownloadUrl(String mediaId, String mediaName, FireStorageListener fireStorageListener);
    void uploadMedia(String mediaId, Uri filePath);
    void uploadMedia(String mediaId, Uri filePath, FireStorageListener fireStorageListener);
    void downloadFileAsBytes(String mediaId, String mediaName);
    void downloadFileAsBytes(String mediaId, String mediaName, FireStorageListener fireStorageListener);
}
