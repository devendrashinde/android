package com.example.dshinde.myapplication_xmlpref.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.dshinde.myapplication_xmlpref.model.KeyValue

@Dao
interface MyNotesDao {
    // returns all notes names
    @Query("SELECT contentId AS 'key', noteId AS 'value' FROM MyNotes WHERE userId = :userId AND contentId = :userId")
    fun getNotes(userId: String): List<KeyValue>

    // returns all note details of given noteId
    @Query("SELECT contentId AS 'key', content AS 'value' FROM MyNotes WHERE userId = :userId AND noteId = :noteId AND contentId != :userId")
    fun getNoteDetails(noteId:String, userId: String): List<KeyValue>

    @Query("SELECT contentId AS 'key', content AS 'value' FROM MyNotes WHERE contentId = :contentId AND userId = :userId AND noteId = :noteId")
    fun getNoteDetailsItem(noteId:String, contentId: String, userId: String): List<KeyValue>

    // returns all note details where contentId or content contains searchText
    @Query("SELECT noteId AS 'key', noteId || '\n\n' || CASE WHEN contentId = :userId THEN '' ELSE COALESCE(contentId, '') END || '\n\n' || COALESCE(content, '') AS 'value' FROM MyNotes WHERE userId = :userId AND noteId NOT LIKE 'screenDesign:%' AND noteId NOT LIKE 'mediaNote:%' AND (noteId like :searchText OR contentId like :searchText OR content like :searchText)")
    fun searchNoteDetails(searchText:String, userId: String): List<KeyValue>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserNote(note: MyNotesEntity)

    @Update
    suspend fun updateUserNote(note: MyNotesEntity)

    @Delete
    suspend fun deleteUserNote(note: MyNotesEntity)

    @Query("DELETE FROM MyNotes WHERE userId = :userId")
    suspend fun clearNotesForUser(userId: String)

    @Query("DELETE FROM MyNotes WHERE userId = :userId AND noteId = :noteId")
    suspend fun clearNotesForUserAndNoteId(userId: String, noteId: String)
}