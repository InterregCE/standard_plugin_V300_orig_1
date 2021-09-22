package io.cloudflight.jems.plugin.standard.pre_condition_check

import io.cloudflight.jems.plugin.contract.models.call.ApplicationFormFieldConfigurationData
import io.cloudflight.jems.plugin.contract.models.call.CallDetailData
import io.cloudflight.jems.plugin.contract.models.call.CallStatusData
import io.cloudflight.jems.plugin.contract.models.call.FieldVisibilityStatusData
import io.cloudflight.jems.plugin.contract.models.call.flatrate.FlatRateSetupData
import io.cloudflight.jems.plugin.contract.models.common.InputTranslationData
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.programme.fund.ProgrammeFundData
import io.cloudflight.jems.plugin.contract.models.programme.lumpsum.ProgrammeLumpSumListData
import io.cloudflight.jems.plugin.contract.models.programme.priority.ProgrammePriorityData
import io.cloudflight.jems.plugin.contract.models.programme.priority.ProgrammePriorityDataSimple
import io.cloudflight.jems.plugin.contract.models.programme.priority.ProgrammeSpecificObjectiveData
import io.cloudflight.jems.plugin.contract.models.programme.strategy.ProgrammeStrategyData
import io.cloudflight.jems.plugin.contract.models.programme.unitcost.ProgrammeUnitCostListData
import io.cloudflight.jems.plugin.contract.models.project.ApplicationFormFieldId
import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ApplicationStatusData
import io.cloudflight.jems.plugin.standard.pre_condition_check.helpers.CallDataContainer
import io.cloudflight.jems.plugin.standard.pre_condition_check.helpers.LifecycleDataContainer
import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ProjectLifecycleData
import io.cloudflight.jems.plugin.contract.models.project.sectionA.ProjectDataSectionA
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.MessageType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime

internal class ChecksSectionA {

    companion object {
        val mandatoryLanguages = setOf(SystemLanguageData.EN, SystemLanguageData.DE)
        val onceStepCallData = CallDetailData(
            id = 0,
            name = "UT Call",
            status = CallStatusData.PUBLISHED,
            startDateTime = ZonedDateTime.of(2020, 12, 3, 12, 20, 59, 90000, ZoneId.systemDefault()),
            endDateTimeStep1 = null,
            endDateTime = ZonedDateTime.of(2027, 12, 3, 12, 20, 59, 90000, ZoneId.systemDefault()),
            isAdditionalFundAllowed = true,
            lengthOfPeriod = 12,
            description = emptySet(),
            objectives  = emptyList(),
            strategies = emptyList(),
            funds = emptyList(),
            flatRates = FlatRateSetupData(null, null, null, null, null),
            lumpSums = emptyList(),
            unitCosts  = emptyList(),
            applicationFormFieldConfigurations =
                mutableSetOf(
                    ApplicationFormFieldConfigurationData(ApplicationFormFieldId.PROJECT_TITLE.id, FieldVisibilityStatusData.STEP_ONE_AND_TWO),
                    ApplicationFormFieldConfigurationData(ApplicationFormFieldId.PROJECT_ACRONYM.id, FieldVisibilityStatusData.STEP_ONE_AND_TWO),
                    ApplicationFormFieldConfigurationData(ApplicationFormFieldId.PROJECT_DURATION.id, FieldVisibilityStatusData.STEP_ONE_AND_TWO),
                    ApplicationFormFieldConfigurationData(ApplicationFormFieldId.PROJECT_PRIORITY.id, FieldVisibilityStatusData.STEP_ONE_AND_TWO),
                    ApplicationFormFieldConfigurationData(ApplicationFormFieldId.PROJECT_OBJECTIVE.id, FieldVisibilityStatusData.STEP_ONE_AND_TWO),
                    ApplicationFormFieldConfigurationData(ApplicationFormFieldId.PROJECT_TITLE.id, FieldVisibilityStatusData.STEP_ONE_AND_TWO),
                    ApplicationFormFieldConfigurationData(ApplicationFormFieldId.PROJECT_SUMMARY.id, FieldVisibilityStatusData.STEP_ONE_AND_TWO),
                ),
            inputLanguages = mandatoryLanguages)
        val projectLifecycleData = ProjectLifecycleData(status = ApplicationStatusData.DRAFT)
        val sectionAData = ProjectDataSectionA(
            title = emptySet(),
            intro  = emptySet(),
            acronym = null,
            duration = null,
            specificObjective = null,
            programmePriority = null)
    }

    @Test
    fun `Project EN Intro Is not Provided`() {
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.messageType == MessageType.ERROR).isTrue
        assertThat(verification.subSectionMessages.any
            {message ->
                message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.intro.in.en.is.not.provided" }
        ).isTrue
    }

