/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.scanner

import android.os.Parcelable
import de.rki.covpass.checkapp.validitycheck.CovPassCheckValidationResult
import de.rki.covpass.sdk.cert.models.Name
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.time.ZonedDateTime

@Parcelize
public data class ValidationResult2gData(
    public val certificateName: String?,
    public val certificateTransliteratedName: String?,
    public val certificateBirthDate: String?,
    public val sampleCollection: ZonedDateTime?,
    public val certificateResult: CovPassCheckValidationResult,
    public val certificateId: String?,
    public val type: ValidationResult2gCertificateType,
    public val validFrom: Instant? = null,
    public val validationName: ValidationResult2gName?,
    public val isRecoveryOlder90Days: Boolean = false,
) : Parcelable {
    public fun isBooster(): Boolean =
        type == ValidationResult2gCertificateType.Booster

    public fun isVaccination(): Boolean =
        type == ValidationResult2gCertificateType.Vaccination

    public fun isRecovery(): Boolean =
        type == ValidationResult2gCertificateType.Recovery

    public fun isPCRTest(): Boolean =
        type == ValidationResult2gCertificateType.PcrTest

    public fun isTest(): Boolean =
        type == ValidationResult2gCertificateType.AntigenTest ||
            type == ValidationResult2gCertificateType.PcrTest

    public fun isValid(): Boolean =
        certificateResult == CovPassCheckValidationResult.Success

    public fun isInvalid(): Boolean =
        certificateResult == CovPassCheckValidationResult.TechnicalError ||
            certificateResult == CovPassCheckValidationResult.ValidationError
}

@Parcelize
public enum class ValidationResult2gCertificateType : Parcelable {
    Booster,
    Vaccination,
    Recovery,
    PcrTest,
    AntigenTest,
    NullCertificateOrUnknown
}

@Parcelize
public data class ValidationResult2gName(
    val givenName: String? = null,
    val familyName: String? = null,
    val givenNameTransliterated: String? = null,
    val familyNameTransliterated: String = "",
) : Parcelable

public fun Name.toValidationResult2gName(): ValidationResult2gName =
    ValidationResult2gName(
        this.givenName,
        this.familyName,
        this.givenNameTransliterated,
        this.familyNameTransliterated,
    )

public fun ValidationResult2gName.toName(): Name =
    Name(
        this.givenName,
        this.familyName,
        this.givenNameTransliterated,
        this.familyNameTransliterated,
    )
