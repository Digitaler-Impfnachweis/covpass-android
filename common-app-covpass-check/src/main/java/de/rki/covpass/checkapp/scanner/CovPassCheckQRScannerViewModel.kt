/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.scanner

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ensody.reactivestate.ErrorEvents
import de.rki.covpass.checkapp.validitycheck.CovPassCheckImmunityValidationResult
import de.rki.covpass.checkapp.validitycheck.validateImmunityStatus
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
    fun onValidationSuccess(
        certificate: CovCertificate,
        isSecondCertificate: Boolean,
        dataComparison: DataComparison,
    )

    fun onValidationFailure(
        certificate: CovCertificate,
        isSecondCertificate: Boolean,
        dataComparison: DataComparison,
    )

    fun onValidationTechnicalFailure(certificate: CovCertificate? = null, numberOfCertificates: Int)
    fun onImmunityValidationSuccess(
        certificate: CovCertificate,
        dataComparison: DataComparison3Certs,
        firstCovCert: CovCertificate?,
        secondCovCert: CovCertificate?,
        numberOfCertificates: Int,
    )

    fun onImmunityValidationFailure(
        certificate: CovCertificate,
        dataComparison: DataComparison3Certs,
        firstCovCert: CovCertificate?,
        secondCovCert: CovCertificate?,
        numberOfCertificates: Int,
    )

    fun onImmunityValidationTechnicalFailure(
        certificate: CovCertificate? = null,
        numberOfCertificates: Int,
    )

    fun showWarningDuplicatedType()
    fun showWarningDifferentData()
    fun showWarningDuplicatedCertificate()
}

/**
 * ViewModel holding the business logic for decoding and validating a [CovCertificate].
 */
internal class CovPassCheckQRScannerViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val qrCoder: QRCoder = sdkDeps.qrCoder,
    private val domesticRulesValidator: CovPassRulesValidator = sdkDeps.domesticRulesValidator,
    private val revocationRemoteListRepository: RevocationRemoteListRepository = sdkDeps.revocationRemoteListRepository,
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
                if (listOfNotNull(
                        firstCovCert,
                        secondCovCert,
                    ).any { it.dgcEntry.id == dgcEntry.id }
                ) {
                    eventNotifier {
                        showWarningDuplicatedCertificate()
                    }
                    return@launch
                }
                when {
                    firstCovCert == null -> {
                        firstCovCertificate = covCertificate
                    }
                    secondCovCert == null -> {
                        secondCovCertificate = covCertificate
                    }
                    else -> {
                        thirdCovCertificate = covCertificate
                    }
                }
                validateEntity(dgcEntry.idWithoutPrefix)

                val mergedCovCertificate: CovCertificate =
                    getMergedCertificate(
                        listOfNotNull(
                            firstCovCertificate,
                            secondCovCertificate,
                            thirdCovCertificate,
                        ),
                    ) ?: return@launch

                immunityStatusValidation(
                    mergedCovCertificate = mergedCovCertificate,
                    covCertificate = covCertificate,
                    firstCovCert = firstCovCert,
                    secondCovCert = secondCovCert,
                    listOfNotNull(
                        firstCovCertificate,
                        secondCovCertificate,
                        thirdCovCertificate,
                    ).size,
                )
            } catch (exception: Exception) {
                Lumber.e(exception)
                eventNotifier {
                    onImmunityValidationTechnicalFailure(
                        null,
                        listOfNotNull(firstCovCertificate, secondCovCertificate).size + 1,
                    )
                }
            }
        }
    }

    private fun getMergedCertificate(list: List<CovCertificate>): CovCertificate? {
        if (list.isEmpty()) {
            return null
        }
        if (list.size == 1) {
            return list.first()
        }

        val listVaccination = mutableListOf<Vaccination>()
        val listRecoveries = mutableListOf<Recovery>()
        val listTests = mutableListOf<TestCert>()

        list.forEach {
            when (val dgc = it.dgcEntry) {
                is Recovery -> listRecoveries.add(dgc)
                is TestCert -> listTests.add(dgc)
                is Vaccination -> listVaccination.add(dgc)
            }
        }

        return list.first().copy(
            vaccinations = listVaccination.sortedByDescending { it.occurrence },
            recoveries = listRecoveries.sortedByDescending { it.firstResult },
            tests = listTests.sortedByDescending { it.sampleCollection },
        )
    }

    private suspend fun immunityStatusValidation(
        mergedCovCertificate: CovCertificate,
        covCertificate: CovCertificate,
        firstCovCert: CovCertificate?,
        secondCovCert: CovCertificate?,
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
                val validateData = compareDataImmunityStatus(
                    firstCovCert,
                    secondCovCert,
                    covCertificate,
                )
                onImmunityValidationSuccess(
                    covCertificate,
                    validateData,
                    firstCovCert,
                    secondCovCert,
                    numberOfCertificates,
                )
            }
            CovPassCheckImmunityValidationResult.ValidationError -> eventNotifier {
                val validateData = compareDataImmunityStatus(
                    firstCovCert,
                    secondCovCert,
                    covCertificate,
                )
                onImmunityValidationFailure(
                    covCertificate,
                    validateData,
                    firstCovCert,
                    secondCovCert,
                    numberOfCertificates,
                )
            }
            CovPassCheckImmunityValidationResult.TechnicalError -> eventNotifier {
                onImmunityValidationTechnicalFailure(
                    mergedCovCertificate,
                    listOfNotNull(
                        firstCovCertificate,
                        secondCovCertificate,
                        thirdCovCertificate,
                    ).size,
                )
            }
        }
    }

    private fun compareData(
        covCertificate: CovCertificate,
        covCertificate2: CovCertificate,
    ): DataComparison {
        val name1 = covCertificate.name
        val name2 = covCertificate2.name
        val dob1 = covCertificate.birthDate
        val dob2 = covCertificate2.birthDate

        return compareHolder(name1, name2, dob1, dob2)
    }

    private fun compareDataImmunityStatus(
        firstCovCert: CovCertificate?,
        secondCovCert: CovCertificate?,
        covCertificate: CovCertificate,
    ): DataComparison3Certs {
        return when {
            firstCovCert == null -> {
                DataComparison3Certs.Equal
            }
            secondCovCert == null -> {
                when (compareData(firstCovCert, covCertificate)) {
                    DataComparison.NameDifferent -> DataComparison3Certs.SecondDifferentName
                    DataComparison.DateOfBirthDifferent -> DataComparison3Certs.SecondDifferentDate
                    else -> DataComparison3Certs.Equal
                }
            }
            else -> {
                compareData3Certs(firstCovCert, secondCovCert, covCertificate)
            }
        }
    }

    private fun compareData3Certs(
        covCertificate: CovCertificate,
        covCertificate2: CovCertificate,
        covCertificate3: CovCertificate,
    ): DataComparison3Certs {
        val compareFirstAndSecond = compareData(covCertificate, covCertificate2)
        val compareFirstAndThird = compareData(covCertificate, covCertificate3)
        val compareSecondAndThird = compareData(covCertificate2, covCertificate3)

        return when {
            compareFirstAndSecond == DataComparison.Equal &&
                compareFirstAndThird == DataComparison.Equal &&
                compareSecondAndThird == DataComparison.Equal -> {
                DataComparison3Certs.Equal
            }
            compareFirstAndSecond == DataComparison.Equal &&
                compareFirstAndThird != DataComparison.Equal -> {
                if (compareFirstAndThird == DataComparison.NameDifferent) {
                    DataComparison3Certs.ThirdDifferentName
                } else {
                    DataComparison3Certs.ThirdDifferentDate
                }
            }

            compareFirstAndThird == DataComparison.Equal &&
                compareFirstAndSecond != DataComparison.Equal -> {
                if (compareFirstAndSecond == DataComparison.NameDifferent) {
                    DataComparison3Certs.SecondDifferentName
                } else {
                    DataComparison3Certs.SecondDifferentDate
                }
            }
            compareFirstAndSecond == DataComparison.DateOfBirthDifferent ||
                compareFirstAndThird == DataComparison.DateOfBirthDifferent -> {
                DataComparison3Certs.AllDifferentDate
            }
            else -> {
                DataComparison3Certs.AllDifferentName
            }
        }
    }
}

public enum class DataComparison3Certs {
    Equal,
    SecondDifferentDate,
    SecondDifferentName,
    ThirdDifferentDate,
    ThirdDifferentName,
    AllDifferentName,
    AllDifferentDate
}
