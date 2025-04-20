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
interface SequenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSequence(sequence: SequenceEntity): Long

    @Update
    suspend fun updateSequence(sequence: SequenceEntity)

    @Delete
    suspend fun deleteSequence(sequence: SequenceEntity)

    @Query("SELECT * FROM sequences WHERE id = :id")
    suspend fun getSequenceById(id: Long): SequenceEntity?

    @Query("SELECT * FROM sequences ORDER BY createdAt DESC")
    fun getAllSequences(): Flow<List<SequenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSequenceStep(step: SequenceStepEntity): Long

    @Update
    suspend fun updateSequenceStep(step: SequenceStepEntity)

    @Delete
    suspend fun deleteSequenceStep(step: SequenceStepEntity)

    @Query("SELECT * FROM sequence_steps WHERE sequenceId = :sequenceId ORDER BY `order`")
    suspend fun getSequenceStepsBySequenceId(sequenceId: Long): List<SequenceStepEntity>

    @Transaction
    @Query("SELECT * FROM sequences WHERE id = :sequenceId")
    fun getSequenceWithSteps(sequenceId: Long): Flow<SequenceWithSteps>

    @Query("DELETE FROM sequence_steps WHERE sequenceId = :sequenceId")
    suspend fun deleteAllStepsForSequence(sequenceId: Long)

    @Transaction
    suspend fun updateSequenceWithSteps(sequence: SequenceEntity, steps: List<SequenceStepEntity>) {
        updateSequence(sequence)
        deleteAllStepsForSequence(sequence.id)
        steps.forEach { step ->
            insertSequenceStep(step.copy(sequenceId = sequence.id))
        }
    }

    @Query("SELECT * FROM sequences WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveSequences(): Flow<List<SequenceEntity>>
}
