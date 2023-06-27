package de.rki.covpass.commonapp.utils

import de.rki.covpass.commonapp.R
import de.rki.covpass.commonapp.information.SettingItem
import de.rki.covpass.sdk.revocation.RevocationLocalListRepository
import de.rki.covpass.sdk.storage.DscRepository
import io.mockk.every
import io.mockk.mockk
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SettingsUpdateListBuilderTest {

    private val dscRepository: DscRepository = mockk()
    private val revocationLocalListRepository: RevocationLocalListRepository = mockk()

    private val settingUpdateListBuilder = SettingUpdateListBuilder(
        dscRepository,
        revocationLocalListRepository,
    )

    @Test
    fun `CovPassCheck no update yet`() {
        every { dscRepository.lastUpdate.value } returns DscRepository.NO_UPDATE_YET
        every { revocationLocalListRepository.lastRevocationUpdateFinish.value } returns DscRepository.NO_UPDATE_YET

        val actualList = settingUpdateListBuilder.buildList(true)
        val expectedList = listOf(
            SettingItem(
                R.string.settings_rules_list_issuer,
                null,
            ),
            SettingItem(
                R.string.settings_rules_list_ifsg_title,
                date = null,
                staticDate = R.string.settings_rules_list_ifsg_subtitle,
            ),
        )

        assertEquals(expectedList, actualList)
    }

    @Test
    fun `CovPassCheck all updates completed`() {
        val timeNow = Instant.now()

        every { dscRepository.lastUpdate.value } returns timeNow
        every { revocationLocalListRepository.lastRevocationUpdateFinish.value } returns timeNow

        val actualList = settingUpdateListBuilder.buildList(true)
        val expectedList = listOf(
            SettingItem(
                R.string.settings_rules_list_issuer,
                timeNow,
            ),
            SettingItem(
                R.string.settings_rules_list_ifsg_title,
                date = null,
                staticDate = R.string.settings_rules_list_ifsg_subtitle,
            ),
            SettingItem(
                R.string.settings_rules_list_authorities,
                timeNow,
            ),
        )

        assertEquals(expectedList, actualList)
    }

    @Test
    fun `CovPassCheck all updates completed and offline revocation disabled`() {
        val timeNow = Instant.now()

        every { dscRepository.lastUpdate.value } returns timeNow
        every { revocationLocalListRepository.lastRevocationUpdateFinish.value } returns DscRepository.NO_UPDATE_YET

        val actualList = settingUpdateListBuilder.buildList(true)
        val expectedList = listOf(
            SettingItem(
                R.string.settings_rules_list_issuer,
                timeNow,
            ),
            SettingItem(
                R.string.settings_rules_list_ifsg_title,
                date = null,
                staticDate = R.string.settings_rules_list_ifsg_subtitle,
            ),
        )

        assertEquals(expectedList, actualList)
    }

    @Test
    fun `CovPass no update yet`() {
        every { dscRepository.lastUpdate.value } returns DscRepository.NO_UPDATE_YET

        val actualList = settingUpdateListBuilder.buildList(false)
        val expectedList = listOf(
            SettingItem(
                R.string.settings_rules_list_issuer,
                null,
            ),
            SettingItem(
                R.string.settings_rules_list_ifsg_title,
                date = null,
                staticDate = R.string.settings_rules_list_ifsg_subtitle,
            ),
        )

        assertEquals(expectedList, actualList)
    }

    @Test
    fun `CovPass all updates completed`() {
        val timeNow = Instant.now()

        every { dscRepository.lastUpdate.value } returns timeNow
        every { revocationLocalListRepository.lastRevocationUpdateFinish.value } returns timeNow

        val actualList = settingUpdateListBuilder.buildList(false)
        val expectedList = listOf(
            SettingItem(
                R.string.settings_rules_list_issuer,
                timeNow,
            ),
            SettingItem(
                R.string.settings_rules_list_ifsg_title,
                date = null,
                staticDate = R.string.settings_rules_list_ifsg_subtitle,
            ),
        )

        assertEquals(expectedList, actualList)
    }
}
