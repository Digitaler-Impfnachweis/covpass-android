/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.ticketing.data.validate

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

public enum class BookingPortalValidationResponseResult {
    OK, NOK, CHK
}

@Parcelize
@Serializable
public data class BookingValidationResponse(
    val result: BookingPortalValidationResponseResult,
    val sub: String,
    val iss: String,
    val exp: Int? = null,
    val category: List<String>? = emptyList(),
    val confirmation: String,
    val iat: Int,
    @SerialName("results")
    val resultValidations: List<BookingPortalValidationResponseResultItem>,
) : Parcelable

@Parcelize
@Serializable
public data class BookingPortalValidationResponseResultItem(
    val result: BookingPortalValidationResponseResult,
    val identifier: String,
    val details: String,
    val type: String,
) : Parcelable
