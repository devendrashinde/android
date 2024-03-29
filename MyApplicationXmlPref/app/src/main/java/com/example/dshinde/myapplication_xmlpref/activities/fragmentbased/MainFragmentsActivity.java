package com.example.dshinde.myapplication_xmlpref.activities.fragmentbased;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.activities.BaseActivity;
import com.example.dshinde.myapplication_xmlpref.activities.ScrollingTextViewActivity;
import com.example.dshinde.myapplication_xmlpref.activities.listviewbased.Main2Activity;
import com.example.dshinde.myapplication_xmlpref.activities.listviewbased.ScreenDesignActivity;
import com.example.dshinde.myapplication_xmlpref.activities.listviewbased.ShabdaKoshActivity;
import com.example.dshinde.myapplication_xmlpref.adapters.MarginItemDecoration;
import com.example.dshinde.myapplication_xmlpref.adapters.RecyclerViewKeyValueAdapter;
import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.helper.Converter;
import com.example.dshinde.myapplication_xmlpref.helper.Factory;
import com.example.dshinde.myapplication_xmlpref.helper.JsonHelper;
import com.example.dshinde.myapplication_xmlpref.helper.StorageUtil;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.listners.RecyclerViewKeyValueItemListener;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.example.dshinde.myapplication_xmlpref.services.DataStorage;
import com.example.dshinde.myapplication_xmlpref.services.ReadWriteOnceDataStorage;
import com.example.dshinde.myapplication_xmlpref.services.SharedPrefManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

import static com.example.dshinde.myapplication_xmlpref.common.Constants.DRAWABLE_RIGHT;

