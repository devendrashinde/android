package com.example.dshinde.myapplication_xmlpref;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.documentfile.provider.DocumentFile;

import com.example.dshinde.myapplication_xmlpref.adapters.ListviewKeyValueObjectAdapter;
import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.helper.Converter;
import com.example.dshinde.myapplication_xmlpref.helper.Factory;
import com.example.dshinde.myapplication_xmlpref.helper.JsonHelper;
import com.example.dshinde.myapplication_xmlpref.helper.StorageUtil;
import com.example.dshinde.myapplication_xmlpref.listners.ListviewActions;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.example.dshinde.myapplication_xmlpref.services.DataStorage;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.services.ReadOnceDataStorage;
import com.example.dshinde.myapplication_xmlpref.services.SharedPrefManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

public class MainActivity extends BaseActivity implements ListviewActions {
    EditText valueField;
    ListView listView;
    //SimpleAdapter listAdapter;
    //ListviewKeyValueMapAdapter listAdapter;
    ListviewKeyValueObjectAdapter listAdapter;
    DataStorage dataStorageManager;
    ReadOnceDataStorage readOnceDataStorage;
    String key;
    String sharedPreferenceName = Constants.DATABASE_PATH_NOTES;
    public static final int PICKFILE_RESULT_CODE = 42; // 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Bundle bundle = getIntent().getExtras();
        userId = bundle.getString("userId");
        // Check if user is signed in (non-null) and update UI accordingly.
        updateUI();
    }

    private void updateUI(){

        setContentView(R.layout.activity_main);
        valueField = (EditText) findViewById(R.id.VALUE_1);
        listView = (ListView) findViewById(R.id.list);
        populateListView();
        setValueFieldWatcher();
        setValueFieldClearButtonAction();

        dataStorageManager = Factory.getDataStorageIntsance(this,
                getDataStorageType(),
                sharedPreferenceName,
                true,
                false);
        dataStorageManager.addDataStorageListener(new DataStorageListener() {
            @Override
            public void dataChanged(String key, String value) {
                listAdapter.setData(dataStorageManager.getValues());
            }

            @Override
            public void dataLoaded(List<KeyValue> data){
                listAdapter.setData(data);
            }
        });
        dataStorageManager.loadData();
    }

    private void setValueFieldClearButtonAction() {
        valueField.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (valueField.getRight() - valueField.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        valueField.setText("");
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void setValueFieldWatcher() {
        valueField.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count < before) {
                    // We're deleting char so we need to reset the adapter data
                    listAdapter.resetData();
                }
                listAdapter.getFilter().filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    public void onFindCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        if(checked){
            valueField.setHint(R.string.searchText);
        } else{
            valueField.setHint(R.string.subject);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation, menu);
        menu.removeItem(R.id.menu_add);
        menu.removeItem(R.id.menu_clear);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_save:
                save();
                return true;
            case R.id.menu_clear:
                clear();
                return true;
            case R.id.menu_copy:
                copy();
                return true;
            case R.id.menu_edit:
                edit();
                return true;
            case R.id.menu_remove:
                remove();
                return true;
            case R.id.menu_share:
                share();
                return true;
            case R.id.menu_export:
                export();
                return true;
            case R.id.menu_backup:
                backup();
                return true;
            case R.id.menu_import:
                importFile();
                return true;
            case R.id.menu_view:
                viewFile();
                return true;
            case R.id.menu_sell:
                doSell();
                return true;
            case R.id.menu_settings:
                doSettings();
                return true;
            case R.id.menu_design_screen:
                doDesignOrCapture();
                return true;
            case R.id.menu_add_to_shadba_kosh:
                addToShadaKosh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void remove(View view) {
        remove();
    }

    public void remove() {
        if (key != null) {
            dataStorageManager.remove(key);
        }
        clear();
    }

    @Override
    public void save(View view) {
        save();
    }

    public void save() {
        String value = valueField.getText().toString();
        dataStorageManager.save(key, value);
        clear();
    }

    public void share(View view) {
        share();
    }

    public void share() {
        String fileName = valueField.getText().toString();
        if (!fileName.isEmpty()) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            String textToShare = fileName + "\n" + SharedPrefManager.getDataString(this, fileName);
            sendIntent.putExtra(Intent.EXTRA_TEXT, textToShare);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }
    }

    public void edit() {
        String fileName = valueField.getText().toString();
        if (!fileName.isEmpty()) {
            startActivityForEdit(fileName);
        }
    }

    private void startEditActivity(String fileName) {
        Intent intent = new Intent(MainActivity.this, Main2Activity.class);
        intent.putExtra("filename", fileName);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

    public void edit(View view) {
        edit();
    }

    public void clear() {
        key = null;
        setEditView("");
        valueField.requestFocus();
    }

    public void clear(View view) {
        clear();
    }

    private void populateListView() {
        listAdapter = new ListviewKeyValueObjectAdapter(Collections.emptyList(),this, R.layout.list_view_items);
        listView.setAdapter(listAdapter);
        listView.setTextFilterEnabled(true);
        setOnItemClickListenerToListView();
        setOnItemLongClickListenerToListView();
    }

    private void setOnItemClickListenerToListView() {
        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                KeyValue kv = listAdapter.getItem(position);
                key = kv.getKey();
                setEditView(kv.getValue());
            }
        };
        listView.setOnItemClickListener(listener);
    }

    private void setOnItemLongClickListenerToListView() {
        AdapterView.OnItemLongClickListener listener = new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                KeyValue kv = listAdapter.getItem(position);
                String value = kv.getValue();
                setEditView(value);
                displayTextviewActivity(value);
                return true;
            }
        };
        listView.setOnItemLongClickListener(listener);
    }

    private void displayTextviewActivity(String sharedPreferenceName) {
        if(sharedPreferenceName != null && !sharedPreferenceName.isEmpty()) {
            displayTextviewActivity(sharedPreferenceName, SharedPrefManager.getDataString(this, sharedPreferenceName));
        }
    }

    private void displayTextviewActivity(String title, String text) {
        if(text != null && !text.isEmpty()) {
            Intent intent = new Intent(MainActivity.this, ScrollingTextViewActivity.class);
            intent.putExtra("subject", title);
            intent.putExtra("text", text);
            startActivity(intent);
        }
    }

    private void setEditView(String value) {
        valueField.setText(value);
    }

    public void copy() {
        String fileName = valueField.getText().toString();
        if (!fileName.isEmpty()) {
            copy(fileName);
        }
    }

    public void doSell() {
        String fileName = valueField.getText().toString();
        if (!fileName.isEmpty()) {
            Intent intent = new Intent(MainActivity.this, SellTeaActivity.class);
            intent.putExtra("filename", fileName);
            intent.putExtra("userId", userId);
            startActivity(intent);
        }
    }

    public void doSettings() {
        Intent intent = new Intent(MainActivity.this, CafeSettingsActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

    private void doDesignOrCapture(){

        String fileName = valueField.getText().toString();
        if (!fileName.isEmpty()) {
            startDesignOrEditActivity(fileName, Constants.REQUEST_CODE_SCREEN_DESIGN,null);
        }
    }

    private void startDesignOrEditActivity(String fileName, Integer requestMode, String screeConfig) {
        Intent intent = new Intent(MainActivity.this, ScreenDesignActivity.class);
        intent.putExtra("screenName", fileName);
        intent.putExtra("requestMode", requestMode);
        intent.putExtra("userId", userId);
        if(requestMode == Constants.REQUEST_CODE_SCREEN_CAPTURE) {
            intent.putExtra("screenConfig", screeConfig);
        }
        startActivity(intent);
    }

    /*
    this method will extract words from selected note and it will add to Sanskrit ShabdKosh note
     */
    private void addToShadaKosh() {
        String collectionName = valueField.getText().toString();
        if(collectionName != null && !collectionName.isEmpty()) {
            Intent intent = new Intent(MainActivity.this, ShabdaKoshActivity.class);
            intent.putExtra("collectionToAddToShabdaKosh", collectionName);
            intent.putExtra("userId", userId);
            startActivity(intent);
        }
    }

    private void copy(String initialValue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Subject");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(initialValue);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newFileName = input.getText().toString();
                String fileName = valueField.getText().toString();
                if (newFileName == null || newFileName.equalsIgnoreCase(fileName)) {
                    return;
                }
                performCopy(fileName, newFileName);

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void performCopy(String srcFile, String destFile) {
        if (SharedPrefManager.copy(this, srcFile, destFile)) {
            dataStorageManager.save(null, destFile);
        }
    }

    public void export() {
        startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), StorageUtil.PICK_DOCUMENT_FOLDER_FOR_EXPORT);
    }

    private void export(DocumentFile dir) {
        String path = StorageUtil.saveAsTextToDocumentFile(this, dir, sharedPreferenceName, dataStorageManager.getDataString());
        if (path != null) {
            Toast.makeText(this, "Saved to " + path,
                    Toast.LENGTH_LONG).show();
        }
    }

    public void backup() {
        startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), StorageUtil.PICK_DOCUMENT_FOLDER_FOR_BACKUP);
    }

    private void backup(DocumentFile dir) {
        List<KeyValue> subjects = dataStorageManager.getValues();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String path = StorageUtil.saveAsObjectToDocumentFile(this, dir, sharedPreferenceName, gson.toJson(subjects));

        if (path != null) {
            Toast.makeText(this, "Saved to " + path,
                    Toast.LENGTH_SHORT).show();

            for (KeyValue entry : subjects) {
                String fileName = entry.getValue().trim();

                path = StorageUtil.saveAsObjectToDocumentFile(this, dir, fileName,
                        dataStorageManager.getDataString(fileName));
                if (path != null) {
                    Toast.makeText(this, "Saved to " + path,
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void restore() {

    }

    private String getFileNameWithOutExtension(String filename) {
        return filename.replaceFirst("[.][^.]+$", "");
    }

    public void viewFile() {
        selectFile(StorageUtil.PICK_FILE_FOR_VIEW, false);
    }

    private void selectFile(int actionCode, boolean selectMultiple) {
        Intent selectFile = new Intent(Intent.ACTION_GET_CONTENT);
        selectFile.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        selectFile.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, selectMultiple);
        selectFile.setType("*/*");
        selectFile = Intent.createChooser(selectFile, "Select File");
        startActivityForResult(selectFile, actionCode);
    }

    public void importFile() {
        selectFile(StorageUtil.PICK_FILE_FOR_IMPORT,false);
    }

    private void importFile(String sharedPreferenceName, JSONObject data) {
        try {
            if(SharedPrefManager.loadData(this, sharedPreferenceName, JsonHelper.toMap(data), true)){
                valueField.setText(sharedPreferenceName);
                save();
            }
        } catch (JSONException ex) {
            Toast.makeText(this, "Failed to import file, error\n" + ex.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
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
                    export(dir);
                    break;
                case StorageUtil.PICK_DOCUMENT_FOLDER_FOR_BACKUP:
                    dir = StorageUtil.getDocumentDir(this, fileUri);
                    backup(dir);
                    break;
                case StorageUtil.PICK_FILE_FOR_IMPORT:
                case StorageUtil.PICK_FILE_FOR_VIEW:
                    String fileName = StorageUtil.getFileName(this, fileUri);
                    if(requestCode == StorageUtil.PICK_FILE_FOR_IMPORT) {
                        if (fileName.substring(fileName.lastIndexOf(".")).equalsIgnoreCase(".json")) {
                            JSONObject fileData = StorageUtil.getObjectFromDocumentFile(this, fileUri);
                            Toast.makeText(this, "Importing from file " + fileName,
                                    Toast.LENGTH_LONG).show();
                            importFile(getFileNameWithOutExtension(fileName), fileData);
                        } else {
                            Toast.makeText(this, "Only JSON files are supported",
                                    Toast.LENGTH_LONG).show();
                        }
                    } else{
                        String text = null;
                        if(fileName.substring(fileName.lastIndexOf(".")).equalsIgnoreCase(".json")){
                            JSONObject fileData = StorageUtil.getObjectFromDocumentFile(this, fileUri);
                            text = fileData.toString();
                        } else {
                            text = StorageUtil.getTextFromDocumentFile(this, fileUri);
                        }
                        displayTextviewActivity(fileName, text);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        dataStorageManager.removeDataStorageListeners();
    }
    /*
    if screen config is found then start ScreenDesignActivity
    otherwise start normal edit activity
     */
    private void startActivityForEdit(String collection){
        readOnceDataStorage = Factory.getReadOnceDataStorageIntsance(this,
            getDataStorageType(), Constants.SCREEN_DESIGN + collection,
            new DataStorageListener() {
                @Override
                public void dataChanged(String key, String value) {
                }

                @Override
                public void dataLoaded(List<KeyValue> data) {
                    if(data.size() >0) {
                        String screenConfig = Converter.getValuesJsonString(data);
                        startDesignOrEditActivity(collection, Constants.REQUEST_CODE_SCREEN_CAPTURE, screenConfig);
                    } else {
                        startEditActivity(collection);
                    }
                }
            });
    }
}
