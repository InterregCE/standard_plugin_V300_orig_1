package io.cloudflight.jems.plugin.standard.pre_condition_check

import io.cloudflight.jems.plugin.contract.models.call.CallDetailData
import io.cloudflight.jems.plugin.contract.models.common.InputTranslationData
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.programme.priority.ProgrammePriorityDataSimple
import io.cloudflight.jems.plugin.contract.models.project.ApplicationFormFieldId
import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ProjectLifecycleData
import io.cloudflight.jems.plugin.contract.models.project.sectionA.ProjectDataSectionA
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckMessage
import io.cloudflight.jems.plugin.standard.pre_condition_check.helpers.CallDataContainer
import io.cloudflight.jems.plugin.standard.pre_condition_check.helpers.LifecycleDataContainer

private const val SECTION_A_MESSAGES_PREFIX = "$MESSAGES_PREFIX.section.a"
private const val SECTION_A_ERROR_MESSAGES_PREFIX = "$SECTION_A_MESSAGES_PREFIX.error"
private const val SECTION_A_INFO_MESSAGES_PREFIX = "$SECTION_A_MESSAGES_PREFIX.info"

fun checkSectionA(sectionAData: ProjectDataSectionA?): PreConditionCheckMessage {
    return buildPreConditionCheckMessage(
        messageKey = "$SECTION_A_MESSAGES_PREFIX.header", messageArgs = emptyMap(),

        checkIfTitleIsProvided(sectionAData?.title),

        checkIfAcronymIsProvided(sectionAData?.acronym),

        checkIfDurationIsProvided(sectionAData?.duration),

        checkIfProgrammePriorityIsProvided(sectionAData?.programmePriority),

        //checkIfIntroIsProvidedInEnglish(sectionAData?.intro),

        checkIfIntroIsProvided(sectionAData?.intro)

       // , checkIfDurationNotOver42Months(sectionAData?.duration),
    )
}

private fun checkIfTitleIsProvided(title: Set<InputTranslationData>?) =
    when {
        isFieldVisible(ApplicationFormFieldId.PROJECT_TITLE) && title.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
        -> buildErrorPreConditionCheckMessage("$SECTION_A_ERROR_MESSAGES_PREFIX.title.is.not.provided")
        else -> null
    }

private fun checkIfAcronymIsProvided(acronym: String?) =
    when {
        isFieldVisible(ApplicationFormFieldId.PROJECT_ACRONYM) &&
        acronym.isNullOrBlank() -> buildErrorPreConditionCheckMessage("$SECTION_A_ERROR_MESSAGES_PREFIX.acronym.is.not.provided")
        else -> null
    }

private fun checkIfDurationIsProvided(duration: Int?) =
    when {
        isFieldVisible(ApplicationFormFieldId.PROJECT_DURATION) &&
                (duration == null || duration > 42) -> buildErrorPreConditionCheckMessage("$SECTION_A_ERROR_MESSAGES_PREFIX.duration.is.not.provided")
        else -> null
    }

private fun checkIfProgrammePriorityIsProvided(programmePriority: ProgrammePriorityDataSimple?) =
    when {
        isFieldVisible(ApplicationFormFieldId.PROJECT_PRIORITY) &&
        (programmePriority == null || programmePriority.code.isBlank()) -> buildErrorPreConditionCheckMessage("$SECTION_A_ERROR_MESSAGES_PREFIX.programme.priority.is.not.provided")
        else -> null
    }
// Amund - Not needed, only one language in Interreg CE
private fun checkIfIntroIsProvidedInEnglish(intro: Set<InputTranslationData>?) =
    when {
        intro.isNotFullyTranslated(setOf(SystemLanguageData.EN))
        -> buildErrorPreConditionCheckMessage("$SECTION_A_ERROR_MESSAGES_PREFIX.intro.in.en.is.not.provided")
        else -> null
    }

private fun checkIfIntroIsProvided(intro: Set<InputTranslationData>?) =
    when {
        (intro.isNotFullyTranslated(CallDataContainer.get().inputLanguages))
        -> buildErrorPreConditionCheckMessage("$SECTION_A_ERROR_MESSAGES_PREFIX.intro.is.not.provided")
       else -> null
    }

private fun isFieldVisible(fieldId: ApplicationFormFieldId): Boolean {
    return isFieldVisible(fieldId, LifecycleDataContainer.get()!!, CallDataContainer.get())
}
// Amund - Check for max project duration
//private fun checkIfDurationNotOver42Months(duration: Int?) =
//    when {
//        isFieldVisible(ApplicationFormFieldId.PROJECT_DURATION) &&
//                duration!! > 42 -> buildErrorPreConditionCheckMessage("$SECTION_A_ERROR_MESSAGES_PREFIX.duration.over.42.months")
//        else -> null
//    }
