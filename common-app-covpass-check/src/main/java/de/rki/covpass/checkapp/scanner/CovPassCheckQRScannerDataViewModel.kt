/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.scanner

import com.ensody.reactivestate.BaseReactiveState
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.checkapp.validitycheck.CovPassCheckValidationResult
import de.rki.covpass.sdk.cert.models.*
import de.rki.covpass.sdk.utils.DataComparison
import de.rki.covpass.sdk.utils.DccNameMatchingUtils.compareHolder
import de.rki.covpass.sdk.utils.formatDateFromString
import kotlinx.coroutines.CoroutineScope
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Interface to communicate events from [CovPassCheckQRScannerDataViewModel] to [CovPassCheckQRScannerFragment].
 */
internal interface CovPassCheckQRScannerDataEvents : BaseEvents {
    fun on2gData(firstCertData: ValidationResult2gData?, secondCertData: ValidationResult2gData?)
    fun on2gPlusBData(boosterCertData: ValidationResult2gData)
    fun on3gSuccess(certificate: CovCertificate)
    fun on3gValidPcrTest(certificate: CovCertificate, sampleCollection: ZonedDateTime?)
    fun on3gValidAntigenTest(certificate: CovCertificate, sampleCollection: ZonedDateTime?)
    fun on3gTechnicalFailure(is2gOn: Boolean = false)
    fun on3gFailure(certificate: CovCertificate? = null, is2gOn: Boolean = false)
    fun showWarning2gUnexpectedType()
}

/**
 * ViewModel holding the data preparation for both 2g+ and 3g check.
 */
