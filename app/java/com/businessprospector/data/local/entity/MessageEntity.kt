package com.businessprospector.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.Date



@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contactId: Long,
    val type: String, // np. "email", "sms", "call"
    val direction: String, // "outgoing" lub "incoming"
    val content: String,
    val subject: String? = null, // Dla emaili
    val templateId: Long? = null, // Referencja do szablonu, jeśli wiadomość była wygenerowana z szablonu
    val status: String, // np. "draft", "scheduled", "sent", "delivered", "failed"
    val sentAt: Date? = null,
    val deliveredAt: Date? = null,
    val openedAt: Date? = null, // Dla śledzonych emaili
    val respondedAt: Date? = null,
    val errorMessage: String? = null,
    val metadata: Map<String, String> = emptyMap(), // Dodatkowe informacje o wiadomości
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val isEncrypted: Boolean = false
)

@Entity(tableName = "message_templates")
data class MessageTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // np. "email", "sms", "call_script"
    val subject: String? = null, // Dla szablonów emaili
    val content: String,
    val variables: List<String> = emptyList(), // Lista zmiennych w szablonie, np. "{{imię}}", "{{firma}}"
    val category: String? = null, // np. "initial_contact", "follow_up", "meeting_request"
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
