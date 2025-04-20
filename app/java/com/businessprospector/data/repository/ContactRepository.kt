package com.businessprospector.data.repository

import android.util.Log
import com.businessprospector.data.local.dao.ContactDao
import com.businessprospector.data.local.entity.ContactEntity
import com.businessprospector.data.local.entity.ContactWithMessages
import com.businessprospector.data.remote.api.GoogleSearchApi
import com.businessprospector.data.remote.dto.GoogleSearchResponse
import com.businessprospector.domain.model.Contact
import com.businessprospector.domain.model.SearchQuery
import com.businessprospector.domain.service.EncryptionService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.HttpException
import java.io.IOException
import java.util.Date
import javax.inject.Inject

class ContactRepository @Inject constructor(
    private val contactDao: ContactDao,
    private val encryptionService: EncryptionService
) {
    fun getAllContacts(): Flow<List<Contact>> {
        return contactDao.getAllContacts().map { entities ->
            entities.map { it.toDomain(encryptionService) }
        }
    }

    fun getContactsByStatus(status: String): Flow<List<Contact>> {
        return contactDao.getContactsByStatus(status).map { entities ->
            entities.map { it.toDomain(encryptionService) }
        }
    }

    fun getContactsByCategory(category: String): Flow<List<Contact>> {
        return contactDao.getContactsByCategory(category).map { entities ->
            entities.map { it.toDomain(encryptionService) }
        }
    }

    fun getContactWithMessages(contactId: Long): Flow<ContactWithMessages> {
        return contactDao.getContactWithMessages(contactId)
    }

    suspend fun getContactById(id: Long): Contact? {
        return contactDao.getContactById(id)?.toDomain(encryptionService)
    }

    suspend fun searchContacts(query: String): List<Contact> {
        return contactDao.searchContacts(query).map { it.toDomain(encryptionService) }
    }

    suspend fun insertContact(contact: Contact): Long {
        val entity = contact.toEntity(encryptionService)
        return contactDao.insertContact(entity)
    }

    suspend fun updateContact(contact: Contact) {
        val entity = contact.toEntity(encryptionService)
        contactDao.updateContact(entity)
    }

    suspend fun updateContactStatus(contactId: Long, newStatus: String) {
        contactDao.updateContactStatus(contactId, newStatus)
    }

    suspend fun deleteContact(contact: Contact) {
        val entity = contact.toEntity(encryptionService)
        contactDao.deleteContact(entity)
    }

    // Helper extension functions for mapping between domain and entity
    private fun ContactEntity.toDomain(encryptionService: EncryptionService): Contact {
        // Decrypt sensitive fields if they are encrypted
        val decryptedEmail = if (isEncrypted && email != null) {
            encryptionService.decrypt(email)
        } else {
            email
        }

        val decryptedPhone = if (isEncrypted && phone != null) {
            encryptionService.decrypt(phone)
        } else {
            phone
        }

        return Contact(
            id = id,
            name = name,
            title = title,
            company = company,
            email = decryptedEmail,
            phone = decryptedPhone,
            website = website,
            linkedInUrl = linkedInUrl,
            source = source,
            sourceDetails = sourceDetails,
            notes = notes,
            category = category,
            status = status,
            tags = tags,
            contextData = contextData,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun Contact.toEntity(encryptionService: EncryptionService): ContactEntity {
        // Encrypt sensitive fields
        val encryptedEmail = email?.let { encryptionService.encrypt(it) }
        val encryptedPhone = phone?.let { encryptionService.encrypt(it) }

        return ContactEntity(
            id = id,
            name = name,
            title = title,
            company = company,
            email = encryptedEmail,
            phone = encryptedPhone,
            website = website,
            linkedInUrl = linkedInUrl,
            source = source,
            sourceDetails = sourceDetails,
            notes = notes,
            category = category,
            status = status,
            tags = tags,
            contextData = contextData,
            createdAt = createdAt ?: Date(),
            updatedAt = Date(),
            isEncrypted = true
        )
    }
}
