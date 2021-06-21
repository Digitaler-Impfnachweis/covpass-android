/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert.models

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.temporal.Temporal

/**
 * Data model which holds the certificate owners date of birth
 */
@Serializable
public data class BirthDate(
    val birthDate: Temporal = BIRTH_DATE_EMPTY
) : java.io.Serializable {
    public companion object {
        public val BIRTH_DATE_EMPTY: LocalDate = LocalDate.MIN
    }
}
