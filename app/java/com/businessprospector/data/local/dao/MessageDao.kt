package com.businessprospector.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.businessprospector.data.local.entity.ContactEntity
import com.businessprospector.data.local.entity.ContactWithMessages
import com.businessprospector.data.local.entity.MessageEntity
import com.businessprospector.data.local.entity.MessageTemplateEntity
import com.businessprospector.data.local.entity.SequenceEntity
import com.businessprospector.data.local.entity.SequenceStepEntity
import com.businessprospector.data.local.entity.SequenceWithSteps
import com.businessprospector.data.local.entity.AnalyticsEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Delete
    suspend fun deleteMessage(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getMessageById(id: Long): MessageEntity?

    @Query("SELECT * FROM messages WHERE contactId = :contactId ORDER BY createdAt DESC")
    fun getMessagesByContact(contactId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE status = :status ORDER BY createdAt DESC")
    fun getMessagesByStatus(status: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE type = :type ORDER BY createdAt DESC")
    fun getMessagesByType(type: String): Flow<List<MessageEntity>>

    @Query("UPDATE messages SET status = :status, updatedAt = :updateTime WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: Long, status: String, updateTime: Date = Date())

    @Query("SELECT * FROM messages WHERE status = 'scheduled' AND sentAt IS NULL")
    suspend fun getPendingMessages(): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessageTemplate(template: MessageTemplateEntity): Long

    @Update
    suspend fun updateMessageTemplate(template: MessageTemplateEntity)

    @Delete
    suspend fun deleteMessageTemplate(template: MessageTemplateEntity)

    @Query("SELECT * FROM message_templates WHERE id = :id")
    suspend fun getMessageTemplateById(id: Long): MessageTemplateEntity?

    @Query("SELECT * FROM message_templates WHERE type = :type ORDER BY createdAt DESC")
    fun getMessageTemplatesByType(type: String): Flow<List<MessageTemplateEntity>>

    @Query("SELECT * FROM message_templates ORDER BY createdAt DESC")
    fun getAllMessageTemplates(): Flow<List<MessageTemplateEntity>>
}
