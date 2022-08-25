/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.main

import android.net.Uri
import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ensody.reactivestate.dispatchers
import de.rki.covpass.app.checkerremark.CheckerRemarkRepository
import de.rki.covpass.app.dependencies.CovpassDependencies
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.commonapp.dependencies.CommonDependencies
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.storage.CheckContextRepository
import de.rki.covpass.commonapp.storage.OnboardingRepository.Companion.CURRENT_DATA_PRIVACY_VERSION
import de.rki.covpass.commonapp.updateinfo.UpdateInfoRepository
import de.rki.covpass.sdk.cert.BoosterRulesValidator
import de.rki.covpass.sdk.cert.models.BoosterNotification
import de.rki.covpass.sdk.cert.models.BoosterResult
import de.rki.covpass.sdk.cert.models.CertValidationResult
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import de.rki.covpass.sdk.cert.models.GroupedCertificatesList
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.revocation.RevocationRemoteListRepository
import de.rki.covpass.sdk.revocation.isBeforeUpdateInterval
import de.rki.covpass.sdk.revocation.validateRevocation
import de.rki.covpass.sdk.storage.CertRepository
import de.rki.covpass.sdk.utils.DescriptionLanguage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay

/**
 * ViewModel providing the [onPageSelected] function and holding the [selectedCertId].
 */
