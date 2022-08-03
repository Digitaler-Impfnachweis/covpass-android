/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.information

import com.ensody.reactivestate.*
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.commonapp.R
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.revocation.RevocationLocalListRepository
import de.rki.covpass.sdk.rules.CovPassCountriesRepository
import de.rki.covpass.sdk.rules.CovPassDomesticRulesRepository
import de.rki.covpass.sdk.rules.CovPassEuRulesRepository
import de.rki.covpass.sdk.rules.CovPassValueSetsRepository
import de.rki.covpass.sdk.storage.DscRepository
import de.rki.covpass.sdk.storage.RulesUpdateRepository
import de.rki.covpass.sdk.utils.DscListUpdater
import de.rki.covpass.sdk.utils.formatDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@SuppressWarnings("UnusedPrivateMember")
public class SettingsUpdateViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val isCovPassCheck: Boolean,
    private val dscListUpdater: DscListUpdater = sdkDeps.dscListUpdater,
    private val euRulesRepository: CovPassEuRulesRepository = sdkDeps.covPassEuRulesRepository,
    private val domesticRulesRepository: CovPassDomesticRulesRepository = sdkDeps.covPassDomesticRulesRepository,
    private val valueSetsRepository: CovPassValueSetsRepository = sdkDeps.covPassValueSetsRepository,
    private val countriesRepository: CovPassCountriesRepository = sdkDeps.covPassCountriesRepository,
    private val dscRepository: DscRepository = sdkDeps.dscRepository,
    private val rulesUpdateRepository: RulesUpdateRepository = sdkDeps.rulesUpdateRepository,
    private val revocationLocalListRepository: RevocationLocalListRepository = sdkDeps.revocationLocalListRepository
) : BaseReactiveState<BaseEvents>(scope) {

    public val settingItems: MutableValueFlow<List<SettingItem>> = MutableValueFlow(buildList())
    public val allUpToDate: StateFlow<Boolean> = derived {
        isUpToDate(get(rulesUpdateRepository.lastEuRulesUpdate)) &&
            isUpToDate(get(rulesUpdateRepository.lastDomesticRulesUpdate)) &&
            isUpToDate(get(rulesUpdateRepository.lastValueSetsUpdate)) &&
            isUpToDate(get(dscRepository.lastUpdate)) &&
            isUpToDate(get(rulesUpdateRepository.lastCountryListUpdate)) &&
            isOfflineRevocationActiveAndUpdated(
                get(revocationLocalListRepository.revocationListUpdateIsOn),
                get(revocationLocalListRepository.lastRevocationUpdateFinish)
            )
    }
    private val canceled: MutableValueFlow<Boolean> = MutableValueFlow(false)

    public fun update() {
        launch {
            dscListUpdater.update()
            if (!canceled.value) {
                euRulesRepository.loadRules()
            }
            if (!canceled.value) {
                domesticRulesRepository.loadRules()
            }
            if (!canceled.value) {
                valueSetsRepository.loadValueSets()
            }
            if (!canceled.value) {
                countriesRepository.loadCountries()
            }
            if (isCovPassCheck && !canceled.value) {
                revocationLocalListRepository.update()
            }
            if (canceled.value) {
                canceled.value = false
                revocationLocalListRepository.revocationListUpdateCanceled.value = false
            }
            settingItems.value = buildList()
        }
    }

    public fun deleteRevocationLocalList() {
        launch {
            revocationLocalListRepository.deleteAll()
        }
    }

    public fun cancel() {
        canceled.value = true
        revocationLocalListRepository.revocationListUpdateCanceled.value = true
    }

    private fun isOfflineRevocationActiveAndUpdated(
        revocationListUpdateIsOn: Boolean,
        lastRevocationUpdateFinish: Instant
    ): Boolean {
        return if (isCovPassCheck) {
            if (revocationListUpdateIsOn) {
                isUpToDate(lastRevocationUpdateFinish)
            } else {
                true
            }
        } else {
            true
        }
    }

    private fun buildList(): List<SettingItem> {
        return listOf(
            SettingItem(
                R.string.settings_rules_list_entry,
                getDate(rulesUpdateRepository.lastEuRulesUpdate.value)
            ),
            SettingItem(
                R.string.settings_rules_list_domestic,
                getDate(rulesUpdateRepository.lastDomesticRulesUpdate.value)
            ),
            SettingItem(
                R.string.settings_rules_list_features,
                getDate(rulesUpdateRepository.lastValueSetsUpdate.value)
            ),
            SettingItem(
                R.string.settings_rules_list_issuer,
                getDate(dscRepository.lastUpdate.value)
            ),
            SettingItem(
                R.string.settings_rules_list_countries,
                getDate(rulesUpdateRepository.lastCountryListUpdate.value)
            ),
        ) + getOfflineRevocationItem()
    }

    private fun getOfflineRevocationItem(): List<SettingItem> {
        return if (isCovPassCheck) {
            listOf(
                SettingItem(
                    R.string.settings_rules_list_authorities,
                    getDate(revocationLocalListRepository.lastRevocationUpdateFinish.value)
                )
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

    private fun isUpToDate(lastUpdate: Instant): Boolean {
        val updateInterval = RevocationLocalListRepository.UPDATE_INTERVAL_HOURS * 60 * 60
        return lastUpdate.isAfter(Instant.now().minusSeconds(updateInterval))
    }
}
