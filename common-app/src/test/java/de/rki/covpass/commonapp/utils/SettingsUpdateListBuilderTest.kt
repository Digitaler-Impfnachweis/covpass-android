package de.rki.covpass.commonapp.utils

import de.rki.covpass.commonapp.R
import de.rki.covpass.commonapp.information.SettingItem
import de.rki.covpass.sdk.revocation.RevocationLocalListRepository
import de.rki.covpass.sdk.storage.DscRepository
import de.rki.covpass.sdk.storage.RulesUpdateRepository
import de.rki.covpass.sdk.utils.formatDateTime
import io.mockk.every
import io.mockk.mockk
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SettingsUpdateListBuilderTest {

    private val rulesUpdateRepository: RulesUpdateRepository = mockk()
    private val dscRepository: DscRepository = mockk()
    private val revocationLocalListRepository: RevocationLocalListRepository = mockk()

    private val settingUpdateListBuilder = SettingUpdateListBuilder(
        rulesUpdateRepository,
        dscRepository,
        revocationLocalListRepository,
    )

    @Test
    fun `CovPassCheck no update yet`() {
        every { rulesUpdateRepository.lastEuRulesUpdate.value } returns DscRepository.NO_UPDATE_YET
        every { rulesUpdateRepository.lastDomesticRulesUpdate.value } returns DscRepository.NO_UPDATE_YET
        every { rulesUpdateRepository.lastValueSetsUpdate.value } returns DscRepository.NO_UPDATE_YET
        every { rulesUpdateRepository.lastCountryListUpdate.value } returns DscRepository.NO_UPDATE_YET
        every { dscRepository.lastUpdate.value } returns DscRepository.NO_UPDATE_YET
        every { revocationLocalListRepository.lastRevocationUpdateFinish.value } returns DscRepository.NO_UPDATE_YET

        val actualList = settingUpdateListBuilder.buildList(true)
        val expectedList = listOf(
            SettingItem(
                R.string.settings_rules_list_entry,
                "",
            ),
            SettingItem(
                R.string.settings_rules_list_domestic,
                "",
            ),
            SettingItem(
                R.string.settings_rules_list_features,
                "",
            ),
            SettingItem(
                R.string.settings_rules_list_issuer,
                "",
            ),
            SettingItem(
                R.string.settings_rules_list_countries,
                "",
            ),
        )

        assertEquals(expectedList, actualList)
    }

    @Test
    fun `CovPassCheck all updates completed`() {
        val timeNow = Instant.now()

        every { rulesUpdateRepository.lastEuRulesUpdate.value } returns timeNow
        every { rulesUpdateRepository.lastDomesticRulesUpdate.value } returns timeNow
        every { rulesUpdateRepository.lastValueSetsUpdate.value } returns timeNow
        every { rulesUpdateRepository.lastCountryListUpdate.value } returns timeNow
        every { dscRepository.lastUpdate.value } returns timeNow
        every { revocationLocalListRepository.lastRevocationUpdateFinish.value } returns timeNow

        val formattedDate = formatInstantDate(timeNow)

        val actualList = settingUpdateListBuilder.buildList(true)
        val expectedList = listOf(
            SettingItem(
                R.string.settings_rules_list_entry,
                formattedDate,
            ),
            SettingItem(
                R.string.settings_rules_list_domestic,
                formattedDate,
            ),
            SettingItem(
                R.string.settings_rules_list_features,
                formattedDate,
            ),
            SettingItem(
                R.string.settings_rules_list_issuer,
                formattedDate,
            ),
            SettingItem(
                R.string.settings_rules_list_countries,
                formattedDate,
            ),
            SettingItem(
                R.string.settings_rules_list_authorities,
                formattedDate,
            ),
        )

        assertEquals(expectedList, actualList)
    }

    @Test
    fun `CovPassCheck all updates completed and offline revocation disabled`() {
        val timeNow = Instant.now()

        every { rulesUpdateRepository.lastEuRulesUpdate.value } returns timeNow
        every { rulesUpdateRepository.lastDomesticRulesUpdate.value } returns timeNow
        every { rulesUpdateRepository.lastValueSetsUpdate.value } returns timeNow
        every { rulesUpdateRepository.lastCountryListUpdate.value } returns timeNow
        every { dscRepository.lastUpdate.value } returns timeNow
        every { revocationLocalListRepository.lastRevocationUpdateFinish.value } returns DscRepository.NO_UPDATE_YET

        val formattedDate = formatInstantDate(timeNow)

        val actualList = settingUpdateListBuilder.buildList(true)
        val expectedList = listOf(
            SettingItem(
                R.string.settings_rules_list_entry,
                formattedDate,
            ),
            SettingItem(
                R.string.settings_rules_list_domestic,
                formattedDate,
            ),
            SettingItem(
                R.string.settings_rules_list_features,
                formattedDate,
            ),
            SettingItem(
                R.string.settings_rules_list_issuer,
                formattedDate,
            ),
            SettingItem(
                R.string.settings_rules_list_countries,
                formattedDate,
            ),
        )

        assertEquals(expectedList, actualList)
    }

    @Test
    fun `CovPass no update yet`() {
        every { rulesUpdateRepository.lastEuRulesUpdate.value } returns DscRepository.NO_UPDATE_YET
        every { rulesUpdateRepository.lastDomesticRulesUpdate.value } returns DscRepository.NO_UPDATE_YET
        every { rulesUpdateRepository.lastValueSetsUpdate.value } returns DscRepository.NO_UPDATE_YET
        every { rulesUpdateRepository.lastCountryListUpdate.value } returns DscRepository.NO_UPDATE_YET
        every { dscRepository.lastUpdate.value } returns DscRepository.NO_UPDATE_YET

        val actualList = settingUpdateListBuilder.buildList(false)
        val expectedList = listOf(
            SettingItem(
                R.string.settings_rules_list_entry,
                "",
            ),
            SettingItem(
                R.string.settings_rules_list_domestic,
                "",
            ),
            SettingItem(
                R.string.settings_rules_list_features,
                "",
            ),
            SettingItem(
                R.string.settings_rules_list_issuer,
                "",
            ),
            SettingItem(
                R.string.settings_rules_list_countries,
                "",
            ),
        )

        assertEquals(expectedList, actualList)
    }

    @Test
    fun `CovPass all updates completed`() {
        val timeNow = Instant.now()

        every { rulesUpdateRepository.lastEuRulesUpdate.value } returns timeNow
        every { rulesUpdateRepository.lastDomesticRulesUpdate.value } returns timeNow
        every { rulesUpdateRepository.lastValueSetsUpdate.value } returns timeNow
        every { rulesUpdateRepository.lastCountryListUpdate.value } returns timeNow
        every { dscRepository.lastUpdate.value } returns timeNow
        every { revocationLocalListRepository.lastRevocationUpdateFinish.value } returns timeNow

        val formattedDate = formatInstantDate(timeNow)

        val actualList = settingUpdateListBuilder.buildList(false)
        val expectedList = listOf(
            SettingItem(
                R.string.settings_rules_list_entry,
                formattedDate,
            ),
            SettingItem(
                R.string.settings_rules_list_domestic,
                formattedDate,
            ),
            SettingItem(
                R.string.settings_rules_list_features,
                formattedDate,
            ),
            SettingItem(
                R.string.settings_rules_list_issuer,
                formattedDate,
            ),
            SettingItem(
                R.string.settings_rules_list_countries,
                formattedDate,
            ),
        )

        assertEquals(expectedList, actualList)
    }

    private fun formatInstantDate(date: Instant): String {
        return LocalDateTime.ofInstant(date, ZoneId.systemDefault()).formatDateTime()
    }
}
