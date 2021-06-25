/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.license.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data model which contains a list of [License] and other information about the licenses.
 */
@Serializable
public data class OpenSourceItem(
    val project: String?,
    val description: String?,
    val version: String?,
    val developers: List<String>?,
    val url: String?,
    val year: String?,
    val licenses: List<License>?,
    val dependency: String?,
)

/**
 * Data model which contains licence name and url.
 */
@Serializable
public data class License(
    val license: String?,
    @SerialName("license_url")
    val licenseUrl: String?,
)
