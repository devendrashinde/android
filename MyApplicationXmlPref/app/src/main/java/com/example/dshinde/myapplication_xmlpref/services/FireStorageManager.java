package com.example.dshinde.myapplication_xmlpref.services;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.helper.StorageUtil;
import com.example.dshinde.myapplication_xmlpref.listners.FireStorageListener;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Objects;

public class FireStorageManager implements FileStorage {
    Context context;
    StorageReference storageReference;
    FireStorageListener fireStorageListener;
    String collectionName;
    final static long ONE_MEGABYTE = 1024 * 1024;

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
    public void getDownloadUrl(String mediaId, String mediaName) {
        getDownloadUrl(mediaId, mediaName, fireStorageListener);
    }

    @Override
    public void getDownloadUrl(String mediaId, String mediaName, FireStorageListener fireStorageListener) {
        StorageReference storageFileRef = getStorageReference(mediaId, mediaName);
        //adding the file to reference
        storageFileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if(fireStorageListener != null) {
                    fireStorageListener.downloadUriReceived(uri);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(context, context.getResources().getString(R.string.download_failed) + "\n" + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private StorageReference getStorageReference(String mediaId, String mediaName){
        return storageReference.child(collectionName + "/" + mediaId + "/" + mediaName);
    }

    public void uploadMedia(String mediaId, Uri filePath) {
        uploadMedia(mediaId, filePath, fireStorageListener);
    }

    @Override
    public void uploadMedia(String mediaId, Uri filePath, FireStorageListener fireStorageListener) {
        StorageReference storageFileRef = getStorageReference(mediaId, StorageUtil.getFileName(context, filePath));
        //adding the file to reference
        storageFileRef
                .putFile(filePath).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return storageFileRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    if(fireStorageListener != null) {
                        Uri uri = task.getResult();
                        fireStorageListener.uploadedUriReceived(uri);
                    }
                } else {
                    Toast.makeText(context, context.getResources().getString(R.string.upload_failed) + "\n" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void downloadFileAsBytes(String mediaId, String mediaName){
        downloadFileAsBytes(mediaId, mediaName, fireStorageListener);
    }
    public void downloadFileAsBytes(String mediaId, String mediaName, FireStorageListener fireStorageListener){
        File file = StorageUtil.createDocumentFile(context, mediaId + "_" + mediaName);
        if( file.exists()) {
            fireStorageListener.downloadFileBytesReceived(StorageUtil.readBytesFromFile(file));
        } else {
            StorageReference storageFileRef = getStorageReference(mediaId, mediaName);
            storageFileRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    StorageUtil.writeBytesToFile(bytes, file);
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
    public void downloadImageFile(String mediaId, String mediaName, FireStorageListener fireStorageListener){
        File file = StorageUtil.createImageFile(context, mediaId + "_" + mediaName);
        if( file.exists()) {
            fireStorageListener.downloadUriReceived(Uri.fromFile(file));
        } else {
            downloadFile(mediaId, mediaName, file, fireStorageListener);
        }
    }

    @Override
    public void downloadDocumentFile(String mediaId, String mediaName, FireStorageListener fireStorageListener){
        File file = StorageUtil.createImageFile(context, mediaId + "_" + mediaName);
        if( file.exists()) {
            fireStorageListener.downloadUriReceived(Uri.fromFile(file));
        } else {
            downloadFile(mediaId, mediaName, file, fireStorageListener);
        }
    }

    private void downloadFile(String mediaId, String mediaName, File file, FireStorageListener fireStorageListener){
        StorageReference storageFileRef = getStorageReference(mediaId, mediaName);

        storageFileRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                fireStorageListener.downloadUriReceived(Uri.fromFile(file));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(context, context.getResources().getString(R.string.download_failed) +"\n" + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
