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
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.budget.ProjectPartnerContributionStatusData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.relevance.ProjectTargetGroupData
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckMessage
import io.cloudflight.jems.plugin.standard.pre_condition_check.helpers.CallDataContainer
import io.cloudflight.jems.plugin.standard.pre_condition_check.helpers.LifecycleDataContainer
import java.math.BigDecimal

private const val SECTION_B_MESSAGES_PREFIX = "$MESSAGES_PREFIX.section.b"
private const val SECTION_B_ERROR_MESSAGES_PREFIX = "$SECTION_B_MESSAGES_PREFIX.error"
private const val SECTION_B_INFO_MESSAGES_PREFIX = "$SECTION_B_MESSAGES_PREFIX.info"
private const val SECTION_B_WARNING_MESSAGES_PREFIX = "$SECTION_B_MESSAGES_PREFIX.warning"

fun checkSectionB(sectionBData: ProjectDataSectionB): PreConditionCheckMessage {
    return buildPreConditionCheckMessage(
        messageKey = "$SECTION_B_MESSAGES_PREFIX.header", messageArgs = emptyMap(),
        // Amund updated wording
        //checkIfAtLeastThreePartnersAreAdded(sectionBData.partners),

        checkPartnerComposition(sectionBData.partners),

        checkIfExactlyOneLeadPartnerIsAdded(sectionBData.partners),

        checkIfPartnerIdentityContentIsProvided(sectionBData.partners),

        checkIfPartnerAddressContentIsProvided(sectionBData.partners),

        checkIfPartnerPersonContentIsProvided(sectionBData.partners),

        checkIfPartnerMotivationContentIsProvided(sectionBData.partners),

        checkIfPartnerAssociatedOrganisationIsProvided(sectionBData.associatedOrganisations),

        checkGeneralBudget(sectionBData.partners),

        checkIfStaffContentIsProvided(sectionBData.partners),
        // Amund
        checkBudgetOptions(sectionBData.partners),

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
// Amund - Updated from at least 1 to at least 3
//private fun checkIfAtLeastThreePartnersAreAdded(partners: Set<ProjectPartnerData>?) =
//    when {
//        partners?.size!! <= 1 -> buildErrorPreConditionCheckMessage("$SECTION_B_ERROR_MESSAGES_PREFIX.at.least.three.partners.should.be.added")
//        else -> buildInfoPreConditionCheckMessage("$SECTION_B_INFO_MESSAGES_PREFIX.at.least.three.partners.are.added")
//    }

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

// Amund - CE check for partner compsition:
//  - At least 3 partners (updates check above)
//  - From at least 3 countries
//  - At least 2 partners from CE regions
//  - If partner type = EGTC => at least 1 partner EGTC must be in CE region
private fun checkPartnerComposition(partners: Set<ProjectPartnerData>) =
   when {
       partners.isNotEmpty() ->
       {
           val errorMessages = mutableListOf<PreConditionCheckMessage>()
           val countries = mutableListOf<String>()
           val CECountries = mutableListOf<String>()
           var isEGTC = false
           partners.forEach { partner ->
               if (partner.partnerType == ProjectTargetGroupData.Egtc) {
                    isEGTC = true
               }
               partner.addresses.forEach { address ->
                   if (address.type == ProjectPartnerAddressTypeData.Organization){
                       countries.add(address.country.toString())
                   }
                   if (address.type == ProjectPartnerAddressTypeData.Organization && address.country in listOf("Magyarország (HU)","Italia (IT)","Slovenija (SI)","Slovensko (SK)","Česko (CZ)","Polska (PL)","Hrvatska (HR)","Deutschland (DE)","Österreich (AT)")) {
                       CECountries.add(address.country.toString())
                   }
               }
           }
           if (countries.distinct().size < 3 && !isEGTC) {
               errorMessages.add(
                   buildErrorPreConditionCheckMessage(
                       "$SECTION_B_ERROR_MESSAGES_PREFIX.partner.composition.not.three.countries",
                       mapOf("countries" to (countries.toString()))
                   )
               )
           }
           if (CECountries.distinct().size < 2 && !isEGTC) {
               errorMessages.add(
                   buildErrorPreConditionCheckMessage(
                       "$SECTION_B_ERROR_MESSAGES_PREFIX.partner.composition.not.two.CE.countries",
                       mapOf("CECountries" to (CECountries.toString()))
                   )
               )
           }
           if (isEGTC) {
               partners.forEach { partner ->
                   if (partner.partnerType == ProjectTargetGroupData.Egtc) {
                       partner.addresses.forEach { address ->
                           if (address.type == ProjectPartnerAddressTypeData.Organization && address.country !in listOf("Magyarország (HU)","Italia (IT)","Slovenija (SI)","Slovensko (SK)","Česko (CZ)","Polska (PL)","Hrvatska (HR)","Deutschland (DE)","Österreich (AT)")) {
                               errorMessages.add(
                                   buildErrorPreConditionCheckMessage(
                                       "$SECTION_B_ERROR_MESSAGES_PREFIX.partner.composition.EGTC.not.in.CE.area",
                                       mapOf("CECountries" to (CECountries.toString()))
                                   )
                               )
                           }
                       }
                   }
               }
           }
           if (errorMessages.count() > 0) {
               buildErrorPreConditionCheckMessages(
                   "$SECTION_B_ERROR_MESSAGES_PREFIX.partner.composition",
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

private fun checkIfCoFinancingContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.isNullOrEmpty() ||
                partners.any { partner ->
                    partner.budget.projectPartnerCoFinancing.partnerContributions.any { partnerContribution -> partnerContribution.amount ?: BigDecimal.ZERO <= BigDecimal.ZERO } ||
                    partner.budget.projectPartnerCoFinancing.partnerContributions.any { partnerContribution -> partnerContribution.status == null } ||
                    partner.budget.projectPartnerCoFinancing.finances.isNullOrEmpty() ||
                    partner.budget.projectPartnerCoFinancing.finances.any { finance -> finance.percentage <= BigDecimal.ZERO } ||
                            // Amund add check for co-financing percentage
                            partner.budget.projectPartnerCoFinancing.finances.any { finance -> (finance.fundType == ProjectPartnerCoFinancingFundTypeData.MainFund && finance.percentage.intValueExact() != 80) }
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

            // Amund check legal status match of Partner and partner contribution
            if (!partner.budget.projectPartnerCoFinancing.partnerContributions.isEmpty()) {
                if ((partner.legalStatusId.toString() == "1" && partner.budget.projectPartnerCoFinancing.partnerContributions.elementAt(0).status == ProjectPartnerContributionStatusData.Private) ||
                    (partner.legalStatusId.toString() == "2" && partner.budget.projectPartnerCoFinancing.partnerContributions.elementAt(0).status == ProjectPartnerContributionStatusData.Public)
                ) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.partner.contribution.legal.status.not.matching",
                            mapOf(
                                "name" to (partner.abbreviation),
                                "partner" to (partner.legalStatusId.toString()),
                                "contribution" to (partner.budget.projectPartnerCoFinancing.partnerContributions.elementAt(0).status.toString())
                            )
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

// Amund General budget checks
private fun checkGeneralBudget(partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            (partner.budget.projectPartnerBudgetTotalCost <= BigDecimal.ZERO)
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (partner.budget.projectPartnerBudgetTotalCost <= BigDecimal.ZERO) {
                    errorMessages.add(
                        buildWarningPreConditionCheckMessage(
                            "$SECTION_B_WARNING_MESSAGES_PREFIX.budget.is.zero",
                            mapOf("name" to (partner.abbreviation), "budget" to (partner.budget.projectPartnerBudgetTotalCost.toString()), "legal" to (partner.legalStatusId.toString()))
                        )
                    )
                }
                // if total of infrastructure and equipment is over 25000, add a warning
                var totalSum = 0.00
                partner.budget.projectPartnerBudgetCosts.equipmentCosts.forEach { equipment ->
                    totalSum += equipment.rowSum?.toDouble()!!
                }
                partner.budget.projectPartnerBudgetCosts.infrastructureCosts.forEach { infrastructure ->
                    totalSum += infrastructure.rowSum?.toDouble()!!
                }
                if (totalSum > 25000) {
                    errorMessages.add(
                        buildWarningPreConditionCheckMessage(
                            "$SECTION_B_WARNING_MESSAGES_PREFIX.infrastructure.and.equipment.over.25000",
                            mapOf("name" to (partner.abbreviation), "sum" to (totalSum.toString()))
                        )
                    )
                }
            }
            if (errorMessages.count() > 0) {
                buildWarningPreConditionCheckMessages(
                    "$SECTION_B_WARNING_MESSAGES_PREFIX.budget.general",
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
// Amund end new section

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

// Amund TODO: Add new Project budget options section:
private fun checkBudgetOptions(partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            (partner.budget.projectPartnerOptions?.otherCostsOnStaffCostsFlatRate == null && partner.budget.projectPartnerOptions?.officeAndAdministrationOnStaffCostsFlatRate != 15) ||
                    (partner.budget.projectPartnerOptions?.otherCostsOnStaffCostsFlatRate == null && partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate == null) ||
                    (partner.budget.projectPartnerOptions?.otherCostsOnStaffCostsFlatRate != null && partner.budget.projectPartnerBudgetCosts.staffCosts.isEmpty()) ||
                    (partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate != null
                            && partner.addresses.elementAt(0).type == ProjectPartnerAddressTypeData.Organization
                            && partner.addresses.elementAt(0).country !in listOf("Italia (IT)", "Slovenija (SI)", "Slovensko (SK)", "Česko (CZ)", "Magyarország (HU)", "Polska (PL)", "Hrvatska (HR)")
                            && partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate != 5) ||
                    (partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate != null
                            && partner.addresses.elementAt(0).type == ProjectPartnerAddressTypeData.Organization
                            && partner.addresses.elementAt(0).country in listOf("Italia (IT)", "Slovenija (SI)", "Slovensko (SK)")
                            && partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate != 6) ||
                    (partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate != null
                            && partner.addresses.elementAt(0).type == ProjectPartnerAddressTypeData.Organization
                            && partner.addresses.elementAt(0).country == "Česko (CZ)"
                            && partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate != 7) ||
                    (partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate != null
                            && partner.addresses.elementAt(0).type == ProjectPartnerAddressTypeData.Organization
                            && partner.addresses.elementAt(0).country == "Magyarország (HU)"
                            && partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate != 8) ||
                    (partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate != null
                            && partner.addresses.elementAt(0).type == ProjectPartnerAddressTypeData.Organization
                            && partner.addresses.elementAt(0).country == "Polska (PL)"
                            && partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate != 9) ||
                    (partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate != null
                            && partner.addresses.elementAt(0).type == ProjectPartnerAddressTypeData.Organization
                            && partner.addresses.elementAt(0).country == "Hrvatska (HR)"
                            && partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate != 11)



        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (partner.budget.projectPartnerOptions?.otherCostsOnStaffCostsFlatRate == null
                    && partner.budget.projectPartnerOptions?.officeAndAdministrationOnStaffCostsFlatRate != 15) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.options.office.and.admin.not.15",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                // test Amund - Travel and accommodation flat rate is mandatory when not using Other costs flat rate
                if (partner.budget.projectPartnerOptions?.otherCostsOnStaffCostsFlatRate == null
                    && partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate == null) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.options.travel.and.accommodation.not.selected",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                // test Amund - If in the partner budget options Other costs Flat Rate  40% is selected, Staff costs have to be created (add button has to be ticked)
                if (partner.budget.projectPartnerOptions?.otherCostsOnStaffCostsFlatRate != null
                    && partner.budget.projectPartnerBudgetCosts.staffCosts.isEmpty()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.options.staff.cost.not.selected",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                // test Amund - check flatrates depending on countries
                partner.addresses.forEach { address ->
                    if (partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate != null
                        && address.type == ProjectPartnerAddressTypeData.Organization
                        && address.country !in listOf("Italia (IT)", "Slovenija (SI)", "Slovensko (SK)", "Česko (CZ)", "Magyarország (HU)", "Polska (PL)", "Hrvatska (HR)")
                        && partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate != 5) {
                        errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.options.wrong.AT.DE.travel.flat.rate",
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
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.options.wrong.it.si.sk.travel.flat.rate",
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
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.options.wrong.cz.travel.flat.rate",
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
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.options.wrong.hu.travel.flat.rate",
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
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.options.wrong.pl.travel.flat.rate",
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
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.options.wrong.hr.travel.flat.rate",
                                mapOf("name" to (partner.abbreviation), "flatrate" to (partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate.toString()))
                            )
                        )
                    }
                }
            }
            if (errorMessages.count() > 0) {
                buildErrorPreConditionCheckMessages(
                    "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.options",
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
//end new section Project Budget options

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
                    partner.vat!!.take(2) != partner.addresses.elementAt(0).nutsRegion2?.substringAfterLast("(")?.take(2) ||
                    // TODO: change from elementAT(0) to type = ProjectPartnerAddressTypeData.Organization (See assiciated contacts section)
                    (!partner.vat.isNullOrEmpty() && partner.addresses.elementAt(0).nutsRegion2?.substringAfterLast("(")?.take(2) == "AT" && Regex("^(ATU)[0-9]{8}\$").matchEntire(partner.vat!!)==null) ||
                    (partner.addresses.elementAt(0).nutsRegion2?.substringAfterLast("(")?.take(2) == "DE" && Regex("^(DE)[0-9]{9}\$").matchEntire(partner.vat!!)==null) ||
                    (partner.addresses.elementAt(0).nutsRegion2?.substringAfterLast("(")?.take(2) == "HR" && Regex("^(HR)[0-9]{11}\$").matchEntire(partner.vat!!)==null) ||
                    (partner.addresses.elementAt(0).nutsRegion2?.substringAfterLast("(")?.take(2) == "CZ" && Regex("^(CZ)[0-9]{8,10}\$").matchEntire(partner.vat!!)==null) ||
                    (partner.addresses.elementAt(0).nutsRegion2?.substringAfterLast("(")?.take(2) == "HU" && Regex("^(HU)[0-9]{8}\$").matchEntire(partner.vat!!)==null) ||
                    (partner.addresses.elementAt(0).nutsRegion2?.substringAfterLast("(")?.take(2) == "IT" && Regex("^(IT)[0-9]{11}\$").matchEntire(partner.vat!!)==null) ||
                    (partner.addresses.elementAt(0).nutsRegion2?.substringAfterLast("(")?.take(2) == "PL" && Regex("^(PL)[0-9]{10}\$").matchEntire(partner.vat!!)==null) ||
                    (partner.addresses.elementAt(0).nutsRegion2?.substringAfterLast("(")?.take(2) == "SK" && Regex("^(SK)[0-9]{10}\$").matchEntire(partner.vat!!)==null) ||
                    (partner.addresses.elementAt(0).nutsRegion2?.substringAfterLast("(")?.take(2) == "SI" && Regex("^(SI)[0-9]{8}\$").matchEntire(partner.vat!!)==null) ||
                    (!partner.otherIdentifierNumber.isNullOrEmpty() && partner.otherIdentifierDescription.isNullOrEmpty()) ||
                    (partner.otherIdentifierNumber.isNullOrEmpty() && !partner.otherIdentifierDescription.isNullOrEmpty()) ||
                    (partner.partnerType == ProjectTargetGroupData.GeneralPublic) ||
                    ((partner.partnerType == ProjectTargetGroupData.Sme || partner.partnerType == ProjectTargetGroupData.EnterpriseExceptSme) && partner.partnerSubType == null) ||
                    (partner.partnerType == null)

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
                // Amund - added 'OR other identifier'
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_VAT_IDENTIFIER) && partner.vat.isNullOrEmpty() && partner.otherIdentifierNumber.isNullOrEmpty()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.vat.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                // Amund - if other id is used, then a descriptions should also be given
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_VAT_IDENTIFIER)
                    && !partner.otherIdentifierNumber.isNullOrEmpty() && partner.otherIdentifierDescription.isNullOrEmpty()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.other.id.needs.description",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                // Amund - if other id description is filled in, then a id number should also be given
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_VAT_IDENTIFIER)
                    && partner.otherIdentifierNumber.isNullOrEmpty() && !partner.otherIdentifierDescription.isNullOrEmpty()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.no.other.id.for.description",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                // Amund - Partner type must be selected
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_VAT_IDENTIFIER)
                    && partner.partnerType == null) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.type.not.added",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                // Amund - The selected type of partner cannot be "General Public"
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_VAT_IDENTIFIER)
                    && partner.partnerType == ProjectTargetGroupData.GeneralPublic ) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.type.cannot.be.general.public",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                // Amund - If he selected type of partner is SME subtype is also mandatory
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_VAT_IDENTIFIER)
                    && (partner.partnerType == ProjectTargetGroupData.Sme || partner.partnerType == ProjectTargetGroupData.EnterpriseExceptSme)
                    && partner.partnerSubType == null) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.type.SME.must.have.subtype",
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


                    if (address.type == ProjectPartnerAddressTypeData.Organization
                        && !partner.vat.isNullOrEmpty()) {
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
                    ) ||    // Amund - add CE check to build address error message
                            (partner.role.isLead && address.type == ProjectPartnerAddressTypeData.Organization && address.nutsRegion2?.substringAfterLast("(")?.take(2)!! !in listOf("AT", "IT", "HR", "CZ", "HU", "PL", "SI", "SK", "DE")) ||
                            (address.type == ProjectPartnerAddressTypeData.Organization && address.country !in listOf("Österreich (AT)", "Belgique/België (BE)", "Bulgaria (BG)", "Hrvatska (HR)", "Kýpros (CY)", "Česko (CZ)", "Danmark (DK)", "Eesti (EE)", "Suomi/Finland (FI)", "France (FR)", "Deutschland (DE)", "Elláda (EL)", "Magyarország (HU)", "Éire/Ireland (IE)", "Italia (IT)", "Latvija (LV)", "Lietuva (LT)", "Luxembourg (LU)", "Malta (MT)", "Nederland (NL)", "Polska (PL)", "Portugal (PT)", "România (RO)", "Slovensko (SK)", "Slovenija (SI)", "España (ES)", "Sverige (SE)")) ||
                            (address.type == ProjectPartnerAddressTypeData.Organization && address.homepage.isNullOrBlank()) ||
                            (partner.role.isLead
                                    && address.type == ProjectPartnerAddressTypeData.Organization
                                    && address.nutsRegion2?.substringAfterLast("(")?.take(2) == "DE"
                                    && address.nutsRegion2?.substringAfterLast("(")?.substringBefore(')') !in listOf("DE11","DE12","DE13","DE14","DE21","DE22","DE23","DE24","DE25","DE26","DE27","DE30","DE40","DE80","DE91")) ||
                            (partner.role.isLead
                                    && address.type == ProjectPartnerAddressTypeData.Organization
                                    && address.nutsRegion2?.substringAfterLast("(")?.take(2) == "IT"
                                    && address.nutsRegion2?.substringAfterLast("(")?.substringBefore(')') !in listOf("ITC1","ITC2","ITC3","ITC4","ITH1","ITH2","ITH3","ITH4","ITH5"))
                })
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            // amund added boolean for waring message
            var warning = false
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
                    // test Amund - Partners must be in EU (other partners must be associated partners)
                    if (address.type == ProjectPartnerAddressTypeData.Organization
                        && address.country !in listOf("Österreich (AT)", "Belgique/België (BE)", "Bulgaria (BG)", "Hrvatska (HR)", "Kýpros (CY)", "Česko (CZ)", "Danmark (DK)", "Eesti (EE)", "Suomi/Finland (FI)", "France (FR)", "Deutschland (DE)", "Elláda (EL)", "Magyarország (HU)", "Éire/Ireland (IE)", "Italia (IT)", "Latvija (LV)", "Lietuva (LT)", "Luxembourg (LU)", "Malta (MT)", "Nederland (NL)", "Polska (PL)", "Portugal (PT)", "România (RO)", "Slovensko (SK)", "Slovenija (SI)", "España (ES)", "Sverige (SE)")){
                        errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.address.not.in.eu",
                                mapOf("name" to (partner.abbreviation), "countrycode" to (address.nutsRegion2?.substringAfterLast("(")?.take(2)!!))
                            )
                        )
                    }
                    // test Amund - homepage must be added
                    if (address.type == ProjectPartnerAddressTypeData.Organization
                        && address.homepage.isNullOrBlank()) {
                        errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.address.homepage.missing",
                                mapOf("name" to (partner.abbreviation))
                            )
                        )
                    }
                    // test Amund - if lead partner from IT or DE but outside programme area, show warning
                    if (partner.role.isLead
                        && address.type == ProjectPartnerAddressTypeData.Organization
                        && address.nutsRegion2?.substringAfterLast("(")?.take(2) == "DE"
                        && address.nutsRegion2?.substringAfterLast("(")?.substringBefore(')') !in listOf("DE11","DE12","DE13","DE14","DE21","DE22","DE23","DE24","DE25","DE26","DE27","DE30","DE40","DE80","DE91","DED2","DED4","DED5","DEE0","DEG0")) {
                        warning = true
                        errorMessages.add(
                            buildWarningPreConditionCheckMessage(
                                "$SECTION_B_WARNING_MESSAGES_PREFIX.project.partner.DE.lp.not.in.programme.area",
                                mapOf("name" to (partner.abbreviation), "countrycode" to (address.nutsRegion2?.substringAfterLast("(")?.substringBefore(')')!!))
                            )
                        )
                    }
                    // test Amund - if lead partner from IT or DE but outside programme area, show warning
                    if (partner.role.isLead
                        && address.type == ProjectPartnerAddressTypeData.Organization
                        && address.nutsRegion2?.substringAfterLast("(")?.take(2) == "IT"
                        && address.nutsRegion2?.substringAfterLast("(")?.substringBefore(')') !in listOf("ITC1","ITC2","ITC3","ITC4","ITH1","ITH2","ITH3","ITH4","ITH5")) {
                        warning = true
                        errorMessages.add(
                            buildWarningPreConditionCheckMessage(
                                "$SECTION_B_WARNING_MESSAGES_PREFIX.project.partner.IT.lp.not.in.programme.area",
                                mapOf("name" to (partner.abbreviation), "countrycode" to (address.nutsRegion2?.substringAfterLast("(")?.substringBefore(')')!!))
                            )
                        )
                    }
                }
            }
            if (errorMessages.count() == 1 && warning) {
                buildWarningPreConditionCheckMessages(
                    "$SECTION_B_WARNING_MESSAGES_PREFIX.project.partner.main.address.warning",
                    messageArgs = emptyMap(),
                    errorMessages
                )
            }
            else if (errorMessages.count() > 0) {
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
                                contact.email.isNullOrBlank()) ||
                                contact.title.isNullOrBlank()
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
                        // Amund - check for contact person title
                        if (isFieldVisible(ApplicationFormFieldId.PARTNER_CONTACT_PERSON_TELEPHONE) && contact.title.isNullOrBlank()) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.person.title.is.not.provided",
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
                        if (isFieldVisible(ApplicationFormFieldId.PARTNER_LEGAL_REPRESENTATIVE_LAST_NAME) && contact.title.isNullOrBlank() ) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.representative.title.is.not.provided",
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
                    partner.motivation?.organizationRole.isNotFullyTranslated(CallDataContainer.get().inputLanguages) ||
                    (partner.motivation?.organizationExperience.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
                    && partner.role.isLead)
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
                // Amund - LP need to fill in capacity
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_MOTIVATION_ROLE)
                    && partner.motivation?.organizationExperience.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
                    && partner.role.isLead) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.experience.role.is.not.provided",
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
                    associatedOrganization.nameInEnglish.isNullOrBlank() ||
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
                        }  /*||
                        associatedOrganization.contacts.any { contact ->
                            contact.type == ProjectContactTypeData.LegalRepresentative &&
                                    (contact.firstName.isNullOrBlank() ||
                                            contact.lastName.isNullOrBlank())
                        }*/
                    ) || (associatedOrganization.contacts.isEmpty() || (associatedOrganization.contacts.size == 1 && associatedOrganization.contacts.elementAt(0).type == ProjectContactTypeData.LegalRepresentative))
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
                if (associatedOrganization.nameInEnglish.isNullOrBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.name.english.is.not.provided",
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
                // Amund updated to ignore legal rep
                if (associatedOrganization.contacts.isEmpty() || (associatedOrganization.contacts.size == 1 && associatedOrganization.contacts.elementAt(0).type == ProjectContactTypeData.LegalRepresentative))
                {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.contact.person.or.representative.is.not.provided",
                            mapOf(
                                "name" to (associatedOrganization.nameInOriginalLanguage
                                    ?: associatedOrganization.id.toString())
                                , "size" to (associatedOrganization.contacts.size.toString())
                            )
                        )
                    )
                }

                associatedOrganization.contacts.forEach { contact ->
                    if (contact.type == ProjectContactTypeData.ContactPerson || associatedOrganization.contacts.isEmpty()) {
                        if (contact.title.isNullOrBlank()) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.contact.person.title.is.not.provided",
                                    mapOf(
                                        "name" to (associatedOrganization.nameInOriginalLanguage
                                            ?: associatedOrganization.id.toString())
                                    )
                                )
                            )
                        }
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
                    }
                    /*else {
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
                    } */
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
                if (partner.stateAid?.answer1 == null) // Amund - Removed or-statement
                {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.state.aid.partner.criteria1.answer1.not.answered",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                // Amund - split check, one for answer and on for justification IF yes
                if (partner.stateAid?.answer1 == true &&
                    partner.stateAid?.justification1.isNotFullyTranslated(CallDataContainer.get().inputLanguages))
                {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.state.aid.partner.criteria1.answer1.justification.missing",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.stateAid?.answer2 == null) // Amund - Removed or-statement
                {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.state.aid.partner.criteria1.answer2.not.answered",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                // Amund - split check, one for answer and on for justification IF yes
                if (partner.stateAid?.answer2 == true &&
                    partner.stateAid?.justification2.isNotFullyTranslated(CallDataContainer.get().inputLanguages))
                {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.state.aid.partner.criteria1.answer2.justification.missing",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.stateAid?.answer3 == null) // Amund - Removed or-statement
                {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.state.aid.partner.criteria2.answer1.not.answered",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                // Amund - split check, one for answer and on for justification IF yes
                if (partner.stateAid?.answer3 == true &&
                    partner.stateAid?.justification3.isNotFullyTranslated(CallDataContainer.get().inputLanguages))
                {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.state.aid.partner.criteria2.answer1.justification.missing",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.stateAid?.answer4 == null) // Amund - Removed or-statement
                {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.state.aid.partner.criteria2.answer2.not.answered",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                // Amund - split check, one for answer and on for justification IF yes
                if (partner.stateAid?.answer4 == true &&
                    partner.stateAid?.justification4.isNotFullyTranslated(CallDataContainer.get().inputLanguages))
                {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.state.aid.partner.criteria2.answer2.justification.missing",
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
                // Amund updated criteria for error
                if ((partner.stateAid?.answer4 == true || partner.stateAid?.answer3 == true) &&
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
