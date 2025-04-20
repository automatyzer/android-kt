package com.businessprospector.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**

 */

data class SequenceStepEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sequenceId: Long,
    val type: String, // np. "email", "sms", "call"
    val templateId: Long? = null, // Referencja do szablonu wiadomości
    val order: Int, // Kolejność kroku w sekwencji
    val delayDays: Int = 0, // Opóźnienie w dniach od poprzedniego kroku
    val delayHours: Int = 0, // Opóźnienie w godzinach od poprzedniego kroku
    val condition: String? = null, // Warunek wykonania kroku, np. "no_response", "email_opened"
    val isActive: Boolean = true,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

@Entity(tableName = "sequences")
data class SequenceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val isActive: Boolean = true,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

@Entity(
    tableName = "sequence_steps",
    foreignKeys = [
        ForeignKey(
            entity = SequenceEntity::class,
            parentColumns = ["id"],
            childColumns = ["sequenceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sequenceId")]
)
