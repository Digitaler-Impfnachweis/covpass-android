/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.information

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.commonapp.dependencies.CommonDependencies
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.sdk.cert.GStatusAndMaskValidator
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.storage.CertRepository
import kotlinx.coroutines.CoroutineScope

public class FederalStateSettingsViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val gStatusAndMaskValidator: GStatusAndMaskValidator = sdkDeps.gStatusAndMaskValidator,
    private val certRepository: CertRepository = covpassDeps.certRepository,
    private val commonDependencies: CommonDependencies = commonDeps,
) : BaseReactiveState<BaseEvents>(scope) {

    public fun updateCertificatesStatus() {
        launch {
            gStatusAndMaskValidator.validate(
                certRepository,
                commonDependencies.federalStateRepository.federalState.value,
            )
        }
    }
}
