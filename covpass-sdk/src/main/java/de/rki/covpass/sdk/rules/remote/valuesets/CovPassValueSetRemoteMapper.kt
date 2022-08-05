/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.remote.valuesets

import de.rki.covpass.sdk.rules.CovPassValueSet

public fun CovPassValueSetRemote.toCovPassValueSet(hash: String): CovPassValueSet = CovPassValueSet(
    valueSetId = valueSetId,
    valueSetDate = valueSetDate,
    valueSetValues = valueSetValues.toString(),
    hash = hash,
)
