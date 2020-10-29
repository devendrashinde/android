package com.example.dshinde.myapplication_xmlpref.listners;

import android.net.Uri;
import android.view.View;

import com.example.dshinde.myapplication_xmlpref.model.KeyValue;

import java.util.List;

public interface FireStorageListener {
    void downloadUriReceived(Uri fileUri);
    void downloadFileBytesReceived(byte[] bytes);
    void uploadedUriReceived(Uri fileUri);
}
