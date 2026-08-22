package com.example.dshinde.myapplication_xmlpref.services

import android.content.Context
import android.os.Process
import android.util.Log
import com.example.dshinde.myapplication_xmlpref.db.AppDatabase
import com.example.dshinde.myapplication_xmlpref.db.MyNotesDao
import com.example.dshinde.myapplication_xmlpref.db.MyNotesRepository
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageTransactionWorker
import com.example.dshinde.myapplication_xmlpref.model.KeyValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RoomDbDataStorageManagerImpl @JvmOverloads constructor(
    context: Context,
    collectionName: String,
    autoKey: Boolean,
    descendingOrder: Boolean = false,
    dataStorageListener: DataStorageListener? = null,
    private val syncToFirebase: Boolean = true
) : DataStorageManager() {
    private var context : Context
    var collectionName: String? = null
    var collectionItemId: String? = null
    private var mAuth: FirebaseAuth? = null
    private var mDatabase: DatabaseReference? = null
    private var repository: MyNotesRepository? = null
    private lateinit var myNotesDao: MyNotesDao
    private var valueEventListener: ValueEventListener? = null
    private var childEventListener: ChildEventListener? = null
    // Add FireDbDataStorageManagerImpl instance
    private var fireDbManager: FireDbDataStorageManagerImpl? = null

    init {
        var collection = collectionName
        if (collectionName.contains("/")) {
            this.collectionItemId =
                collectionName.split("/").dropLastWhile { it.isEmpty() }
                    .toTypedArray()[1]
            collection = collectionName.split("/").dropLastWhile { it.isEmpty() }
                .toTypedArray()[0]
        }
        this.context = context
        this.collectionName = collection
        this.autoKey = autoKey
        this.descendingOrder = descendingOrder
        this.addDataStorageListener(dataStorageListener)
        // Initialize FireDbDataStorageManagerImpl
        fireDbManager = FireDbDataStorageManagerImpl(
            collection,
            autoKey,
            descendingOrder,
            dataStorageListener
        )
        databaseCollectionReference
    }

    private val databaseCollectionReference: Unit
        get() {
            Log.d(CLASS_TAG, "getDatabaseCollectionReference")
            val db = AppDatabase.getInstance(context = this.context)
            myNotesDao = db.myNotesDao()
            mAuth = FirebaseAuth.getInstance()
            repository = mAuth!!.uid?.let { MyNotesRepository(myNotesDao, it) }
        }

    override fun loadData() {
        Log.d(CLASS_TAG, "loadData")
        Thread {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
            Log.d(CLASS_TAG, "addReadDataOnce->onDataChange")
            repository?.let { loadAllData(it.getNotes(if(autoKey) null else collectionName, collectionItemId)) }
        }.start()
    }

    private fun loadAllData(collection: List<KeyValue>) {
        Log.d(CLASS_TAG, "loadAllData received " + collection.size + " records")
        data.clear()
        if (collectionItemId != null && !collectionItemId!!.isEmpty()) {
            if(collection.isNotEmpty()) {
                loadItem(collection[0])
            }
            collectionItemId = null
        } else {
            for (dataSnapshot in collection) {
                loadItem(dataSnapshot)
            }
        }
        notifyDataChanged()
    }

    private fun loadItem(keyValue: KeyValue) {
        try {
            data.add(keyValue)
        } catch (e: Exception) {
            Log.d(CLASS_TAG, e.message!!)
        }
    }

    private fun notifyDataChanged() {
        notifyDataLoaded()
    }

    override fun remove(key: String) {
        Log.d(CLASS_TAG, "remove record")
        val normalizedKey = key.trim()
        if (normalizedKey.isEmpty()) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository?.deleteNote(KeyValue(normalizedKey, ""), collectionName)
                if (syncToFirebase) {
                    fireDbManager?.remove(normalizedKey)
                }
                removeFromDataSource(key)
                notifyDataChanged()
            } catch (e: Exception) {
                Log.e(CLASS_TAG, "remove failed", e)
            }
        }
    }

    override fun removeAll() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository?.clearNotes(collectionName)
                data.clear()
                if (syncToFirebase) {
                    fireDbManager?.removeAll()
                }
                notifyDataChanged()
            } catch (e: Exception) {
                Log.e(CLASS_TAG, "removeAll failed", e)
            }
        }
    }

    override fun save(key: String?, value: String) {
        updateDB(listOf(KeyValue(key, value)))
    }

    override fun saveTransaction(
        key: String,
        value: String,
        dataStorageTransactionWorker: DataStorageTransactionWorker
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            // Read first (suspend context), compute new value
            val currentList = myNotesDao.getNoteDetailsItem(collectionName ?: "", key, mAuth?.uid ?: "")
            val current = currentList.firstOrNull()
            val oldValue = current?.value
            val newValue: String? = dataStorageTransactionWorker.updateTransactionData(oldValue)

            if (newValue != null) {
                if (current != null) {
                    repository?.updateNote(KeyValue(key, newValue), collectionName)
                } else {
                    repository?.insertNote(KeyValue(key, newValue), collectionName)
                }
                if (syncToFirebase) {
                    fireDbManager?.save(key, newValue)
                }
            }
        }
    }

    override fun save(values: List<KeyValue>) {
        updateDB(values)
    }

    private fun updateDB(values: List<KeyValue>) {
        Log.d(CLASS_TAG, "updating DB")
        CoroutineScope(Dispatchers.IO).launch {
            for (kv in values) {
                val noKey = kv.key == null || kv.key.isEmpty()
                val newKv = if (autoKey) {
                    if (noKey) kv.key = mAuth?.currentUser?.uid
                    KeyValue(kv.key, kv.value)
                } else {
                    kv
                }

                updateDataSource(newKv.key, newKv.value)
                repository?.insertNote(newKv, if (autoKey) null else (collectionName ?: ""))

                if (syncToFirebase) {
                    fireDbManager?.save(if (noKey) null else newKv.key, newKv.value)
                }
            }
            notifyDataChanged()
        }
    }

    override fun getValue(index: Int): KeyValue {
        return data[index]
    }

    override fun getValues(): List<KeyValue> {
        return data
    }

    override fun getDataString(collectionName: String): String {
        return getDataMap(collectionName).toString()
    }

    override fun getDataMap(collectionName: String): Map<String, String> {
        val dataMap: MutableMap<String, String> = HashMap()
        for (keyValue in data) {
            dataMap[keyValue.key] = keyValue.value
        }
        return dataMap
    }

    override fun searchNoteDetails(searchText: String?): MutableList<KeyValue> {

        val query = searchText?.trim().orEmpty()
        val uid = mAuth?.uid.orEmpty()
        return try {
            val result = repository?.searchNoteDetails(query)
                ?.toMutableList()
                ?: mutableListOf()

            result
        } catch (e: Exception) {
            Log.e(CLASS_TAG, "searchNoteDetails failed", e)
            mutableListOf()
        }
    }


    override fun loadData(data: Map<String, String>, removeExistingData: Boolean) {
        if (removeExistingData) {
            removeAll()
        }
        val values: MutableList<KeyValue> = ArrayList()
        for ((key, value) in data) {
            values.add(KeyValue(key, value))
        }
        save(values)
    }

    override fun removeDataStorageListeners() {
        super.removeDataStorageListeners()
        if (valueEventListener != null) {
            mDatabase!!.removeEventListener(valueEventListener!!)
        }
        if (childEventListener != null) {
            mDatabase!!.removeEventListener(childEventListener!!)
        }
    }

    companion object {
        private const val CLASS_TAG = "FireDataStorageManager"
    }
}
