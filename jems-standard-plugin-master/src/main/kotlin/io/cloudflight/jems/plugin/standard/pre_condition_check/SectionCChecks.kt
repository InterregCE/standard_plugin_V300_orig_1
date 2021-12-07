package io.cloudflight.jems.plugin.standard.pre_condition_check

import io.cloudflight.jems.plugin.contract.models.common.InputTranslationData
import io.cloudflight.jems.plugin.contract.models.project.ApplicationFormFieldId
import io.cloudflight.jems.plugin.contract.models.project.sectionC.ProjectDataSectionC
import io.cloudflight.jems.plugin.contract.models.project.sectionC.longTermPlans.ProjectLongTermPlansData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.management.ProjectCooperationCriteriaData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.management.ProjectHorizontalPrinciplesData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.management.ProjectHorizontalPrinciplesEffectData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.management.ProjectManagementData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.overallObjective.ProjectOverallObjectiveData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.partnership.ProjectPartnershipData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.relevance.ProjectRelevanceBenefitData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.relevance.ProjectRelevanceStrategyData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.relevance.ProjectRelevanceSynergyData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.relevance.ProjectTargetGroupData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.results.ProjectResultData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.workpackage.ProjectWorkPackageData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.workpackage.WorkPackageActivityData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.workpackage.WorkPackageInvestmentData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.workpackage.WorkPackageOutputData
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckMessage
import io.cloudflight.jems.plugin.standard.pre_condition_check.helpers.CallDataContainer
import io.cloudflight.jems.plugin.standard.pre_condition_check.helpers.LifecycleDataContainer
import java.math.BigDecimal

private const val SECTION_C_MESSAGES_PREFIX = "$MESSAGES_PREFIX.section.c"
private const val SECTION_C_ERROR_MESSAGES_PREFIX = "$SECTION_C_MESSAGES_PREFIX.error"
private const val SECTION_C_WARNING_MESSAGES_PREFIX = "$SECTION_C_MESSAGES_PREFIX.warning"
private const val SECTION_C_INFO_MESSAGES_PREFIX = "$SECTION_C_MESSAGES_PREFIX.info"

fun checkSectionC(sectionCData: ProjectDataSectionC?): PreConditionCheckMessage {
    return buildPreConditionCheckMessage(
        messageKey = "$SECTION_C_MESSAGES_PREFIX.header", messageArgs = emptyMap(),

        buildPreConditionCheckMessage(
            messageKey = "$SECTION_C_INFO_MESSAGES_PREFIX.project.c1", messageArgs = emptyMap(),
            checkIfProjectOverallObjectiveIsProvided(sectionCData?.projectOverallObjective),

            checkIfProjectOverallObjectiveIsFilledIn(sectionCData?.projectOverallObjective)
        ),

        buildPreConditionCheckMessage(
            messageKey = "$SECTION_C_INFO_MESSAGES_PREFIX.project.c2", messageArgs = emptyMap(),

            checkIfTerritorialChallengeGroupIsProvided(sectionCData?.projectRelevance?.territorialChallenge),

            checkIfCommonChallengeGroupIsProvided(sectionCData?.projectRelevance?.commonChallenge),

            checkIfTransnationalCooperationGroupIsProvided(sectionCData?.projectRelevance?.transnationalCooperation),

            checkIfAtLeastOneTargetGroupIsAdded(sectionCData?.projectRelevance?.projectBenefits),

            checkIfSpecificationIsProvidedForAllTargetGroups(sectionCData?.projectRelevance?.projectBenefits),

            checkIfTargetGroupAddedSeveralTimes(sectionCData?.projectRelevance?.projectBenefits),

            checkIfStrategyAddedSeveralTimes(sectionCData?.projectRelevance?.projectStrategies),

            checkIfAtLeastOneStrategyIsAdded(sectionCData?.projectRelevance?.projectStrategies),

            checkIfSpecificationIsProvidedForAllStrategies(sectionCData?.projectRelevance?.projectStrategies),

            checkIfSynergiesAreNotEmpty(sectionCData?.projectRelevance?.projectSynergies),

            checkIfAvailableKnowledgeAreNotEmpty(sectionCData?.projectRelevance?.availableKnowledge)
        ),

        buildPreConditionCheckMessage(
            messageKey = "$SECTION_C_INFO_MESSAGES_PREFIX.project.c3", messageArgs = emptyMap(),

            checkIfProjectPpartnershipIsAdded(sectionCData?.projectPartnership)
        ),

        buildPreConditionCheckMessage(
            messageKey = "$SECTION_C_INFO_MESSAGES_PREFIX.project.c4", messageArgs = emptyMap(),

            checkIfAtLeastOneWorkPackageIsAdded(sectionCData?.projectWorkPackages),

            checkIfNamesOfWorkPackagesAreProvided(sectionCData?.projectWorkPackages),

            checkIfMoreThan5WorkPackagesAreAdded(sectionCData?.projectWorkPackages),

            checkIfObjectivesOfWorkPackagesAreProvided(sectionCData?.projectWorkPackages),

            checkIfAtLeastOneOutputForEachWorkPackageIsAdded(sectionCData?.projectWorkPackages),

            checkIfWorkPackageContentIsProvided(sectionCData?.projectWorkPackages)
        ),

        buildPreConditionCheckMessage(
            messageKey = "$SECTION_C_INFO_MESSAGES_PREFIX.project.c5", messageArgs = emptyMap(),
            checkIfAtLeastOneResultIsAdded(sectionCData?.projectResults),

            checkIfResultContentIsProvided(sectionCData)
        ),

        buildPreConditionCheckMessage(
            messageKey = "$SECTION_C_INFO_MESSAGES_PREFIX.project.c7", messageArgs = emptyMap(),

            checkIfCoordinateProjectIsValid(sectionCData?.projectManagement),

            checkIfMeasuresQualityIsValid(sectionCData?.projectManagement),

            checkIfCommunicationIsValid(sectionCData?.projectManagement),

            checkIfFinancialManagementIsProvided(sectionCData?.projectManagement?.projectFinancialManagement),

            checkIfSelectedCooperationCriteriaAreValid(sectionCData?.projectManagement?.projectCooperationCriteria),

            checkIfDescriptionForAllSelectedCooperationCriteriaIsProvided(sectionCData?.projectManagement),

            checkIfTypeOfContributionsAreProvided(sectionCData?.projectManagement?.projectHorizontalPrinciples),

            checkIfDescriptionForTypeOfContributionIsProvided(sectionCData?.projectManagement)
        ),

        buildPreConditionCheckMessage(
            messageKey = "$SECTION_C_INFO_MESSAGES_PREFIX.project.c8", messageArgs = emptyMap(),

            checkIfOwnershipIsValid(sectionCData?.projectLongTermPlans),

            checkIfDurabilityIsValid(sectionCData?.projectLongTermPlans),

            checkIfTransferabilityIsValid(sectionCData?.projectLongTermPlans)
        )
    )
}

