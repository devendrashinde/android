package com.example.dshinde.myapplication_xmlpref;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Main2Activity extends AppCompatActivity implements ListviewActions {

    EditText keyField;
    EditText value1Field;
    ListView listView;
    //SimpleAdapter listAdapter;
    //ListviewKeyValueMapAdapter listAdapter;
    ListviewKeyValueObjectAdapter listAdapter;
    SharedPrefManager sharedPrefManager;
    String sharedPreferenceName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        keyField = (EditText) findViewById(R.id.etKey);
        value1Field = (EditText) findViewById(R.id.etValue);
        listView = (ListView) findViewById(R.id.list);
        Bundle bundle = getIntent().getExtras();
        sharedPreferenceName = bundle.getString("filename");
        setTitle(sharedPreferenceName);
        sharedPrefManager = new SharedPrefManager(this, sharedPreferenceName, false);
        sharedPrefManager.add(new SharedPrefListener() {
            @Override
            public void sharedPrefChanged(String key, String value) {
                listAdapter.setData(sharedPrefManager.getValues());
            }
        });

        populateListView();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation, menu);
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
        //listAdapter = new SimpleAdapter(this, sharedPrefManager.getValues(), R.layout.list_view_items, from, to);
        listAdapter = new ListviewKeyValueObjectAdapter(sharedPrefManager.getValues(),this, R.layout.list_view_items_flexbox);
        //listAdapter = new ListviewKeyValueMapAdapter(sharedPrefManager.getValues(),this, R.layout.list_view_items_flexbox);
        listView.setAdapter(listAdapter);
        setOnItemClickListenerToListView();
        setSharedPrefManagerListener();
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

    private void setSharedPrefManagerListener() {
        SharedPrefListener listener = new SharedPrefListener() {
            public void sharedPrefChanged(String changedKey, String changedValue) {
                listAdapter.notifyDataSetChanged();
                setEditView(changedKey, changedValue);
            }
        };
    }

    @Override
    public void save(View view) {
        save();
    }

    public void save() {
        String key = keyField.getText().toString();
        String value = value1Field.getText().toString();
        clear();
        sharedPrefManager.save(key, value);
    }

    @Override
    public void remove(View view) {
        remove();
    }

    public void remove() {
        String key = keyField.getText().toString();
        clear();
        sharedPrefManager.remove(key);
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

    public void get(View view) {
        get();
    }

    public void get() {
        String key = keyField.getText().toString();
        if (!key.isEmpty()) {
            value1Field.setText(sharedPrefManager.getValue(key));
        }
    }

    public void export() {
        startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), StorageUtil.PICK_DOCUMENT_FOLDER_FOR_EXPORT);
    }

    private void export(DocumentFile dir) {
        String path = StorageUtil.saveAsTextToDocumentFile(this, dir, sharedPreferenceName, sharedPrefManager.getDataString());
        if (path != null) {
            Toast.makeText(this, "Saved to " + path,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        StorageSelectionResult result = StorageUtil.getStorageSelectionResult(this, requestCode, resultCode, data);
        if (result.getRequestCode() == StorageUtil.PICK_DOCUMENT_FOLDER_FOR_EXPORT) {
            export(result.getDir());
        }
    }

}
