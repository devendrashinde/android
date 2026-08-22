package com.example.dshinde.myapplication_xmlpref.services

import android.content.Context
import com.example.dshinde.myapplication_xmlpref.db.AppDatabase
import com.example.dshinde.myapplication_xmlpref.db.MyNotesDao
import com.example.dshinde.myapplication_xmlpref.db.MyNotesRepository
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener
import com.example.dshinde.myapplication_xmlpref.model.KeyValue
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RoomReadWriteOnceDataStorageImpl(
    context: Context,
    private val noteId: String?,
    private val dataStorageListener: DataStorageListener? = null
) : ReadWriteOnceDataStorage {
    private val myNotesDao: MyNotesDao
    private val repository: MyNotesRepository
    private var data: List<KeyValue> = emptyList()
    private var listeners: MutableList<DataStorageListener> = mutableListOf()
    private val resolvedUserId: String

    init {
        // Always resolve userId from FirebaseAuth
        resolvedUserId = FirebaseAuth.getInstance().uid ?: ""
        val db = AppDatabase.getInstance(context)
        myNotesDao = db.myNotesDao()
        repository = MyNotesRepository(myNotesDao, resolvedUserId)
        loadData()
    }

    private fun loadData() {
        CoroutineScope(Dispatchers.IO).launch {
            data = repository.getNotes(noteId)
            dataStorageListener?.dataLoaded(data)
            listeners.forEach { it.dataLoaded(data) }
        }
    }

    override fun getKeyIndex(key: String): Int {
        return data.indexOfFirst { it.key == key }
    }

    override fun getValue(index: Int): KeyValue {
        return data[index]
    }

    override fun getValue(key: String): String? {
        return data.find { it.key == key }?.value
    }

    override fun getValues(): List<KeyValue> {
        return data
    }

    override fun addDataStorageListener(listener: DataStorageListener) {
        listeners.add(listener)
    }

    override fun removeDataStorageListener(listener: DataStorageListener) {
        listeners.remove(listener)
    }

    override fun removeDataStorageListeners() {
        listeners.clear()
    }
}