private fun checkIfProjectOverallObjectiveIsProvided(projectOverallObjectiveData: ProjectOverallObjectiveData?) =
    when {
        !isFieldVisible(ApplicationFormFieldId.PROJECT_OVERALL_OBJECTIVE) -> null
        projectOverallObjectiveData == null -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.overall.objective.should.be.provided")
        else -> null
    }

private fun checkIfProjectOverallObjectiveIsFilledIn(projectOverallObjectiveData: ProjectOverallObjectiveData?) =
    when {
        !isFieldVisible(ApplicationFormFieldId.PROJECT_OVERALL_OBJECTIVE) -> null
        projectOverallObjectiveData!!.overallObjective.isEmpty() -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.overall.objective.not.filled.in")
        else -> null
    }

private fun checkIfTerritorialChallengeGroupIsProvided(territorialChallenge: Set<InputTranslationData>?) =
    when {
        !isFieldVisible(ApplicationFormFieldId.PROJECT_TERRITORIAL_CHALLENGES) -> null
        territorialChallenge.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
        -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.territorial.challenge.is.not.provided")
        else -> null
    }

private fun checkIfCommonChallengeGroupIsProvided(commonChallenge: Set<InputTranslationData>?) =
    when {
        !isFieldVisible(ApplicationFormFieldId.PROJECT_HOW_ARE_CHALLENGES_AND_OPPORTUNITIES_TACKLED) -> null
        commonChallenge.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
        -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.common.challenges.is.not.provided")
        else -> null
    }

private fun checkIfTransnationalCooperationGroupIsProvided(transnationalCooperation: Set<InputTranslationData>?) =
    when {
        !isFieldVisible(ApplicationFormFieldId.PROJECT_WHY_IS_COOPERATION_NEEDED) -> null
        transnationalCooperation.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
        -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.transnational.cooperation.is.not.provided")
        else -> null
    }

private fun checkIfAtLeastOneTargetGroupIsAdded(projectBenefits: List<ProjectRelevanceBenefitData>?) =
    when {
        !isFieldVisible(ApplicationFormFieldId.PROJECT_TARGET_GROUP) -> null
        projectBenefits.isNullOrEmpty() -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.at.least.one.target.group.should.be.added")
        else -> null
    }

private fun checkIfSpecificationIsProvidedForAllTargetGroups(projectBenefits: List<ProjectRelevanceBenefitData>?) =
    when {
        !isFieldVisible(ApplicationFormFieldId.PROJECT_TARGET_GROUP) -> null
        projectBenefits.isNullOrEmpty() -> null
        projectBenefits.any { it.specification.isNotFullyTranslated(CallDataContainer.get().inputLanguages) }
        -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.specifications.for.all.target.groups.should.be.added")
        else -> null
    }


// Amund - Warning for several of the same target groups TODO: make into warning
private fun checkIfTargetGroupAddedSeveralTimes(projectBenefits: List<ProjectRelevanceBenefitData>?) =
    when {
        projectBenefits != null  -> {
            val groups = mutableListOf<String>()
            projectBenefits.forEach { projectBenefit ->
                groups.add(projectBenefit.group.toString())
            }
            val dupes = groups.groupingBy { it }.eachCount().filter { it.value > 1 }
            if (dupes.isNotEmpty()) {
                buildWarningPreConditionCheckMessage("$SECTION_C_WARNING_MESSAGES_PREFIX.target.group.used.several.times"
                    , mapOf("groups" to (groups.toString()), "dupes" to (dupes.keys.toString()) ))
            }
            else {
                null
            }

        }

        else -> null
    }

