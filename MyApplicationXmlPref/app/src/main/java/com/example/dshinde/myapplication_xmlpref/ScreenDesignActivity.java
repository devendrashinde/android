package com.example.dshinde.myapplication_xmlpref;

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
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import com.example.dshinde.myapplication_xmlpref.adapters.ListviewKeyValueObjectAdapter;
import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.helper.Converter;
import com.example.dshinde.myapplication_xmlpref.helper.Factory;
import com.example.dshinde.myapplication_xmlpref.helper.JsonHelper;
import com.example.dshinde.myapplication_xmlpref.helper.StorageSelectionResult;
import com.example.dshinde.myapplication_xmlpref.helper.StorageUtil;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.example.dshinde.myapplication_xmlpref.model.ScreenControl;
import com.example.dshinde.myapplication_xmlpref.services.DataStorage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Collections;
import java.util.List;

public class ScreenDesignActivity extends BaseActivity {

    String keyField;
    String valueField;
    ListView listView;
    Button addButton;
    Button editButton;
    Button delButton;
    Button screenPreview;
    EditText searchText;
    ListviewKeyValueObjectAdapter listAdapter;
    DataStorage dataStorageManager;
    DataStorageListener dataStorageListener;
    String collectionName = null;
    Integer requestMode = null;
    String screenConfig = null;
    LinearLayout editViewLayout;
    Gson gson = new GsonBuilder().create();
    Menu myMenu;
    private static final String CLASS_TAG = "ScreenDesignActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get parameters
        Bundle bundle = getIntent().getExtras();
        collectionName = bundle.getString("screenName");
        userId = bundle.getString("userId");
        requestMode = bundle.getInt("requestMode", Constants.REQUEST_CODE_SCREEN_DESIGN);
        loadUI(bundle);
        initDataStorageAndLoadData(this);
    }

    private void loadUI(Bundle bundle) {
        if (isDesignMode()) {
            setContentView(R.layout.screen_design_activity);
            setTitle(collectionName + ": Design");
            screenConfig = screenDesign();
        } else {
            setContentView(R.layout.screen_capture_activity);
            setTitle(collectionName + ": Edit");
            screenConfig = bundle.getString("screenConfig");
        }
        listView = (ListView) findViewById(R.id.list);
        addButton = (Button) findViewById(R.id.btnAdd);
        editButton = (Button) findViewById(R.id.btnEdit);
        delButton = (Button) findViewById(R.id.btnDel);
        editViewLayout = (LinearLayout) findViewById(R.id.editView);
        searchText = (EditText) findViewById(R.id.searchText);

        populateListView();
        if (isDesignMode()) {
            setAddActionListener();
            setDeleteActionListener();
            setEditActionListener();
            setPreviewActionListener();
        }
        setSearchFieldWatcher();
        setValueFieldClearButtonAction();
    }

    private void setValueFieldClearButtonAction() {
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

    private void setSearchFieldWatcher() {
        searchText.addTextChangedListener(new TextWatcher() {

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

    private void initDataStorageAndLoadData(Context context) {
        Log.d(CLASS_TAG, "initDataStorageAndLoadData->getDataStorageIntsance");
        dataStorageManager = Factory.getDataStorageIntsance(context, getDataStorageType(),
                (isDesignMode() ? Constants.SCREEN_DESIGN : "") + collectionName,
                false, false, getDataStorageListener());
        Log.d(CLASS_TAG, "initDataStorageAndLoadData->loadData");
        dataStorageManager.loadData();
    }

    private DataStorageListener getDataStorageListener(){
        dataStorageListener = new DataStorageListener() {
            @Override
            public void dataChanged(String key, String value) {
                Log.d(CLASS_TAG, "dataChanged->loadDataInListView key: " + key + ", value: " + value);
                loadDataInListView(dataStorageManager.getValues());
            }

            @Override
            public void dataLoaded(List<KeyValue> data) {
                Log.d(CLASS_TAG, "dataLoaded->loadDataInListView");
                loadDataInListView(data);
            }
        };
        return dataStorageListener;
    }

    private void loadDataInListView(List<KeyValue> data) {
        Log.d(CLASS_TAG, "loadDataInListView");
        runOnUiThread(() -> listAdapter.setData(data));
    }

    private boolean isDesignMode() {
        return requestMode == Constants.REQUEST_CODE_SCREEN_DESIGN;
    }

    private void setAddActionListener() {
        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startDyanmicScreenDesignActivity(false);
            }
        });
    }

    private void startDyanmicScreenDesignActivity(boolean edit) {
        if (edit && (valueField == null || valueField.length() == 0)) {
            Toast.makeText(this, "Please select record to Edit it.",
                    Toast.LENGTH_SHORT).show();
        } else {

            Intent intent = new Intent(this, DynamicLinearLayoutActivity.class);
            intent.putExtra("screenConfig", screenConfig);
            intent.putExtra("requestMode", requestMode);
            if (edit) {
                intent.putExtra("screenData", valueField);
            }
            startActivityForResult(intent, requestMode);
        }
    }

    private void startDyanmicScreenPreviewActivity() {
        if (dataStorageManager.count() > 0) {
            Intent intent = new Intent(this, DynamicLinearLayoutActivity.class);
            intent.putExtra("screenConfig", Converter.getValuesJsonString(dataStorageManager.getValues()));
            intent.putExtra("requestMode", Constants.REQUEST_CODE_SCREEN_PREVIEW);
            startActivityForResult(intent, Constants.REQUEST_CODE_SCREEN_PREVIEW);
        }
    }

    private void setEditActionListener() {
        editButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startDyanmicScreenDesignActivity(true);
            }
        });
    }

    private void setDeleteActionListener() {
        delButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                remove();
            }
        });
    }

    private void setPreviewActionListener() {
        screenPreview = (Button) findViewById(R.id.btnPreview);
        screenPreview.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startDyanmicScreenPreviewActivity();
            }
        });
    }

    private void showEditView(boolean show) {
        editViewLayout.setVisibility(show ? View.VISIBLE : View.GONE);
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
        menu.removeItem(R.id.menu_add_to_shadba_kosh);
        menu.removeItem(R.id.menu_backup);
        menu.removeItem(R.id.menu_sell);
        menu.removeItem(R.id.menu_settings);
        menu.removeItem(R.id.menu_save);
        menu.removeItem(R.id.menu_clear);
        menu.removeItem(R.id.menu_design_screen);
        menu.removeItem(R.id.menu_export);
        menu.removeItem(R.id.menu_pay);
        if (isDesignMode()) {
            menu.removeItem(R.id.menu_add);
            menu.removeItem(R.id.menu_remove);
            menu.removeItem(R.id.menu_view);
        }
        myMenu = menu;
        showEditView(isDesignMode());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_add:
                startDyanmicScreenDesignActivity(false);
                return true;
            case R.id.menu_edit:
                if (isDesignMode()) {
                    showEditView(!(editViewLayout.getVisibility() == View.VISIBLE));
                } else {
                    startDyanmicScreenDesignActivity(true);
                }
                return true;
            case R.id.menu_remove:
                remove();
                return true;
            case R.id.menu_view:
                startDyanmicScreenPreviewActivity();
                return true;
            case R.id.menu_share:
                share();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setEditView(String key, String value) {
        keyField = key;
        valueField = value;
    }

    private void populateListView() {
        listAdapter = new ListviewKeyValueObjectAdapter(Collections.emptyList(), this, R.layout.list_view_items_flexbox);
        listView.setAdapter(listAdapter);
        setOnItemClickListenerToListView();
    }

    private void setOnItemClickListenerToListView() {
        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                KeyValue kv = listAdapter.getItem(position);
                setEditView(kv.getKey(), kv.getValue());
                view.setSelected(true);
            }
        };
        listView.setOnItemClickListener(listener);
    }

    public void save() {
        dataStorageManager.addDataStorageListener(dataStorageListener);
        if (keyField != null && keyField.length() > 0) {
            dataStorageManager.save(keyField, valueField);
            showEditView(false);
            clear();
        }
    }

    public void remove() {
        if (keyField != null && keyField.length() > 0) {
            dataStorageManager.remove(keyField);
            clear();
        }
    }

    public void clear() {
        setEditView("", "");
    }

    public void share() {
        if (!valueField.isEmpty() && !valueField.isEmpty()) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, JsonHelper.formatAsString(valueField));
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
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
        if (requestCode == StorageUtil.PICK_DOCUMENT_FOLDER_FOR_EXPORT) {
            export(result.getDir());
        }

        if (requestCode == Constants.REQUEST_CODE_SCREEN_DESIGN && resultCode == Constants.RESULT_CODE_OK) {
            Log.d(CLASS_TAG, "onActivityResult design");
            valueField = data.getExtras().getString("data");
//            Toast.makeText(this, "Received\n" + valueField,
//                    Toast.LENGTH_LONG).show();
            ScreenControl screenControl = gson.fromJson(valueField, ScreenControl.class);
            keyField = screenControl.getControlId();
            save();
        }

        if (requestCode == Constants.REQUEST_CODE_SCREEN_CAPTURE && resultCode == Constants.RESULT_CODE_OK) {
            Log.d(CLASS_TAG, "onActivityResult capture");
            valueField = data.getExtras().getString("data");
            keyField = data.getExtras().getString("key");
//            Toast.makeText(this, "Received\n" + valueField,
//                    Toast.LENGTH_LONG).show();
            save();
        }
    }

    @Override
    public void onStop() {
        Log.d(CLASS_TAG, "onStop");
        super.onStop();
        dataStorageManager.removeDataStorageListeners();
    }

    private String screenDesign() {
        return "[\n" +
                "    {\n" +
                "        \"controlId\": \"controlType\",\n" +
                "        \"positionId\": \"1\",\n" +
                "        \"controlType\": \"DropDownList\",\n" +
                "        \"textLabel\": \"Field Type:\",\n" +
                "        \"options\": \"Text\\nEditText\\nCheckBox\\nRadioButton\\nDropDownList\\nDatePicker\\nTimePicker\\nSaveButton\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"controlId\": \"positionId\",\n" +
                "        \"positionId\": \"2\",\n" +
                "        \"controlType\": \"EditText\",\n" +
                "        \"textLabel\": \"Field position on screen:\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"controlId\": \"controlId\",\n" +
                "        \"positionId\": \"3\",\n" +
                "        \"controlType\": \"EditText\",\n" +
                "        \"textLabel\": \"Field Id:\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"controlId\": \"defaultValue\",\n" +
                "        \"controlType\": \"EditText\",\n" +
                "        \"positionId\": \"4\",\n" +
                "        \"textLabel\": \"Default value:\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"controlId\": \"indexField\",\n" +
                "        \"positionId\": \"5\",\n" +
                "        \"controlType\": \"RadioButton\",\n" +
                "        \"textLabel\": \"Is this field part of index?\",\n" +
                "        \"defaultValue\": \"No\",\n" +
                "        \"options\": \"Yes\\nNo\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"controlId\": \"textLabel\",\n" +
                "        \"positionId\": \"6\",\n" +
                "        \"controlType\": \"EditText\",\n" +
                "        \"textLabel\": \"Label Text:\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"controlId\": \"options\",\n" +
                "        \"positionId\": \"7\",\n" +
                "        \"controlType\": \"MultiLineEditText\",\n" +
                "        \"textLabel\": \"Enter values for DropdownList/Checkbox/RadioButtons on separate lines:\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"controlId\": \"saveButton\",\n" +
                "        \"positionId\": \"9\",\n" +
                "        \"controlType\": \"SaveButton\",\n" +
                "        \"textLabel\": \"Save\"\n" +
                "    }\n" +
                "]";
    }

}
