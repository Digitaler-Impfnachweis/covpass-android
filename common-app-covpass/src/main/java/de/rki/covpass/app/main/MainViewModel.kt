/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.main

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ensody.reactivestate.dispatchers
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.sdk.cert.BoosterRulesValidator
import de.rki.covpass.sdk.cert.models.BoosterNotification
import de.rki.covpass.sdk.cert.models.BoosterResult
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.storage.CertRepository
import de.rki.covpass.sdk.utils.getDescriptionLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay

/**
 * ViewModel providing the [onPageSelected] function and holding the [selectedCertId].
 */
internal class MainViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val certRepository: CertRepository = covpassDeps.certRepository,
    private val boosterRulesValidator: BoosterRulesValidator = sdkDeps.boosterRulesValidator
) : BaseReactiveState<BaseEvents>(scope) {

    init {
        runBoosterValidation()
    }

    var selectedCertId: GroupedCertificatesId? = null

    fun onPageSelected(position: Int) {
        selectedCertId = certRepository.certs.value.getSortedCertificates()[position].id
    }

    private fun runBoosterValidation() {
        launch(dispatchers.default) {
            while (true) {
                checkBoosterNotification()
                delay(BOOSTER_RULE_VALIDATION_INTERVAL_MS)
            }
        }
    }

    private suspend fun checkBoosterNotification() {
        val groupedCertificatesList = certRepository.certs.value
        for (groupedCert in groupedCertificatesList.certificates) {
            val latestVaccination = groupedCert.getLatestVaccination()?.covCertificate
            val latestRecovery = groupedCert.getLatestRecovery()?.covCertificate
            val recovery = latestRecovery?.recovery
            val boosterNotification = when {
                latestVaccination != null && recovery != null -> {
                    val mergedCertificate = latestVaccination.copy(
                        recoveries = listOf(recovery)
                    )
                    validateBoosterRules(boosterRulesValidator, mergedCertificate)
                }
                latestVaccination != null -> {
                    validateBoosterRules(boosterRulesValidator, latestVaccination)
                }
                else -> BoosterNotification(BoosterResult.Failed)
            }
            groupedCert.boosterNotification = boosterNotification
        }

        if (groupedCertificatesList.certificates.isNotEmpty()) {
            certRepository.certs.update {
                it.certificates = groupedCertificatesList.certificates
            }
        }
    }

    private suspend fun validateBoosterRules(
        boosterRulesValidator: BoosterRulesValidator,
        covCertificate: CovCertificate,
    ): BoosterNotification {
        val boosterResult = boosterRulesValidator.validate(covCertificate).firstOrNull {
            it.result == de.rki.covpass.sdk.cert.BoosterResult.PASSED
        }
        return if (boosterResult != null) {
            BoosterNotification(
                BoosterResult.Passed,
                boosterResult.rule.getDescriptionFor(getDescriptionLanguage()),
                boosterResult.rule.identifier
            )
        } else {
            BoosterNotification(BoosterResult.Failed)
        }
    }

    private companion object {
        private const val BOOSTER_RULE_VALIDATION_INTERVAL_MS: Long = 60L * 60L * 1000L
    }
}
