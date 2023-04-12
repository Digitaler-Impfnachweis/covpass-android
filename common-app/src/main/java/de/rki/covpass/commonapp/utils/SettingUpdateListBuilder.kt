package de.rki.covpass.commonapp.utils

import de.rki.covpass.commonapp.R
import de.rki.covpass.commonapp.information.SettingItem
import de.rki.covpass.sdk.revocation.RevocationLocalListRepository
import de.rki.covpass.sdk.storage.DscRepository
import java.time.Instant

public class SettingUpdateListBuilder(
    private val dscRepository: DscRepository,
    private val revocationLocalListRepository: RevocationLocalListRepository,
) {
    public fun buildList(isCovPassCheck: Boolean): List<SettingItem> {
        return buildList {
            add(
                SettingItem(
                    R.string.settings_rules_list_issuer,
                    getDate(dscRepository.lastUpdate.value),
                ),
            )
            if (isCovPassCheck) {
                val lastUpdate =
                    getDate(revocationLocalListRepository.lastRevocationUpdateFinish.value)
                if (lastUpdate != null) {
                    add(
                        SettingItem(
                            R.string.settings_rules_list_authorities,
                            lastUpdate,
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
