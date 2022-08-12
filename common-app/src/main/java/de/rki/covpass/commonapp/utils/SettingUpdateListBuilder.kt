package de.rki.covpass.commonapp.utils

import de.rki.covpass.commonapp.R
import de.rki.covpass.commonapp.information.SettingItem
import de.rki.covpass.sdk.revocation.RevocationLocalListRepository
import de.rki.covpass.sdk.storage.DscRepository
import de.rki.covpass.sdk.storage.RulesUpdateRepository
import de.rki.covpass.sdk.utils.formatDateTime
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

public class SettingUpdateListBuilder(
    private val rulesUpdateRepository: RulesUpdateRepository,
    private val dscRepository: DscRepository,
    private val revocationLocalListRepository: RevocationLocalListRepository,
) {
    public fun buildList(isCovPassCheck: Boolean): List<SettingItem> {
        return listOf(
            SettingItem(
                R.string.settings_rules_list_entry,
                getDate(rulesUpdateRepository.lastEuRulesUpdate.value),
            ),
            SettingItem(
                R.string.settings_rules_list_domestic,
                getDate(rulesUpdateRepository.lastDomesticRulesUpdate.value),
            ),
            SettingItem(
                R.string.settings_rules_list_features,
                getDate(rulesUpdateRepository.lastValueSetsUpdate.value),
            ),
            SettingItem(
                R.string.settings_rules_list_issuer,
                getDate(dscRepository.lastUpdate.value),
            ),
            SettingItem(
                R.string.settings_rules_list_countries,
                getDate(rulesUpdateRepository.lastCountryListUpdate.value),
            ),
        ) + getOfflineRevocationItem(isCovPassCheck).filterNot { it.date.isBlank() }
    }

    private fun getOfflineRevocationItem(isCovPassCheck: Boolean): List<SettingItem> {
        return if (isCovPassCheck) {
            listOf(
                SettingItem(
                    R.string.settings_rules_list_authorities,
                    getDate(revocationLocalListRepository.lastRevocationUpdateFinish.value),
                ),
            )
        } else {
            emptyList()
        }
    }

    private fun getDate(date: Instant): String {
        if (date == DscRepository.NO_UPDATE_YET) {
            return ""
        }
        return LocalDateTime.ofInstant(date, ZoneId.systemDefault()).formatDateTime()
    }
}
