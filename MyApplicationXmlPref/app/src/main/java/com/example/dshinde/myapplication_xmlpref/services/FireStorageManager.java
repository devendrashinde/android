package com.example.dshinde.myapplication_xmlpref.services;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.dshinde.myapplication_xmlpref.MyNotesApplication;
import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.common.FileType;
import com.example.dshinde.myapplication_xmlpref.helper.StorageUtil;
import com.example.dshinde.myapplication_xmlpref.listners.FireStorageListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.Objects;

public class FireStorageManager implements FileStorage {
    Context context;
    StorageReference storageReference;
    FireStorageListener fireStorageListener = null;
    String collectionName = null;
    String storagePath;
    final static long SIZE = 1024 * 4;

    public FireStorageManager(Context context, String collectionName) {
        this.context = context;
        this.collectionName = collectionName;
        setStoragePath();
        storageReference = FirebaseStorage.getInstance().getReference();
    }
    public FireStorageManager(Context context, String collectionName, FireStorageListener fireStorageListener) {
        this.context = context;
        this.collectionName = collectionName;
        setStoragePath();
        this.fireStorageListener = fireStorageListener;
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    private void setStoragePath(){
        MyNotesApplication app = (MyNotesApplication) context.getApplicationContext();
        storagePath = Constants.STORAGE_PATH_NOTES + "/" + app.getUserId();
    }

    private void getDownloadUri(FireStorageListener fireStorageListener, StorageReference storageFileRef) {
        //adding the file to reference
        storageFileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            if(fireStorageListener != null) {
                fireStorageListener.downloadUriReceived(uri);
            }
        }).addOnFailureListener(exception -> Toast.makeText(context, context.getResources().getString(R.string.download_failed) + "\n" + exception.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private StorageReference getStorageReference(String mediaName){
        return storageReference.child(storagePath + "/" + mediaName);
    }

    public void uploadMedia(Uri fileUri, FileType fileType) {
        uploadMedia(fileUri, fileType, fireStorageListener);
    }

    @Override
    public void uploadMedia(Uri fileUri, FileType fileType, FireStorageListener fireStorageListener) {
        String mediaName = StorageUtil.getFileName(context, fileUri);
        StorageReference storageFileRef = getStorageReference(mediaName);
        storageFileRef.getMetadata()
            .addOnSuccessListener(storageMetadata -> {
                // File exists, copy to external storage of application
                StorageUtil.copyFileToExternalStorage(context, fileUri, fileType, collectionName);
            })
            .addOnFailureListener(exception -> {
                if (exception instanceof StorageException &&
                        ((StorageException) exception).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                    // File does not exist, safe to upload
                    uploadFile(fileUri, fireStorageListener, storageFileRef);
                    // Copy to external storage of application
                    StorageUtil.copyFileToExternalStorage(context, fileUri, fileType, collectionName);
                } else {
                    // Some other error occurred
                    Toast.makeText(context, "Error checking file existence" +"\n" + exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

    }

    private void uploadFile(Uri fileUri, FireStorageListener fireStorageListener, StorageReference storageFileRef) {
        storageFileRef
            .putFile(fileUri).continueWithTask(task -> {
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

    public void downloadDocumentFileAsBytes(String mediaName){
        downloadFileAsBytes(mediaName, FileType.DOCUMENT, fireStorageListener);
    }
    public void downloadFileAsBytes(String mediaName, FileType fileType, FireStorageListener fireStorageListener){
        File file = StorageUtil.getExternalStorageFile(context, mediaName, fileType, collectionName);
        assert file != null;
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
        downloadImageFile(mediaName, fireStorageListener);
    }

    @Override
    public void downloadImageFile(String mediaName, FireStorageListener fireStorageListener){
        downloadFile(mediaName, FileType.PICTURE, fireStorageListener);
    }

    @Override
    public void downloadDocumentFile(String mediaName, FireStorageListener fireStorageListener){
        downloadFile(mediaName, FileType.DOCUMENT, fireStorageListener);
    }

    @Override
    public void downloadAudioFile(String mediaName){
        downloadAudioFile(mediaName, fireStorageListener);
    }

    @Override
    public void downloadAudioFile(String mediaName, FireStorageListener fireStorageListener){
        downloadFile(mediaName, FileType.MUSIC, fireStorageListener);
    }

    @Override
    public void downloadFile(String mediaName, FileType fileType, FireStorageListener fireStorageListener){
        File file = StorageUtil.getExternalStorageFile(context, mediaName, fileType, collectionName);
        assert file != null;
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
