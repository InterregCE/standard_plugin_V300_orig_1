package io.cloudflight.jems.plugin.standard.pre_condition_check

import io.cloudflight.jems.plugin.contract.models.call.CallDetailData
import io.cloudflight.jems.plugin.contract.models.project.ApplicationFormFieldId
import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ProjectLifecycleData
import io.cloudflight.jems.plugin.contract.models.project.sectionE.ProjectDataSectionE
import io.cloudflight.jems.plugin.contract.models.project.sectionE.lumpsum.ProjectLumpSumData
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckMessage
import io.cloudflight.jems.plugin.standard.pre_condition_check.helpers.CallDataContainer
import io.cloudflight.jems.plugin.standard.pre_condition_check.helpers.LifecycleDataContainer
import java.math.BigDecimal

private const val SECTION_E_MESSAGES_PREFIX = "$MESSAGES_PREFIX.section.e"
private const val SECTION_E_ERROR_MESSAGES_PREFIX = "$SECTION_E_MESSAGES_PREFIX.error"
private const val SECTION_E_INFO_MESSAGES_PREFIX = "$SECTION_E_MESSAGES_PREFIX.info"

fun checkSectionE(sectionEData: ProjectDataSectionE): PreConditionCheckMessage {
    return buildPreConditionCheckMessage(
        messageKey = "$SECTION_E_MESSAGES_PREFIX.header", messageArgs = emptyMap(),

        checkIfPartnersShareSumUpToTotalLumpSum(sectionEData.projectLumpSums),

        checkIfLumpSumPeriodsProvided(sectionEData.projectLumpSums)
    )
}

private fun checkIfPartnersShareSumUpToTotalLumpSum(lumpSums: List<ProjectLumpSumData>) =
    when {
        lumpSums.any { lumpSum -> lumpSum.programmeLumpSum?.cost ?: BigDecimal.ZERO != lumpSum.lumpSumContributions.sumOf { it.amount } } ->
            buildErrorPreConditionCheckMessage("$SECTION_E_ERROR_MESSAGES_PREFIX.partner.amount.do.not.sum.up.to.budget.entry.sum")
        else -> null
    }

private fun checkIfLumpSumPeriodsProvided(lumpSums: List<ProjectLumpSumData>) =
    when {
        isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_PERIODS) &&
        lumpSums.any { lumpSum -> lumpSum.period == null } ->
            buildErrorPreConditionCheckMessage("$SECTION_E_ERROR_MESSAGES_PREFIX.lump.sum.periods.is.not.provided")
        else -> null
    }

private fun isFieldVisible(fieldId: ApplicationFormFieldId): Boolean {
    return isFieldVisible(fieldId, LifecycleDataContainer.get()!!, CallDataContainer.get())
}

