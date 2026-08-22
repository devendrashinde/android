package com.example.dshinde.myapplication_xmlpref.services;

import android.util.Log;

import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.common.DataChangeType;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public abstract class DataStorageManager implements DataStorage {
    private final Object dataLock = new Object();
    private final Object listenersLock = new Object();

    List<DataStorageListener> listeners = new ArrayList<>();
    List<KeyValue> data = new ArrayList<>();
    boolean autoKey = false;
    boolean descendingOrder = false;
    boolean sortData = true;
    boolean notifyDataChange = true;
    private int lastModifiedIndex = -1;
    private DataChangeType lastDataChangeType = DataChangeType.ALL_DATA;

    public void addDataStorageListener(DataStorageListener listener) {
        if (listener != null) {
            synchronized (listenersLock) {
                listeners.add(listener);
            }
        }
    }

    public void removeDataStorageListener(DataStorageListener listener) {
        if (listener != null) {
            synchronized (listenersLock) {
                listeners.remove(listener);
            }
        }
    }

    public void removeDataStorageListeners() {
        synchronized (listenersLock) {
            listeners.clear();
        }
    }

    public int count() {
        synchronized (dataLock) {
            return data.size();
        }
    }

    @Override
    public void loadData() {
    }

    @Override
    public void loadData(String filterPath) {
    }

    private Optional<KeyValue> getKeyValue(String key) {
        synchronized (dataLock) {
            return data.stream()
                    .filter(Objects::nonNull)
                    .filter(x -> autoKey ? x.getValue() != null && x.getValue().equals(key)
                            : x.getKey() != null && x.getKey().equals(key))
                    .findFirst();
        }
    }

    public String[] getKeys() {
        return new String[]{KEY, VALUE};
    }

    public KeyValue getValue(int index) {
        synchronized (dataLock) {
            return data.get(index);
        }
    }

    public List<KeyValue> getValues() {
        synchronized (dataLock) {
            return new ArrayList<>(data);
        }
    }

    public int getKeyIndex(String key) {
        synchronized (dataLock) {
            KeyValue keyValue = getKeyValue(key).orElse(null);
            if (keyValue != null) {
                return data.indexOf(keyValue);
            }
            return -1;
        }
    }

    public String getValue(String key) {
        synchronized (dataLock) {
            int index = getKeyIndex(key);
            if (index >= 0) {
                return data.get(index).getValue();
            }
            return "";
        }
    }

    public int getLastModifiedIndex() {
        synchronized (dataLock) {
            return lastModifiedIndex;
        }
    }

    public DataChangeType getLastDataChangeType() {
        synchronized (dataLock) {
            return lastDataChangeType;
        }
    }

    public void save(String value) {
        save(null, value);
    }

    public void save(List<KeyValue> values) {
    }

    void removeFromDataSource(String key) {
        synchronized (dataLock) {
            int keyIndex = getKeyIndex(key);
            if (keyIndex >= 0) {
                data.remove(keyIndex);
                lastModifiedIndex = keyIndex;
                lastDataChangeType = DataChangeType.DELETED;
            }
        }
    }

    public void updateDataSource(String key, String value) {
        synchronized (dataLock) {
            int keyIndex = getKeyIndex(key);
            if (keyIndex >= 0) {
                data.set(keyIndex, new KeyValue(key, value));
                lastDataChangeType = DataChangeType.MODIFIED;
                lastModifiedIndex = keyIndex;
            } else {
                data.add(new KeyValue(key, value));
                lastDataChangeType = DataChangeType.ADDED;
                lastModifiedIndex = data.size() - 1;
            }
        }
    }

    @Override
    public void disableSort() {
        sortData = false;
    }

    @Override
    public void enableSort() {
        sortData = true;
    }

    @Override
    public void disableNotifyDataChange() {
        notifyDataChange = false;
    }

    @Override
    public void enableNotifyDataChange() {
        notifyDataChange = true;
    }

    @Override
    public void save(String key, String value) {
    }

    void notifyDataSetChanged(String key, String value) {
        if (!notifyDataChange) return;

        Log.d("DataStorageManager", "notifyDataSetChanged");

        List<DataStorageListener> listenersSnapshot;
        synchronized (listenersLock) {
            listenersSnapshot = new ArrayList<>(listeners);
        }

        synchronized (dataLock) {
            if (sortData) Collections.sort(data, keyValueComparator);
        }

        for (DataStorageListener listener : listenersSnapshot) {
            listener.dataChanged(key, value);
        }
    }

    void notifyDataLoaded() {
        if (!notifyDataChange) return;

        Log.d("DataStorageManager", "notifyDataLoaded");

        List<KeyValue> dataSnapshot;
        List<DataStorageListener> listenersSnapshot;

        synchronized (dataLock) {
            KeyValue addedItem = null;
            if (lastDataChangeType == DataChangeType.ADDED && lastModifiedIndex >= 0 && lastModifiedIndex < data.size()) {
                addedItem = data.get(lastModifiedIndex);
            }

            if (sortData) Collections.sort(data, keyValueComparator);
            if (addedItem != null) {
                lastModifiedIndex = data.indexOf(addedItem);
            }

            dataSnapshot = new ArrayList<>(data);
        }

        synchronized (listenersLock) {
            listenersSnapshot = new ArrayList<>(listeners);
        }

        for (DataStorageListener listener : listenersSnapshot) {
            listener.dataLoaded(dataSnapshot);
        }
    }

    Comparator<Map<String, String>> mapComparator = new Comparator<Map<String, String>>() {
        public int compare(Map<String, String> m1, Map<String, String> m2) {
            return autoKey ? (descendingOrder ? m2.get(VALUE).compareTo(m1.get(VALUE)) : m1.get(VALUE).compareTo(m2.get(VALUE)))
                    : (descendingOrder ? m2.get(KEY).compareTo(m1.get(KEY)) : m1.get(KEY).compareTo(m2.get(KEY)));
        }
    };

    Comparator<KeyValue> keyValueComparator = new Comparator<KeyValue>() {
        public int compare(KeyValue m1, KeyValue m2) {
            return autoKey ? (descendingOrder ? m2.getValue().compareTo(m1.getValue()) : m1.getValue().compareTo(m2.getValue()))
                    : (descendingOrder ? m2.getKey().compareTo(m1.getKey()) : m1.getKey().compareTo(m2.getKey()));
        }
    };

    public String getDataString() {
        StringBuilder dataString = new StringBuilder();
        synchronized (dataLock) {
            if (!data.isEmpty()) {
                for (KeyValue entry : data) {
                    dataString.append(Constants.CR_LF)
                            .append(entry.getKey().trim())
                            .append(Constants.CR_LF)
                            .append(entry.getValue().trim());
                }
            }
        }
        return dataString.toString();
    }
}
