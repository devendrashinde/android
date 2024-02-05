package com.example.dshinde.myapplication_xmlpref.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.common.DataStorageType;
import com.example.dshinde.myapplication_xmlpref.helper.DynamicControls;
import com.example.dshinde.myapplication_xmlpref.helper.ReadAloud;
import com.example.dshinde.myapplication_xmlpref.helper.StorageUtil;

import java.util.ArrayList;
import java.util.Collections;

public class BaseActivity extends AppCompatActivity {

    @VisibleForTesting
    public ProgressDialog mProgressDialog;
    protected String userId = null;
    protected ReadAloud readAloud;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void enableTextToSpeech() {
        readAloud = new ReadAloud(this);
    }

    private void selectTtsLanguage(String key, String value) {
        if(readAloud != null) {
            selectOption(Constants.SELECT_LANGUAGE, R.string.select_langguage,
                    R.array.reading_languages,
                    null, key, value);
        }
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public void showKeyboard(View view) {
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            view.requestFocus();
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.SHOW_IMPLICIT);
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
        Intent intent = null;
        intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, selectMultiple);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent = Intent.createChooser(intent, getString(R.string.select_file));
        startActivityForResult(intent, actionCode);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                    String fileName = StorageUtil.getFileName(this, fileUri);
                    if (fileName.substring(fileName.lastIndexOf(".")).equalsIgnoreCase(".json")) {
                        String text = StorageUtil.getTextFromDocumentFile(this, fileUri);
                        doImport(StorageUtil.getFileNameWithOutExtension(fileName), text);
                    } else {
                        Toast.makeText(this, R.string.only_json_files_are_supported,
                                Toast.LENGTH_LONG).show();
                    }
                    break;
                case StorageUtil.PICK_FILE_FOR_VIEW:
                    String fileToView = StorageUtil.getFileName(this, fileUri);
                    String text = StorageUtil.getTextFromDocumentFile(this, fileUri);
                    if (text != null) {
                        doView(fileToView, text);
                    } else {
                        Toast.makeText(this, R.string.unable_to_read_data_from_file + " : " + fileToView,
                                Toast.LENGTH_LONG).show();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public void setTheme(LinearLayout linearLayout, String themeName) {
        int backGroundColor = getResources().getColor(R.color.colorBlack);
        int textColor = getResources().getColor(R.color.colorWhite);
        if(themeName.equals(Constants.DAY_MODE)) {
            int tempColor = textColor;
            textColor = backGroundColor;
            backGroundColor = tempColor;
        }
        linearLayout.setBackgroundColor(backGroundColor);
        getWindow().getDecorView().setBackgroundColor(backGroundColor);
        // then you can iterate for each child view inside the layout
        int count = linearLayout.getChildCount();

        for (int i = 0; i < count; i++) {
            // get the child View
            View view = linearLayout.getChildAt(i);
            view.setBackgroundColor(backGroundColor);
            // then check if it an instance of a Button
            if (view instanceof Button) {
                ((Button) view).setTextColor(textColor);
            } else if( view instanceof TextView) {
                ((TextView) view).setTextColor(textColor);
            } else if( view instanceof EditText) {
                EditText et = ((EditText) view);
                et.setTextColor(textColor);
            } else if( view instanceof RadioButton) {
                ((RadioButton) view).setTextColor(textColor);
            } else if( view instanceof RadioGroup) {
                RadioGroup rg = ((RadioGroup) view);
                for(int k = 0; k < rg.getChildCount(); k++) {
                    RadioButton rb = (RadioButton) rg.getChildAt(k);
                    rb.setTextColor(textColor);
                    rb.setBackgroundColor(backGroundColor);
                }
            } else if( view instanceof CheckBox) {
                ((CheckBox) view).setTextColor(textColor);
            }
        }
    }

    public void selectOption(String id, int title, int optionsArray, Integer defaultOption, String key, String value) {
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(title))
                .create();
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_dynamic_linear_layout, null);
        LinearLayout linearLayout = view.findViewById(R.id.linear_layout);

        String[] optionText = getResources().getStringArray(optionsArray);
        RadioGroup rg = DynamicControls.getRadioGroupControl(this,
                optionText,
                defaultOption != null ? Collections.singletonList(getResources().getString(defaultOption)) : new ArrayList<>());
        rg.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedButton = alertDialog.findViewById(checkedId);
            if (selectedButton != null) {
                alertDialog.dismiss();
                processSelectedOption(id, selectedButton.getText().toString(), key, value);
            }
        });
        linearLayout.addView(rg);
        alertDialog.setView(view);
        alertDialog.show();
    }

    protected void processSelectedOption(@NonNull String id, @NonNull String selectedOption, String key, String value) {
        switch (selectedOption) {
            case Constants.READ_ALOUD:
                selectTtsLanguage(key, value);
                break;
            case Constants.MARATHI:
            case Constants.HINDI:
            case Constants.ENGLISH:
                readNoteText(selectedOption, key, value);
                break;
        }
    }

    public void readNoteText(@NonNull String language, String key, String value) {
        readAloud.readNoteText(language, key, value);
    }

    protected void setEditTextClearButtonAction(EditText editText) {
        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[Constants.DRAWABLE_RIGHT].getBounds().width())) {
                        editText.setText("");
                        showKeyboard(editText);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public void onPause(){
        if( readAloud != null ) {
            readAloud.clearTTS();
        }
        super.onPause();
    }

    public void onDestroy() {
        if( readAloud != null ) {
            readAloud.clearTTS();
        }
        super.onDestroy();
    }
}
