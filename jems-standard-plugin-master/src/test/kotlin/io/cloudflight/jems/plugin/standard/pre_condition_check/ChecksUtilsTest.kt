package io.cloudflight.jems.plugin.standard.pre_condition_check

import io.cloudflight.jems.plugin.contract.models.common.InputTranslationData
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ChecksUtilsTest {

    companion object {
        val mandatoryLanguages = setOf(SystemLanguageData.EN, SystemLanguageData.DE)
    }

    @Test
    fun `InputTranslations isNotFullyTranslated on empty or missing`() {
        val inputEmpty = setOf(InputTranslationData(SystemLanguageData.FR, "translation"),
            InputTranslationData(SystemLanguageData.EN, ""))
        assertThat(inputEmpty.isNotFullyTranslated(mandatoryLanguages)).isTrue
        assertThat(inputEmpty.isFullyTranslated(mandatoryLanguages)).isFalse
    }

    @Test
    fun `InputTranslations isNotFullyTranslated on null`() {
        val inputNull = null
        assertThat(inputNull.isNotFullyTranslated(mandatoryLanguages)).isTrue
        assertThat(inputNull.isFullyTranslated(mandatoryLanguages)).isFalse
    }

    @Test
    fun `InputTranslations isNotFullyTranslated on empty or blank`() {
        val inputBlanks = setOf(InputTranslationData(SystemLanguageData.EN, ""),
            InputTranslationData(SystemLanguageData.DE, "  "))
        assertThat(inputBlanks.isNotFullyTranslated(mandatoryLanguages)).isTrue
        assertThat(inputBlanks.isFullyTranslated(mandatoryLanguages)).isFalse
    }

    @Test
    fun `InputTranslations isFullyTranslated successful`() {
        val inputFilled = setOf(InputTranslationData(SystemLanguageData.FR, "transl"),
            InputTranslationData(SystemLanguageData.EN, "t"),
            InputTranslationData(SystemLanguageData.DE, "translation"))
        assertThat(inputFilled.isNotFullyTranslated(mandatoryLanguages)).isFalse
        assertThat(inputFilled.isFullyTranslated(mandatoryLanguages)).isTrue
    }
}