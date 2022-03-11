/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
public data class ExpertModeData(
    val transactionNumber: String,
    val kid: String,
    val rValueSignature: String,
    val issuingCountry: String,
    val dateOfIssue: String,
    val technicalExpiryDate: String,
) : Parcelable
