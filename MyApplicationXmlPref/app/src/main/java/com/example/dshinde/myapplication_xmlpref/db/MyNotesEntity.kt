package com.example.dshinde.myapplication_xmlpref.db

import androidx.room.Entity

@Entity(
    tableName = "MyNotes",
    primaryKeys = ["userId", "noteId", "contentId"]
)
data class MyNotesEntity(
    val userId: String,          // e.g. user123
    val noteId: String,          // e.g "Geeta"
    val contentId: String,       // e.g., for note subject, it will be userId
    val content: String?          // raw text or JSON string
)
