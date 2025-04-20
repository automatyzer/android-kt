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


class ExecuteSequenceUseCase @Inject constructor(
    private val sequenceDao: SequenceDao,
    private val messageDao: MessageDao,
    private val contactRepository: ContactRepository,
    private val llmRepository: LlmRepository,
    private val communicationRepository: CommunicationRepository
) {
    suspend fun execute(
        sequenceId: Long,
        contactId: Long,
        llmConfig: LlmConfig
    ): Result<Int> {
        try {
            // Pobierz sekwencję z krokami
            val sequenceWithSteps = sequenceDao.getSequenceWithSteps(sequenceId).first()
            if (sequenceWithSteps.steps.isEmpty()) {
                return Result.failure(IllegalStateException("Sequence has no steps"))
            }

            // Pobierz dane kontaktu
            val contact = contactRepository.getContactById(contactId)
                ?: return Result.failure(IllegalArgumentException("Contact not found"))

            // Zaktualizuj status kontaktu
            contactRepository.updateContactStatus(contactId, "sequence_in_progress")

            var executedSteps = 0

            // Wykonaj pierwszy krok sekwencji
            val firstStep = sequenceWithSteps.steps.minByOrNull { it.order }
                ?: return Result.failure(IllegalStateException("Sequence has no valid steps"))

            executeStep(firstStep, contact, llmConfig)
            executedSteps++

            return Result.success(executedSteps)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    private suspend fun executeStep(
        step: SequenceStepEntity,
        contact: Contact,
        llmConfig: LlmConfig
    ): Result<Message> {
        // Pobierz szablon wiadomości
        val templateId = step.templateId
            ?: return Result.failure(IllegalArgumentException("Step has no template"))

        val template = messageDao.getMessageTemplateById(templateId)
            ?: return Result.failure(IllegalArgumentException("Template not found"))

        // Personalizuj wiadomość na podstawie szablonu i danych kontaktu
        val personalizedContent = llmRepository.generatePersonalizedMessage(
            contact,
            template.content,
            llmConfig
        ).getOrDefault(template.content)

        // Wykonaj akcję w zależności od typu kroku
        return when (step.type.lowercase()) {
            "email" -> {
                val subject = template.subject ?: "No subject"
                communicationRepository.sendEmail(contact, subject, personalizedContent)
            }
            "sms" -> {
                communicationRepository.sendSms(contact, personalizedContent)
            }
            "call" -> {
                communicationRepository.makeCall(contact, personalizedContent)
            }
            else -> {
                Result.failure(IllegalArgumentException("Unknown step type: ${step.type}"))
            }
        }
    }
}


class GetSequencesUseCase @Inject constructor(
    private val sequenceDao: SequenceDao
) {
    operator fun invoke(activeOnly: Boolean = false): Flow<List<Sequence>> {
        return if (activeOnly) {
            sequenceDao.getActiveSequences().map { entities ->
                entities.map { entity ->
                    Sequence(
                        id = entity.id,
                        name = entity.name,
                        description = entity.description,
                        isActive = entity.isActive,
                        createdAt = entity.createdAt,
                        updatedAt = entity.updatedAt
                    )
                }
            }
        } else {
            sequenceDao.getAllSequences().map { entities ->
                entities.map { entity ->
                    Sequence(
                        id = entity.id,
                        name = entity.name,
                        description = entity.description,
                        isActive = entity.isActive,
                        createdAt = entity.createdAt,
                        updatedAt = entity.updatedAt
                    )
                }
            }
        }
    }
}

class GetSequenceWithStepsUseCase @Inject constructor(
    private val sequenceDao: SequenceDao
) {
    suspend operator fun invoke(sequenceId: Long): Sequence? {
        val sequenceWithSteps = sequenceDao.getSequenceWithSteps(sequenceId).first()

        val sequence = sequenceWithSteps.sequence
        val steps = sequenceWithSteps.steps

        if (sequence != null) {
            return Sequence(
                id = sequence.id,
                name = sequence.name,
                description = sequence.description,
                isActive = sequence.isActive,
                steps = steps.map { step ->
                    SequenceStep(
                        id = step.id,
                        sequenceId = step.sequenceId,
                        type = step.type,
                        templateId = step.templateId,
                        order = step.order,
                        delayDays = step.delayDays,
                        delayHours = step.delayHours,
                        condition = step.condition,
                        isActive = step.isActive,
                        createdAt = step.createdAt,
                        updatedAt = step.updatedAt
                    )
                }.sortedBy { it.order },
                createdAt = sequence.createdAt,
                updatedAt = sequence.updatedAt
            )
        }

        return null
    }
}