internal class CovPassCheckQRScannerDataViewModel constructor(
    scope: CoroutineScope,
    private val isTwoGOn: Boolean,
    private val isTwoGPlusBOn: Boolean,
) : BaseReactiveState<CovPassCheckQRScannerDataEvents>(scope) {

    var firstCertificateData2G: ValidationResult2gData? = null
    var secondCertificateData2G: ValidationResult2gData? = null

    fun prepareDataOnSuccess(certificate: CovCertificate) {
        if (isTwoGOn) {
            when {
                isTwoGPlusBOn && certificate.dgcEntry is Vaccination &&
                    (certificate.dgcEntry as Vaccination).isBooster -> {
                    firstCertificateData2G = ValidationResult2gData(
                        certificate.fullName,
                        certificate.fullTransliteratedName,
                        formatDateFromString(certificate.birthDateFormatted),
                        null,
                        CovPassCheckValidationResult.Success,
                        certificate.dgcEntry.id,
                        verify2gCertificateType(certificate),
                        (certificate.dgcEntry as Vaccination)
                            .occurrence?.atStartOfDay(ZoneId.systemDefault())?.toInstant(),
                        validationName = certificate.name.toValidationResult2gName()
                    )
                    secondCertificateData2G = null
                    eventNotifier {
                        firstCertificateData2G?.let {
                            on2gPlusBData(it)
                        }
                    }
                }
                isNewCertificateValid(certificate) -> {
                    onDataPreparationFinish(
                        ValidationResult2gData(
                            certificate.fullName,
                            certificate.fullTransliteratedName,
                            formatDateFromString(certificate.birthDateFormatted),
                            null,
                            CovPassCheckValidationResult.Success,
                            certificate.dgcEntry.id,
                            verify2gCertificateType(certificate),
                            when (certificate.dgcEntry) {
                                is Recovery -> {
                                    (certificate.dgcEntry as Recovery)
                                        .firstResult?.atStartOfDay(ZoneId.systemDefault())?.toInstant()
                                }
                                is TestCert -> {
                                    (certificate.dgcEntry as TestCert).sampleCollection
                                }
                                is Vaccination -> {
                                    (certificate.dgcEntry as Vaccination)
                                        .occurrence?.atStartOfDay(ZoneId.systemDefault())?.toInstant()
                                }
                            } as Instant?,
                            validationName = certificate.name.toValidationResult2gName()
                        )
                    )
                }
                else -> {
                    show2GError()
                }
            }
        } else {
            eventNotifier {
                on3gSuccess(certificate)
            }
        }
    }

    fun prepareDataOnValidPcrTest(certificate: CovCertificate, sampleCollection: ZonedDateTime?) {
        if (isTwoGOn) {
            if (isNewCertificateValid(certificate)) {
                onDataPreparationFinish(
                    ValidationResult2gData(
                        certificate.fullName,
                        certificate.fullTransliteratedName,
                        formatDateFromString(certificate.birthDateFormatted),
                        sampleCollection,
                        CovPassCheckValidationResult.Success,
                        certificate.dgcEntry.id,
                        ValidationResult2gCertificateType.PcrTest,
                        validationName = certificate.name.toValidationResult2gName()
                    )
                )
            } else {
                show2GError()
            }
        } else {
            eventNotifier {
                on3gValidPcrTest(certificate, sampleCollection)
            }
        }
    }

    fun prepareDataOnValidAntigenTest(certificate: CovCertificate, sampleCollection: ZonedDateTime?) {
        if (isTwoGOn) {
            if (isNewCertificateValid(certificate)) {
                onDataPreparationFinish(
                    ValidationResult2gData(
                        certificate.fullName,
                        certificate.fullTransliteratedName,
                        formatDateFromString(certificate.birthDateFormatted),
                        sampleCollection,
                        CovPassCheckValidationResult.Success,
                        certificate.dgcEntry.id,
                        ValidationResult2gCertificateType.AntigenTest,
                        validationName = certificate.name.toValidationResult2gName()
                    )
                )
            } else {
                show2GError()
            }
        } else {
            eventNotifier {
                on3gValidAntigenTest(certificate, sampleCollection)
            }
        }
    }

    fun prepareDataOnValidationFailure(isTechnical: Boolean, certificate: CovCertificate?) {
        val validationResult = if (isTechnical) {
            CovPassCheckValidationResult.TechnicalError
        } else {
            CovPassCheckValidationResult.ValidationError
        }
        if (isTwoGOn) {
            if (certificate != null) {
                prepareData2g(certificate, validationResult)
            } else {
                prepareData2gNullCert(isTechnical, validationResult)
            }
        } else {
            prepareDataOnValidationFailure3g(isTechnical, certificate)
        }
    }

    fun compareData(certData: ValidationResult2gData?, testData: ValidationResult2gData?): DataComparison {
        val name1 = certData?.validationName?.toName()
        val name2 = testData?.validationName?.toName()
        val dob1 = certData?.certificateBirthDate
        val dob2 = testData?.certificateBirthDate
        return compareHolder(name1, name2, dob1, dob2)
    }

    private fun addValidationResult2gData(certificateData2G: ValidationResult2gData) {
        if (firstCertificateData2G == null) {
            firstCertificateData2G = certificateData2G
        } else {
            secondCertificateData2G = certificateData2G
        }
    }

    private fun onDataPreparationFinish(certificateData2G: ValidationResult2gData) {
        addValidationResult2gData(certificateData2G)
        eventNotifier {
            on2gData(
                firstCertificateData2G,
                secondCertificateData2G
            )
        }
    }

    private fun prepareData2g(certificate: CovCertificate, validationResult: CovPassCheckValidationResult) {
        when (certificate.dgcEntry) {
            is Recovery,
            is Vaccination,
            -> {
                if (isNewCertificateValid(certificate)) {
                    onDataPreparationFinish(
                        ValidationResult2gData(
                            certificate.fullName,
                            certificate.fullTransliteratedName,
                            formatDateFromString(certificate.birthDateFormatted),
                            null,
                            validationResult,
                            certificate.dgcEntry.id,
                            verify2gCertificateType(certificate),
                            validationName = certificate.name.toValidationResult2gName()
                        )
                    )
                } else {
                    show2GError()
                }
            }
            is TestCert -> {
                if (isNewCertificateValid(certificate)) {
                    onDataPreparationFinish(
                        ValidationResult2gData(
                            certificate.fullName,
                            certificate.fullTransliteratedName,
                            formatDateFromString(certificate.birthDateFormatted),
                            null,
                            validationResult,
                            certificate.dgcEntry.id,
                            verify2gCertificateType(certificate),
                            validationName = certificate.name.toValidationResult2gName()
                        )
                    )
                } else {
                    show2GError()
                }
            }
        }
    }

    private fun prepareData2gNullCert(
        isTechnical: Boolean,
        validationResult: CovPassCheckValidationResult
    ) {
        when {
            firstCertificateData2G == null && secondCertificateData2G == null && isTechnical -> {
                eventNotifier {
                    on3gTechnicalFailure(true)
                }
            }
            firstCertificateData2G == null && secondCertificateData2G == null && !isTechnical -> {
                eventNotifier {
                    on3gFailure(is2gOn = true)
                }
            }
            firstCertificateData2G != null -> {
                onDataPreparationFinish(
                    ValidationResult2gData(
                        null,
                        null,
                        null,
                        null,
                        validationResult,
                        null,
                        ValidationResult2gCertificateType.NullCertificateOrUnknown,
                        validationName = null
                    )
                )
            }
            secondCertificateData2G != null -> {
                onDataPreparationFinish(
                    ValidationResult2gData(
                        null,
                        null,
                        null,
                        null,
                        validationResult,
                        null,
                        ValidationResult2gCertificateType.NullCertificateOrUnknown,
                        validationName = null
                    )
                )
            }
        }
    }

    private fun prepareDataOnValidationFailure3g(
        isTechnical: Boolean,
        certificate: CovCertificate?,
    ) {
        if (isTechnical) {
            eventNotifier {
                on3gTechnicalFailure()
            }
        } else {
            eventNotifier {
                on3gFailure(certificate = certificate)
            }
        }
    }

    private fun show2GError() {
        eventNotifier {
            showWarning2gUnexpectedType()
        }
    }

    private fun isNewCertificateValid(certificate: CovCertificate) =
        firstCertificateData2G == null ||
            (
                firstCertificateData2G != null &&
                    secondCertificateData2G == null &&
                    firstCertificateData2G?.let {
                        compareCertificateTypes(verify2gCertificateType(certificate), it.type)
                    } ?: false
                )

    private fun compareCertificateTypes(
        firstCertType: ValidationResult2gCertificateType,
        secondCertType: ValidationResult2gCertificateType
    ) = when {
        firstCertType == ValidationResult2gCertificateType.Vaccination &&
            secondCertType == ValidationResult2gCertificateType.Booster -> false
        secondCertType == ValidationResult2gCertificateType.Vaccination &&
            firstCertType == ValidationResult2gCertificateType.Booster -> false
        firstCertType == ValidationResult2gCertificateType.PcrTest &&
            secondCertType == ValidationResult2gCertificateType.AntigenTest -> false
        firstCertType == ValidationResult2gCertificateType.AntigenTest &&
            secondCertType == ValidationResult2gCertificateType.PcrTest -> false
        else -> firstCertType != secondCertType
    }

    private fun verify2gCertificateType(certificate: CovCertificate?) =
        when {
            certificate == null -> {
                ValidationResult2gCertificateType.NullCertificateOrUnknown
            }
            certificate.dgcEntry is Vaccination &&
                (certificate.dgcEntry as? Vaccination)?.isBooster == true -> {
                ValidationResult2gCertificateType.Booster
            }
            certificate.dgcEntry is Vaccination -> {
                ValidationResult2gCertificateType.Vaccination
            }
            certificate.dgcEntry is Recovery -> {
                ValidationResult2gCertificateType.Recovery
            }
            certificate.dgcEntry is TestCert &&
                (
                    certificate.dgcEntry.type == TestCertType.NEGATIVE_PCR_TEST ||
                        certificate.dgcEntry.type == TestCertType.POSITIVE_PCR_TEST
                    ) -> {
                ValidationResult2gCertificateType.PcrTest
            }
            certificate.dgcEntry is TestCert &&
                (
                    certificate.dgcEntry.type == TestCertType.NEGATIVE_ANTIGEN_TEST ||
                        certificate.dgcEntry.type == TestCertType.POSITIVE_ANTIGEN_TEST
                    ) -> {
                ValidationResult2gCertificateType.AntigenTest
            }
            else -> {
                ValidationResult2gCertificateType.NullCertificateOrUnknown
            }
        }
}
