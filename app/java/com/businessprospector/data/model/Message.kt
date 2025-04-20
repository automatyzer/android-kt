
// Domain model for Message
package com.businessprospector.domain.model

import java.util.Date

data class Message(
    val id: Long = 0,
    val contactId: Long,
    val type: String, // "email", "sms", "call"
    val direction: String, // "outgoing" lub "incoming"
    val content: String,
    val subject: String? = null,
    val templateId: Long? = null,
    val status: String, // "draft", "scheduled", "sent", "delivered", "failed"
    val sentAt: Date? = null,
    val deliveredAt: Date? = null,
    val openedAt: Date? = null,
    val respondedAt: Date? = null,
    val errorMessage: String? = null,
    val metadata: Map<String, String> = emptyMap(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

data class MessageTemplate(
    val id: Long = 0,
    val name: String,
    val type: String, // "email", "sms", "call_script"
    val subject: String? = null,
    val content: String,
    val variables: List<String> = emptyList(),
    val category: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)