    @Test
    fun `Project Intro Is not Provided`() {
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.messageType == MessageType.ERROR).isTrue
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.intro.is.not.provided" }
        ).isTrue
    }

    @Test
    fun `Project Title Is not Provided`() {
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.messageType == MessageType.ERROR).isTrue
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.title.is.not.provided" }
        ).isTrue
    }

    @Test
    fun `Project Acronym Is not Provided`() {
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.messageType == MessageType.ERROR).isTrue
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.acronym.is.not.provided" }
        ).isTrue
    }

    @Test
    fun `Project Duration Is not Provided`() {
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.messageType == MessageType.ERROR).isTrue
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.duration.is.not.provided" }
        ).isTrue
    }

    @Test
    fun `Project Priority Is not Provided`() {
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.messageType == MessageType.ERROR).isTrue
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.programme.priority.is.not.provided" }
        ).isTrue
    }

    @Test
    fun `Project Intro Is not fully Provided`() {
        val sectionAData = ProjectDataSectionA(
            title = emptySet(),
            intro  = setOf(
                InputTranslationData(SystemLanguageData.DE, "TEST DATA"),
            ),
            acronym = null,
            duration = null,
            specificObjective = null,
            programmePriority = null)
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.intro.is.not.provided" }
        ).isTrue
    }

    @Test
    fun `Project Intro Is not fully Provided due to empty text`() {
        val sectionAData = ProjectDataSectionA(
            title = emptySet(),
            intro  = setOf(
                InputTranslationData(SystemLanguageData.DE, "TEST DATA"),
                InputTranslationData(SystemLanguageData.EN, ""),
            ),
            acronym = null,
            duration = null,
            specificObjective = null,
            programmePriority = null)
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.intro.is.not.provided" }
        ).isTrue
    }

    @Test
    fun `Project Title Is not fully Provided`() {
        val sectionAData = ProjectDataSectionA(
            title = setOf(
                InputTranslationData(SystemLanguageData.DE, "TEST DATA"),
            ),
            intro = emptySet(),
            acronym = null,
            duration = null,
            specificObjective = null,
            programmePriority = null)
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.title.is.not.provided" }
        ).isTrue
    }

    @Test
    fun `Project Title Is not fully Provided due to empty text`() {
        val sectionAData = ProjectDataSectionA(
            title = setOf(
                InputTranslationData(SystemLanguageData.DE, "TEST DATA"),
                InputTranslationData(SystemLanguageData.EN, ""),
            ),
            intro = emptySet(),
            acronym = null,
            duration = null,
            specificObjective = null,
            programmePriority = null)
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.title.is.not.provided" }
        ).isTrue
    }

    @Test
    fun `Project EN Intro Is Provided`() {
        val sectionAData = ProjectDataSectionA(
            title = emptySet(),
            intro  = setOf(InputTranslationData(SystemLanguageData.EN, "TEST DATA")),
            acronym = null,
            duration = null,
            specificObjective = null,
            programmePriority = null)
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.intro.in.en.is.not.provided" }
        ).isFalse
    }

    @Test
    fun `Project Intro Is fully Provided`() {
        val sectionAData = ProjectDataSectionA(
            title = emptySet(),
            intro  = setOf(
                InputTranslationData(SystemLanguageData.EN, "TEST DATA"),
                InputTranslationData(SystemLanguageData.DE, "TEST DATA"),
            ),
            acronym = null,
            duration = null,
            specificObjective = null,
            programmePriority = null)
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.intro.is.not.provided" }
        ).isFalse
    }

    @Test
    fun `Project Title Is fully Provided`() {
        val sectionAData = ProjectDataSectionA(
            title = setOf(
                InputTranslationData(SystemLanguageData.EN, "TEST DATA"),
                InputTranslationData(SystemLanguageData.DE, "TEST DATA"),
            ),
            intro = emptySet(),
            acronym = null,
            duration = null,
            specificObjective = null,
            programmePriority = null)
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.title.is.not.provided" }
        ).isFalse
    }

    @Test
    fun `Project Acronym Is fully Provided`() {
        val sectionAData = ProjectDataSectionA(
            title = emptySet(),
            intro = emptySet(),
            acronym = "TEST DATA",
            duration = null,
            specificObjective = null,
            programmePriority = null)
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.acronym.is.not.provided" }
        ).isFalse
    }

    @Test
    fun `Project Duration Is Provided`() {
        val sectionAData = ProjectDataSectionA(
            title = emptySet(),
            intro = emptySet(),
            acronym = null,
            duration = 12,
            specificObjective = null,
            programmePriority = null)
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.duration.is.not.provided" }
        ).isFalse
    }

    @Test
    fun `Project Priority Is Provided`() {
        val sectionAData = ProjectDataSectionA(
            title = emptySet(),
            intro = emptySet(),
            acronym = null,
            duration = null,
            specificObjective = null,
            programmePriority = ProgrammePriorityDataSimple(code = "TEST_DATA"))
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.programme.priority.is.not.provided" }
        ).isFalse
    }
}