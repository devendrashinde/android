package com.example.dshinde.myapplication_xmlpref.services;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.helper.StorageUtil;
import com.example.dshinde.myapplication_xmlpref.listners.FireStorageListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.Objects;

public class FireStorageManager implements FileStorage {
    Context context;
    StorageReference storageReference;
    FireStorageListener fireStorageListener = null;
    String collectionName;
    final static long SIZE = 1024 * 4;

    public FireStorageManager(Context context, String collectionName) {
        this.context = context;
        this.collectionName = collectionName;
        storageReference = FirebaseStorage.getInstance().getReference();
    }
    public FireStorageManager(Context context, String collectionName, FireStorageListener fireStorageListener) {
        this.context = context;
        this.collectionName = collectionName;
        this.fireStorageListener = fireStorageListener;
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public void getDownloadUrl(String mediaName) {
        getDownloadUrl(mediaName, fireStorageListener);
    }

    @Override
    public void getDownloadUrl(String mediaName, FireStorageListener fireStorageListener) {
        StorageReference storageFileRef = getStorageReference(mediaName);
        //adding the file to reference
        storageFileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            if(fireStorageListener != null) {
                fireStorageListener.downloadUriReceived(uri);
            }
        }).addOnFailureListener(exception -> Toast.makeText(context, context.getResources().getString(R.string.download_failed) + "\n" + exception.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private StorageReference getStorageReference(String mediaName){
        return storageReference.child(collectionName + "/" + mediaName);
    }

    public void uploadMedia(Uri filePath) {
        uploadMedia(filePath, fireStorageListener);
    }

    @Override
    public void uploadMedia(Uri filePath, FireStorageListener fireStorageListener) {
        StorageReference storageFileRef = getStorageReference(StorageUtil.getFileName(context, filePath));
        //adding the file to reference
        storageFileRef
                .putFile(filePath).continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return storageFileRef.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if(fireStorageListener != null) {
                            Uri uri = task.getResult();
                            fireStorageListener.uploadedUriReceived(uri);
                        }
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.upload_failed) + "\n" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void downloadFileAsBytes(String mediaName){
        downloadFileAsBytes(mediaName, fireStorageListener);
    }
    public void downloadFileAsBytes(String mediaName, FireStorageListener fireStorageListener){
        File file = StorageUtil.getFile(context, mediaName);
        if( file.exists()) {
            fireStorageListener.downloadFileBytesReceived(StorageUtil.readBytesFromFile(file));
        } else {
            StorageReference storageFileRef = getStorageReference(mediaName);
            storageFileRef.getBytes(SIZE).addOnSuccessListener(bytes -> {
                StorageUtil.writeBytesToFile(bytes, file);
                if(fireStorageListener != null) {
                    fireStorageListener.downloadFileBytesReceived(bytes);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(context, context.getResources().getString(R.string.download_failed) + "\n" + exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void downloadImageFile(String mediaName){
        downloadFile(mediaName, fireStorageListener);
    }

    @Override
    public void downloadImageFile(String mediaName, FireStorageListener fireStorageListener){
        downloadFile(mediaName, fireStorageListener);
    }

    @Override
    public void downloadDocumentFile(String mediaName){
        downloadFile(mediaName, fireStorageListener);
    }

    @Override
    public void downloadDocumentFile(String mediaName, FireStorageListener fireStorageListener){
        downloadFile(mediaName, fireStorageListener);
    }
    @Override
    public void downloadFile(String mediaName){
        downloadFile(mediaName, fireStorageListener);
    }
    @Override
    public void downloadFile(String mediaName, FireStorageListener fireStorageListener){
        File file = StorageUtil.getFile(context, mediaName);
        if( file.exists()) {
            if(fireStorageListener != null) {
                fireStorageListener.downloadUriReceived(Uri.fromFile(file));
            }
        } else {
            downloadFile(mediaName, file, fireStorageListener);
        }
    }
    private void downloadFile(String mediaName, File file, FireStorageListener fireStorageListener){
        StorageReference storageFileRef = getStorageReference(mediaName);

        storageFileRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                if(fireStorageListener != null) {
                    fireStorageListener.downloadUriReceived(Uri.fromFile(file));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(context, context.getResources().getString(R.string.download_failed) +"\n" + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
