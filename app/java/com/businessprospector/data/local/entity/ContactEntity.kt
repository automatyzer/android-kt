package com.businessprospector.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.Date

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val title: String? = null,
    val company: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val website: String? = null,
    val linkedInUrl: String? = null,
    val source: String? = null,  // np. "Google Search", "LinkedIn", "Manual"
    val sourceDetails: String? = null, // np. wyszukiwane hasło
    val notes: String? = null,
    val category: String? = null, // np. "high_potential", "medium_potential", "low_potential"
    val status: String = "new", // np. "new", "contacted", "responded", "meeting_scheduled", "deal", "not_interested"
    val tags: List<String> = emptyList(),
    val contextData: Map<String, String> = emptyMap(), // Dodatkowe dane kontekstowe o kontakcie
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val isEncrypted: Boolean = false // Flaga określająca czy wrażliwe pola są zaszyfrowane
)



// Klasy relacyjne dla łatwiejszej pracy z danymi
data class ContactWithMessages(
    @androidx.room.Embedded
    val contact: ContactEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "contactId"
    )
    val messages: List<MessageEntity>
)

data class SequenceWithSteps(
    @androidx.room.Embedded
    val sequence: SequenceEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "sequenceId"
    )
    val steps: List<SequenceStepEntity>
)
