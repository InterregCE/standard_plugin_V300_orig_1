package io.cloudflight.jems.plugin.standard.pre_condition_check

import io.cloudflight.jems.plugin.contract.models.call.CallDetailData
import io.cloudflight.jems.plugin.contract.models.call.FieldVisibilityStatusData
import io.cloudflight.jems.plugin.contract.models.common.I18nMessageData
import io.cloudflight.jems.plugin.contract.models.common.InputTranslationData
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.project.ApplicationFormFieldId
import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ApplicationStatusData
import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ProjectLifecycleData
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.MessageType
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckMessage
import java.math.BigDecimal
import java.math.RoundingMode

fun buildPreConditionCheckMessage(
    messageKey: String, messageArgs: Map<String, String> = emptyMap(), vararg checkResults: PreConditionCheckMessage?
): PreConditionCheckMessage =
    mutableListOf<PreConditionCheckMessage>().plus(
        checkResults.filterNotNull()
    ).let { subSectionMessages ->
        PreConditionCheckMessage(
            I18nMessageData(messageKey, messageArgs),
            // Amund - Added one extra firstOrNull to check for warning messages
            subSectionMessages.firstOrNull { it.messageType == MessageType.ERROR }?.messageType ?: subSectionMessages.firstOrNull { it.messageType == MessageType.WARNING }?.messageType ?: MessageType.INFO,
            subSectionMessages
        )
    }

fun buildPreConditionCheckMessage(
    messageKey: String, messageArgs: Map<String, String> = emptyMap(), messageType: MessageType,
    vararg checkResults: PreConditionCheckMessage?
): PreConditionCheckMessage =
    PreConditionCheckMessage(
        I18nMessageData(messageKey, messageArgs), messageType,
        mutableListOf<PreConditionCheckMessage>().plus(checkResults.filterNotNull())
    )

fun buildPreConditionCheckMessage(
    messageKey: String, messageArgs: Map<String, String> = emptyMap(), messageType: MessageType,
    checkResults: List<PreConditionCheckMessage>?
): PreConditionCheckMessage =
    PreConditionCheckMessage(
        I18nMessageData(messageKey, messageArgs), messageType, checkResults ?: emptyList()
    )

fun buildInfoPreConditionCheckMessage(
    messageKey: String, messageArgs: Map<String, String> = emptyMap(), vararg checkResults: PreConditionCheckMessage?
): PreConditionCheckMessage =
    buildPreConditionCheckMessage(messageKey, messageArgs, MessageType.INFO, *checkResults)

fun buildInfoPreConditionCheckMessages(
    messageKey: String, messageArgs: Map<String, String> = emptyMap(), checkResults: List<PreConditionCheckMessage>?
): PreConditionCheckMessage =
    buildPreConditionCheckMessage(messageKey, messageArgs, MessageType.INFO, checkResults)

fun buildErrorPreConditionCheckMessage(
    messageKey: String, messageArgs: Map<String, String> = emptyMap(), vararg checkResults: PreConditionCheckMessage?
): PreConditionCheckMessage =
    buildPreConditionCheckMessage(messageKey, messageArgs, MessageType.ERROR, *checkResults)

fun buildErrorPreConditionCheckMessages(
    messageKey: String, messageArgs: Map<String, String> = emptyMap(), checkResults: List<PreConditionCheckMessage>?
): PreConditionCheckMessage =
    buildPreConditionCheckMessage(messageKey, messageArgs, MessageType.ERROR, checkResults)

// Amund Added warning message
fun buildWarningPreConditionCheckMessage(
    messageKey: String, messageArgs: Map<String, String> = emptyMap(), vararg checkResults: PreConditionCheckMessage?
): PreConditionCheckMessage =
    buildPreConditionCheckMessage(messageKey, messageArgs, MessageType.WARNING, *checkResults)

fun buildWarningPreConditionCheckMessages(
    messageKey: String, messageArgs: Map<String, String> = emptyMap(), checkResults: List<PreConditionCheckMessage>?
): PreConditionCheckMessage =
    buildPreConditionCheckMessage(messageKey, messageArgs, MessageType.WARNING, checkResults)

fun Set<InputTranslationData>?.isNotFullyTranslated(mandatoryLanguages: Set<SystemLanguageData>): Boolean {
    return !this.isFullyTranslated(mandatoryLanguages)
}
fun Set<InputTranslationData>?.isFullyTranslated(mandatoryLanguages: Set<SystemLanguageData>): Boolean {
    if (this.isNullOrEmpty()) return false
    val allLanguagesUsed = this.mapTo(HashSet()) { it.language }.containsAll(mandatoryLanguages)
    val allLanguagesTranslated = this.filter { mandatoryLanguages.contains(it.language) }.all { !it.translation.isNullOrBlank() }
    return allLanguagesUsed && allLanguagesTranslated
}

fun <T> Iterable<T>.sumOf(fieldExtractor: (T) -> BigDecimal?): BigDecimal =
    this.map { fieldExtractor.invoke(it) ?: BigDecimal.ZERO }.fold(BigDecimal.ZERO, BigDecimal::add)

fun BigDecimal.truncate(): BigDecimal =
    setScale(2, RoundingMode.FLOOR)

fun BigDecimal.truncateDown(): BigDecimal =
    setScale(2, RoundingMode.DOWN)

fun BigDecimal.percentage(percentage: Int): BigDecimal =
    multiply(BigDecimal.valueOf(percentage.toLong()))
        .divide(BigDecimal(100))
        .truncate()

fun BigDecimal.percentageDown(percentage: BigDecimal): BigDecimal =
    multiply(percentage)
        .divide(BigDecimal(100))
        .truncateDown()

fun Set<InputTranslationData>?.getFirstOrDefaultTranslation(): String {
    if (this == null || this.isEmpty())
    {
        return ""
    }
    return this.first { !it.translation.isNullOrBlank() }.translation ?: ""
}

fun CallDetailData.isTwoStepCall()=
    this.endDateTimeStep1 != null

fun ApplicationStatusData.isInStepOne() =
    this == ApplicationStatusData.STEP1_DRAFT || this == ApplicationStatusData.STEP1_SUBMITTED || this == ApplicationStatusData.STEP1_ELIGIBLE || this == ApplicationStatusData.STEP1_INELIGIBLE || this == ApplicationStatusData.STEP1_APPROVED
            || this == ApplicationStatusData.STEP1_APPROVED_WITH_CONDITIONS || this == ApplicationStatusData.STEP1_NOT_APPROVED

fun isFieldVisible(fieldId: ApplicationFormFieldId, lifecycleData: ProjectLifecycleData, callData: CallDetailData) : Boolean {
    val fieldConfiguration = callData.applicationFormFieldConfigurations.firstOrNull { it.id == fieldId.id }
        ?: return false
    return when{
        fieldConfiguration.visibilityStatus == FieldVisibilityStatusData.NONE -> false
        callData.isTwoStepCall() && lifecycleData.status.isInStepOne() && fieldConfiguration.visibilityStatus == FieldVisibilityStatusData.STEP_TWO_ONLY -> false
        else -> true
    }
}