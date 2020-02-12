package com.example.dshinde.myapplication_xmlpref.helper;


import androidx.documentfile.provider.DocumentFile;

public class StorageSelectionResult {
    private final int requestCode;
    private final DocumentFile dir;
    private final String fileName;

    public StorageSelectionResult(){
        requestCode = StorageUtil.NOT_AVAILABLE;
        dir = null;
        fileName = null;
    }
    public StorageSelectionResult(int requestCode, DocumentFile dir){
        this.dir = dir;
        this.requestCode = requestCode;
        this.fileName = null;
    }

    public StorageSelectionResult(int requestCode, String fileName){
        this.dir = null;
        this.requestCode = requestCode;
        this.fileName = fileName;
    }

    public int getRequestCode(){
        return requestCode;
    }

    public String getFileName(){
        return fileName;
    }

    public DocumentFile getDir(){
        return dir;
    }
}
