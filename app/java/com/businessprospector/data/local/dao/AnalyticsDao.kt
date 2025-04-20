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
interface AnalyticsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalyticsEvent(event: AnalyticsEntity): Long

    @Query("SELECT * FROM analytics WHERE contactId = :contactId ORDER BY timestamp DESC")
    fun getAnalyticsForContact(contactId: Long): Flow<List<AnalyticsEntity>>

    @Query("SELECT * FROM analytics WHERE sequenceId = :sequenceId ORDER BY timestamp DESC")
    fun getAnalyticsForSequence(sequenceId: Long): Flow<List<AnalyticsEntity>>

    @Query("SELECT * FROM analytics WHERE event = :eventType ORDER BY timestamp DESC")
    fun getAnalyticsByEventType(eventType: String): Flow<List<AnalyticsEntity>>

    @Query("SELECT COUNT(*) FROM analytics WHERE event = :eventType AND timestamp BETWEEN :startDate AND :endDate")
    suspend fun countEventsBetweenDates(eventType: String, startDate: Date, endDate: Date): Int

    @Query("SELECT COUNT(*) FROM analytics WHERE event = 'response_received' AND contactId IN (SELECT id FROM contacts WHERE status = 'contacted')")
    suspend fun getResponseRate(): Int

    @Query("SELECT AVG((SELECT MIN(a2.timestamp) FROM analytics a2 WHERE a2.contactId = a1.contactId AND a2.event = 'response_received' AND a2.timestamp > a1.timestamp) - a1.timestamp) FROM analytics a1 WHERE a1.event = 'email_sent'")
    suspend fun getAverageResponseTime(): Long?
}