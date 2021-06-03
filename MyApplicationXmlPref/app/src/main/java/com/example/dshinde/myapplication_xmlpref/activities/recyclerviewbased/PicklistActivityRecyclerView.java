package com.example.dshinde.myapplication_xmlpref.activities.recyclerviewbased;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.activities.BaseActivity;
import com.example.dshinde.myapplication_xmlpref.adapters.MarginItemDecoration;
import com.example.dshinde.myapplication_xmlpref.adapters.RecyclerViewKeyValueAdapter;
import com.example.dshinde.myapplication_xmlpref.helper.Factory;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.listners.RecyclerViewKeyValueItemListener;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.example.dshinde.myapplication_xmlpref.services.DataStorage;
import com.google.gson.GsonBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.example.dshinde.myapplication_xmlpref.common.Constants.DRAWABLE_RIGHT;

public class PicklistActivityRecyclerView extends BaseActivity  {
    EditText valueField;
    RecyclerView listView;
    RecyclerViewKeyValueAdapter listAdapter;
    DataStorage dataStorageManager;
    String dataField = null;
    String dataItem = null;
    String collectionName = null;
    private static final String CLASS_TAG = "PicklistActivityRecyclerView";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(CLASS_TAG, "onCreate");
        getCollectionNameAndDataItemField();
        setTitle(collectionName);
        // Check if user is signed in (non-null) and update UI accordingly.
        loadUI();
        initDataStorageAndLoadData(this);
    }

    private void getCollectionNameAndDataItemField() {
        Bundle bundle = getIntent().getExtras();
        String[] data = bundle.getString("filename").split("/");
        collectionName = data[0];
        if(data.length > 1) {
            dataField = data[1];
        }
        if(data.length > 2) {
            dataItem = data[2];
        }
        userId = bundle.getString("userId");
    }

    private void loadUI() {
        Log.d(CLASS_TAG, "loadUI");
        setContentView(R.layout.activity_picklist_recycler_view);
        valueField = (EditText) findViewById(R.id.VALUE_1);
        listView = (RecyclerView) findViewById(R.id.list);
        populateListView();
        setValueFieldWatcher();
        setValueFieldClearButtonAction();
    }

    private void initDataStorageAndLoadData(Context context) {
        Log.d(CLASS_TAG, "initDataStorageAndLoadData->getDataStorageIntsance");
        dataStorageManager = Factory.getDataStorageIntsance(context,
                getDataStorageType(),
                collectionName,
                false,
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

    public void clear() {
        valueField.setText("");
        valueField.requestFocus();
    }

    private void populateListView() {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        listAdapter = new RecyclerViewKeyValueAdapter(Collections.emptyList(), this,
                R.layout.list_view_items_recyclerview, getOnItemClickListenerToListView());
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
            }
        });
    }

    private RecyclerViewKeyValueItemListener getOnItemClickListenerToListView() {
        RecyclerViewKeyValueItemListener listener = new RecyclerViewKeyValueItemListener() {
            public void onItemClick(KeyValue kv) {
                String value = getSelectedItemsValue(kv);
                Intent intent = new Intent();
                intent.putExtra("data", value);
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public boolean onItemLongClick(KeyValue kv) {
                String value = kv.getValue();
                return true;
            }
        };
        return listener;
    }

    private String getSelectedItemsValue(KeyValue kv) {
        if(dataItem != null) {
            Map<String, String> data = new GsonBuilder().create().fromJson(kv.getValue(), Map.class);
            return data.get(dataItem);
        }
        if(dataField != null && dataField.equalsIgnoreCase("KEY")){
            return kv.getKey();
        }
        return kv.getValue();
    }

    @Override
    public void onStop() {
        super.onStop();
        dataStorageManager.removeDataStorageListeners();
    }

}
