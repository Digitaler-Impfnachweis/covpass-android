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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant

@SuppressWarnings("UnusedPrivateMember")
public class SettingsUpdateViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    isCovPassCheck: Boolean,
    private val dscRepository: DscRepository = sdkDeps.dscRepository,
    settingUpdateListBuilder: SettingUpdateListBuilder = commonDeps.settingsUpdateListBuilder,
) : BaseReactiveState<BaseEvents>(scope) {

    public val settingItems: MutableValueFlow<List<SettingItem>> =
        MutableValueFlow(settingUpdateListBuilder.buildList(isCovPassCheck))
    public val allUpToDate: StateFlow<Boolean> = derived {
        isUpToDate(get(dscRepository.lastUpdate))
    }

    private fun isUpToDate(lastUpdate: Instant): Boolean {
        val updateInterval = RevocationLocalListRepository.UPDATE_INTERVAL_HOURS * 60 * 60
        return lastUpdate.isAfter(Instant.now().minusSeconds(updateInterval))
    }
}
