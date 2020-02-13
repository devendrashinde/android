package com.example.dshinde.myapplication_xmlpref;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import com.example.dshinde.myapplication_xmlpref.adapters.ListviewKeyValueObjectAdapter;
import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.helper.Converter;
import com.example.dshinde.myapplication_xmlpref.helper.Factory;
import com.example.dshinde.myapplication_xmlpref.helper.StorageSelectionResult;
import com.example.dshinde.myapplication_xmlpref.helper.StorageUtil;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.listners.ListviewActions;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.example.dshinde.myapplication_xmlpref.model.ScreenControl;
import com.example.dshinde.myapplication_xmlpref.services.DataStorage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScreenDesignActivity extends BaseActivity {

    String keyField;
    String value1Field;
    ListView listView;
    Button addControl;
    Button editControl;
    Button delControl;
    Button screenPreview;
    ListviewKeyValueObjectAdapter listAdapter;
    DataStorage dataStorageManager;
    String collectionName = null;
    String requestMode = null;
    String screenConfig = null;
    LinearLayout editViewLayout;
    Gson gson = new GsonBuilder().create();
    Menu myMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_design_activity);

        listView = (ListView) findViewById(R.id.list);
        addControl = (Button)  findViewById(R.id.btnAdd);
        editControl = (Button)  findViewById(R.id.btnEdit);
        delControl = (Button)  findViewById(R.id.btnDel);
        screenPreview = (Button)  findViewById(R.id.btnPreview);
        editViewLayout = (LinearLayout) findViewById(R.id.editView);

        // get parameters
        Bundle bundle = getIntent().getExtras();
        collectionName = bundle.getString("screenName");
        userId = bundle.getString("userId");
        requestMode = bundle.getString("requestMode");
        if(requestMode.equals(Constants.REQUEST_MODE_CAPTURE)) {
            setTitle(collectionName + ": Edit");
            screenConfig = bundle.getString("screenConfig");
        } else {
            setTitle(collectionName + ": Design");
            screenConfig = screenDesign();
        }

        dataStorageManager = Factory.getDataStorageIntsance(this,
                getDataStorageType(),
                (requestMode.equals(Constants.REQUEST_MODE_DESIGN) ? Constants.SCREEN_DESIGN : "") + collectionName,
                false,
                false);
        dataStorageManager.addDataStorageListener(new DataStorageListener() {
            @Override
            public void dataChanged(String key, String value) {
                listAdapter.setData(dataStorageManager.getValues());
            }

            @Override
            public void dataLoaded(List<KeyValue> data) {
                listAdapter.setData(data);
            }
        });

        populateListView();
        setAddControlListener();
        setDeleteControlListener();
        setEditControlListener();
        setScreenPreviewListener();
        dataStorageManager.loadData();
    }

    private void setAddControlListener(){
        addControl.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startDyanmicScreenDesignActivity(false);
            }
        });
    }

    private void startDyanmicScreenDesignActivity(boolean edit) {
        Intent intent=new Intent(this, DynamicLinearLayoutActivity.class);
        intent.putExtra("screenConfig", screenConfig);
        if(edit) {
            intent.putExtra("screenData", value1Field);
        }
        startActivityForResult(intent, Constants.RESULT_CODE_SCREEN_DESIGN);
    }

    private void startDyanmicScreenPreviewActivity() {
        Intent intent=new Intent(this, DynamicLinearLayoutActivity.class);
        intent.putExtra("screenConfig", Converter.getValuesJsonString(dataStorageManager.getValues()));
        startActivityForResult(intent, Constants.RESULT_CODE_SCREEN_DESIGN);
    }

    private void setEditControlListener(){
        editControl.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startDyanmicScreenDesignActivity(true);
            }
        });
    }

    private void setDeleteControlListener(){
        delControl.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                remove();
            }
        });
    }

    private void setScreenPreviewListener(){
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
        menu.removeItem(R.id.menu_remove);
        menu.removeItem(R.id.menu_dynaform);
        menu.removeItem(R.id.menu_export);
        menu.removeItem(R.id.menu_pay);
        menu.removeItem(R.id.menu_share);
        myMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_view:
                return true;
            case R.id.menu_edit:
                showEditView(editViewLayout.getVisibility() == View.GONE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setEditView(String key, String value) {
        keyField = key;
        value1Field = value;
    }

    private void populateListView() {
        listAdapter = new ListviewKeyValueObjectAdapter(Collections.emptyList(),this, R.layout.list_view_items_flexbox);
        listView.setAdapter(listAdapter);
        setOnItemClickListenerToListView();
    }

    private void setOnItemClickListenerToListView() {
        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String key = ((TextView) view.findViewById(R.id.listKey)).getText().toString();
                String value = ((TextView) view.findViewById(R.id.listValue)).getText().toString();
                setEditView(key, value);
            }
        };
        listView.setOnItemClickListener(listener);
    }

    public void save() {
        if (keyField != null && keyField.length() > 0) {
            dataStorageManager.save(keyField, value1Field);
            showEditView(false);
            clear();
        }
    }

    public void remove() {
        if (keyField != null && keyField.length() > 0){
            dataStorageManager.remove(keyField);
            clear();
        }
    }

    public void clear(View view) {
        clear();
    }

    public void clear() {
        setEditView("", "");
    }

    public void share(View view) {
        share();
    }

    public void share() {
        if (!value1Field.isEmpty() && !value1Field.isEmpty()) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, keyField + "\n" + value1Field);
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

        if (requestCode == Constants.RESULT_CODE_SCREEN_DESIGN && resultCode == Constants.RESULT_CODE_OK) {
            String value = data.getExtras().getString("data");
            Toast.makeText(this, "Received\n" + value,
                    Toast.LENGTH_LONG).show();
            ScreenControl screenControl = gson.fromJson(value, ScreenControl.class);
            keyField = screenControl.getControlId();
            value1Field = value;
            save();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        dataStorageManager.removeDataStorageListeners();
    }

    private String jsonData(){
        return new Gson().toJson(dataStorageManager.getValues() );
    }

    private String screenDesign(){
        return "[\n" +
                "    {\n" +
                "        \"controlId\": \"controlType\",\n" +
                "        \"controlType\": \"DropDownList\",\n" +
                "        \"textLabel\": \"Control Type:\",\n" +
                "        \"options\": \"Text\\nEditText\\nCheckBox\\nRadioButton\\nDropDownList\\nDatePicker\\nTimePicker\\nSaveButton\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"controlId\": \"controlId\",\n" +
                "        \"controlType\": \"EditText\",\n" +
                "        \"textLabel\": \"Control Id:\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"controlId\": \"textLabel\",\n" +
                "        \"controlType\": \"EditText\",\n" +
                "        \"textLabel\": \"Label Text:\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"controlId\": \"options\",\n" +
                "        \"controlType\": \"MultiLineEditText\",\n" +
                "        \"textLabel\": \"Enter values for DropdownList/Checkbox/RadioButtons on separate lines:\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"controlId\": \"saveButton\",\n" +
                "        \"controlType\": \"SaveButton\",\n" +
                "        \"textLabel\": \"Save\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"controlId\": \"cancelButton\",\n" +
                "        \"controlType\": \"CancelButton\",\n" +
                "        \"textLabel\": \"Cancel\"\n" +
                "    }\n" +
                "]";
    }
}