// Amund - Warning for several of the same target groups TODO: make into warning
private fun checkIfStrategyAddedSeveralTimes(projectStrategies: List<ProjectRelevanceStrategyData>?) =
    when {
        projectStrategies != null  -> {
            val strategies = mutableListOf<String>()
            projectStrategies.forEach { projectStrategy ->
                strategies.add(projectStrategy.strategy.toString())
            }
            val dupes = strategies.groupingBy { it }.eachCount().filter { it.value > 1 }
            if (dupes.isNotEmpty()) {
                buildWarningPreConditionCheckMessage("$SECTION_C_WARNING_MESSAGES_PREFIX.strategy.used.several.times"
                    , mapOf("strategies" to (strategies.toString()), "dupes" to (dupes.keys.toString()) ))
            }
            else {
                null
            }

        }

        else -> null
    }

private fun checkIfAtLeastOneStrategyIsAdded(projectStrategies: List<ProjectRelevanceStrategyData>?) =
    when {
        !isFieldVisible(ApplicationFormFieldId.PROJECT_STRATEGY_CONTRIBUTION) -> null
        projectStrategies.isNullOrEmpty() -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.at.least.one.strategy.should.be.added")
        else -> null
    }

private fun checkIfSpecificationIsProvidedForAllStrategies(projectStrategies: List<ProjectRelevanceStrategyData>?) =
    when {
        !isFieldVisible(ApplicationFormFieldId.PROJECT_STRATEGY_CONTRIBUTION) -> null
        projectStrategies.isNullOrEmpty() -> null
        projectStrategies.any { it.specification.isNotFullyTranslated(CallDataContainer.get().inputLanguages) }
        -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.specifications.for.all.strategies.should.be.added")
        else -> null
    }

private fun checkIfSynergiesAreNotEmpty(relevanceSynergies: List<ProjectRelevanceSynergyData>?) =
    when {
        !isFieldVisible(ApplicationFormFieldId.PROJECT_SYNERGIES) -> null
        relevanceSynergies.isNullOrEmpty() -> null
        relevanceSynergies.any { it.synergy.isNotFullyTranslated(CallDataContainer.get().inputLanguages) ||
                it.specification.isNotFullyTranslated(CallDataContainer.get().inputLanguages) }
        -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.synergies.should.be.provided")
        else -> null
    }

private fun checkIfAvailableKnowledgeAreNotEmpty(availableKnowledge: Set<InputTranslationData>?) =
    when {
        !isFieldVisible(ApplicationFormFieldId.PROJECT_HOW_BUILDS_PROJECT_ON_AVAILABLE_KNOWLEDGE) -> null
        availableKnowledge == null ||
        availableKnowledge.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
        -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.build.available.knowledge.is.not.provided")
        else -> null
    }

private fun checkIfWorkPackageContentIsProvided(workPackages: List<ProjectWorkPackageData>?) =
    when {
        workPackages != null &&
                workPackages.any { workPackage ->
                    isActivitiesContentMissing(workPackage.activities) ||
                    isOutputsContentMissing(workPackage.outputs) ||
                    isInvestmentsContentMissing(workPackage.investments)
                } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            workPackages.forEach { workPackage ->
                if (workPackage.activities.isEmpty()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_C_ERROR_MESSAGES_PREFIX.at.least.one.work.package.activity.should.be.added",
                            mapOf("id" to ("WP" + workPackage.workPackageNumber.toString()))
                        )
                    )
                }
                val errorInvestmentsMessages = checkIfInvestmentsAreValid(workPackage.workPackageNumber, workPackage.investments)
                if (errorInvestmentsMessages.isNotEmpty()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessages(
                            "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.investments",
                            mapOf("id" to ("WP" + workPackage.workPackageNumber.toString())),
                            errorInvestmentsMessages
                        )
                    )
                }
                val errorActivitiesMessages = checkIfActivitiesAreValid(workPackage.workPackageNumber, workPackage.activities)
                if (errorActivitiesMessages.isNotEmpty()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessages(
                            "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.activity",
                            mapOf("id" to ("WP" + workPackage.workPackageNumber.toString())),
                            errorActivitiesMessages
                        )
                    )
                }
                val errorOutputMessages = checkIfOutputsAreValid(workPackage.workPackageNumber, workPackage.outputs)
                if (errorOutputMessages.isNotEmpty()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessages(
                            "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.output",
                            mapOf("id" to ("WP" + workPackage.workPackageNumber.toString())),
                            errorOutputMessages
                        )
                    )
                }
            }
            if (errorMessages.count() > 0) {
                buildErrorPreConditionCheckMessages(
                    "$SECTION_C_ERROR_MESSAGES_PREFIX.project.c4.content",
                    messageArgs = emptyMap(),
                    errorMessages
                )
            }
            else
            {
                null
            }
        }
        else -> null
    }


private fun checkIfAtLeastOneWorkPackageIsAdded(workPackages: List<ProjectWorkPackageData>?) =
    when {
        workPackages.isNullOrEmpty() -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.at.least.one.work.package.should.be.added")
        else -> null
    }

