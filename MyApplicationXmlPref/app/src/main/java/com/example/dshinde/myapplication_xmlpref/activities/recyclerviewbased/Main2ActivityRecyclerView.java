package com.example.dshinde.myapplication_xmlpref.activities.recyclerviewbased;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
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
import com.example.dshinde.myapplication_xmlpref.helper.DynamicControls;
import com.example.dshinde.myapplication_xmlpref.helper.Factory;
import com.example.dshinde.myapplication_xmlpref.helper.StorageSelectionResult;
import com.example.dshinde.myapplication_xmlpref.helper.StorageUtil;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.listners.ListviewActions;
import com.example.dshinde.myapplication_xmlpref.listners.RecyclerViewKeyValueItemListener;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.example.dshinde.myapplication_xmlpref.services.DataStorage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
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
    private static final String CLASS_TAG = "Main2Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get parameters
        Bundle bundle = getIntent().getExtras();
        collectionName = bundle.getString("filename");
        userId = bundle.getString("userId");
        loadUI();
        initDataStorageAndLoadData(this);
    }

    private void initDataStorageAndLoadData(Context context) {

        Log.d(CLASS_TAG, "initDataStorageAndLoadData->getDataStorageIntsance");
        dataStorageManager = Factory.getDataStorageIntsance(context, getDataStorageType(), collectionName, false, false, new DataStorageListener() {
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
        String dataToImport = getIntent().getExtras().getString("dataToImport");
        if (dataToImport != null && !dataToImport.isEmpty()) {
            dataStorageManager.disableNotifyDataChange();
            importData(dataToImport);
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
        setSearchFieldClearButtonAction();
    }

    private void setSearchFieldClearButtonAction() {
        searchText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (searchText.getRight() - searchText.getCompoundDrawables()[Constants.DRAWABLE_RIGHT].getBounds().width())) {
                        searchText.setText("");
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void showEditView(boolean show) {
        editViewLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        searchViewLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        MenuItem menuItem = myMenu.findItem(R.id.menu_edit);
        Drawable icon = getDrawable(show ? R.drawable.ic_format_line_spacing_black_24dp : R.drawable.ic_edit_black);
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
        myMenu.removeItem(R.id.menu_pay);
        myMenu.removeItem(R.id.menu_add_to_shadba_kosh);
        myMenu.removeItem(R.id.menu_backup);
        myMenu.removeItem(R.id.menu_design_screen);
        myMenu.removeItem(R.id.menu_sell);
        myMenu.removeItem(R.id.menu_settings);
        myMenu.removeItem(R.id.menu_view);
        myMenu.removeItem(R.id.menu_import);
        myMenu.removeItem(R.id.menu_export);
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
        String key = keyField.getText().toString();
        String value = valueField.getText().toString();
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
        String key = keyField.getText().toString(); // keep last key for ease of editing
        setEditView(key, "");
        keyField.requestFocus();
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
            Toast.makeText(this, "Saved to " + path,
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

    private void importData(String data) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<KeyValue> values = gson.fromJson(data, new TypeToken<List<KeyValue>>() {
        }.getType());
        dataStorageManager.save(values);
    }

    private void showPopup(String key, String value) {
        Dialog builder = new Dialog(this);
        builder.setTitle("What you want to do");
        //builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //builder.getWindow().setBackgroundDrawable(
        //        new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //nothing;
            }
        });
        RadioGroup rg = DynamicControls.getRadioGroupControl(this,
                new String[]{Constants.NAAMASMRAN, Constants.MEANING, Constants.AUDIO_NOTE},
                new ArrayList<>());
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton selectedButton = (RadioButton) builder.findViewById(checkedId);
                if (selectedButton != null) {
                    switch (selectedButton.getText().toString()) {
                        case Constants.NAAMASMRAN:
                            displayRandomButtonActivity(value);
                            break;
                        case Constants.MEANING:
                            break;
                        case Constants.AUDIO_NOTE:
                            displayAudioVideoActivity(key, value);
                            break;
                    }
                    builder.dismiss();
                }
            }
        });

        builder.addContentView(rg, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        builder.show();
    }

    private void displayAudioVideoActivity(String key, String value) {
        Intent intent = new Intent(Main2ActivityRecyclerView.this, AudioVideoActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("note", collectionName);
        intent.putExtra("key", key);
        intent.putExtra("value", value);
        startActivity(intent);
    }

}
