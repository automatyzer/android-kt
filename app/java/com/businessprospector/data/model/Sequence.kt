package com.businessprospector.data.model

import java.util.Date

/**
 * Model danych reprezentujący sekwencję komunikacji.
 * Sekwencja składa się z serii kroków komunikacyjnych (np. e-mail, SMS, telefon),
 * które są wykonywane w określonej kolejności z ustalonymi opóźnieniami.
 */
data class Sequence(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val isActive: Boolean = true,
    val steps: List<SequenceStep> = emptyList(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

/**
 * Model danych reprezentujący pojedynczy krok w sekwencji komunikacji.
 */
data class SequenceStep(
    val id: Long = 0,
    val sequenceId: Long,
    val type: String, // "email", "sms", "call"
    val templateId: Long? = null,
    val order: Int,
    val delayDays: Int = 0,
    val delayHours: Int = 0,
    val condition: String? = null, // np. "no_response", "email_opened"
    val isActive: Boolean = true,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

/**
 * Model danych reprezentujący wykonanie sekwencji dla konkretnego kontaktu.
 */
data class SequenceExecution(
    val id: Long = 0,
    val sequenceId: Long,
    val contactId: Long,
    val currentStepIndex: Int = 0,
    val status: String = "in_progress", // "in_progress", "completed", "stopped"
    val startedAt: Date = Date(),
    val completedAt: Date? = null
)

/**
 * Model danych reprezentujący wykonanie kroku sekwencji.
 */
data class StepExecution(
    val id: Long = 0,
    val sequenceExecutionId: Long,
    val sequenceStepId: Long,
    val status: String, // "pending", "executed", "skipped", "failed"
    val executedAt: Date? = null,
    val messageId: Long? = null, // Odniesienie do wysłanej wiadomości
    val result: String? = null, // Wynik wykonania kroku
    val nextScheduledDate: Date? = null // Data zaplanowania następnego kroku
)