private fun checkIfNamesOfWorkPackagesAreProvided(workPackages: List<ProjectWorkPackageData>?) =
    when {
        !isFieldVisible(ApplicationFormFieldId.PROJECT_WORK_PACKAGE_TITLE) -> null
        workPackages.isNullOrEmpty() -> null
        workPackages.any { it.name.isNotFullyTranslated(CallDataContainer.get().inputLanguages) }
            -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.names.of.work.packages.should.be.added")
        else -> null
    }
// Amund CE check: max 5 WPs in total
private fun checkIfMoreThan5WorkPackagesAreAdded(workPackages: List<ProjectWorkPackageData>?) =
    when {
        workPackages?.size!! > 5 -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.max.5.work.packages")
        else -> null
    }

private fun checkIfObjectivesOfWorkPackagesAreProvided(workPackages: List<ProjectWorkPackageData>?) =
    when {
        workPackages.isNullOrEmpty() -> null
        workPackages.any { it.objectiveAndAudience.isNotFullyTranslated(CallDataContainer.get().inputLanguages) ||
                it.specificObjective.isNotFullyTranslated(CallDataContainer.get().inputLanguages) } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            workPackages.forEach { workPackage ->
                if (isFieldVisible(ApplicationFormFieldId.PROJECT_SPECIFIC_OBJECTIVE) &&
                    workPackage.specificObjective.isNotFullyTranslated(CallDataContainer.get().inputLanguages)) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.specific.objective.is.not.provided",
                            mapOf("id" to ("WP" + workPackage.workPackageNumber.toString()))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PROJECT_COMMUNICATION_OBJECTIVES_AND_TARGET_AUDIENCE) &&
                    workPackage.objectiveAndAudience.isNotFullyTranslated(CallDataContainer.get().inputLanguages)) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.audience.objective.is.not.provided",
                            mapOf("id" to ("WP" + workPackage.workPackageNumber.toString()))
                        )
                    )
                }
            }
            if (errorMessages.count() > 0) {
                buildErrorPreConditionCheckMessages(
                    "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.objectives",
                    messageArgs = emptyMap(),
                    errorMessages
                )
            }
            else
            {
                null
            }
        }
        else -> null
    }

private fun checkIfAtLeastOneOutputForEachWorkPackageIsAdded(workPackages: List<ProjectWorkPackageData>?) =
    when {
        workPackages.isNullOrEmpty() -> null
        workPackages.any { it.outputs.isNullOrEmpty() } -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.at.least.one.output.for.each.work.package.should.be.added")
        else -> null
    }

private fun checkIfAtLeastOneResultIsAdded(results: List<ProjectResultData>?) =
    when {
        !isFieldVisible(ApplicationFormFieldId.PROJECT_RESULTS_PROGRAMME_RESULT_INDICATOR_AMD_MEASUREMENT_UNIT) &&
        !isFieldVisible(ApplicationFormFieldId.PROJECT_RESULTS_TARGET_VALUE) &&
        !isFieldVisible(ApplicationFormFieldId.PROJECT_RESULTS_DELIVERY_PERIOD) &&
        !isFieldVisible(ApplicationFormFieldId.PROJECT_RESULTS_DESCRIPTION) -> null
        results.isNullOrEmpty() -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.at.least.one.result.should.be.added")
        else -> null
    }

private fun checkIfResultContentIsProvided(projectData: ProjectDataSectionC?) =
    when {
        projectData?.projectResults != null &&
            projectData.projectResults.any { result ->
            result.programmeResultIndicatorId ?: 0 <= 0 ||
            result.targetValue ?: BigDecimal.ZERO <= BigDecimal.ZERO ||
            result.periodNumber ?: 0 <= 0 ||
            result.description.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            projectData.projectResults.forEach { result ->
                if (isFieldVisible(ApplicationFormFieldId.PROJECT_RESULTS_PROGRAMME_RESULT_INDICATOR_AMD_MEASUREMENT_UNIT) &&
                    result.programmeResultIndicatorId ?: 0 <= 0) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_C_ERROR_MESSAGES_PREFIX.project.result.indicator.is.not.provided",
                            mapOf("name" to (result.resultNumber.toString()))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PROJECT_RESULTS_TARGET_VALUE) && result.targetValue ?: BigDecimal.ZERO <= BigDecimal.ZERO) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_C_ERROR_MESSAGES_PREFIX.project.result.target.is.not.provided",
                            mapOf("name" to (result.resultNumber.toString()))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PROJECT_RESULTS_DELIVERY_PERIOD) && result.periodNumber ?: 0 <= 0) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_C_ERROR_MESSAGES_PREFIX.project.result.delivery.is.not.provided",
                            mapOf("name" to (result.resultNumber.toString()))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PROJECT_RESULTS_DESCRIPTION) &&
                    result.description.isNotFullyTranslated(CallDataContainer.get().inputLanguages)) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_C_ERROR_MESSAGES_PREFIX.project.result.description.is.not.provided",
                            mapOf("name" to (result.resultNumber.toString()))
                        )
                    )
                }
            }
            if (errorMessages.count() > 0) {
                buildErrorPreConditionCheckMessages(
                    "$SECTION_C_ERROR_MESSAGES_PREFIX.project.c5.content",
                    messageArgs = emptyMap(),
                    errorMessages
                )
            }
            else
            {
                null
            }
        }
        else -> null
    }

