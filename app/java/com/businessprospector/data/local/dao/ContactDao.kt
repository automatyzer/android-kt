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
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactEntity>): List<Long>

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Delete
    suspend fun deleteContact(contact: ContactEntity)

    @Query("SELECT * FROM contacts WHERE id = :id")
    suspend fun getContactById(id: Long): ContactEntity?

    @Query("SELECT * FROM contacts ORDER BY createdAt DESC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE status = :status ORDER BY createdAt DESC")
    fun getContactsByStatus(status: String): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE category = :category ORDER BY createdAt DESC")
    fun getContactsByCategory(category: String): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE name LIKE '%' || :query || '%' OR company LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%'")
    suspend fun searchContacts(query: String): List<ContactEntity>

    @Transaction
    @Query("SELECT * FROM contacts WHERE id = :contactId")
    fun getContactWithMessages(contactId: Long): Flow<ContactWithMessages>

    @Query("UPDATE contacts SET status = :newStatus, updatedAt = :updateTime WHERE id = :contactId")
    suspend fun updateContactStatus(contactId: Long, newStatus: String, updateTime: Date = Date())
}

