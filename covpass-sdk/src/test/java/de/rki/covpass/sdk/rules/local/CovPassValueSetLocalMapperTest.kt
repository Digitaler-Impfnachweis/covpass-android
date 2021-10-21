/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.local

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.rki.covpass.sdk.rules.CovPassValueSet
import de.rki.covpass.sdk.rules.local.valuesets.*
import dgca.verifier.app.engine.data.ValueSet
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

public class CovPassValueSetLocalMapperTest {

    @Test
    public fun `test toCovPassValueSetLocal()`() {
        val valueSet = CovPassValueSet("id", LocalDate.parse("2021-05-18"), "values", "hash")
        val valueSetLocal = CovPassValueSetLocal(0, "id", LocalDate.parse("2021-05-18"), "values", "hash")
        val convertedValueSetLocal: CovPassValueSetLocal = valueSet.toCovPassValueSetLocal()
        assertEquals(convertedValueSetLocal, valueSetLocal)
    }

    @Test
    public fun `test toCovPassValueSetsLocal()`() {
        val valueSets = listOf(
            CovPassValueSet("id1", LocalDate.parse("2021-05-18"), "values1", "hash1"),
            CovPassValueSet("id2", LocalDate.parse("2021-05-18"), "values2", "hash2")
        )
        val valueSetsLocal = listOf(
            CovPassValueSetLocal(0, "id1", LocalDate.parse("2021-05-18"), "values1", "hash1"),
            CovPassValueSetLocal(0, "id2", LocalDate.parse("2021-05-18"), "values2", "hash2")
        )
        val convertedValueSetLocal: List<CovPassValueSetLocal> = valueSets.toCovPassValueSetsLocal()
        assertEquals(convertedValueSetLocal, valueSetsLocal)
    }

    @Test
    public fun `test toValueSet()`() {
        val covpassValueSetLocal =
            CovPassValueSetLocal(0, "id", LocalDate.parse("2021-05-18"), "{\"value\": 1}", "hash")
        val valueSet = ValueSet("id", LocalDate.parse("2021-05-18"), jacksonObjectMapper().readTree("{\"value\": 1}"))
        val convertedValueSet: ValueSet = covpassValueSetLocal.toValueSet()
        assertEquals(convertedValueSet, valueSet)
    }

    @Test
    public fun `test List toValueSet()`() {
        val valueSetsLocal = listOf(
            CovPassValueSetLocal(0, "id1", LocalDate.parse("2021-05-18"), "{\"value\": 1}", "hash1"),
            CovPassValueSetLocal(0, "id2", LocalDate.parse("2021-05-18"), "{\"value\": 2}", "hash2")
        )
        val valueSets = listOf(
            ValueSet("id1", LocalDate.parse("2021-05-18"), jacksonObjectMapper().readTree("{\"value\": 1}")),
            ValueSet("id2", LocalDate.parse("2021-05-18"), jacksonObjectMapper().readTree("{\"value\": 2}"))
        )
        val convertedValueSetLocal: List<ValueSet> = valueSetsLocal.toValueSets()
        assertEquals(convertedValueSetLocal, valueSets)
    }
}
