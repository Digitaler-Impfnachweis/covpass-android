/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.scanner

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ensody.reactivestate.ErrorEvents
import de.rki.covpass.checkapp.dependencies.covpassCheckDeps
import de.rki.covpass.checkapp.storage.CheckAppRepository
import de.rki.covpass.checkapp.storage.CheckingMode
import de.rki.covpass.checkapp.validitycheck.CovPassCheckImmunityValidationResult
import de.rki.covpass.checkapp.validitycheck.CovPassCheckValidationResult
import de.rki.covpass.checkapp.validitycheck.validate
import de.rki.covpass.checkapp.validitycheck.validateImmunityStatus
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.logging.Lumber
import de.rki.covpass.sdk.cert.CovPassRulesValidator
import de.rki.covpass.sdk.cert.QRCoder
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.TestCert
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.cert.validateEntity
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.revocation.RevocationRemoteListRepository
import de.rki.covpass.sdk.utils.DataComparison
import de.rki.covpass.sdk.utils.DccNameMatchingUtils.compareHolder
import kotlinx.coroutines.CoroutineScope

/**
 * Interface to communicate events from [CovPassCheckQRScannerViewModel] to [CovPassCheckQRScannerFragment].
 */
internal interface CovPassCheckQRScannerEvents : ErrorEvents {
    fun onValidationSuccess(certificate: CovCertificate, isSecondCertificate: Boolean, dataComparison: DataComparison)
    fun onValidationFailure(certificate: CovCertificate, isSecondCertificate: Boolean)
    fun onValidationTechnicalFailure(certificate: CovCertificate? = null)
    fun onValidationNoRulesFailure(certificate: CovCertificate)
    fun onImmunityValidationSuccess(
        certificate: CovCertificate,
        numberOfCertificates: Int,
    )

    fun onImmunityValidationFailure(
        certificate: CovCertificate,
        numberOfCertificates: Int,
    )

    fun onImmunityValidationTechnicalFailure(certificate: CovCertificate? = null)
    fun showWarningDuplicatedType()
    fun showWarningDifferentData()
}

/**
 * ViewModel holding the business logic for decoding and validating a [CovCertificate].
 */
internal class CovPassCheckQRScannerViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val qrCoder: QRCoder = sdkDeps.qrCoder,
    private val domesticRulesValidator: CovPassRulesValidator = sdkDeps.domesticRulesValidator,
    private val euRulesValidator: CovPassRulesValidator = sdkDeps.euRulesValidator,
    private val revocationRemoteListRepository: RevocationRemoteListRepository = sdkDeps.revocationRemoteListRepository,
    private val regionId: String = commonDeps.federalStateRepository.federalState.value,
    private val checkAppRepository: CheckAppRepository = covpassCheckDeps.checkAppRepository,
) : BaseReactiveState<CovPassCheckQRScannerEvents>(scope) {

    var firstCovCertificate: CovCertificate? = null
    var secondCovCertificate: CovCertificate? = null
    var thirdCovCertificate: CovCertificate? = null

    fun onQrContentReceived(qrContent: String) {
        launch {
            try {
                val covCertificate = qrCoder.decodeCovCert(qrContent)
                val dgcEntry = covCertificate.dgcEntry
                val firstCovCert = firstCovCertificate
                val secondCovCert = secondCovCertificate
                if (checkAppRepository.activatedCheckingMode.value == CheckingMode.ModeMaskStatus) {
                    if (firstCovCert != null && dgcEntry.type == firstCovCert.dgcEntry.type) {
                        eventNotifier {
                            showWarningDuplicatedType()
                        }
                        return@launch
                    }
                } else {
                    if (firstCovCert != null && compareData(firstCovCert, covCertificate) != DataComparison.Equal) {
                        eventNotifier {
                            showWarningDifferentData()
                        }
                        return@launch
                    }
                }
                val isSecondCertificate = when {
                    firstCovCert == null -> {
                        firstCovCertificate = covCertificate
                        false
                    }
                    secondCovCert == null -> {
                        secondCovCertificate = covCertificate
                        true
                    }
                    else -> {
                        thirdCovCertificate = covCertificate
                        false
                    }
                }
                validateEntity(dgcEntry.idWithoutPrefix)
                val mergedCovCertificate: CovCertificate = when {
                    firstCovCert != null && dgcEntry is Recovery -> {
                        firstCovCert.copy(
                            recoveries = listOf(dgcEntry),
                        )
                    }
                    firstCovCert != null && dgcEntry is Vaccination -> {
                        firstCovCert.copy(
                            vaccinations = listOf(dgcEntry),
                        )
                    }
                    firstCovCert != null && dgcEntry is TestCert -> {
                        firstCovCert.copy(
                            tests = listOf(dgcEntry),
                        )
                    }
                    else -> {
                        covCertificate
                    }
                }
                if (checkAppRepository.activatedCheckingMode.value == CheckingMode.ModeMaskStatus) {
                    maskStatusValidation(
                        mergedCovCertificate = mergedCovCertificate,
                        covCertificate = covCertificate,
                        firstCovCert = firstCovCert,
                        isSecondCertificate,
                    )
                } else {
                    immunityStatusValidation(
                        mergedCovCertificate = mergedCovCertificate,
                        covCertificate = covCertificate,
                        listOfNotNull(firstCovCertificate, secondCovCertificate, thirdCovCertificate).size,
                    )
                }
            } catch (exception: Exception) {
                Lumber.e(exception)
                if (checkAppRepository.activatedCheckingMode.value == CheckingMode.ModeMaskStatus) {
                    eventNotifier {
                        onValidationTechnicalFailure()
                    }
                } else {
                    eventNotifier {
                        onImmunityValidationTechnicalFailure()
                    }
                }
            }
        }
    }

    private suspend fun maskStatusValidation(
        mergedCovCertificate: CovCertificate,
        covCertificate: CovCertificate,
        firstCovCert: CovCertificate?,
        isSecondCertificate: Boolean,
    ) {
        when (
            validate(
                mergedCovCertificate = mergedCovCertificate,
                covCertificate = covCertificate,
                domesticRulesValidator = domesticRulesValidator,
                euRulesValidator = euRulesValidator,
                revocationRemoteListRepository,
                regionId,
            )
        ) {
            CovPassCheckValidationResult.Success -> eventNotifier {
                val validateData = if (firstCovCert != null) {
                    compareData(firstCovCert, covCertificate)
                } else {
                    DataComparison.HasNullData
                }
                onValidationSuccess(mergedCovCertificate, isSecondCertificate, validateData)
            }
            CovPassCheckValidationResult.ValidationError -> eventNotifier {
                onValidationFailure(mergedCovCertificate, isSecondCertificate)
            }
            CovPassCheckValidationResult.NoMaskRulesError -> eventNotifier {
                onValidationNoRulesFailure(mergedCovCertificate)
            }
            CovPassCheckValidationResult.TechnicalError -> eventNotifier {
                onValidationTechnicalFailure(mergedCovCertificate)
            }
        }
    }

    private suspend fun immunityStatusValidation(
        mergedCovCertificate: CovCertificate,
        covCertificate: CovCertificate,
        numberOfCertificates: Int,
    ) {
        when (
            validateImmunityStatus(
                mergedCovCertificate = mergedCovCertificate,
                covCertificate = covCertificate,
                domesticRulesValidator = domesticRulesValidator,
                revocationRemoteListRepository,
            )
        ) {
            CovPassCheckImmunityValidationResult.Success -> eventNotifier {
                onImmunityValidationSuccess(
                    mergedCovCertificate,
                    numberOfCertificates,
                )
            }
            CovPassCheckImmunityValidationResult.ValidationError -> eventNotifier {
                onImmunityValidationFailure(
                    mergedCovCertificate,
                    numberOfCertificates,
                )
            }
            CovPassCheckImmunityValidationResult.TechnicalError -> eventNotifier {
                onImmunityValidationTechnicalFailure(mergedCovCertificate)
            }
        }
    }

    private fun compareData(covCertificate: CovCertificate, covCertificate2: CovCertificate): DataComparison {
        val name1 = covCertificate.name
        val name2 = covCertificate2.name
        val dob1 = covCertificate.birthDate
        val dob2 = covCertificate2.birthDate

        return compareHolder(name1, name2, dob1, dob2)
    }
}