private fun checkIfFinancialManagementIsProvided(financialManagement: Set<InputTranslationData>?) =
    when {
        !isFieldVisible(ApplicationFormFieldId.PROJECT_FINANCIAL_MANAGEMENT_AND_REPORTING) -> null
        financialManagement.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
        -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.financial.management.should.be.provided")
        else -> null
    }

private fun checkIfSelectedCooperationCriteriaAreValid(cooperationCriteria: ProjectCooperationCriteriaData?) =
    when {
        !isFieldVisible(ApplicationFormFieldId.PROJECT_COOPERATION_CRITERIA) -> null
        cooperationCriteria == null || !cooperationCriteria.projectJointDevelopment || !cooperationCriteria.projectJointImplementation ||
                !(cooperationCriteria.projectJointFinancing || cooperationCriteria.projectJointStaffing) ->
            buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.selected.cooperation.criteria.are.not.valid")
        else -> null
    }

private fun checkIfDescriptionForAllSelectedCooperationCriteriaIsProvided(projectManagement: ProjectManagementData?) =
    when {
        projectManagement?.projectCooperationCriteria == null -> null
        isJointDevelopmentSelectedAndHasMissingTranslation(projectManagement) ||
                isJointImplementationSelectedAndHasMissingTranslation(projectManagement) ||
                isJointStaffingSelectedAndHasMissingTranslation(projectManagement) ->
            buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.description.for.selected.cooperation.criteria.is.not.provided")
        else -> null
    }

private fun checkIfTypeOfContributionsAreProvided(projectHorizontalPrinciples: ProjectHorizontalPrinciplesData?) =
    when {
        !isFieldVisible(ApplicationFormFieldId.PROJECT_HORIZONTAL_PRINCIPLES) -> null
        projectHorizontalPrinciples?.sustainableDevelopmentCriteriaEffect == null ||
                projectHorizontalPrinciples.equalOpportunitiesEffect == null ||
                projectHorizontalPrinciples.sexualEqualityEffect == null ->
            buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.type.of.contribution.in.horizontal.principles.are.not.valid")
        else -> null
    }

private fun checkIfDescriptionForTypeOfContributionIsProvided(projectManagement: ProjectManagementData?) =
    when {
        !isFieldVisible(ApplicationFormFieldId.PROJECT_HORIZONTAL_PRINCIPLES) -> null
        isSustainableDevelopmentCriteriaEffectNotNeutralAndHasMissingTranslation(projectManagement) ||
                isEqualOpportunitiesEffectNotNeutralAndHasMissingTranslation(projectManagement) ||
                isSexualEqualityEffectNotNeutralAndHasMissingTranslation(projectManagement) ->
            buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.description.for.type.of.contribution.in.horizontal.principles.is.not.provided")
        else -> null
    }

private fun checkIfCoordinateProjectIsValid(projectManagement: ProjectManagementData?) =
    when {
        !isFieldVisible(ApplicationFormFieldId.PROJECT_COORDINATION) -> null
        projectManagement == null ||
        projectManagement.projectCoordination.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
        -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.c71.is.not.provided")
        else -> null
    }

private fun checkIfMeasuresQualityIsValid(projectManagement: ProjectManagementData?) =
    when {
        !isFieldVisible(ApplicationFormFieldId.PROJECT_QUALITY_MEASURES) -> null
        projectManagement == null || projectManagement.projectQualityAssurance.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
        -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.c72.is.not.provided")
        else -> null
    }

private fun checkIfCommunicationIsValid(projectManagement: ProjectManagementData?) =
    when {
        !isFieldVisible(ApplicationFormFieldId.PROJECT_COMMUNICATION_APPROACH) -> null
        projectManagement == null || projectManagement.projectCommunication.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
        -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.c73.is.not.provided")
        else -> null
    }

private fun checkIfOwnershipIsValid(projectLongTermPlans: ProjectLongTermPlansData?) =
    when {
        !isFieldVisible(ApplicationFormFieldId.PROJECT_OWNERSHIP) -> null
        projectLongTermPlans == null || projectLongTermPlans.projectOwnership.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
        -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.c81.is.not.provided")
        else -> null
    }

private fun checkIfDurabilityIsValid(projectLongTermPlans: ProjectLongTermPlansData?) =
    when {
        !isFieldVisible(ApplicationFormFieldId.PROJECT_DURABILITY) -> null
        projectLongTermPlans == null || projectLongTermPlans.projectDurability.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
        -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.c82.is.not.provided")
        else -> null
    }

private fun checkIfTransferabilityIsValid(projectLongTermPlans: ProjectLongTermPlansData?) =
    when {
        !isFieldVisible(ApplicationFormFieldId.PROJECT_TRANSFERABILITY) -> null
        projectLongTermPlans == null || projectLongTermPlans.projectTransferability.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
        -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.c83.is.not.provided")
        else -> null
    }

