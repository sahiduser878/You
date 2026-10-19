package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val category: String, // "Preference", "Fact", "Relationship", "Work", "Habit"
    val timestamp: Long = System.currentTimeMillis(),
    val isSensitive: Boolean = false
)
