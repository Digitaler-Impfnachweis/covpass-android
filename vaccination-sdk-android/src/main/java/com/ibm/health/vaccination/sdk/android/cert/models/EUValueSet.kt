package com.ibm.health.vaccination.sdk.android.cert.models

import kotlinx.serialization.Serializable

@Serializable
internal data class EUValueSet(
    val valueSetId: String,
    val valueSetDate: String,
    val valueSetValues: Map<String, EUValueSetValue>
)