internal class MainViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val uri: Uri?,
    private val certRepository: CertRepository = covpassDeps.certRepository,
    private val boosterRulesValidator: BoosterRulesValidator = sdkDeps.boosterRulesValidator,
    private val covpassDependencies: CovpassDependencies = covpassDeps,
    private val commonDependencies: CommonDependencies = commonDeps,
    private val revocationRemoteListRepository: RevocationRemoteListRepository = sdkDeps.revocationRemoteListRepository,
) : BaseReactiveState<NotificationEvents>(scope) {

    // prevent the import to be done more than one time
    private var importFromShareOption: Boolean = true

    init {
        runValidations()
    }

    internal var showingNotification = CompletableDeferred<Unit>()
    var selectedCertId: GroupedCertificatesId? = null

    /**
     * @return true if the notification is displayed
     */
    private suspend fun validateNotifications(): Boolean =
        when {
            commonDependencies.onboardingRepository.dataPrivacyVersionAccepted.value
                != CURRENT_DATA_PRIVACY_VERSION -> {
                eventNotifier {
                    showNewDataPrivacy()
                }
                true
            }
            commonDependencies.updateInfoRepository.updateInfoVersionShown.value
                != UpdateInfoRepository.CURRENT_UPDATE_VERSION &&
                commonDependencies.updateInfoRepository.updateInfoNotificationActive.value -> {
                eventNotifier {
                    showNewUpdateInfo()
                }
                true
            }
            commonDependencies.checkContextRepository.checkContextNotificationVersionShown.value !=
                CheckContextRepository.CURRENT_CHECK_CONTEXT_NOTIFICATION_VERSION -> {
                eventNotifier {
                    showDomesticRulesNotification()
                }
                true
            }
            certRepository.certs.value.certificates.any { it.hasSeenExpiryNotification } ||
                checkExpiredReissueNotification() -> {
                eventNotifier {
                    showExpiryNotification()
                }
                true
            }
            covpassDependencies.checkerRemarkRepository.checkerRemarkShown.value
                != CheckerRemarkRepository.CURRENT_CHECKER_REMARK_VERSION -> {
                eventNotifier {
                    showCheckerRemark()
                }
                true
            }
            checkBoosterNotification() -> {
                eventNotifier {
                    showBoosterNotification()
                }
                true
            }
            checkBoosterReissueNotification() -> {
                eventNotifier {
                    showBoosterReissueNotification(getBoosterReissueIdsList())
                }
                true
            }
            validateRevokedCertificates() -> {
                eventNotifier {
                    showRevokedNotification()
                }
                true
            }
            importFromShareOption && uri != null -> {
                importFromShareOption = false
                eventNotifier {
                    importCertificateFromShareOption(uri)
                }
                true
            }
            else -> false
        }

    private fun checkBoosterReissueNotification(): Boolean {
        certRepository.certs.value.certificates.forEach { it.validateBoosterReissue() }
        return checkBoosterReadyForReissue()
    }

    private fun checkBoosterReadyForReissue() =
        certRepository.certs.value.certificates.any { it.isBoosterReadyForReissue() && !it.hasSeenReissueNotification }

    private fun getBoosterReissueIdsList(): List<String> {
        return certRepository.certs.value.certificates.first {
            it.isBoosterReadyForReissue() && !it.hasSeenReissueNotification
        }.getListOfIdsReadyForBoosterReissue()
    }

    private fun checkExpiredReissueNotification(): Boolean {
        certRepository.certs.value.certificates.forEach { it.validateExpiredReissue() }
        return checkExpiredReadyForReissue()
    }

    private fun checkExpiredReadyForReissue() =
        certRepository.certs.value.certificates.any {
            it.isExpiredReadyForReissue() && !it.hasSeenExpiredReissueNotification
        }

    fun onPageSelected(position: Int) {
        selectedCertId = certRepository.certs.value.getSortedCertificates()[position].id
    }

    private suspend fun validateRevokedCertificates(): Boolean {
        if (!revocationRemoteListRepository.lastRevocationValidation.value.isBeforeUpdateInterval()) {
            return false
        }
        val listRevokedCertificates = mutableListOf<String>()
        certRepository.certs.value.certificates.forEach { groupedCertificates ->
            groupedCertificates.certificates.forEach {
                if (validateRevocation(it.covCertificate, revocationRemoteListRepository)) {
                    listRevokedCertificates.add(it.covCertificate.dgcEntry.id)
                }
            }
        }
        certRepository.certs.update { groupedCertificatesList ->
            groupedCertificatesList.certificates.forEach { groupedCertificates ->
                groupedCertificates.certificates = groupedCertificates.certificates.map {
                    if (listRevokedCertificates.contains(it.covCertificate.dgcEntry.id)) {
                        it.copy(
                            status = CertValidationResult.Revoked,
                            isRevoked = true,
                        )
                    } else {
                        it
                    }
                }.toMutableList()
            }
        }
        revocationRemoteListRepository.updateLastRevocationValidation()
        return certRepository.certs.value.certificates.any { it.showRevokedNotification() }
    }

    private fun runValidations() {
        launch(dispatchers.default) {
            while (true) {
                showingNotification = CompletableDeferred()
                if (validateNotifications()) {
                    showingNotification.await()
                    continue
                }
                delay(BOOSTER_RULE_VALIDATION_INTERVAL_MS)
            }
        }
    }

    private suspend fun checkBoosterNotification(): Boolean {
        val groupedCertificatesList = certRepository.certs.value
        for (groupedCert in groupedCertificatesList.certificates) {
            val latestVaccination = groupedCert.getLatestVaccination()?.covCertificate
            val latestRecovery = groupedCert.getLatestRecovery()?.covCertificate
            val recovery = latestRecovery?.recovery
            val boosterNotifications = when {
                latestVaccination != null && recovery != null -> {
                    val mergedCertificate = latestVaccination.copy(
                        recoveries = listOf(recovery),
                    )
                    validateBoosterRules(boosterRulesValidator, mergedCertificate)
                }
                latestVaccination != null -> {
                    validateBoosterRules(boosterRulesValidator, latestVaccination)
                }
                else -> emptyList()
            }

            boosterNotifications.find { it.ruleId !in groupedCert.boosterNotificationRuleIds }
                ?.let {
                    groupedCert.boosterNotification = it
                    groupedCert.boosterNotificationRuleIds += it.ruleId
                    groupedCert.hasSeenBoosterNotification = false
                    groupedCert.hasSeenBoosterDetailNotification = false
                }

            if (boosterNotifications.isNotEmpty()) {
                groupedCert.boosterNotification = boosterNotifications.find {
                    it.ruleId == groupedCert.boosterNotificationRuleIds.lastOrNull()
                } ?: boosterNotifications.last()
            }
        }

        if (groupedCertificatesList.certificates.isNotEmpty()) {
            certRepository.certs.update {
                it.certificates = groupedCertificatesList.certificates
            }
        }

        return isBoosterNotificationToShow(groupedCertificatesList)
    }

    private fun isBoosterNotificationToShow(groupedCertificatesList: GroupedCertificatesList): Boolean {
        return groupedCertificatesList.certificates.any {
            it.boosterNotification.result == BoosterResult.Passed && !it.hasSeenBoosterNotification
        }
    }

    private suspend fun validateBoosterRules(
        boosterRulesValidator: BoosterRulesValidator,
        covCertificate: CovCertificate,
    ): List<BoosterNotification> {
        return boosterRulesValidator.validate(covCertificate).filter {
            it.result == de.rki.covpass.sdk.cert.BoosterResult.PASSED
        }.map {
            BoosterNotification(
                BoosterResult.Passed,
                it.rule.getDescriptionFor(DescriptionLanguage.ENGLISH.languageCode),
                it.rule.getDescriptionFor(DescriptionLanguage.GERMAN.languageCode),
                it.rule.identifier,
            )
        }
    }

    private companion object {
        private const val BOOSTER_RULE_VALIDATION_INTERVAL_MS: Long = 60L * 60L * 1000L
    }
}
