package com.example.dshinde.myapplication_xmlpref.listners;

import android.net.Uri;

public interface FireStorageListener {
    void downloadUriReceived(Uri fileUri);
    void downloadFileBytesReceived(byte[] bytes);
    void uploadedUriReceived(Uri fileUri);
}
