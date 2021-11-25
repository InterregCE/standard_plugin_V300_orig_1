package io.cloudflight.jems.plugin.standard.pre_condition_check

import io.cloudflight.jems.plugin.contract.models.call.CallDetailData
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.project.ApplicationFormFieldId
import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ProjectLifecycleData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.ProjectDataSectionB
import io.cloudflight.jems.plugin.contract.models.project.sectionB.associatedOrganisation.ProjectAssociatedOrganizationData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectContactTypeData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerAddressData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerAddressTypeData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerRoleData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.budget.ProjectPartnerCoFinancingFundTypeData
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckMessage
import io.cloudflight.jems.plugin.standard.pre_condition_check.helpers.CallDataContainer
import io.cloudflight.jems.plugin.standard.pre_condition_check.helpers.LifecycleDataContainer
import java.math.BigDecimal

private const val SECTION_B_MESSAGES_PREFIX = "$MESSAGES_PREFIX.section.b"
private const val SECTION_B_ERROR_MESSAGES_PREFIX = "$SECTION_B_MESSAGES_PREFIX.error"
private const val SECTION_B_INFO_MESSAGES_PREFIX = "$SECTION_B_MESSAGES_PREFIX.info"

fun checkSectionB(sectionBData: ProjectDataSectionB): PreConditionCheckMessage {
    return buildPreConditionCheckMessage(
        messageKey = "$SECTION_B_MESSAGES_PREFIX.header", messageArgs = emptyMap(),

        checkIfAtLeastOnePartnerIsAdded(sectionBData.partners),

        checkIfExactlyOneLeadPartnerIsAdded(sectionBData.partners),

        checkIfPartnerIdentityContentIsProvided(sectionBData.partners),

        checkIfPartnerAddressContentIsProvided(sectionBData.partners),

        checkIfPartnerPersonContentIsProvided(sectionBData.partners),

        checkIfPartnerMotivationContentIsProvided(sectionBData.partners),

        checkIfPartnerAssociatedOrganisationIsProvided(sectionBData.associatedOrganisations),

        checkIfStaffContentIsProvided(sectionBData.partners),

        checkIfTravelAndAccommodationContentIsProvided(sectionBData.partners),

        checkIfExternalExpertiseAndServicesContentIsProvided(sectionBData.partners),

        checkIfEquipmentContentIsProvided(sectionBData.partners),

        checkIfInfrastructureAndWorksContentIsProvided(sectionBData.partners),

        checkIfUnitCostsContentIsProvided(sectionBData.partners),

        checkIfTotalBudgetIsGreaterThanZero(sectionBData.partners),

        checkIfPeriodsAmountSumUpToBudgetEntrySum(sectionBData.partners),

        checkIfTotalCoFinancingIsGreaterThanZero(sectionBData.partners),

        checkIfCoFinancingContentIsProvided(sectionBData.partners),

        checkIfPartnerContributionEqualsToBudget(sectionBData.partners),

        checkIfStateAidIsValid(sectionBData.partners)
    )
}

private fun checkIfAtLeastOnePartnerIsAdded(partners: Set<ProjectPartnerData>?) =
    when {
        partners.isNullOrEmpty() -> buildErrorPreConditionCheckMessage("$SECTION_B_ERROR_MESSAGES_PREFIX.at.least.one.partner.should.be.added")
        else -> buildInfoPreConditionCheckMessage("$SECTION_B_INFO_MESSAGES_PREFIX.at.least.one.partner.is.added")
    }

private fun checkIfExactlyOneLeadPartnerIsAdded(partners: Set<ProjectPartnerData>?) =
    when {
        partners.isNullOrEmpty() || partners.filter { it.role == ProjectPartnerRoleData.LEAD_PARTNER }.size != 1 ->
            buildErrorPreConditionCheckMessage("$SECTION_B_ERROR_MESSAGES_PREFIX.exactly.one.lead.partner.should.be.added")
        else -> buildInfoPreConditionCheckMessage("$SECTION_B_INFO_MESSAGES_PREFIX.exactly.one.lead.partner.is.added")
    }

private fun checkIfTotalCoFinancingIsGreaterThanZero(partners: Set<ProjectPartnerData>) =
    when {
        partners.isNullOrEmpty() -> null
        partners.any { partner -> partner.budget.projectPartnerCoFinancing.finances.any {it.percentage <= BigDecimal.ZERO && it.fundType != ProjectPartnerCoFinancingFundTypeData.PartnerContribution  }} ->
                buildErrorPreConditionCheckMessage("$SECTION_B_ERROR_MESSAGES_PREFIX.total.co.financing.should.be.greater.than.zero")
        else -> buildInfoPreConditionCheckMessage("$SECTION_B_INFO_MESSAGES_PREFIX.total.co.financing.is.greater.than.zero")
    }

private fun checkIfTotalBudgetIsGreaterThanZero(partners: Set<ProjectPartnerData>) =
    when {
        partners.sumOf { it.budget.projectPartnerBudgetTotalCost } <= BigDecimal.ZERO ->
            buildErrorPreConditionCheckMessage("$SECTION_B_ERROR_MESSAGES_PREFIX.total.budget.should.be.greater.than.zero")
        else -> buildInfoPreConditionCheckMessage("$SECTION_B_INFO_MESSAGES_PREFIX.total.budget.is.greater.than.zero")
    }