public class MainFragmentsActivity extends BaseActivity {
    EditText valueField;
    RecyclerView listView;
    RecyclerViewKeyValueAdapter listAdapter;
    DataStorage dataStorageManager;
    DocumentFile selectedDir;
    ReadWriteOnceDataStorage readWriteOnceDataStorage;
    String key;
    String sharedPreferenceName = Constants.DATABASE_PATH_NOTES;
    private static final String CLASS_TAG = "MainActivityRV";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(CLASS_TAG, "onCreate");
        Bundle bundle = getIntent().getExtras();
        userId = bundle.getString(Constants.USERID);
        // Check if user is signed in (non-null) and update UI accordingly.
        loadUI();
        initDataStorageAndLoadData(this);
    }

    private void loadUI() {
        Log.d(CLASS_TAG, "loadUI");
        setContentView(R.layout.activity_main_recycler_view);
        valueField = (EditText) findViewById(R.id.VALUE_1);
        listView = (RecyclerView) findViewById(R.id.list);
        populateListView();
        setValueFieldWatcher();
        setValueFieldClearButtonAction();
    }

    private void initDataStorageAndLoadData(Context context) {
        Log.d(CLASS_TAG, "initDataStorageAndLoadData->getDataStorageIntsance");
        dataStorageManager = Factory.getDataStorageInstance(context,
                getDataStorageType(),
                sharedPreferenceName,
                true,
                false, new DataStorageListener() {
                    @Override
                    public void dataChanged(String key, String value) {
                        Log.d(CLASS_TAG, "dataChanged key: " + key + ", value: " + value);
                        loadDataInListView(dataStorageManager.getValues());
                    }

                    @Override
                    public void dataLoaded(List<KeyValue> data) {
                        Log.d(CLASS_TAG, "dataLoaded");
                        loadDataInListView(data);
                    }
                });
        Log.d(CLASS_TAG, "initDataStorageAndLoadData->loadData");
        dataStorageManager.loadData();
    }

    private void loadDataInListView(List<KeyValue> data) {
        Log.d(CLASS_TAG, "loadDataInListView");
        runOnUiThread(() -> listAdapter.setData(data));
    }

    private void setValueFieldClearButtonAction() {
        valueField.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (valueField.getRight() - valueField.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {

                        clear();
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
                // as user is typing test, need to clear earlier key if any and user need to select the record from list
                key = null;
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
            case R.id.menu_daylight:
                return true;
            case R.id.menu_test:
                doSettings();
                return true;
            case R.id.menu_design_screen:
                doDesignOrCapture();
                return true;
            case R.id.menu_add_to_dictionary:
                addToShadaKosh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void remove() {
        Log.d(CLASS_TAG, "remove");
        if (key != null) {
            dataStorageManager.remove(key);
        }
        clear();
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
            startActivityForAction(fileName, "EDIT");
        }
    }

    private void startEditActivity(String fileName) {
        Intent intent = new Intent(MainFragmentsActivity.this, Main2Activity.class);
        intent.putExtra(Constants.PARAM_FILENAME, fileName);
        intent.putExtra(Constants.USERID, userId);
        startActivity(intent);
    }

    public void clear() {
        key = null;
        setEditView("");
        valueField.requestFocus();
    }

    private void populateListView() {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        listAdapter = new RecyclerViewKeyValueAdapter(Collections.emptyList(), this, R.layout.list_view_items_recyclerview, getOnItemClickListenerToListView());
        listView.setLayoutManager(mLayoutManager);
        listView.addItemDecoration(new MarginItemDecoration(8));
        listView.setAdapter(listAdapter);

        listAdapter.registerAdapterDataObserver( new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.scrollToPosition(dataStorageManager.getLastModifiedIndex());
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                listView.scrollToPosition(dataStorageManager.getLastModifiedIndex());
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                listView.scrollToPosition(dataStorageManager.getLastModifiedIndex());
            }
        });
    }

    private RecyclerViewKeyValueItemListener getOnItemClickListenerToListView() {
        RecyclerViewKeyValueItemListener listener = new RecyclerViewKeyValueItemListener() {
            public void onItemClick(KeyValue kv) {
                key = kv.getKey();
                setEditView(kv.getValue());
            }

            @Override
            public boolean onItemLongClick(KeyValue kv) {
                String value = kv.getValue();
                setEditView(value);
                viewNote(value);
                return true;
            }
        };
        return listener;
    }

    private void viewNote(String fileName) {
        if (fileName != null && !fileName.isEmpty()) {
            startActivityForAction(fileName, "VIEW");
        }
    }

    private void startViewNoteActivity(String title, String text) {
        if (text != null && !text.isEmpty()) {
            Intent intent = new Intent(MainFragmentsActivity.this, ScrollingTextViewActivity.class);
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

    public void doSettings() {
    }

    private void doDesignOrCapture() {

        String fileName = valueField.getText().toString();
        if (!fileName.isEmpty()) {
            startDesignOrEditActivity(fileName, Constants.REQUEST_CODE_SCREEN_DESIGN, null);
        }
    }

    private void startDesignOrEditActivity(String fileName, Integer requestMode, String screeConfig) {
        Intent intent = new Intent(MainFragmentsActivity.this, ScreenDesignActivity.class);
        intent.putExtra("screenName", fileName);
        intent.putExtra("requestMode", requestMode);
        intent.putExtra(Constants.USERID, userId);
        if (requestMode == Constants.REQUEST_CODE_SCREEN_CAPTURE) {
            intent.putExtra("screenConfig", screeConfig);
        }
        startActivity(intent);
    }

    /*
    this method will extract words from selected note and it will add to Sanskrit ShabdKosh note
     */
    private void addToShadaKosh() {
        String collectionName = valueField.getText().toString();
        if (collectionName != null && !collectionName.isEmpty()) {
            Intent intent = new Intent(MainFragmentsActivity.this, ShabdaKoshActivity.class);
            intent.putExtra("collectionToAddToShabdaKosh", collectionName);
            intent.putExtra(Constants.USERID, userId);
            startActivity(intent);
        }
    }

    private void copy(String initialValue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.new_subject);

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(initialValue);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
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
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void performCopy(String srcFile, String destFile) {
        // TODO implement copy
        if (SharedPrefManager.copy(this, srcFile, destFile)) {
            dataStorageManager.save(null, destFile);
        }
    }

    public void export() {
        startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), StorageUtil.PICK_DOCUMENT_FOLDER_FOR_EXPORT);
    }

    private void export(DocumentFile dir) {
        // TODO getDataString in dataStorageManager
        selectedDir = dir;
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
        selectedDir = dir;
        List<KeyValue> subjects = dataStorageManager.getValues();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String path = StorageUtil.saveAsObjectToDocumentFile(this, dir, sharedPreferenceName, gson.toJson(subjects));

        if (path != null) {
            Toast.makeText(this, "Saved to " + path,
                    Toast.LENGTH_SHORT).show();

            for (KeyValue entry : subjects) {
                String fileName = entry.getValue().trim();
                startActivityForAction(fileName, "BACKUP");
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

    public void importFile() {
        selectFile(StorageUtil.PICK_FILE_FOR_IMPORT, false);
    }

    private void importFile(String sharedPreferenceName, JSONObject data) {
        try {
            if (SharedPrefManager.loadData(this, sharedPreferenceName, JsonHelper.toMap(data), true)) {
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
                    if (requestCode == StorageUtil.PICK_FILE_FOR_IMPORT) {
                        if (fileName.substring(fileName.lastIndexOf(".")).equalsIgnoreCase(".json")) {
                            JSONObject fileData = StorageUtil.getObjectFromDocumentFile(this, fileUri);
                            Toast.makeText(this, getResources().getString(R.string.importing_from_file) + " " + fileName,
                                    Toast.LENGTH_LONG).show();
                            importFile(getFileNameWithOutExtension(fileName), fileData);
                        } else {
                            Toast.makeText(this, getResources().getString(R.string.only_json_files_are_supported),
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        String text = StorageUtil.getTextFromDocumentFile(this, fileUri);
                        if (text != null) {
                            startViewNoteActivity(fileName, text);
                        } else {
                            Toast.makeText(this, getResources().getString(R.string.unable_to_read_data_from_file) + " " + fileName,
                                    Toast.LENGTH_LONG).show();
                        }
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

    private void startActivityForAction(String collection, String action) {
        readWriteOnceDataStorage = Factory.getReadOnceFireDataStorageInstance(
            (action.equals("EDIT") ? Constants.SCREEN_DESIGN_NOTE_PREFIX : "") + collection,
            new DataStorageListener() {
                @Override
                public void dataChanged(String key, String value) {
                }

                @Override
                public void dataLoaded(List<KeyValue> data) {
                    switch (action) {
                        case "EDIT":
                            /*
                            if screen config is found then start ScreenDesignActivity
                            otherwise start normal edit activity
                             */
                            if (data.size() > 0) {
                                String screenConfig = Converter.getValuesJsonString(data);
                                startDesignOrEditActivity(collection, Constants.REQUEST_CODE_SCREEN_CAPTURE, screenConfig);
                            } else {
                                startEditActivity(collection);
                            }
                            break;
                        case "VIEW":
                            startViewNoteActivity(collection, Converter.getKeyValuesJsonString(data));
                            break;
                        case "BACKUP":
                            String path = StorageUtil.saveAsObjectToDocumentFile(getApplicationContext(), selectedDir, collection,
                                    dataStorageManager.getDataString(collection));
                            if (path != null) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.save_to)  + " " + path,
                                        Toast.LENGTH_SHORT).show();
                            }
                            break;
                        default: break;
                    }
                    readWriteOnceDataStorage.removeDataStorageListeners();
                }
            });
    }
}
