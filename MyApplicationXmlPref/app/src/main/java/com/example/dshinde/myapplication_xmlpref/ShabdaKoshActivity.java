package com.example.dshinde.myapplication_xmlpref;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.helper.Factory;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.model.CafeItem;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.example.dshinde.myapplication_xmlpref.model.ShabdaDetails;
import com.example.dshinde.myapplication_xmlpref.model.ShabdaUsage;
import com.example.dshinde.myapplication_xmlpref.services.DataStorage;
import com.example.dshinde.myapplication_xmlpref.services.ReadOnceDataStorage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

public class ShabdaKoshActivity extends BaseActivity {

    EditText keyField;
    EditText value1Field;
    DataStorage dataStorageManager;
    ReadOnceDataStorage firebaseReadOnceImpl;
    String collectionName = null;
    String collectionToAdd = null;
    boolean addingToShabdKosh = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        keyField = (EditText) findViewById(R.id.etKey);
        value1Field = (EditText) findViewById(R.id.etValue);
        Bundle bundle = getIntent().getExtras();
        collectionName = Constants.SHABDA_KOSH;
        userId = bundle.getString("userId");
        collectionToAdd = bundle.getString("collectionToAddToShabdaKosh");
        setTitle(collectionName);

        initDataStorage();

    }

    private void initDataStorage() {
        dataStorageManager = Factory.getDataStorageIntsance(this,
                getDataStorageType(),
                collectionName, false,
                false, new DataStorageListener() {
                    @Override
                    public void dataChanged(String key, String value) {
                    }

                    @Override
                    public void dataLoaded(List<KeyValue> data) {
                        dataStorageManager.disableSort();
                        dataStorageManager.disableNotifyDataChange();
                        addToShadaKosh();
                    }
                });
        dataStorageManager.loadData();
    }

    private void setEditView(String key, String string) {
        keyField.setText(key);
        value1Field.setText(string);
    }

    public void clear(View view) {
        clear();
    }

    public void clear() {
        setEditView("", "");
        keyField.requestFocus();
    }

    private void addToShadaKosh() {
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
                            Gson gson = new GsonBuilder().setPrettyPrinting().create();
                            for (KeyValue kv : dataOfCollectionToBeAdded) {
                                setEditView(kv.getKey(), kv.getValue());
                                String[] shabds = kv.getValue().split("\\W+");

                                for (String shabd : shabds) {
                                    ShabdaUsage shabdaUsage = null;
                                    ShabdaDetails shabdaDetails = new ShabdaDetails();
                                    int existingValueIndex = dataStorageManager.getKeyIndex(shabd);
                                    if (existingValueIndex != -1) {
                                        KeyValue keyValue = dataStorageManager.getValue(existingValueIndex);
                                        if (keyValue.getValue() != null && keyValue.getValue().length() > 0) {
                                            shabdaDetails = gson.fromJson(keyValue.getValue(), ShabdaDetails.class);
                                        }
                                    }
                                    shabdaUsage = shabdaDetails.getUsage(collectionToAdd);
                                    shabdaUsage.setNote(collectionToAdd);
                                    shabdaUsage.addReference(kv.getKey());
                                    shabdaDetails.addUsage(shabdaUsage);
                                    dataStorageManager.save(shabd, gson.toJson(shabdaDetails));
                                }
                            }
                            clear();
                            value1Field.setText("Successfully added  " + collectionToAdd + " to " + collectionName);
                        }
                    }
                });
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (dataStorageManager != null) dataStorageManager.removeDataStorageListeners();
        if (firebaseReadOnceImpl != null) firebaseReadOnceImpl.removeDataStorageListeners();
    }
}
