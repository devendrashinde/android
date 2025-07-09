package com.example.dshinde.myapplication_xmlpref.services;

import android.net.Uri;

import com.example.dshinde.myapplication_xmlpref.common.FileType;
import com.example.dshinde.myapplication_xmlpref.listners.FireStorageListener;

public interface FileStorage {
    void getDownloadUrl(String mediaName);
    void getDownloadUrl(String mediaName, FireStorageListener fireStorageListener);
    void uploadMedia(Uri filePath);
    void uploadMedia(Uri filePath, FireStorageListener fireStorageListener);
    void downloadDocumentFileAsBytes(String mediaName);
    void downloadFileAsBytes(String mediaName, FileType fileType, FireStorageListener fireStorageListener);

    void downloadImageFile(String mediaName);

    void downloadImageFile(String mediaName, FireStorageListener fireStorageListener);

    void downloadDocumentFile(String mediaName);

    void downloadAudioFile(String mediaName, FireStorageListener fireStorageListener);

    void downloadAudioFile(String mediaName);

    void downloadDocumentFile(String mediaName, FireStorageListener fireStorageListener);

    void downloadFile(String mediaName, FileType fileType, FireStorageListener fireStorageListener);
}
