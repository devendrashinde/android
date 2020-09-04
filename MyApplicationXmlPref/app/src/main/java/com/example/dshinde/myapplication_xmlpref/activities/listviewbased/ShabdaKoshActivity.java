package com.example.dshinde.myapplication_xmlpref.activities.listviewbased;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.activities.BaseActivity;
import com.example.dshinde.myapplication_xmlpref.adapters.MarginItemDecoration;
import com.example.dshinde.myapplication_xmlpref.adapters.RecyclerViewKeyValueAdapter;
import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.helper.Factory;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.listners.RecyclerViewKeyValueItemListener;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.example.dshinde.myapplication_xmlpref.model.ShabdaDetails;
import com.example.dshinde.myapplication_xmlpref.model.ShabdaUsage;
import com.example.dshinde.myapplication_xmlpref.services.DataStorage;
import com.example.dshinde.myapplication_xmlpref.services.ReadOnceDataStorage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Collections;
import java.util.List;

public class ShabdaKoshActivity extends BaseActivity {

    EditText keyField;
    EditText valueField;
    EditText searchText;
    RecyclerView listView;
    RecyclerViewKeyValueAdapter listAdapter;
    DataStorage dataStorageManager;
    ReadOnceDataStorage firebaseReadOnceImpl;
    String collectionName = null;
    String collectionToAdd = null;
    boolean addingToShabdKosh = false;
    private static final String CLASS_TAG = "ShabdaKoshActivity";
    LinearLayout editViewLayout;
    Menu myMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        collectionName = Constants.SHABDA_KOSH;
        userId = bundle.getString("userId");
        collectionToAdd = bundle.getString("collectionToAddToShabdaKosh");
        setTitle(collectionName);
        loadUI();
        initDataStorage();

    }

    private void loadUI() {
        Log.d(CLASS_TAG, "loadUI");
        setContentView(R.layout.activity_main2_recycler_view);
        editViewLayout = (LinearLayout) findViewById(R.id.editView);
        keyField = (EditText) findViewById(R.id.etKey);
        valueField = (EditText) findViewById(R.id.etValue);
        searchText = (EditText) findViewById(R.id.searchText);
        listView = (RecyclerView) findViewById(R.id.list);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

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

    public void remove(View view) {
        remove();
    }

    public void remove() {
        String key = keyField.getText().toString();
        clear();
        dataStorageManager.remove(key);
    }

    private void populateListView() {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        listAdapter = new RecyclerViewKeyValueAdapter(Collections.emptyList(), this,
                R.layout.list_view_items_flexbox, getOnItemClickListenerToListView());
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
            @Override
            public void onItemClick(KeyValue kv) {
                setEditView(kv.getKey(), kv.getValue());
            }

            @Override
            public boolean onItemLongClick(KeyValue kv) {
                return true;
            }
        };
        return listener;
    }

    private void initDataStorage() {
        dataStorageManager = Factory.getDataStorageIntsance(this,
            getDataStorageType(),
            collectionName, false,
            false, new DataStorageListener() {
                @Override
                public void dataChanged(String key, String value) {
                    Log.d(CLASS_TAG, "dataChanged key: " + key + ", value: " + value);
                    loadDataInListView(dataStorageManager.getValues());
                }

                @Override
                public void dataLoaded(List<KeyValue> data) {
                    if(collectionToAdd != null && !collectionToAdd.isEmpty()) {
                        dataStorageManager.disableSort();
                        dataStorageManager.disableNotifyDataChange();
                        addCollectionToShadaKosh();
                    } else {
                        Log.d(CLASS_TAG, "dataLoaded");
                        loadDataInListView(data);
                    }
                }
            });
        dataStorageManager.loadData();
    }

    private void loadDataInListView(List<KeyValue> data) {
        Log.d(CLASS_TAG, "loadDataInListView");
        runOnUiThread(() -> listAdapter.setData(data));
    }

    private void setEditView(String key, String string) {
        keyField.setText(key);
        valueField.setText(string);
    }

    public void clear(View view) {
        clear();
    }

    public void clear() {
        setEditView("", "");
        keyField.requestFocus();
    }

    public void share() {
        String key = keyField.getText().toString();
        String value = valueField.getText().toString();
        if (!value.isEmpty() && !value.isEmpty()) {
            share(key + "\n" + value);
        }
    }

    private void addCollectionToShadaKosh() {
        if (collectionToAdd != null && !collectionToAdd.isEmpty()) {
            firebaseReadOnceImpl = Factory.getReadOnceDataStorageIntsance(this,
                getDataStorageType(), collectionToAdd,
                new DataStorageListener() {
                    @Override
                    public void dataChanged(String key, String value) {
                    }

                    @Override
                    public void dataLoaded(List<KeyValue> dataOfCollectionToBeAdded) {
                        if (!addingToShabdKosh) {
                            addingToShabdKosh = true;
                            new Thread(()-> updateShabdKosh(dataOfCollectionToBeAdded)).start();
                        }
                    }
                });
        }
    }

    private void updateShabdKosh(List<KeyValue> dataOfCollectionToBeAdded) {
        runOnUiThread(()->keyField.setText("Adding  " + collectionToAdd + " to " + collectionName));
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        for (KeyValue kv : dataOfCollectionToBeAdded) {
            runOnUiThread(()->setEditView(kv.getKey(), kv.getValue()));
            String[] shabds = kv.getValue().split("\\W+");

            for (String shabd : shabds) {
                ShabdaDetails shabdaDetails = new ShabdaDetails();
                int existingValueIndex = dataStorageManager.getKeyIndex(shabd);
                if (existingValueIndex != -1) {
                    KeyValue keyValue = dataStorageManager.getValue(existingValueIndex);
                    if (keyValue.getValue() != null && keyValue.getValue().length() > 0) {
                        shabdaDetails = gson.fromJson(keyValue.getValue(), ShabdaDetails.class);
                    }
                }
                ShabdaUsage shabdaUsage = shabdaDetails.getUsage(collectionToAdd);
                shabdaUsage.setNote(collectionToAdd);
                shabdaUsage.addReference(kv.getKey());
                shabdaDetails.addUsage(shabdaUsage);
                dataStorageManager.save(shabd, gson.toJson(shabdaDetails));
                runOnUiThread(()-> valueField.setText("added  " + shabd));
            }
        }
        runOnUiThread(()-> valueField.setText("Successfully added  " + collectionToAdd + " to " + collectionName));
    }

    @Override
    public void onStop() {
        super.onStop();
        if (dataStorageManager != null) dataStorageManager.removeDataStorageListeners();
        if (firebaseReadOnceImpl != null) firebaseReadOnceImpl.removeDataStorageListeners();
    }
}