private fun checkIfCoFinancingContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.isNullOrEmpty() ||
                partners.any { partner ->
                    partner.budget.projectPartnerCoFinancing.partnerContributions.any { partnerContribution -> partnerContribution.amount ?: BigDecimal.ZERO <= BigDecimal.ZERO } ||
                    partner.budget.projectPartnerCoFinancing.partnerContributions.any { partnerContribution -> partnerContribution.status == null } ||
                    partner.budget.projectPartnerCoFinancing.finances.isNullOrEmpty() ||
                    partner.budget.projectPartnerCoFinancing.finances.any { finance -> finance.percentage <= BigDecimal.ZERO }
                } ->
        {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (partner.budget.projectPartnerCoFinancing.finances.isNullOrEmpty()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.co.financing.source.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.budget.projectPartnerCoFinancing.partnerContributions.any { partnerContribution -> partnerContribution.status == null }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.co.financing.legal.status.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
            }
            if (errorMessages.count() > 0) {
                buildErrorPreConditionCheckMessages(
                    "$SECTION_B_ERROR_MESSAGES_PREFIX.co.financing",
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

private fun checkIfPartnerContributionEqualsToBudget(partners: Set<ProjectPartnerData>) =
    when { partners.isNotEmpty()
    -> {
        val errorMessages = mutableListOf<PreConditionCheckMessage>()
        partners.forEach { partner ->
            var fundsAmount = BigDecimal.ZERO
            partner.budget.projectPartnerCoFinancing.finances.forEach { finance ->
                if (finance.fundType != ProjectPartnerCoFinancingFundTypeData.PartnerContribution) {
                    fundsAmount += partner.budget.projectPartnerBudgetTotalCost.percentageDown(finance.percentage)
                }
            }
            fundsAmount = partner.budget.projectPartnerBudgetTotalCost - fundsAmount

            if (partner.budget.projectPartnerCoFinancing.finances.isEmpty() ||
                fundsAmount !=
                partner.budget.projectPartnerCoFinancing.partnerContributions.sumOf { it.amount }
            ) {
                errorMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.partner.contribution.failed",
                        mapOf("name" to (partner.abbreviation))
                    )
                )
            }
            // remove: only need to check that erdf is 80% for all EU Partners
            // test Amund - no erdf if partner outside programme area TODO: depending on critereia (all EU or not) can be merged with ERDF 80% check
            // partner.addresses.forEach { address ->
            //    if (address.type == ProjectPartnerAddressTypeData.Organization && address.country !in listOf("Österreich (AT)", "Deutschland (DE)","Italia (IT)", "Slovenija (SI)", "Slovensko (SK)")){
            //        partner.budget.projectPartnerCoFinancing.finances.forEach { finance ->
            //            if (finance.fundType == ProjectPartnerCoFinancingFundTypeData.MainFund && finance.percentage.intValueExact() != 0) {
            //                errorMessages.add(
            //                    buildErrorPreConditionCheckMessage(
            //                        "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.partner.contribution.no.erdf.outside",
            //                        mapOf("name" to (partner.abbreviation))
            //                    )
            //                )
            //            }
            //        }
            //    }
            //}
            // test Amund - check erdf 80% ADDED: remove for partners outside EU (have no ERDF)
            partner.addresses.forEach { address ->
                if (address.type == ProjectPartnerAddressTypeData.Organization
                    && address.country in listOf("Österreich (AT)", "Belgique/België (BE)", "Bulgaria (BG)", "Hrvatska (HR)", "Kýpros (CY)", "Česko (CZ)", "Danmark (DK)", "Eesti (EE)", "Suomi/Finland (FI)", "France (FR)", "Deutschland (DE)", "Elláda (EL)", "Magyarország (HU)", "Éire/Ireland (IE)", "Italia (IT)", "Latvija (LV)", "Lietuva (LT)", "Luxembourg (LU)", "Malta (MT)", "Nederland (NL)", "Polska (PL)", "Portugal (PT)", "România (RO)", "Slovensko (SK)", "Slovenija (SI)", "España (ES)", "Sverige (SE)")) {
                    partner.budget.projectPartnerCoFinancing.finances.forEach { finance ->
                        if (finance.fundType == ProjectPartnerCoFinancingFundTypeData.MainFund && finance.percentage.intValueExact() != 80) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.partner.contribution.erdf.not.80",
                                    mapOf(
                                        "name" to (partner.abbreviation),
                                        "erdf" to (finance.percentage.intValueExact().toString())
                                    )
                                )
                            )
                        }
                    }
                }
            }
            // test Amund - BL2 15% mandatory when not using Other costs flat rate
            if (partner.budget.projectPartnerOptions?.otherCostsOnStaffCostsFlatRate == null
                && partner.budget.projectPartnerOptions?.officeAndAdministrationOnStaffCostsFlatRate != 15) {
                errorMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.partner.contribution.office.and.admin.not.15",
                        mapOf("name" to (partner.abbreviation))
                    )
                )
            }

            // test Amund - check flatrates depending on countries
            partner.addresses.forEach { address ->
                if (partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate != null
                    && address.type == ProjectPartnerAddressTypeData.Organization
                    && address.country in listOf("Österreich (AT)", "Deutschland (DE)")
                    && partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate != 5) {
                    errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.partner.contribution.wrong.AT.DE.travel.flat.rate",
                                    mapOf("name" to (partner.abbreviation), "flatrate" to (partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate.toString()))
                            )
                    )
                }
                else if (partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate != null
                    && address.type == ProjectPartnerAddressTypeData.Organization
                    && address.country in listOf("Italia (IT)", "Slovenija (SI)", "Slovensko (SK)")
                    && partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate != 6) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.partner.contribution.wrong.it.si.sk.travel.flat.rate",
                            mapOf("name" to (partner.abbreviation), "flatrate" to (partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate.toString()))
                        )
                    )
                }
                else if (partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate != null
                    && address.type == ProjectPartnerAddressTypeData.Organization
                    && address.country == "Česko (CZ)"
                    && partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate != 7) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.partner.contribution.wrong.cz.travel.flat.rate",
                            mapOf("name" to (partner.abbreviation), "flatrate" to (partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate.toString()))
                        )
                    )
                }
                else if (partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate != null
                    && address.type == ProjectPartnerAddressTypeData.Organization
                    && address.country == "Magyarország (HU)"
                    && partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate != 8) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.partner.contribution.wrong.hu.travel.flat.rate",
                            mapOf("name" to (partner.abbreviation), "flatrate" to (partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate.toString()))
                        )
                    )
                }
                else if (partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate != null
                    && address.type == ProjectPartnerAddressTypeData.Organization
                    && address.country == "Polska (PL)"
                    && partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate != 9) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.partner.contribution.wrong.pl.travel.flat.rate",
                            mapOf("name" to (partner.abbreviation), "flatrate" to (partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate.toString()))
                        )
                    )
                }
                else if (partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate != null
                    && address.type == ProjectPartnerAddressTypeData.Organization
                    && address.country == "Hrvatska (HR)"
                    && partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate != 11) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.partner.contribution.wrong.hr.travel.flat.rate",
                            mapOf("name" to (partner.abbreviation), "flatrate" to (partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate.toString()))
                        )
                    )
                }
            }

        }
        if (errorMessages.size > 0) {
            buildErrorPreConditionCheckMessages(
                "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.partner.contribution",
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

private fun checkIfPeriodsAmountSumUpToBudgetEntrySum(partners: Set<ProjectPartnerData>) =
    when {
        !isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_PERIODS) -> null
        partners.isNullOrEmpty() ||
            partners.any { partner ->
                partner.budget.projectPartnerBudgetCosts.staffCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } } ||
                    partner.budget.projectPartnerBudgetCosts.travelCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } } ||
                    partner.budget.projectPartnerBudgetCosts.externalCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } } ||
                    partner.budget.projectPartnerBudgetCosts.equipmentCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } } ||
                    partner.budget.projectPartnerBudgetCosts.infrastructureCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } } ||
                    partner.budget.projectPartnerBudgetCosts.unitCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } }
            } ->
        {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (partner.budget.projectPartnerBudgetCosts.staffCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } } ||
                    partner.budget.projectPartnerBudgetCosts.travelCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } } ||
                    partner.budget.projectPartnerBudgetCosts.externalCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } } ||
                    partner.budget.projectPartnerBudgetCosts.equipmentCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } } ||
                    partner.budget.projectPartnerBudgetCosts.infrastructureCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } } ||
                    partner.budget.projectPartnerBudgetCosts.unitCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.allocation.periods.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
            }
            if (errorMessages.count() > 0) {
                buildErrorPreConditionCheckMessages(
                    "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.allocation.periods",
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

private fun checkIfStaffContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.budget.projectPartnerBudgetCosts.staffCosts.any { budgetEntry ->
                (budgetEntry.unitCostId == null && budgetEntry.unitType.isNotFullyTranslated(CallDataContainer.get().inputLanguages)) ||
                        budgetEntry.numberOfUnits <= BigDecimal.ZERO ||
                        budgetEntry.pricePerUnit <= BigDecimal.ZERO ||
                        budgetEntry.description.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
            }
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_STAFF_COST_UNIT_TYPE_AND_NUMBER_OF_UNITS) &&
                    partner.budget.projectPartnerBudgetCosts.staffCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.unitType.isNotFullyTranslated(CallDataContainer.get().inputLanguages) }
                ) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.type.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_STAFF_COST_UNIT_TYPE_AND_NUMBER_OF_UNITS) &&
                    partner.budget.projectPartnerBudgetCosts.staffCosts.any { budgetEntry -> budgetEntry.numberOfUnits <= BigDecimal.ZERO }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.no.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_STAFF_COST_PRICE_PER_UNIT) &&
                    partner.budget.projectPartnerBudgetCosts.staffCosts.any { budgetEntry -> budgetEntry.pricePerUnit <= BigDecimal.ZERO }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.price.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_STAFF_COST_STAFF_FUNCTION) &&
                    partner.budget.projectPartnerBudgetCosts.staffCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.description.isNotFullyTranslated(CallDataContainer.get().inputLanguages) }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.description.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
            }
            if (errorMessages.count() > 0) {
                buildErrorPreConditionCheckMessages(
                    "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.staff.costs",
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

private fun checkIfTravelAndAccommodationContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.budget.projectPartnerBudgetCosts.travelCosts.any { budgetEntry ->
                (budgetEntry.unitCostId == null && budgetEntry.unitType.isNotFullyTranslated(CallDataContainer.get().inputLanguages)) ||
                        budgetEntry.numberOfUnits <= BigDecimal.ZERO ||
                        budgetEntry.pricePerUnit <= BigDecimal.ZERO ||
                        budgetEntry.description.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
            }
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_TRAVEL_AND_ACCOMMODATION_UNIT_TYPE_AND_NUMBER_OF_UNITS) &&
                    partner.budget.projectPartnerBudgetCosts.travelCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.unitType.isNotFullyTranslated(CallDataContainer.get().inputLanguages) }
                ) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.type.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_TRAVEL_AND_ACCOMMODATION_UNIT_TYPE_AND_NUMBER_OF_UNITS) &&
                    partner.budget.projectPartnerBudgetCosts.travelCosts.any { budgetEntry -> budgetEntry.numberOfUnits <= BigDecimal.ZERO }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.no.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_TRAVEL_AND_ACCOMMODATION_PRICE_PER_UNIT) &&
                    partner.budget.projectPartnerBudgetCosts.travelCosts.any { budgetEntry -> budgetEntry.pricePerUnit <= BigDecimal.ZERO }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.price.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_TRAVEL_AND_ACCOMMODATION_DESCRIPTION) &&
                    partner.budget.projectPartnerBudgetCosts.travelCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.description.isNotFullyTranslated(CallDataContainer.get().inputLanguages) }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.description.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
            }
            if (errorMessages.count() > 0) {
                buildErrorPreConditionCheckMessages(
                    "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.travel.accommodation",
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

private fun checkIfExternalExpertiseAndServicesContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.budget.projectPartnerBudgetCosts.externalCosts.any { budgetEntry ->
                (budgetEntry.unitCostId == null && budgetEntry.unitType.isNotFullyTranslated(CallDataContainer.get().inputLanguages)) ||
                        budgetEntry.numberOfUnits <= BigDecimal.ZERO ||
                        budgetEntry.pricePerUnit <= BigDecimal.ZERO ||
                        budgetEntry.description.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
            }
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_EXTERNAL_EXPERTISE_UNIT_TYPE_AND_NUMBER_OF_UNITS) &&
                    partner.budget.projectPartnerBudgetCosts.externalCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.unitType.isNotFullyTranslated(CallDataContainer.get().inputLanguages) }
                ) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.type.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_EXTERNAL_EXPERTISE_UNIT_TYPE_AND_NUMBER_OF_UNITS) &&
                    partner.budget.projectPartnerBudgetCosts.externalCosts.any { budgetEntry -> budgetEntry.numberOfUnits <= BigDecimal.ZERO }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.no.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_EXTERNAL_EXPERTISE_PRICE_PER_UNIT) &&
                    partner.budget.projectPartnerBudgetCosts.externalCosts.any { budgetEntry -> budgetEntry.pricePerUnit <= BigDecimal.ZERO }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.price.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_EXTERNAL_EXPERTISE_DESCRIPTION) &&
                    partner.budget.projectPartnerBudgetCosts.externalCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.description.isNotFullyTranslated(CallDataContainer.get().inputLanguages) }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.description.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
            }
            if (errorMessages.count() > 0) {
                buildErrorPreConditionCheckMessages(
                    "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.external.expertise",
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

private fun checkIfEquipmentContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.budget.projectPartnerBudgetCosts.equipmentCosts.any { budgetEntry ->
                (budgetEntry.unitCostId == null && budgetEntry.unitType.isNotFullyTranslated(CallDataContainer.get().inputLanguages)) ||
                        budgetEntry.numberOfUnits <= BigDecimal.ZERO ||
                        budgetEntry.pricePerUnit <= BigDecimal.ZERO ||
                        budgetEntry.description.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
            }
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_EQUIPMENT_UNIT_TYPE_AND_NUMBER_OF_UNITS) &&
                    partner.budget.projectPartnerBudgetCosts.equipmentCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.unitType.isNotFullyTranslated(CallDataContainer.get().inputLanguages) }
                ) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.type.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_EQUIPMENT_UNIT_TYPE_AND_NUMBER_OF_UNITS) &&
                    partner.budget.projectPartnerBudgetCosts.equipmentCosts.any { budgetEntry -> budgetEntry.numberOfUnits <= BigDecimal.ZERO }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.no.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_EQUIPMENT_PRICE_PER_UNIT) &&
                    partner.budget.projectPartnerBudgetCosts.equipmentCosts.any { budgetEntry -> budgetEntry.pricePerUnit <= BigDecimal.ZERO }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.price.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_EQUIPMENT_DESCRIPTION) &&
                    partner.budget.projectPartnerBudgetCosts.equipmentCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.description.isNotFullyTranslated(CallDataContainer.get().inputLanguages) }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.description.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
            }
            if (errorMessages.count() > 0) {
                buildErrorPreConditionCheckMessages(
                    "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.equipment",
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

private fun checkIfInfrastructureAndWorksContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.budget.projectPartnerBudgetCosts.infrastructureCosts.any { budgetEntry ->
                (budgetEntry.unitCostId == null && budgetEntry.unitType.isNotFullyTranslated(CallDataContainer.get().inputLanguages)) ||
                        budgetEntry.numberOfUnits <= BigDecimal.ZERO ||
                        budgetEntry.pricePerUnit <= BigDecimal.ZERO ||
                        budgetEntry.description.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
            }
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_INFRASTRUCTURE_AND_WORKS_UNIT_TYPE_AND_NUMBER_OF_UNITS) &&
                    partner.budget.projectPartnerBudgetCosts.infrastructureCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.unitType.isNotFullyTranslated(CallDataContainer.get().inputLanguages) }
                ) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.type.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_INFRASTRUCTURE_AND_WORKS_UNIT_TYPE_AND_NUMBER_OF_UNITS) &&
                    partner.budget.projectPartnerBudgetCosts.infrastructureCosts.any { budgetEntry -> budgetEntry.numberOfUnits <= BigDecimal.ZERO }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.no.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_INFRASTRUCTURE_AND_WORKS_PRICE_PER_UNIT) &&
                    partner.budget.projectPartnerBudgetCosts.infrastructureCosts.any { budgetEntry -> budgetEntry.pricePerUnit <= BigDecimal.ZERO }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.price.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_INFRASTRUCTURE_AND_WORKS_DESCRIPTION) &&
                    partner.budget.projectPartnerBudgetCosts.infrastructureCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.description.isNotFullyTranslated(CallDataContainer.get().inputLanguages) }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.description.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
            }
            if (errorMessages.count() > 0) {
                buildErrorPreConditionCheckMessages(
                    "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.infrastructure.works",
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

private fun checkIfUnitCostsContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.budget.projectPartnerBudgetCosts.unitCosts.any { budgetEntry ->
                budgetEntry.numberOfUnits <= BigDecimal.ZERO ||
                        budgetEntry.unitCostId <= 0
            }
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (partner.budget.projectPartnerBudgetCosts.unitCosts.any { budgetEntry -> budgetEntry.unitCostId <= 0 }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.cost.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_UNIT_COSTS__UNIT_TYPE_AND_NUMBER_OF_UNITS) &&
                    partner.budget.projectPartnerBudgetCosts.unitCosts.any { budgetEntry -> budgetEntry.numberOfUnits <= BigDecimal.ZERO }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.no.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
            }
            buildErrorPreConditionCheckMessages(
                "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.cost",
                messageArgs = emptyMap(),
                errorMessages
            )
        }
        else -> null
    }

private fun checkIfPartnerIdentityContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.abbreviation.isBlank() ||
                    partner.nameInEnglish.isNullOrBlank() ||
                    partner.nameInOriginalLanguage.isNullOrBlank() ||
                    partner.legalStatusId ?: 0 <= 0 ||
                    partner.vat.isNullOrEmpty() ||
                    partner.vatRecovery == null ||

                    // test Amund - must add new if statements here to activate parent error message:
                    partner.vat?.length!! >= 15 ||
                    partner.vat!!.take(2) != partner.addresses.elementAt(0).nutsRegion2?.substringAfterLast("(")?.take(2) ||
                    // TODO: change from elementAT(0) to type = ProjectPartnerAddressTypeData.Organization
                    (partner.addresses.elementAt(0).nutsRegion2?.substringAfterLast("(")?.take(2) == "AT" && Regex("^(ATU)[0-9]{8}\$").matchEntire(partner.vat!!)==null) ||
                    (partner.addresses.elementAt(0).nutsRegion2?.substringAfterLast("(")?.take(2) == "DE" && Regex("^(DE)[0-9]{9}\$").matchEntire(partner.vat!!)==null) ||
                    (partner.addresses.elementAt(0).nutsRegion2?.substringAfterLast("(")?.take(2) == "HR" && Regex("^(HR)[0-9]{11}\$").matchEntire(partner.vat!!)==null) ||
                    (partner.addresses.elementAt(0).nutsRegion2?.substringAfterLast("(")?.take(2) == "CZ" && Regex("^(CZ)[0-9]{8,10}\$").matchEntire(partner.vat!!)==null) ||
                    (partner.addresses.elementAt(0).nutsRegion2?.substringAfterLast("(")?.take(2) == "HU" && Regex("^(HU)[0-9]{8}\$").matchEntire(partner.vat!!)==null) ||
                    (partner.addresses.elementAt(0).nutsRegion2?.substringAfterLast("(")?.take(2) == "IT" && Regex("^(IT)[0-9]{11}\$").matchEntire(partner.vat!!)==null) ||
                    (partner.addresses.elementAt(0).nutsRegion2?.substringAfterLast("(")?.take(2) == "PL" && Regex("^(PL)[0-9]{10}\$").matchEntire(partner.vat!!)==null) ||
                    (partner.addresses.elementAt(0).nutsRegion2?.substringAfterLast("(")?.take(2) == "SK" && Regex("^(SK)[0-9]{10}\$").matchEntire(partner.vat!!)==null) ||
                    (partner.addresses.elementAt(0).nutsRegion2?.substringAfterLast("(")?.take(2) == "SI" && Regex("^(SI)[0-9]{8}\$").matchEntire(partner.vat!!)==null)

        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_ABBREVIATED_ORGANISATION_NAME) && partner.abbreviation.isBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.abbreviated.name.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_ENGLISH_NAME_OF_ORGANISATION) && partner.nameInEnglish.isNullOrBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.organisation.name.english.language.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_ORIGINAL_NAME_OF_ORGANISATION) && partner.nameInOriginalLanguage.isNullOrBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.organisation.name.original.language.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_LEGAL_STATUS) && partner.legalStatusId ?: 0 <= 0) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.legal.status.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_VAT_IDENTIFIER) && partner.vat.isNullOrEmpty()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.vat.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_VAT_RECOVERY) && partner.vatRecovery == null) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.entitled.recover.vat.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }

                // test Amund - VAT validation (Regex) TODO: add check for visible fields
                partner.addresses.forEach { address ->
                    val regexAT = Regex("^(ATU)[0-9]{8}\$")
                    val regexDE = Regex("^(DE)[0-9]{9}\$")
                    val regexHR = Regex("^(HR)[0-9]{11}\$")
                    val regexCZ = Regex("^(CZ)[0-9]{8,10}\$")
                    val regexHU = Regex("^(HU)[0-9]{8}\$")
                    val regexIT = Regex("^(IT)[0-9]{11}\$")
                    val regexPL = Regex("^(PL)[0-9]{10}\$")
                    val regexSK = Regex("^(SK)[0-9]{10}\$")
                    val regexSI = Regex("^(SI)[0-9]{8}\$")


                    if (address.type == ProjectPartnerAddressTypeData.Organization && !partner.vat.isNullOrEmpty()) {
                        val countryCode: String? = address.nutsRegion2?.substringAfterLast("(")?.take(2)
                        if (countryCode == "AT" && regexAT.matchEntire(partner.vat!!) == null) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.vat.wrong.vat.at.regex",
                                    mapOf("name" to (partner.abbreviation))
                                )
                            )
                        }
                        if (countryCode == "DE" && regexDE.matchEntire(partner.vat!!) == null) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.vat.wrong.vat.de.regex",
                                    mapOf("name" to (partner.abbreviation))
                                )
                            )
                        }
                        if (countryCode == "HR" && regexHR.matchEntire(partner.vat!!) == null) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.vat.wrong.vat.hr.regex",
                                    mapOf("name" to (partner.abbreviation))
                                )
                            )
                        }
                        if (countryCode == "CZ" && regexCZ.matchEntire(partner.vat!!) == null) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.vat.wrong.vat.cz.regex",
                                    mapOf("name" to (partner.abbreviation))
                                )
                            )
                        }
                        if (countryCode == "HU" && regexHU.matchEntire(partner.vat!!) == null) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.vat.wrong.vat.hu.regex",
                                    mapOf("name" to (partner.abbreviation))
                                )
                            )
                        }
                        if (countryCode == "IT" && regexIT.matchEntire(partner.vat!!) == null) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.vat.wrong.vat.it.regex",
                                    mapOf("name" to (partner.abbreviation))
                                )
                            )
                        }
                        if (countryCode == "PL" && regexPL.matchEntire(partner.vat!!) == null) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.vat.wrong.vat.pl.regex",
                                    mapOf("name" to (partner.abbreviation))
                                )
                            )
                        }
                        if (countryCode == "SK" && regexSK.matchEntire(partner.vat!!) == null) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.vat.wrong.vat.sk.regex",
                                    mapOf("name" to (partner.abbreviation))
                                )
                            )
                        }
                        if (countryCode == "SI" && regexSI.matchEntire(partner.vat!!) == null) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.vat.wrong.vat.si.regex",
                                    mapOf("name" to (partner.abbreviation))
                                )
                            )
                        }
                    }
                }
            }
            if (errorMessages.count() > 0) {
                buildErrorPreConditionCheckMessages(
                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.identity",
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

private fun checkIfPartnerAddressContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.addresses.isEmpty() || (
                partner.addresses.any { address ->
                    (address.type == ProjectPartnerAddressTypeData.Organization &&
                        (address.country.isNullOrBlank() ||
                        address.nutsRegion2.isNullOrBlank() ||
                        address.nutsRegion3.isNullOrBlank() ||
                        address.street.isNullOrBlank() ||
                        address.houseNumber.isNullOrBlank() ||
                        address.postalCode.isNullOrBlank() ||
                        address.city.isNullOrBlank())) ||
                    (address.type == ProjectPartnerAddressTypeData.Department &&
                        checkIfOneOfAddressFieldTouched(address)
                    ) ||
                            (partner.role.isLead && address.type == ProjectPartnerAddressTypeData.Organization && address.nutsRegion2?.substringAfterLast("(")?.take(2)!! !in listOf("AT", "IT", "HR", "CZ", "HU", "PL", "SI", "SK", "DE"))
                })
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (partner.addresses.isEmpty())
                {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.address.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                partner.addresses.forEach { address ->
                    val messagePostfix = if (address.type == ProjectPartnerAddressTypeData.Organization)
                        "main"
                        else
                        "department"
                    var fieldId = if (address.type == ProjectPartnerAddressTypeData.Organization) ApplicationFormFieldId.PARTNER_MAIN_ADDRESS_COUNTRY_AND_NUTS
                        else ApplicationFormFieldId.PARTNER_SECONDARY_ADDRESS_COUNTRY_AND_NUTS
                    if (isFieldVisible(fieldId) && address.country.isNullOrBlank()) {
                        errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.$messagePostfix.address.country.is.not.provided",
                                mapOf("name" to (partner.abbreviation))
                            )
                        )
                    }
                    if (isFieldVisible(fieldId) && address.nutsRegion2.isNullOrBlank()) {
                        errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.$messagePostfix.address.nuts2.is.not.provided",
                                mapOf("name" to (partner.abbreviation))
                            )
                        )
                    }
                    if (isFieldVisible(fieldId) && address.nutsRegion3.isNullOrBlank()) {
                        errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.$messagePostfix.address.nuts2.nuts3.is.not.provided",
                                mapOf("name" to (partner.abbreviation))
                            )
                        )
                    }
                    fieldId = if (address.type == ProjectPartnerAddressTypeData.Organization) ApplicationFormFieldId.PARTNER_MAIN_ADDRESS_STREET
                        else ApplicationFormFieldId.PARTNER_SECONDARY_ADDRESS_STREET
                    if (isFieldVisible(fieldId) && address.street.isNullOrBlank()) {
                        errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.$messagePostfix.address.street.is.not.provided",
                                mapOf("name" to (partner.abbreviation))
                            )
                        )
                    }
                    fieldId = if (address.type == ProjectPartnerAddressTypeData.Organization) ApplicationFormFieldId.PARTNER_MAIN_ADDRESS_HOUSE_NUMBER
                        else ApplicationFormFieldId.PARTNER_SECONDARY_ADDRESS_HOUSE_NUMBER
                    if (isFieldVisible(fieldId) && address.houseNumber.isNullOrBlank()) {
                        errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.$messagePostfix.address.house.is.not.provided",
                                mapOf("name" to (partner.abbreviation))
                            )
                        )
                    }
                    fieldId = if (address.type == ProjectPartnerAddressTypeData.Organization) ApplicationFormFieldId.PARTNER_MAIN_ADDRESS_POSTAL_CODE
                        else ApplicationFormFieldId.PARTNER_SECONDARY_ADDRESS_POSTAL_CODE
                    if (isFieldVisible(fieldId) && address.postalCode.isNullOrBlank()) {
                        errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.$messagePostfix.address.postal.is.not.provided",
                                mapOf("name" to (partner.abbreviation))
                            )
                        )
                    }
                    fieldId = if (address.type == ProjectPartnerAddressTypeData.Organization) ApplicationFormFieldId.PARTNER_MAIN_ADDRESS_CITY
                        else ApplicationFormFieldId.PARTNER_SECONDARY_ADDRESS_CITY
                    if (isFieldVisible(fieldId) && address.city.isNullOrBlank()) {
                        errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.$messagePostfix.address.city.is.not.provided",
                                mapOf("name" to (partner.abbreviation))
                            )
                        )
                    }
                    // test Amund - Lp must be in CE area (UPDATE: only some regions of DE and IT, not all)
                    if (partner.role.isLead && address.type == ProjectPartnerAddressTypeData.Organization && address.nutsRegion2?.substringAfterLast("(")?.take(2)!! !in listOf("AT", "IT", "HR", "CZ", "HU", "PL", "SI", "SK", "DE")){
                        errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.lead.not.in.programme.area",
                                mapOf("name" to (partner.abbreviation), "countrycode" to (address.nutsRegion2?.substringAfterLast("(")?.take(2)!!))
                            )
                        )
                    }


                }
            }
            if (errorMessages.count() > 0) {
                buildErrorPreConditionCheckMessages(
                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.main.address",
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

private fun checkIfPartnerPersonContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.contacts.isEmpty() || partner.contacts.size < 2 || (
                    partner.contacts.any { contact ->
                        contact.type == ProjectContactTypeData.ContactPerson &&
                            (contact.firstName.isNullOrBlank() ||
                                contact.lastName.isNullOrBlank() ||
                                contact.telephone.isNullOrBlank() ||
                                contact.email.isNullOrBlank())
                    } ||
                    partner.contacts.any { contact ->
                        contact.type == ProjectContactTypeData.LegalRepresentative &&
                            (contact.firstName.isNullOrBlank() ||
                                contact.lastName.isNullOrBlank())
                    })
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (partner.contacts.isEmpty() || partner.contacts.size < 2)
                {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.person.or.representative.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                partner.contacts.forEach { contact ->
                    if (contact.type == ProjectContactTypeData.ContactPerson) {
                        if (isFieldVisible(ApplicationFormFieldId.PARTNER_CONTACT_PERSON_FIRST_NAME) && contact.firstName.isNullOrBlank()) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.person.first.name.is.not.provided",
                                    mapOf("name" to (partner.abbreviation))
                                )
                            )
                        }
                        if (isFieldVisible(ApplicationFormFieldId.PARTNER_CONTACT_PERSON_LAST_NAME) && contact.lastName.isNullOrBlank()) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.person.last.name.is.not.provided",
                                    mapOf("name" to (partner.abbreviation))
                                )
                            )
                        }
                        if (isFieldVisible(ApplicationFormFieldId.PARTNER_CONTACT_PERSON_EMAIL) && contact.email.isNullOrBlank()) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.person.email.is.not.provided",
                                    mapOf("name" to (partner.abbreviation))
                                )
                            )
                        }
                        if (isFieldVisible(ApplicationFormFieldId.PARTNER_CONTACT_PERSON_TELEPHONE) && contact.telephone.isNullOrBlank()) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.person.phone.is.not.provided",
                                    mapOf("name" to (partner.abbreviation))
                                )
                            )
                        }
                    } else {
                        if (isFieldVisible(ApplicationFormFieldId.PARTNER_LEGAL_REPRESENTATIVE_FIRST_NAME) && contact.firstName.isNullOrBlank()) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.representative.first.name.is.not.provided",
                                    mapOf("name" to (partner.abbreviation))
                                )
                            )
                        }
                        if (isFieldVisible(ApplicationFormFieldId.PARTNER_LEGAL_REPRESENTATIVE_LAST_NAME) && contact.lastName.isNullOrBlank()) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.representative.last.name.is.not.provided",
                                    mapOf("name" to (partner.abbreviation))
                                )
                            )
                        }
                    }
                }
            }
            buildErrorPreConditionCheckMessages(
                "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.person",
                messageArgs = emptyMap(),
                errorMessages
            )
        }
        else -> null
    }

private fun checkIfPartnerMotivationContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.motivation?.organizationRelevance.isNotFullyTranslated(CallDataContainer.get().inputLanguages) ||
                    partner.motivation?.organizationRole.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_MOTIVATION_COMPETENCES) && partner.motivation?.organizationRelevance.isNotFullyTranslated(CallDataContainer.get().inputLanguages)) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.motivation.thematic.competences.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_MOTIVATION_ROLE) && partner.motivation?.organizationRole.isNotFullyTranslated(CallDataContainer.get().inputLanguages)) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.motivation.role.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
            }
            if (errorMessages.count() > 0) {
                buildErrorPreConditionCheckMessages(
                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.motivation",
                    messageArgs = emptyMap(),
                    errorMessages
                )
            }
            else {
                null
            }
        }
        else -> null
    }

private fun checkIfPartnerAssociatedOrganisationIsProvided(associatedOrganizations: Set<ProjectAssociatedOrganizationData>) =
    when {
        isFieldVisible(ApplicationFormFieldId.PARTNER_ASSOCIATED_ORGANIZATIONS) &&
        associatedOrganizations.any { associatedOrganization ->
            associatedOrganization.nameInOriginalLanguage.isNullOrBlank() ||
                    associatedOrganization.partner.id ?: 0 <= 0 ||
                    associatedOrganization.roleDescription.isNotFullyTranslated(CallDataContainer.get().inputLanguages) ||
                    (associatedOrganization.address != null &&
                            (associatedOrganization.address?.country.isNullOrBlank() ||
                                    associatedOrganization.address?.nutsRegion2.isNullOrBlank() ||
                                    associatedOrganization.address?.nutsRegion3.isNullOrBlank() ||
                                    associatedOrganization.address?.postalCode.isNullOrBlank() ||
                                    associatedOrganization.address?.houseNumber.isNullOrBlank() ||
                                    associatedOrganization.address?.city.isNullOrBlank()
                                    )
                            ) ||
                    (associatedOrganization.contacts.isNullOrEmpty() ||
                        associatedOrganization.contacts.any { contact ->
                            contact.type == ProjectContactTypeData.ContactPerson &&
                                    (contact.firstName.isNullOrBlank() ||
                                        contact.lastName.isNullOrBlank() ||
                                        contact.telephone.isNullOrBlank() ||
                                        contact.email.isNullOrBlank())
                        }  ||
                        associatedOrganization.contacts.any { contact ->
                            contact.type == ProjectContactTypeData.LegalRepresentative &&
                                    (contact.firstName.isNullOrBlank() ||
                                            contact.lastName.isNullOrBlank())
                        }
                    )
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            associatedOrganizations.forEach { associatedOrganization ->
                if (associatedOrganization.nameInOriginalLanguage.isNullOrBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.name.is.not.provided",
                            mapOf(
                                "name" to (associatedOrganization.nameInOriginalLanguage
                                    ?: associatedOrganization.id.toString())
                            )
                        )
                    )
                }
                if (associatedOrganization.partner.id ?: 0 <= 0) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.partner.is.not.provided",
                            mapOf(
                                "name" to (associatedOrganization.nameInOriginalLanguage
                                    ?: associatedOrganization.id.toString())
                            )
                        )
                    )
                }
                if (associatedOrganization.address?.country.isNullOrBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.country.is.not.provided",
                            mapOf(
                                "name" to (associatedOrganization.nameInOriginalLanguage
                                    ?: associatedOrganization.id.toString())
                            )
                        )
                    )
                }
                if (associatedOrganization.address?.nutsRegion2.isNullOrBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.nuts2.is.not.provided",
                            mapOf(
                                "name" to (associatedOrganization.nameInOriginalLanguage
                                    ?: associatedOrganization.id.toString())
                            )
                        )
                    )
                }
                if (associatedOrganization.address?.nutsRegion3.isNullOrBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.nuts3.is.not.provided",
                            mapOf(
                                "name" to (associatedOrganization.nameInOriginalLanguage
                                    ?: associatedOrganization.id.toString())
                            )
                        )
                    )
                }
                if (associatedOrganization.address?.street.isNullOrBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.street.is.not.provided",
                            mapOf(
                                "name" to (associatedOrganization.nameInOriginalLanguage
                                    ?: associatedOrganization.id.toString())
                            )
                        )
                    )
                }
                if (associatedOrganization.address?.houseNumber.isNullOrBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.house.is.not.provided",
                            mapOf(
                                "name" to (associatedOrganization.nameInOriginalLanguage
                                    ?: associatedOrganization.id.toString())
                            )
                        )
                    )
                }
                if (associatedOrganization.address?.postalCode.isNullOrBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.postal.is.not.provided",
                            mapOf(
                                "name" to (associatedOrganization.nameInOriginalLanguage
                                    ?: associatedOrganization.id.toString())
                            )
                        )
                    )
                }
                if (associatedOrganization.address?.city.isNullOrBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.city.is.not.provided",
                            mapOf(
                                "name" to (associatedOrganization.nameInOriginalLanguage
                                    ?: associatedOrganization.id.toString())
                            )
                        )
                    )
                }
                if (associatedOrganization.contacts.isEmpty() || associatedOrganization.contacts.size < 2)
                {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.contact.person.or.representative.is.not.provided",
                            mapOf(
                                "name" to (associatedOrganization.nameInOriginalLanguage
                                    ?: associatedOrganization.id.toString())
                            )
                        )
                    )
                }
                associatedOrganization.contacts.forEach { contact ->
                    if (contact.type == ProjectContactTypeData.ContactPerson) {
                        if (contact.firstName.isNullOrBlank()) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.contact.person.first.name.is.not.provided",
                                    mapOf(
                                        "name" to (associatedOrganization.nameInOriginalLanguage
                                            ?: associatedOrganization.id.toString())
                                    )
                                )
                            )
                        }
                        if (contact.lastName.isNullOrBlank()) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.contact.person.last.name.is.not.provided",
                                    mapOf(
                                        "name" to (associatedOrganization.nameInOriginalLanguage
                                            ?: associatedOrganization.id.toString())
                                    )
                                )
                            )
                        }
                        if (contact.email.isNullOrBlank()) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.contact.person.email.is.not.provided",
                                    mapOf(
                                        "name" to (associatedOrganization.nameInOriginalLanguage
                                            ?: associatedOrganization.id.toString())
                                    )
                                )
                            )
                        }
                        if (contact.telephone.isNullOrBlank()) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.contact.person.phone.is.not.provided",
                                    mapOf(
                                        "name" to (associatedOrganization.nameInOriginalLanguage
                                            ?: associatedOrganization.id.toString())
                                    )
                                )
                            )
                        }
                    } else {
                        if (contact.firstName.isNullOrBlank()) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.first.name.is.not.provided",
                                    mapOf(
                                        "name" to (associatedOrganization.nameInOriginalLanguage
                                            ?: associatedOrganization.id.toString())
                                    )
                                )
                            )
                        }
                        if (contact.lastName.isNullOrBlank()) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.last.name.is.not.provided",
                                    mapOf(
                                        "name" to (associatedOrganization.nameInOriginalLanguage
                                            ?: associatedOrganization.id.toString())
                                    )
                                )
                            )
                        }
                    }
                }
                if (associatedOrganization.roleDescription.isNotFullyTranslated(CallDataContainer.get().inputLanguages)) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.contact.person.role.is.not.provided",
                            mapOf(
                                "name" to (associatedOrganization.nameInOriginalLanguage
                                    ?: associatedOrganization.id.toString())
                            )
                        )
                    )
                }
            }
            buildErrorPreConditionCheckMessages(
                "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation",
                messageArgs = emptyMap(),
                errorMessages
            )
        }
        else -> null
    }

