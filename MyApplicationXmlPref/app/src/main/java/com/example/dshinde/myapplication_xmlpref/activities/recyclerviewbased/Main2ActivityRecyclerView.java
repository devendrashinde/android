package com.example.dshinde.myapplication_xmlpref.activities.recyclerviewbased;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.activities.AudioVideoActivity;
import com.example.dshinde.myapplication_xmlpref.activities.BaseActivity;
import com.example.dshinde.myapplication_xmlpref.activities.RandomButtonActivity;
import com.example.dshinde.myapplication_xmlpref.adapters.MarginItemDecoration;
import com.example.dshinde.myapplication_xmlpref.adapters.RecyclerViewKeyValueAdapter;
import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.helper.Factory;
import com.example.dshinde.myapplication_xmlpref.helper.StorageSelectionResult;
import com.example.dshinde.myapplication_xmlpref.helper.StorageUtil;
import com.example.dshinde.myapplication_xmlpref.helper.VerticalResizeTouchHandler;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.listners.ListviewActions;
import com.example.dshinde.myapplication_xmlpref.listners.RecyclerViewKeyValueItemListener;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.example.dshinde.myapplication_xmlpref.services.DataStorage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.Collections;
import java.util.List;

public class Main2ActivityRecyclerView extends BaseActivity implements ListviewActions {

