package de.rki.covpass.commonapp.utils

import de.rki.covpass.commonapp.R
import de.rki.covpass.commonapp.information.SettingItem
import de.rki.covpass.sdk.revocation.RevocationLocalListRepository
import de.rki.covpass.sdk.storage.DscRepository
import de.rki.covpass.sdk.utils.SunsetChecker
import java.time.Instant

public class SettingUpdateListBuilder(
    private val revocationLocalListRepository: RevocationLocalListRepository,
) {
    public fun buildList(isCovPassCheck: Boolean): List<SettingItem> {
        return buildList {
            add(
                SettingItem(
                    R.string.settings_rules_list_issuer,
                    R.string.settings_rules_list_issuer_lastupdated,
                ),
            )
            add(
                SettingItem(
                    R.string.settings_rules_list_features,
                    R.string.settings_rules_list_features_lastupdated,
                ),
            )
            if (isCovPassCheck) {
                val lastUpdate =
                    getDate(revocationLocalListRepository.lastRevocationUpdateFinish.value)
                if (lastUpdate != null && !SunsetChecker.isSunset()) {
                    add(
                        SettingItem(
                            R.string.settings_rules_list_authorities,
                            R.string.settings_rules_list_issuer_lastupdated,
                        ),
                    )
                }
            }
        }
    }

    private fun getDate(date: Instant): Instant? {
        if (date == DscRepository.NO_UPDATE_YET) {
            return null
        }
        return date
    }
}