private fun checkIfActivitiesAreValid(workPackageNumber: Int, activities: List<WorkPackageActivityData>): List<PreConditionCheckMessage> {
    val errorActivitiesMessages = mutableListOf<PreConditionCheckMessage>()
    if (activities.isNotEmpty()) {
        activities.forEach { activity ->
            if (isFieldVisible(ApplicationFormFieldId.PROJECT_ACTIVITIES_TITLE) &&
                activity.title.isNotFullyTranslated(CallDataContainer.get().inputLanguages)) {
                errorActivitiesMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.activity.title.is.not.provided",
                        mapOf("id" to (workPackageNumber.toString() + "." + activity.activityNumber.toString()))
                    )
                )
            }
            if (isFieldVisible(ApplicationFormFieldId.PROJECT_ACTIVITIES_DESCRIPTION) &&
                activity.description.isNotFullyTranslated(CallDataContainer.get().inputLanguages)) {
                errorActivitiesMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.activity.description.is.not.provided",
                        mapOf("id" to (workPackageNumber.toString() + "." + activity.activityNumber.toString()))
                    )
                )
            }
            if (isFieldVisible(ApplicationFormFieldId.PROJECT_ACTIVITIES_START_PERIOD) &&
                activity.startPeriod ?: 0 <= 0) {
                errorActivitiesMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.activity.start.period.is.not.provided",
                        mapOf("id" to (workPackageNumber.toString() + "." + activity.activityNumber.toString()))
                    )
                )
            }
            if (isFieldVisible(ApplicationFormFieldId.PROJECT_ACTIVITIES_END_PERIOD) &&
                activity.endPeriod ?: 0 <= 0) {
                errorActivitiesMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.activity.end.period.is.not.provided",
                        mapOf("id" to (workPackageNumber.toString() + "." + activity.activityNumber.toString()))
                    )
                )
            }
            if (isFieldVisible(ApplicationFormFieldId.PROJECT_ACTIVITIES_DELIVERABLES) &&
                activity.deliverables.isEmpty()) {
                errorActivitiesMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.activity.deliverable.is.not.provided",
                        mapOf("id" to (workPackageNumber.toString() + "." + activity.activityNumber.toString()))
                    )
                )
            }
            if (isFieldVisible(ApplicationFormFieldId.PROJECT_ACTIVITIES_DELIVERABLES) &&
                activity.deliverables.any {deliverable -> deliverable.period == null ||
                        deliverable.description.isNotFullyTranslated(CallDataContainer.get().inputLanguages) }) {
                activity.deliverables.forEach { deliverable ->
                    errorActivitiesMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.activity.deliverable.delivery.period.or.description.is.not.provided",
                            mapOf("id" to (workPackageNumber.toString() + "." + activity.activityNumber.toString() + "." + deliverable.deliverableNumber.toString()))
                        )
                    )
                }
            }
            if (isFieldVisible(ApplicationFormFieldId.PROJECT_ACTIVITIES_STATE_AID_PARTNERS_INVOLVED) &&
                activity.partnerIds.isEmpty()) {
                errorActivitiesMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.activity.partner.is.not.provided",
                        mapOf("id" to (workPackageNumber.toString() + "." + activity.activityNumber.toString()))
                    )
                )
            }
        }
    } else {
        errorActivitiesMessages.add(
            buildErrorPreConditionCheckMessage(
                "$SECTION_C_ERROR_MESSAGES_PREFIX.at.least.one.work.package.activity.should.be.added")
        )
    }
    return errorActivitiesMessages
}

private fun checkIfOutputsAreValid(workPackageNumber: Int, outputs: List<WorkPackageOutputData>): List<PreConditionCheckMessage> {
    val errorOutputsMessages = mutableListOf<PreConditionCheckMessage>()
    if (outputs.isNotEmpty()) {
        outputs.forEach { output ->
            if (isFieldVisible(ApplicationFormFieldId.PROJECT_OUTPUT_TITLE) &&
                output.title.isNotFullyTranslated(CallDataContainer.get().inputLanguages)) {
                errorOutputsMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.output.title.is.not.provided",
                        mapOf("id" to (workPackageNumber.toString() + "." + output.outputNumber.toString()))
                    )
                )
            }
            if (isFieldVisible(ApplicationFormFieldId.PROJECT_OUTPUT_DESCRIPTION) &&
                output.description.isNotFullyTranslated(CallDataContainer.get().inputLanguages)) {
                errorOutputsMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.output.description.is.not.provided",
                        mapOf("id" to (workPackageNumber.toString() + "." + output.outputNumber.toString()))
                    )
                )
            }
            if (output.programmeOutputIndicatorId ?: 0 <= 0) {
                errorOutputsMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.output.indicator.is.not.provided",
                        mapOf("id" to (workPackageNumber.toString() + "." + output.outputNumber.toString()))
                    )
                )
            }
            if (output.targetValue ?: BigDecimal.ZERO <= BigDecimal.ZERO) {
                errorOutputsMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.output.value.is.not.provided",
                        mapOf("id" to (workPackageNumber.toString() + "." + output.outputNumber.toString()))
                    )
                )
            }
            if (output.periodNumber ?: 0 <= 0) {
                errorOutputsMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.output.delivery.period.is.not.provided",
                        mapOf("id" to (workPackageNumber.toString() + "." + output.outputNumber.toString()))
                    )
                )
            }
        }
    } else {
        errorOutputsMessages.add(
            buildErrorPreConditionCheckMessage(
                "$SECTION_C_ERROR_MESSAGES_PREFIX.at.least.one.work.package.output.should.be.added")
        )
    }
    return errorOutputsMessages
}

