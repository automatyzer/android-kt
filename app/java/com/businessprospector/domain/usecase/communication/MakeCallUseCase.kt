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

class MakeCallUseCase @Inject constructor(
    private val communicationRepository: CommunicationRepository,
    private val contactRepository: ContactRepository
) {
    suspend operator fun invoke(
        contactId: Long,
        script: String
    ): Result<Message> {
        val contact = contactRepository.getContactById(contactId)
            ?: return Result.failure(IllegalArgumentException("Contact not found"))

        return communicationRepository.makeCall(contact, script)
    }
}