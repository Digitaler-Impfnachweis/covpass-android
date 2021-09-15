/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.local

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.rki.covpass.sdk.rules.CovPassValueSet
import de.rki.covpass.sdk.rules.local.valuesets.CovPassValueSetLocal
import dgca.verifier.app.engine.data.ValueSet

public fun CovPassValueSet.toCovPassValueSetLocal(): CovPassValueSetLocal =
    CovPassValueSetLocal(
        valueSetId = valueSetId,
        valueSetDate = valueSetDate,
        valueSetValues = valueSetValues,
        hash = hash
    )

public fun Collection<CovPassValueSet>.toCovPassValueSetsLocal(): List<CovPassValueSetLocal> =
    map { it.toCovPassValueSetLocal() }

public fun CovPassValueSetLocal.toValueSet(): ValueSet =
    ValueSet(
        valueSetId = valueSetId,
        valueSetDate = valueSetDate,
        valueSetValues = jacksonObjectMapper().readTree(valueSetValues)
    )

public fun Collection<CovPassValueSetLocal>.toValueSets(): List<ValueSet> =
    map { it.toValueSet() }