private fun checkIfInvestmentsAreValid(workPackageNumber: Int, investments: List<WorkPackageInvestmentData>): List<PreConditionCheckMessage> {
    val errorInvestmentsMessages = mutableListOf<PreConditionCheckMessage>()
    if (investments.isEmpty()) {
        return errorInvestmentsMessages;
    }
    investments.forEach { investment ->
        if (isFieldVisible(ApplicationFormFieldId.PROJECT_INVESTMENT_TITLE) &&
            investment.title.isNotFullyTranslated(CallDataContainer.get().inputLanguages)) {
            errorInvestmentsMessages.add(
                buildErrorPreConditionCheckMessage(
                    "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.investments.title.is.not.provided",
                    mapOf("id" to (workPackageNumber.toString() + "." + investment.investmentNumber.toString()))
                )
            )
        }
        if (isFieldVisible(ApplicationFormFieldId.PROJECT_INVESTMENT_WHY_IS_INVESTMENT_NEEDED) &&
            investment.justificationExplanation.isNotFullyTranslated(CallDataContainer.get().inputLanguages)) {
            errorInvestmentsMessages.add(
                buildErrorPreConditionCheckMessage(
                    "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.investments.justification.explain.is.not.provided",
                    mapOf("id" to (workPackageNumber.toString() + "." + investment.investmentNumber.toString()))
                )
            )
        }
        if (isFieldVisible(ApplicationFormFieldId.PROJECT_INVESTMENT_WHO_IS_BENEFITING) &&
            investment.justificationBenefits.isNotFullyTranslated(CallDataContainer.get().inputLanguages)) {
            errorInvestmentsMessages.add(
                buildErrorPreConditionCheckMessage(
                    "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.investments.benefiting.is.not.provided",
                    mapOf("id" to (workPackageNumber.toString() + "." + investment.investmentNumber.toString()))
                )
            )
        }
        if (isFieldVisible(ApplicationFormFieldId.PROJECT_INVESTMENT_CROSS_BORDER_TRANSNATIONAL_RELEVANCE_OF_INVESTMENT) &&
            investment.justificationTransactionalRelevance.isNotFullyTranslated(CallDataContainer.get().inputLanguages)) {
            errorInvestmentsMessages.add(
                buildErrorPreConditionCheckMessage(
                    "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.investments.transnational.relevance.is.not.provided",
                    mapOf("id" to (workPackageNumber.toString() + "." + investment.investmentNumber.toString()))
                )
            )
        }
        if (isFieldVisible(ApplicationFormFieldId.PROJECT_INVESTMENT_WHO_OWNS_THE_INVESTMENT_SITE) &&
            investment.ownershipSiteLocation.isNotFullyTranslated(CallDataContainer.get().inputLanguages)) {
            errorInvestmentsMessages.add(
                buildErrorPreConditionCheckMessage(
                    "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.investments.site.owner.is.not.provided",
                    mapOf("id" to (workPackageNumber.toString() + "." + investment.investmentNumber.toString()))
                )
            )
        }
        if (isFieldVisible(ApplicationFormFieldId.PROJECT_INVESTMENT_OWNERSHIP_AFTER_END_OF_PROJECT) &&
            investment.ownershipRetain.isNotFullyTranslated(CallDataContainer.get().inputLanguages)) {
            errorInvestmentsMessages.add(
                buildErrorPreConditionCheckMessage(
                    "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.investments.end.project.is.not.provided",
                    mapOf("id" to (workPackageNumber.toString() + "." + investment.investmentNumber.toString()))
                )
            )
        }
        if (isFieldVisible(ApplicationFormFieldId.PROJECT_INVESTMENT_MAINTENANCE) &&
            investment.ownershipMaintenance.isNotFullyTranslated(CallDataContainer.get().inputLanguages)) {
            errorInvestmentsMessages.add(
                buildErrorPreConditionCheckMessage(
                    "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.investments.maintenance.is.not.provided",
                    mapOf("id" to (workPackageNumber.toString() + "." + investment.investmentNumber.toString()))
                )
            )
        }
        if (isFieldVisible(ApplicationFormFieldId.PROJECT_INVESTMENT_RISK) &&
            investment.risk.isNotFullyTranslated(CallDataContainer.get().inputLanguages)) {
            errorInvestmentsMessages.add(
                buildErrorPreConditionCheckMessage(
                    "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.investments.risk.is.not.provided",
                    mapOf("id" to (workPackageNumber.toString() + "." + investment.investmentNumber.toString()))
                )
            )
        }
        if (isFieldVisible(ApplicationFormFieldId.PROJECT_INVESTMENT_DOCUMENTATION) &&
            investment.documentation.isNotFullyTranslated(CallDataContainer.get().inputLanguages)) {
            errorInvestmentsMessages.add(
                buildErrorPreConditionCheckMessage(
                    "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.investments.documentation.is.not.provided",
                    mapOf("id" to (workPackageNumber.toString() + "." + investment.investmentNumber.toString()))
                )
            )
        }
    }
    return errorInvestmentsMessages
}

