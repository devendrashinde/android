package com.example.dshinde.myapplication_xmlpref.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.common.DataStorageType;
import com.example.dshinde.myapplication_xmlpref.helper.Factory;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageTransactionWorker;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AddNoteToDictionaryWorker extends Worker {

    private static final String TAG = AddNoteToDictionaryWorker.class.getName();

    DataStorage dictionary;
    String collectionToAdd;
    String dictionaryName;

    String lastWord = null;
    public AddNoteToDictionaryWorker(
            @NonNull Context appContext,
            @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        collectionToAdd = getInputData().getString(Constants.PARAM_NOTE);
        dictionaryName = getInputData().getString(Constants.PARAM_DICTIONARY);
        addToDictionary();
        return Result.success();
    }

    private void addToDictionary() {
        Log.d(TAG, "Adding  " + collectionToAdd + " to " + dictionaryName);
        dictionary = Factory.getDataStorageInstance(getApplicationContext(),
                DataStorageType.FIREBASE_DB,
                dictionaryName, false,
                false, new DataStorageListener() {
                    @Override
                    public void dataChanged(String key, String value) {
                    }

                    @Override
                    public void dataLoaded(List<KeyValue> data) {
                        dictionary.disableSort();
                        dictionary.disableNotifyDataChange();
                        Log.d(TAG, "Adding  " + collectionToAdd + " to " + dictionaryName);
                        getDataOfCollectionToBeAdded();
                    }
                });
        dictionary.disableSort();
        dictionary.disableNotifyDataChange();
        Log.d(TAG, "Adding  " + collectionToAdd + " to " + dictionaryName);
        getDataOfCollectionToBeAdded();
        //dictionary.loadData();
    }

    private void getDataOfCollectionToBeAdded() {
        Factory.getReadOnceFireDataStorageInstance(collectionToAdd,
            new DataStorageListener() {
                @Override
                public void dataChanged(String key, String value) {
                }

                @Override
                public void dataLoaded(List<KeyValue> dataOfCollectionToBeAdded) {
                    updateShabdaKosh(dataOfCollectionToBeAdded);
                }
            });
    }

    private void getDataOfWordFromDictionary(String reference, String word) {
        Factory.getReadOnceFireDataStorageInstance(
            dictionaryName + "/" + word,
            new DataStorageListener() {
                @Override
                public void dataChanged(String key, String value) {
                }

                @Override
                public void dataLoaded(List<KeyValue> wordDetails) {
                    addWordToDictionary(reference, word, wordDetails);
                }
            });
    }

    private void updateShabdaKosh(List<KeyValue> dataOfCollectionToBeAdded) {
        for (KeyValue kv : dataOfCollectionToBeAdded) {
            String[] words = kv.getValue().split("\\W+");
            for (String word : words) {
                word = word.trim().replaceAll("[,|\\d]","");
                if(!word.isEmpty()) {
                    addWordToDictionary(kv.getKey(), word);
                }
            }
        }
        Log.d(TAG, "Successfully added  " + collectionToAdd + " to " + dictionaryName);
    }

    private int getKeyIndex(List<KeyValue> wordDetails, String key) {
        Optional<KeyValue> kv = getKeyValue(wordDetails, key);
        return kv.map(wordDetails::indexOf).orElse(-1);
    }

    private Optional< KeyValue> getKeyValue (List<KeyValue> wordDetails, String key){
        return wordDetails.stream().filter(x -> x.getKey().equals(key)).findFirst();
    }

    private void addWordToDictionary(String reference, String word) {
        Gson gson = new GsonBuilder().create();
        Map<String, List<String>> shabdaDetails = new HashMap<>();
        dictionary.saveTransaction(word, reference, new DataStorageTransactionWorker() {
            @Override
            public void updateTransactionData(KeyValue keyValue) {
            }

            @Override
            public String updateTransactionData(String keyValue) {
                Gson gson = new GsonBuilder().create();
                Map<String, List<String>> shabdaDetails = new HashMap<>();
                if (keyValue != null && !keyValue.isEmpty()) {

                    shabdaDetails = gson.fromJson(keyValue, new TypeToken<Map<String, List<String>>>() {
                    }.getType());
                }
                if (!shabdaDetails.containsKey(collectionToAdd)) {
                    shabdaDetails.put(collectionToAdd, new ArrayList<>());
                }
                List<String> wordUsage = shabdaDetails.getOrDefault(collectionToAdd, new ArrayList<>());
                assert wordUsage != null;
                if (!wordUsage.contains(reference)) {
                    wordUsage.add(reference);
                }
                shabdaDetails.replace(collectionToAdd, wordUsage);
                return gson.toJson(shabdaDetails);
            }
        });
    }

    private void addWordToDictionary(String reference, String word, List<KeyValue> wordDetails) {
        Gson gson = new GsonBuilder().create();
        Map<String, List<String>> shabdaDetails = new HashMap<>();
        int existingValueIndex = getKeyIndex(wordDetails, word);
        if (existingValueIndex != -1) {
            Log.d(TAG, "Adding word " + word);
            KeyValue keyValue = wordDetails.get(existingValueIndex);
            if (keyValue.getValue() != null && !keyValue.getValue().isEmpty()) {
                shabdaDetails = gson.fromJson(keyValue.getValue(), new TypeToken<Map<String, List<String>>>() {
                }.getType());
            }
        }

        if (!shabdaDetails.containsKey(collectionToAdd)) {
            shabdaDetails.put(collectionToAdd, new ArrayList<>());
        }
        List<String> wordUsage = shabdaDetails.getOrDefault(collectionToAdd, new ArrayList<>());
        assert wordUsage != null;
        if (!wordUsage.contains(reference)) {
            wordUsage.add(reference);
        }
        shabdaDetails.replace(collectionToAdd, wordUsage);
        dictionary.save(word, gson.toJson(shabdaDetails));
    }
}
