package com.example.dshinde.myapplication_xmlpref.db

import com.example.dshinde.myapplication_xmlpref.common.Constants
import com.example.dshinde.myapplication_xmlpref.helper.Factory
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener
import com.example.dshinde.myapplication_xmlpref.model.KeyValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyNotesRepository(
    private val notesDao: MyNotesDao,
    private val userId: String
) {

    fun getNotes(noteId: String? = null, contentId: String? = null): List<KeyValue> =
        if (noteId == null) {
            notesDao.getNotes(userId)
        } else if (contentId == null) {
            notesDao.getNoteDetails(noteId, userId)
        } else {
            notesDao.getNoteDetailsItem(noteId, contentId, userId)
        }

    suspend fun insertNote(keyValue: KeyValue, noteId: String? = null) {
        notesDao.insertUserNote(
            myNotesEntity(noteId, keyValue)
        )
    }

    private fun myNotesEntity(
        noteId: String?,
        keyValue: KeyValue
    ) = MyNotesEntity(
        userId = userId,
        noteId = noteId ?: keyValue.value, // if noteId is null, then this is note name, so we will use keyValue.value as noteId
        contentId = if (noteId == null) userId else keyValue.key,
        content = if (noteId == null) null else keyValue.value
    )
    /*
    How record looks in database:
    userId | noteId | contentId | content
    --------------------------------------
    user1  | note1  | user1     | null
    user1  | note1  | content1  | value1
    user1  | note1  | content2  | value2
     */

    suspend fun updateNote(keyValue: KeyValue, noteId: String? = null) {

        notesDao.updateUserNote(
            myNotesEntity(noteId, keyValue)
        )
    }

    suspend fun deleteNote(keyValue: KeyValue, noteId: String? = null) {

        notesDao.deleteUserNote(myNotesEntity(noteId, keyValue))
    }

    suspend fun syncMyNotesFromFirebase() {
        Factory.getReadOnceFireDataStorageInstance(
            Constants.DATABASE_PATH_NOTES,
            object : DataStorageListener {
                override fun dataChanged(key: String, value: String) {
                }

                override fun dataLoaded(data: List<KeyValue>) {
                    data.forEach { keyValue ->
                        CoroutineScope(Dispatchers.IO).launch {
                            insertNote(keyValue)
                            syncMyNoteDetailsFromFirebase(keyValue.key)
                        }
                    }
                }
            });

    }

    suspend fun syncMyNoteDetailsFromFirebase(noteId: String) {
        Factory.getReadOnceFireDataStorageInstance(
            noteId,
            object : DataStorageListener {
                override fun dataChanged(key: String, value: String) {
                }

                override fun dataLoaded(data: List<KeyValue>) {
                    data.forEach { keyValue ->
                        CoroutineScope(Dispatchers.IO).launch {
                            insertNote(keyValue)
                        }
                    }
                }
            });
    }

    suspend fun clearNotes(noteId: String? = null) {
        if (noteId == null) {
            notesDao.clearNotesForUser(userId)
        } else {
            notesDao.clearNotesForUserAndNoteId(userId, noteId)
        }
    }

    suspend fun insertNotes(notes: List<KeyValue>, noteId: String? = null) {
        for (keyValue in notes) {
            insertNote(keyValue, noteId)
        }
    }

    fun searchNoteDetails(query: String): List<KeyValue> =
        notesDao.searchNoteDetails("%${query.trim()}%", userId)

}