/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

@file:UseSerializers(LocalDateSerializer::class)

package de.rki.covpass.sdk.cert.models

import de.rki.covpass.sdk.utils.serialization.LocalDateSerializer
import kotlinx.serialization.UseSerializers

import kotlinx.serialization.Serializable
import java.time.LocalDate

/**
 * Data model which represents an identifier of [GroupedCertificates].
 */
@Serializable
public data class GroupedCertificatesId(
    val name: Name,
    val birthDate: LocalDate?,
) : java.io.Serializable
