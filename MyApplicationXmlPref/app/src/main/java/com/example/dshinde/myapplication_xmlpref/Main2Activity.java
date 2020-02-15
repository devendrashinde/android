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
import com.example.dshinde.myapplication_xmlpref.helper.Factory;
import com.example.dshinde.myapplication_xmlpref.helper.StorageSelectionResult;
import com.example.dshinde.myapplication_xmlpref.helper.StorageUtil;
import com.example.dshinde.myapplication_xmlpref.listners.ListviewActions;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.example.dshinde.myapplication_xmlpref.services.DataStorage;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;

import java.util.Collections;
import java.util.List;

public class Main2Activity extends BaseActivity implements ListviewActions {

    EditText keyField;
    EditText value1Field;
    ListView listView;
    Button divider;
    ListviewKeyValueObjectAdapter listAdapter;
    DataStorage dataStorageManager;
    String collectionName = null;
    LinearLayout editViewLayout;
    Menu myMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2_2);

        keyField = (EditText) findViewById(R.id.etKey);
        value1Field = (EditText) findViewById(R.id.etValue);
        listView = (ListView) findViewById(R.id.list);
        editViewLayout = (LinearLayout) findViewById(R.id.editView);

        // get parameters
        Bundle bundle = getIntent().getExtras();
        collectionName = bundle.getString("filename");
        userId = bundle.getString("userId");

        setTitle(collectionName);
        dataStorageManager = Factory.getDataStorageIntsance(this, getDataStorageType(), collectionName, false, false);
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
        dataStorageManager.loadData();
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

    private void setEditView(String key, String string) {
        keyField.setText(key);
        value1Field.setText(string);
    }

    private void populateListView() {
        listAdapter = new ListviewKeyValueObjectAdapter(Collections.emptyList(),this, R.layout.list_view_items_flexbox);
        listView.setAdapter(listAdapter);
        setOnItemClickListenerToListView();
    }

    private void setOnItemClickListenerToListView() {
        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                KeyValue kv = listAdapter.getItem(position);
                setEditView(kv.getKey(), kv.getValue());
            }
        };
        listView.setOnItemClickListener(listener);
    }

    @Override
    public void save(View view) {
        save();
    }

    public void save() {
        String key = keyField.getText().toString();
        String value = value1Field.getText().toString();
        clear();
        dataStorageManager.save(key, value);
        showEditView(false);
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
        setEditView("", "");
        keyField.requestFocus();
    }

    public void share(View view) {
        share();
    }

    public void share() {
        String key = keyField.getText().toString();
        String value = value1Field.getText().toString();
        if (!value.isEmpty() && !value.isEmpty()) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, key + "\n" + value);
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
        if (result.getRequestCode() == StorageUtil.PICK_DOCUMENT_FOLDER_FOR_EXPORT) {
            export(result.getDir());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        dataStorageManager.removeDataStorageListeners();
    }

}
