/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.information

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ensody.reactivestate.MutableValueFlow
import com.ensody.reactivestate.derived
import com.ensody.reactivestate.get
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.utils.SettingUpdateListBuilder
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.revocation.RevocationLocalListRepository
import de.rki.covpass.sdk.storage.DscRepository
import de.rki.covpass.sdk.utils.DscListUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant

@SuppressWarnings("UnusedPrivateMember")
public class SettingsUpdateViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val isCovPassCheck: Boolean,
    private val dscListUpdater: DscListUpdater = sdkDeps.dscListUpdater,
    private val dscRepository: DscRepository = sdkDeps.dscRepository,
    private val revocationLocalListRepository: RevocationLocalListRepository = sdkDeps.revocationLocalListRepository,
    private val settingUpdateListBuilder: SettingUpdateListBuilder = commonDeps.settingsUpdateListBuilder,
) : BaseReactiveState<BaseEvents>(scope) {

    public val settingItems: MutableValueFlow<List<SettingItem>> =
        MutableValueFlow(settingUpdateListBuilder.buildList(isCovPassCheck))
    public val allUpToDate: StateFlow<Boolean> = derived {
        isUpToDate(get(dscRepository.lastUpdate)) &&
            isOfflineRevocationActiveAndUpdated(
                get(revocationLocalListRepository.revocationListUpdateIsOn),
                get(revocationLocalListRepository.lastRevocationUpdateFinish),
            )
    }
    private val canceled: MutableValueFlow<Boolean> = MutableValueFlow(false)

    public fun update() {
        launch {
            dscListUpdater.update()
            if (isCovPassCheck && !canceled.value) {
                revocationLocalListRepository.update()
            }
            if (canceled.value) {
                canceled.value = false
                revocationLocalListRepository.revocationListUpdateCanceled.value = false
            }
            settingItems.value = settingUpdateListBuilder.buildList(isCovPassCheck)
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
        lastRevocationUpdateFinish: Instant,
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

    private fun isUpToDate(lastUpdate: Instant): Boolean {
        val updateInterval = RevocationLocalListRepository.UPDATE_INTERVAL_HOURS * 60 * 60
        return lastUpdate.isAfter(Instant.now().minusSeconds(updateInterval))
    }
}
