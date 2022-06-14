package com.example.dshinde.myapplication_xmlpref.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.common.DataStorageType;
import com.example.dshinde.myapplication_xmlpref.helper.StorageUtil;

public class BaseActivity extends AppCompatActivity {

    @VisibleForTesting
    public ProgressDialog mProgressDialog;
    protected String userId = null;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public void hideKeyboard(View view) {
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }

    public DataStorageType getDataStorageType() {
        return (userId == null ? DataStorageType.SHARED_PREFERENCES : DataStorageType.FIREBASE_DB);
    }

    public void showInLongToast(String txt) {
        Toast.makeText(getApplicationContext(), txt,
                Toast.LENGTH_LONG).show();
    }

    public void showInShortToast(String txt) {
        Toast.makeText(getApplicationContext(), txt,
                Toast.LENGTH_SHORT).show();
    }

    protected void selectFile(int actionCode, boolean selectMultiple) {
        Intent selectFile = new Intent(Intent.ACTION_GET_CONTENT);
        selectFile.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        selectFile.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, selectMultiple);
        selectFile.setType("*/*");
        selectFile = Intent.createChooser(selectFile, getString(R.string.select_file));
        startActivityForResult(selectFile, actionCode);
    }

    protected void shareText(String text) {
        share(text, Constants.TEXT_PLAIN);
    }

    protected void shareHtml(String text) {
        share(text, Constants.TEXT_HTML);
    }

    protected void share(String text, String type) {
        if (!text.isEmpty()) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            String textToShare = this.getTitle() + Constants.CR_LF + text;
            sendIntent.putExtra(Intent.EXTRA_TEXT, textToShare);
            sendIntent.setType(type);
            startActivity(sendIntent);
        }
    }

    protected void doExport(DocumentFile targetFolder){

    }

    protected void doBackup(DocumentFile targetFolder){

    }

    protected void doImport(String collectionName, String data){

    }

    protected void doView(String collectionName, String data){

    }

    public void export() {
        startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), StorageUtil.PICK_DOCUMENT_FOLDER_FOR_EXPORT);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri fileUri = data.getData();
            DocumentFile dir;
            switch (requestCode) {
                case StorageUtil.PICK_DOCUMENT_FOLDER_FOR_EXPORT:
                    dir = StorageUtil.getDocumentDir(this, fileUri);
                    doExport(dir);
                    break;
                case StorageUtil.PICK_DOCUMENT_FOLDER_FOR_BACKUP:
                    dir = StorageUtil.getDocumentDir(this, fileUri);
                    doBackup(dir);
                    break;
                case StorageUtil.PICK_FILE_FOR_IMPORT:
                case StorageUtil.PICK_FILE_FOR_VIEW:
                    String fileName = StorageUtil.getFileName(this, fileUri);
                    if (requestCode == StorageUtil.PICK_FILE_FOR_IMPORT) {
                        if (fileName.substring(fileName.lastIndexOf(".")).equalsIgnoreCase(".json")) {
                            String fileData = StorageUtil.getTextFromDocumentFile(this, fileUri);
                            doImport(StorageUtil.getFileNameWithOutExtension(fileName), fileData);
                        } else {
                            Toast.makeText(this, "Only JSON files are supported",
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        String text = StorageUtil.getTextFromDocumentFile(this, fileUri);
                        if (text != null) {
                            doView(fileName, text);
                        } else {
                            Toast.makeText(this, "Unable to read data from file : " + fileName,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

}
