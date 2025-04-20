package com.businessprospector.domain.usecase.communication

import com.businessprospector.data.local.dao.MessageDao
import com.businessprospector.data.local.dao.SequenceDao
import com.businessprospector.data.local.entity.SequenceEntity
import com.businessprospector.data.local.entity.SequenceStepEntity
import com.businessprospector.data.repository.CommunicationRepository
import com.businessprospector.data.repository.ContactRepository
import com.businessprospector.data.repository.LlmRepository
import com.businessprospector.domain.model.Contact
import com.businessprospector.domain.model.LlmConfig
import com.businessprospector.domain.model.Message
import com.businessprospector.domain.model.MessageTemplate
import com.businessprospector.domain.model.Sequence
import com.businessprospector.domain.model.SequenceStep
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import kotlin.random.Random

class SendEmailUseCase @Inject constructor(
    private val communicationRepository: CommunicationRepository,
    private val contactRepository: ContactRepository
) {
    suspend operator fun invoke(
        contactId: Long,
        subject: String,
        content: String
    ): Result<Message> {
        val contact = contactRepository.getContactById(contactId)
            ?: return Result.failure(IllegalArgumentException("Contact not found"))

        // Dodaj losowe opóźnienie, aby uniknąć blokady za nadmierną aktywność
        addRandomDelay()

        return communicationRepository.sendEmail(contact, subject, content)
    }

    private suspend fun addRandomDelay() {
        val delayTime = Random.nextLong(500, 2000)
        delay(delayTime)
    }
}


class GetMessageTemplatesUseCase @Inject constructor(
    private val messageDao: MessageDao
) {
    operator fun invoke(type: String? = null): Flow<List<MessageTemplate>> {
        return if (type != null) {
            messageDao.getMessageTemplatesByType(type).map { entities ->
                entities.map { entity ->
                    MessageTemplate(
                        id = entity.id,
                        name = entity.name,
                        type = entity.type,
                        subject = entity.subject,
                        content = entity.content,
                        variables = entity.variables,
                        category = entity.category,
                        createdAt = entity.createdAt,
                        updatedAt = entity.updatedAt
                    )
                }
            }
        } else {
            messageDao.getAllMessageTemplates().map { entities ->
                entities.map { entity ->
                    MessageTemplate(
                        id = entity.id,
                        name = entity.name,
                        type = entity.type,
                        subject = entity.subject,
                        content = entity.content,
                        variables = entity.variables,
                        category = entity.category,
                        createdAt = entity.createdAt,
                        updatedAt = entity.updatedAt
                    )
                }
            }
        }
    }
}