    EditText keyField;
    EditText valueField;
    EditText searchText;
    RecyclerView listView;
    Button divider;
    RecyclerViewKeyValueAdapter listAdapter;
    DataStorage dataStorageManager;
    String collectionName = null;
    LinearLayout editViewLayout;
    LinearLayout searchViewLayout;
    Menu myMenu;
    EditText currentEditText;
    private static final String CLASS_TAG = "Main2Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get parameters
        Bundle bundle = getIntent().getExtras();
        collectionName = bundle.getString(Constants.PARAM_FILENAME);
        userId = bundle.getString(Constants.USERID);
        loadUI();
        initDataStorageAndLoadData(this, collectionName);
    }

    private void initDataStorageAndLoadData(Context context, String noteName) {

        Log.d(CLASS_TAG, "initDataStorageAndLoadData->getDataStorageIntsance");
        dataStorageManager = Factory.getDataStorageInstance(context,
                getDataStorageType(),
                noteName,
                false, false,
                new DataStorageListener() {
                    @Override
                    public void dataChanged(String key, String value) {
                        Log.d(CLASS_TAG, "dataChanged key: " + key + ", value: " + value);
                        loadDataInListView(dataStorageManager.getValues());
                    }

                    @Override
                    public void dataLoaded(List<KeyValue> data) {
                        Log.d(CLASS_TAG, "dataLoaded");
                        loadDataInListView(data);
                        if (data.size() > 0) {
                            enableTextToSpeech();
                        }
                    }
                });
        Log.d(CLASS_TAG, "initDataStorageAndLoadData->loadData");
        String importData = getIntent().getExtras().getString(Constants.PARAM_NOTE_DATA);
        if (importData != null && !importData.isEmpty()) {
            dataStorageManager.disableNotifyDataChange();
            importNoteData(importData);
            dataStorageManager.enableNotifyDataChange();
        }
        dataStorageManager.loadData();
    }

    private void loadDataInListView(List<KeyValue> data) {
        Log.d(CLASS_TAG, "loadDataInListView");
        runOnUiThread(() -> listAdapter.setData(data));
    }

    private void loadUI() {
        setContentView(R.layout.activity_main2_2_recycler_view);
        keyField = (EditText) findViewById(R.id.etKey);
        valueField = (EditText) findViewById(R.id.etValue);
        listView = (RecyclerView) findViewById(R.id.list);
        editViewLayout = (LinearLayout) findViewById(R.id.editView);
        searchViewLayout = (LinearLayout) findViewById(R.id.searchView);
        searchText = (EditText) findViewById(R.id.searchText);
        setTitle(collectionName);
        populateListView();
        setSearchFieldWatcher();
        setEditTextClearButtonAction(searchText);
        setFocusListener();
        //setEditTextClearButtonAction(keyField);
        //setEditTextClearButtonAction(valueField);
    }

    private void setFocusListener() {
        keyField.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                currentEditText = keyField;
            }
        });
        valueField.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                currentEditText = valueField;
            }
        });
    }

    private void showEditView(boolean show) {
        editViewLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        searchViewLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        listView.setVisibility(show ? View.GONE : View.VISIBLE);
        MenuItem menuItem = myMenu.findItem(R.id.menu_edit);
        Drawable icon = getDrawable(show ? R.drawable.ic_format_line_spacing_black_24dp : R.drawable.ic_action_edit);
        menuItem.setIcon(icon);
        if (!show) {
            hideKeyboard(editViewLayout);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation, menu);
        myMenu = menu;
        showEditView(false);
        removeUnwantedMenuItems(menu, new int[]{R.id.menu_add_to_dictionary,
                R.id.menu_backup, R.id.menu_design_screen, R.id.menu_test, R
                .id.menu_view, R.id.menu_import,R.id.menu_export});
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_add:
                clear();
                showEditView(true);
                return true;
            case R.id.menu_save:
                save();
                return true;
            case R.id.menu_clear:
                clear();
                return true;
            case R.id.menu_copy:
                return true;
            case R.id.menu_edit:
                showEditView(editViewLayout.getVisibility() == View.GONE);
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
            case R.id.menu_daylight:
                setTheme(editViewLayout, Constants.DAY_MODE);
                setTheme(searchViewLayout, Constants.DAY_MODE);
                return true;
            case R.id.menu_nightlight:
                setTheme(editViewLayout, Constants.NIGHT_MODE);
                setTheme(searchViewLayout, Constants.NIGHT_MODE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setEditView(String key, String value) {
        keyField.setText(key);
        valueField.setText(value);
    }

    private void setSearchFieldWatcher() {
        searchText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // as user is typing test, need to clear earlier key if any and user need to select the record from list
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
                keyField.setText(s.toString());
                valueField.setText(s.toString());
            }
        });

    }

    private RecyclerViewKeyValueItemListener getOnItemClickListenerToListView() {
        RecyclerViewKeyValueItemListener listener = new RecyclerViewKeyValueItemListener() {
            @Override
            public void onItemClick(KeyValue kv) {
                setEditView(kv.getKey(), kv.getValue());
            }

            @Override
            public boolean onItemLongClick(KeyValue kv) {
                String value = kv.getValue();
                if (value != null && value.length() > 0) {
                    showPopup(kv.getKey(), kv.getValue());
                }
                return true;
            }
        };
        return listener;
    }

    private void populateListView() {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        listAdapter = new RecyclerViewKeyValueAdapter(Collections.emptyList(),
                this, R.layout.list_view_items_flexbox_recycleview,
                getOnItemClickListenerToListView());
        listView.setLayoutManager(mLayoutManager);
        listView.addItemDecoration(new MarginItemDecoration(8));
        listView.setAdapter(listAdapter);

        listAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
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

    private void displayRandomButtonActivity(String value) {
        Intent intent = new Intent(Main2ActivityRecyclerView.this, RandomButtonActivity.class);
        intent.putExtra("text", value);
        startActivity(intent);
    }

    @Override
    public void save(View view) {
        save();
    }

    public void save() {
        String key = keyField.getText().toString().trim();
        String value = valueField.getText().toString().trim();
        dataStorageManager.save(key, value);
        showEditView(false);
        clear();
    }

    @Override
    public void remove(View view) {
        remove();
    }

    public void remove() {
        String key = keyField.getText().toString();
        clear();
        dataStorageManager.remove(key);
    }

    public void clear(View view) {
        clear();
    }

    public void clear() {
        if(currentEditText != null) {
            currentEditText.setText("");
            currentEditText.requestFocus();
        }
    }

    public void share() {
        String key = keyField.getText().toString();
        String value = valueField.getText().toString();
        if (!key.isEmpty() && !value.isEmpty()) {
            shareText(key + Constants.CR_LF + value);
        }
    }

    public void export() {
        startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), StorageUtil.PICK_DOCUMENT_FOLDER_FOR_EXPORT);
    }

    private void export(DocumentFile dir) {
        String path = StorageUtil.saveAsTextToDocumentFile(this, dir, collectionName, dataStorageManager.getDataString());
        if (path != null) {
            Toast.makeText(this, getResources().getString(R.string.save_to) + " " + path,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        StorageSelectionResult result = StorageUtil.getStorageSelectionResult(this, requestCode, resultCode, data);
        if (result.getRequestCode() == StorageUtil.PICK_DOCUMENT_FOLDER_FOR_EXPORT) {
            export(result.getDir());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        dataStorageManager.removeDataStorageListeners();
    }

    private void importNoteData(String data) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<KeyValue> values = gson.fromJson(data, new TypeToken<List<KeyValue>>() {
        }.getType());
        dataStorageManager.save(values);
    }

    private void showPopup(String key, String value) {
        selectOption(Constants.SELECT_ACTION_FOR_NOTE, R.string.what_you_want_to_do,
                R.array.actions_on_note_item,
                null, key, value);
    }

    @Override
    protected void processSelectedOption(String id, String selectedOption, String key, String value) {
        switch (selectedOption) {
            case Constants.NAAMASMRAN:
                displayRandomButtonActivity(value);
                break;
            case Constants.AUDIO_NOTE:
                displayAudioVideoActivity(key, value);
                break;
            default:
                super.processSelectedOption(id, selectedOption, key, value);
                break;
        }
    }

    private void displayAudioVideoActivity(String key, String value) {
        Intent intent = new Intent(Main2ActivityRecyclerView.this, AudioVideoActivity.class);
        intent.putExtra(Constants.USERID, userId);
        intent.putExtra(Constants.PARAM_NOTE, collectionName);
        intent.putExtra(Constants.KEY, key);
        intent.putExtra(Constants.VALUE, value);
        startActivity(intent);
    }

}
