package de.rki.covpass.commonapp.utils

import de.rki.covpass.commonapp.R
import de.rki.covpass.commonapp.information.SettingItem

public class SettingUpdateListBuilder {
    public fun buildList(): List<SettingItem> {
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
        }
    }
}
