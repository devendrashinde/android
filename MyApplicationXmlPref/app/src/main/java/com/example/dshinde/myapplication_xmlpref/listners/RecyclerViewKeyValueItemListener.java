package com.example.dshinde.myapplication_xmlpref.listners;

import com.example.dshinde.myapplication_xmlpref.model.KeyValue;

public interface RecyclerViewKeyValueItemListener {
    void onItemClick(KeyValue keyValue);
    boolean onItemLongClick(KeyValue keyValue);
}