private fun checkIfProjectPpartnershipIsAdded(projectPartnership: ProjectPartnershipData?) =
    when {
        !isFieldVisible(ApplicationFormFieldId.PROJECT_PARTNERSHIP) -> null
        projectPartnership?.partnership == null ||
        projectPartnership.partnership.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
        -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.partnership.is.not.provided")
        else -> null
    }

private fun isSustainableDevelopmentCriteriaEffectNotNeutralAndHasMissingTranslation(projectManagement: ProjectManagementData?) =
    projectManagement?.projectHorizontalPrinciples?.sustainableDevelopmentCriteriaEffect != ProjectHorizontalPrinciplesEffectData.Neutral
            && projectManagement?.sustainableDevelopmentDescription.isNotFullyTranslated(CallDataContainer.get().inputLanguages)

private fun isEqualOpportunitiesEffectNotNeutralAndHasMissingTranslation(projectManagement: ProjectManagementData?) =
    projectManagement?.projectHorizontalPrinciples?.equalOpportunitiesEffect != ProjectHorizontalPrinciplesEffectData.Neutral
            && projectManagement?.equalOpportunitiesDescription.isNotFullyTranslated(CallDataContainer.get().inputLanguages)

private fun isSexualEqualityEffectNotNeutralAndHasMissingTranslation(projectManagement: ProjectManagementData?) =
    projectManagement?.projectHorizontalPrinciples?.sexualEqualityEffect != ProjectHorizontalPrinciplesEffectData.Neutral
            && projectManagement?.sexualEqualityDescription.isNotFullyTranslated(CallDataContainer.get().inputLanguages)

private fun isJointDevelopmentSelectedAndHasMissingTranslation(projectManagement: ProjectManagementData) =
    projectManagement.projectCooperationCriteria?.projectJointDevelopment == true
            && projectManagement.projectJointDevelopmentDescription.isNotFullyTranslated(CallDataContainer.get().inputLanguages)

private fun isJointImplementationSelectedAndHasMissingTranslation(projectManagement: ProjectManagementData) =
    projectManagement.projectCooperationCriteria?.projectJointImplementation == true
            && projectManagement.projectJointImplementationDescription.isNotFullyTranslated(CallDataContainer.get().inputLanguages)

private fun isJointStaffingSelectedAndHasMissingTranslation(projectManagement: ProjectManagementData) =
    projectManagement.projectCooperationCriteria?.projectJointStaffing == true
            && projectManagement.projectJointStaffingDescription.isNotFullyTranslated(CallDataContainer.get().inputLanguages)

private fun isActivitiesContentMissing(activities: List<WorkPackageActivityData>) =
    activities.isEmpty() ||
        (activities.isNotEmpty() &&
            activities.any
            {
                activity -> activity.title.isNotFullyTranslated(CallDataContainer.get().inputLanguages) ||
                activity.description.isNotFullyTranslated(CallDataContainer.get().inputLanguages) ||
                activity.startPeriod ?: 0 <= 0 ||
                activity.endPeriod ?: 0 <= 0 ||
                    activity.deliverables.isEmpty()
            }
        )

private fun isOutputsContentMissing(outputs: List<WorkPackageOutputData>) =
    outputs.isEmpty() ||
        (outputs.isNotEmpty() &&
            outputs.any
            {
                output -> output.title.isNotFullyTranslated(CallDataContainer.get().inputLanguages) ||
                output.description.isNotFullyTranslated(CallDataContainer.get().inputLanguages) ||
                output.targetValue ?: BigDecimal.ZERO <= BigDecimal.ZERO ||
                output.periodNumber ?: 0 <= 0 ||
                output.programmeOutputIndicatorId ?: 0 <= 0
            }
        )

private fun isInvestmentsContentMissing(investments: List<WorkPackageInvestmentData>) =
    investments.isEmpty() ||
        (investments.isNotEmpty() &&
            investments.any
            {
                investment -> investment.title.isNotFullyTranslated(CallDataContainer.get().inputLanguages) ||
                investment.justificationExplanation.isNotFullyTranslated(CallDataContainer.get().inputLanguages) ||
                investment.justificationBenefits.isNotFullyTranslated(CallDataContainer.get().inputLanguages) ||
                investment.justificationTransactionalRelevance.isNotFullyTranslated(CallDataContainer.get().inputLanguages) ||
                investment.ownershipSiteLocation.isNotFullyTranslated(CallDataContainer.get().inputLanguages) ||
                investment.ownershipRetain.isNotFullyTranslated(CallDataContainer.get().inputLanguages) ||
                investment.ownershipMaintenance.isNotFullyTranslated(CallDataContainer.get().inputLanguages) ||
                investment.risk.isNotFullyTranslated(CallDataContainer.get().inputLanguages) ||
                investment.documentation.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
            }
        )

private fun isFieldVisible(fieldId: ApplicationFormFieldId): Boolean {
    return isFieldVisible(fieldId, LifecycleDataContainer.get()!!, CallDataContainer.get())
}

