package com.businessprospector.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Encja przechowująca dane analityczne z aplikacji.
 * Każde zdarzenie analityczne (np. wysłana wiadomość, otrzymana odpowiedź) jest zapisywane
 * jako oddzielny rekord w tej tabeli.
 */
@Entity(tableName = "analytics")
data class AnalyticsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Referencje do powiązanych encji (opcjonalne)
    val sequenceId: Long? = null,
    val contactId: Long? = null,
    val messageId: Long? = null,

    // Typ zdarzenia, np. "email_sent", "email_opened", "email_clicked", "response_received"
    val event: String,

    // Opcjonalna wartość zdarzenia, jeśli potrzebna
    val value: String? = null,

    // Dodatkowe metadane zdarzenia w formie mapy klucz-wartość
    val metadata: Map<String, String> = emptyMap(),

    // Czas wystąpienia zdarzenia
    val timestamp: Date = Date()
)