/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * Data model which represents an identifier of [GroupedCertificates].
 */
@Serializable
public data class GroupedCertificatesId(
    val name: Name,
    @Contextual
    val birthDate: BirthDate
) : java.io.Serializable
