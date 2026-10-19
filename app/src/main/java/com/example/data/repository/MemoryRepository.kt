package com.example.data.repository

import com.example.data.local.dao.YesminDao
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.MemoryEntity
import kotlinx.coroutines.flow.Flow

class MemoryRepository(private val dao: YesminDao) {
    val allMemories: Flow<List<MemoryEntity>> = dao.getAllMemories()
    val chatMessages: Flow<List<ChatMessageEntity>> = dao.getAllChatMessages()

    suspend fun insertMemory(title: String, content: String, category: String, isSensitive: Boolean = false): Long {
        return dao.insertMemory(
            MemoryEntity(
                title = title.trim(),
                content = content.trim(),
                category = category,
                isSensitive = isSensitive
            )
        )
    }

    suspend fun updateMemory(memory: MemoryEntity) {
        dao.updateMemory(memory)
    }

    suspend fun deleteMemory(id: Long) {
        dao.deleteMemoryById(id)
    }

    suspend fun clearAllMemories() {
        dao.clearAllMemories()
    }

    fun searchMemories(query: String): Flow<List<MemoryEntity>> {
        return dao.searchMemories(query)
    }

    suspend fun getRecentMemoriesForContext(): List<MemoryEntity> {
        return dao.getRecentMemoriesForContext()
    }

    suspend fun insertChatMessage(sender: String, text: String, isSpoken: Boolean = false, isCodeSnippet: Boolean = false): Long {
        return dao.insertMessage(
            ChatMessageEntity(
                sender = sender,
                text = text,
                isSpoken = isSpoken,
                isCodeSnippet = isCodeSnippet
            )
        )
    }

    suspend fun getRecentChatMessages(limit: Int = 10): List<ChatMessageEntity> {
        return dao.getRecentMessages(limit)
    }

    suspend fun clearChatHistory() {
        dao.clearChatHistory()
    }
}
