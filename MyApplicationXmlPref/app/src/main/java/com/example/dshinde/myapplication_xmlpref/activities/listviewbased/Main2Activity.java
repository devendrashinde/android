package com.example.dshinde.myapplication_xmlpref.activities.listviewbased;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.activities.BaseActivity;
import com.example.dshinde.myapplication_xmlpref.activities.RandomButtonActivity;
import com.example.dshinde.myapplication_xmlpref.adapters.ListviewKeyValueObjectAdapter;
import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.helper.DynamicControls;
import com.example.dshinde.myapplication_xmlpref.helper.Factory;
import com.example.dshinde.myapplication_xmlpref.helper.StorageSelectionResult;
import com.example.dshinde.myapplication_xmlpref.helper.StorageUtil;
import com.example.dshinde.myapplication_xmlpref.listners.ListviewActions;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.example.dshinde.myapplication_xmlpref.services.DataStorage;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main2Activity extends BaseActivity implements ListviewActions {

    public static final String NAAMASMRAN = "Naamasmran";
    public static final String MEANING = "Meaning";
    EditText keyField;
    EditText valueField;
    ListView listView;
    Button divider;
    ListviewKeyValueObjectAdapter listAdapter;
    DataStorage dataStorageManager;
    String collectionName = null;
    LinearLayout editViewLayout;
    Menu myMenu;
    private static final String CLASS_TAG = "Main2Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get parameters
        Bundle bundle = getIntent().getExtras();
        collectionName = bundle.getString(Constants.PARAM_FILENAME);
        userId = bundle.getString(Constants.USERID);
        loadUI();
        initDataStorageAndLoadData(this);
    }

    private void initDataStorageAndLoadData(Context context) {

        Log.d(CLASS_TAG, "initDataStorageAndLoadData->getDataStorageIntsance");
        dataStorageManager = Factory.getDataStorageInstance(context, getDataStorageType(), collectionName, false, false, new DataStorageListener() {
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
        if(dataToImport != null && !dataToImport.isEmpty()){
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
        setContentView(R.layout.activity_main2_2);
        keyField = (EditText) findViewById(R.id.etKey);
        valueField = (EditText) findViewById(R.id.etValue);
        listView = (ListView) findViewById(R.id.list);
        editViewLayout = (LinearLayout) findViewById(R.id.editView);
        setTitle(collectionName);
        populateListView();
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
        myMenu = menu;
        showEditView(false);
        myMenu.removeItem(R.id.menu_nightlight);
        myMenu.removeItem(R.id.menu_add_to_shadba_kosh);
        myMenu.removeItem(R.id.menu_backup);
        myMenu.removeItem(R.id.menu_design_screen);
        myMenu.removeItem(R.id.menu_daylight);
        myMenu.removeItem(R.id.menu_test);
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

    private void populateListView() {
        listAdapter = new ListviewKeyValueObjectAdapter(Collections.emptyList(), this, R.layout.list_view_items_flexbox);
        listView.setAdapter(listAdapter);
        setOnItemClickListenerToListView();
        setOnItemLongClickListenerToListView();
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

    private void setOnItemLongClickListenerToListView() {
        AdapterView.OnItemLongClickListener listener = new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                KeyValue kv = listAdapter.getItem(position);
                String value = kv.getValue();
                if(value != null && value.length() > 0) {
                    showPopup(kv.getKey(), kv.getValue());
                }
                return true;
            }
        };
        listView.setOnItemLongClickListener(listener);
    }

    private void displayRandomButtonActivity(String value) {
        Intent intent = new Intent(Main2Activity.this, RandomButtonActivity.class);
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

    private void importData(String data){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<KeyValue> values = gson.fromJson(data, new TypeToken<List<KeyValue>>(){}.getType());
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
        RadioGroup rg = DynamicControls.getRadioGroupControl(this,new String[] {NAAMASMRAN, MEANING}, new ArrayList<>());
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        RadioButton selectedButton = (RadioButton) builder.findViewById(checkedId);
                        if(selectedButton != null) {
                            switch (selectedButton.getText().toString()) {
                                case NAAMASMRAN:
                                    displayRandomButtonActivity(value);
                                    break;
                                case MEANING:
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
    };
}