private fun checkIfStateAidIsValid(partners: Set<ProjectPartnerData>) =
    when { partners.isNotEmpty()
    -> {
        val errorMessages = mutableListOf<PreConditionCheckMessage>()
        partners.forEach { partner ->
            if (partner.stateAid != null && isFieldVisible(ApplicationFormFieldId.PARTNER_STATE_AID_CRITERIA_SELF_CHECK))
            {
                if (partner.stateAid?.answer1 == null ||
                    partner.stateAid?.justification1.isNotFullyTranslated(CallDataContainer.get().inputLanguages))
                {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.state.aid.partner.criteria1.answer1.justification.failed",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.stateAid?.answer2 == null ||
                    partner.stateAid?.justification2.isNotFullyTranslated(CallDataContainer.get().inputLanguages))
                {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.state.aid.partner.criteria1.answer2.justification.failed",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.stateAid?.answer3 == null ||
                    partner.stateAid?.justification3.isNotFullyTranslated(CallDataContainer.get().inputLanguages))
                {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.state.aid.partner.criteria2.answer1.justification.failed",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.stateAid?.answer4 == null ||
                    partner.stateAid?.justification4.isNotFullyTranslated(CallDataContainer.get().inputLanguages))
                {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.state.aid.partner.criteria2.answer2.justification.failed",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                /* GBER should not be filled in at this stage at all
                if (partner.stateAid?.answer1 ?: false &&
                    partner.stateAid?.answer2 ?: false &&
                    partner.stateAid?.answer3 ?: false &&
                    partner.stateAid?.answer4 ?: false &&
                    partner.stateAid?.stateAidScheme == null &&
                    isFieldVisible(ApplicationFormFieldId.PARTNER_STATE_AID_SCHEME))
                {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.state.aid.partner.gber.scheme.failed",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                 */
                if (partner.stateAid?.answer4 ?: false &&
                    partner.stateAid?.activities.isNullOrEmpty() &&
                    isFieldVisible(ApplicationFormFieldId.PARTNER_STATE_AID_RELEVANT_ACTIVITIES))
                {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.state.aid.partner.activities.failed",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                // Amund CE check: stateAidScheme should not be filled in
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_STATE_AID_SCHEME) && partner.stateAid?.stateAidScheme != null)
                {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.state.aid.partner.stateAidScheme.not.empty",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
            }
        }
        if (errorMessages.size > 0) {
            buildErrorPreConditionCheckMessages(
                "$SECTION_B_ERROR_MESSAGES_PREFIX.state.aid",
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

private fun checkIfOneOfAddressFieldTouched(address: ProjectPartnerAddressData): Boolean
{
    val oneOfTouched =
        address.country?.isNotBlank() ?: false ||
                address.nutsRegion2?.isNotBlank() ?: false ||
                address.nutsRegion3?.isNotBlank() ?: false ||
                address.street?.isNotBlank() ?: false ||
                address.houseNumber?.isNotBlank() ?: false ||
                address.postalCode?.isNotBlank() ?: false ||
                address.city?.isNotBlank() ?: false

    val oneOfEmpty =
        address.country.isNullOrBlank() ||
                address.nutsRegion2.isNullOrBlank() ||
                address.nutsRegion3.isNullOrBlank() ||
                address.street.isNullOrBlank() ||
                address.houseNumber.isNullOrBlank() ||
                address.postalCode.isNullOrBlank() ||
                address.city.isNullOrBlank()

    return oneOfTouched && oneOfEmpty
}

private fun isFieldVisible(fieldId: ApplicationFormFieldId): Boolean {
    return isFieldVisible(fieldId, LifecycleDataContainer.get()!!, CallDataContainer.get())
}
