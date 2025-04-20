package com.businessprospector.data.repository

import android.content.Context
import android.net.Uri
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.FileProvider
import com.businessprospector.data.local.dao.MessageDao
import com.businessprospector.data.local.entity.MessageEntity
import com.businessprospector.domain.model.Contact
import com.businessprospector.domain.model.Message
import com.businessprospector.domain.model.MessageTemplate
import com.businessprospector.domain.service.EncryptionService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.IOException
import java.util.Date
import java.util.Properties
import javax.inject.Inject
import javax.mail.Authenticator
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CommunicationRepository @Inject constructor(
    private val context: Context,
    private val messageDao: MessageDao,
    private val encryptionService: EncryptionService
) {
    private val TAG = "CommunicationRepository"

    // Email configuration
    private var emailSession: Session? = null
    private var emailAddress: String? = null

    // Text-to-Speech engine for call emulation
    private var textToSpeech: TextToSpeech? = null
    private var ttsInitialized = false

    // Get all messages for a contact
    fun getMessagesForContact(contactId: Long): Flow<List<Message>> {
        return messageDao.getMessagesByContact(contactId).map { entities ->
            entities.map { it.toDomain(encryptionService) }
        }
    }

    // Get message templates
    fun getAllMessageTemplates(): Flow<List<MessageTemplate>> {
        return messageDao.getAllMessageTemplates().map { entities ->
            entities.map { MessageTemplate(
                id = it.id,
                name = it.name,
                type = it.type,
                subject = it.subject,
                content = it.content,
                variables = it.variables,
                category = it.category,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt
            ) }
        }
    }

    // Configure email settings
    fun configureEmail(
        smtpServer: String,
        port: Int,
        username: String,
        password: String,
        useTls: Boolean = true
    ) {
        val props = Properties()
        props["mail.smtp.host"] = smtpServer
        props["mail.smtp.port"] = port.toString()
        props["mail.smtp.auth"] = "true"

        if (useTls) {
            props["mail.smtp.starttls.enable"] = "true"
        }

        emailSession = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })

        emailAddress = username
    }

    // Initialize Text-to-Speech engine
    suspend fun initializeTextToSpeech(): Boolean {
        if (ttsInitialized) return true

        return suspendCancellableCoroutine { continuation ->
            textToSpeech = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    ttsInitialized = true
                    continuation.resume(true)
                } else {
                    Log.e(TAG, "Failed to initialize TextToSpeech: $status")
                    continuation.resume(false)
                }
            }

            continuation.invokeOnCancellation {
                textToSpeech?.shutdown()
            }
        }
    }

    // Send email message
    suspend fun sendEmail(contact: Contact, subject: String, content: String): Result<Message> {
        if (emailSession == null || emailAddress == null) {
            return Result.failure(IllegalStateException("Email not configured. Call configureEmail first."))
        }

        if (contact.email.isNullOrBlank()) {
            return Result.failure(IllegalArgumentException("Contact email is required"))
        }

        return try {
            val message = javax.mail.internet.MimeMessage(emailSession)
            message.setFrom(InternetAddress(emailAddress))
            message.setRecipients(
                javax.mail.Message.RecipientType.TO,
                InternetAddress.parse(contact.email)
            )
            message.subject = subject
            message.setText(content, "UTF-8", "html")

            Transport.send(message)

            // Create a message record
            val messageEntity = MessageEntity(
                contactId = contact.id,
                type = "email",
                direction = "outgoing",
                subject = subject,
                content = encryptionService.encrypt(content),
                status = "sent",
                sentAt = Date(),
                createdAt = Date(),
                updatedAt = Date(),
                isEncrypted = true
            )

            val messageId = messageDao.insertMessage(messageEntity)

            Result.success(
                Message(
                    id = messageId,
                    contactId = contact.id,
                    type = "email",
                    direction = "outgoing",
                    subject = subject,
                    content = content,
                    status = "sent",
                    sentAt = Date(),
                    createdAt = Date(),
                    updatedAt = Date()
                )
            )
        } catch (e: MessagingException) {
            Log.e(TAG, "Error sending email: ${e.message}", e)

            // Create a failed message record
            val messageEntity = MessageEntity(
                contactId = contact.id,
                type = "email",
                direction = "outgoing",
                subject = subject,
                content = encryptionService.encrypt(content),
                status = "failed",
                errorMessage = e.message,
                createdAt = Date(),
                updatedAt = Date(),
                isEncrypted = true
            )

            messageDao.insertMessage(messageEntity)

            Result.failure(e)
        }
    }

    // Send SMS message
    suspend fun sendSms(contact: Contact, content: String): Result<Message> {
        if (contact.phone.isNullOrBlank()) {
            return Result.failure(IllegalArgumentException("Contact phone number is required"))
        }

        return try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            // Split message if it's too long
            val parts = smsManager.divideMessage(content)
            smsManager.sendMultipartTextMessage(
                contact.phone,
                null,
                parts,
                null,
                null
            )

            // Create a message record
            val messageEntity = MessageEntity(
                contactId = contact.id,
                type = "sms",
                direction = "outgoing",
                content = encryptionService.encrypt(content),
                status = "sent",
                sentAt = Date(),
                createdAt = Date(),
                updatedAt = Date(),
                isEncrypted = true
            )

            val messageId = messageDao.insertMessage(messageEntity)

            Result.success(
                Message(
                    id = messageId,
                    contactId = contact.id,
                    type = "sms",
                    direction = "outgoing",
                    content = content,
                    status = "sent",
                    sentAt = Date(),
                    createdAt = Date(),
                    updatedAt = Date()
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error sending SMS: ${e.message}", e)

            // Create a failed message record
            val messageEntity = MessageEntity(
                contactId = contact.id,
                type = "sms",
                direction = "outgoing",
                content = encryptionService.encrypt(content),
                status = "failed",
                errorMessage = e.message,
                createdAt = Date(),
                updatedAt = Date(),
                isEncrypted = true
            )

            messageDao.insertMessage(messageEntity)

            Result.failure(e)
        }
    }

    // Make a call with TTS (emulation)
    suspend fun makeCall(contact: Contact, script: String): Result<Message> {
        if (contact.phone.isNullOrBlank()) {
            return Result.failure(IllegalArgumentException("Contact phone number is required"))
        }

        if (!ttsInitialized) {
            val initialized = initializeTextToSpeech()
            if (!initialized) {
                return Result.failure(IllegalStateException("Failed to initialize Text-to-Speech engine"))
            }
        }

        return try {
            // Generate audio file from script
            val audioFile = generateSpeechAudio(script)

            // Create content URI for the audio file
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                audioFile
            )

            // Create a message record
            val messageEntity = MessageEntity(
                contactId = contact.id,
                type = "call",
                direction = "outgoing",
                content = encryptionService.encrypt(script),
                status = "ready", // Not actually sent yet, just prepared
                createdAt = Date(),
                updatedAt = Date(),
                isEncrypted = true,
                metadata = mapOf("audio_uri" to contentUri.toString())
            )

            val messageId = messageDao.insertMessage(messageEntity)

            Result.success(
                Message(
                    id = messageId,
                    contactId = contact.id,
                    type = "call",
                    direction = "outgoing",
                    content = script,
                    status = "ready",
                    createdAt = Date(),
                    updatedAt = Date(),
                    metadata = mapOf("audio_uri" to contentUri.toString())
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error preparing call: ${e.message}", e)

            // Create a failed message record
            val messageEntity = MessageEntity(
                contactId = contact.id,
                type = "call",
                direction = "outgoing",
                content = encryptionService.encrypt(script),
                status = "failed",
                errorMessage = e.message,
                createdAt = Date(),
                updatedAt = Date(),
                isEncrypted = true
            )

            messageDao.insertMessage(messageEntity)

            Result.failure(e)
        }
    }

    // Helper function to generate speech audio file
    private suspend fun generateSpeechAudio(text: String): File {
        return suspendCancellableCoroutine { continuation ->
            try {
                val file = File(context.cacheDir, "call_script_${System.currentTimeMillis()}.wav")

                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        // Nothing to do
                    }

                    override fun onDone(utteranceId: String?) {
                        if (utteranceId == "speechFile") {
                            continuation.resume(file)
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        continuation.resumeWithException(IOException("TTS error occurred"))
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        continuation.resumeWithException(IOException("TTS error occurred: $errorCode"))
                    }
                })

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val bundle = Bundle()
                    bundle.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "speechFile")
                    textToSpeech?.synthesizeToFile(text, bundle, file, "speechFile")
                } else {
                    @Suppress("DEPRECATION")
                    val params = HashMap<String, String>()
                    params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "speechFile"
                    textToSpeech?.synthesizeToFile(text, params, file.absolutePath)
                }

            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }

            continuation.invokeOnCancellation {
                textToSpeech?.stop()
            }
        }
    }

    // Update message status
    suspend fun updateMessageStatus(messageId: Long, newStatus: String) {
        messageDao.updateMessageStatus(messageId, newStatus)
    }

    // Get pending messages that need to be sent
    suspend fun getPendingMessages(): List<Message> {
        return messageDao.getPendingMessages().map { it.toDomain(encryptionService) }
    }

    // Helper functions for mapping between domain and entity
    private fun MessageEntity.toDomain(encryptionService: EncryptionService): Message {
        // Decrypt content if it's encrypted
        val decryptedContent = if (isEncrypted) {
            encryptionService.decrypt(content)
        } else {
            content
        }

        return Message(
            id = id,
            contactId = contactId,
            type = type,
            direction = direction,
            subject = subject,
            content = decryptedContent,
            templateId = templateId,
            status = status,
            sentAt = sentAt,
            deliveredAt = deliveredAt,
            openedAt = openedAt,
            respondedAt = respondedAt,
            errorMessage = errorMessage,
            metadata = metadata,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
