/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import com.upokecenter.cbor.CBORObject
import kotlin.test.Test
import kotlin.test.assertEquals

public class CBORObjectUtilsTest {

    @Test
    public fun `test trimAllStrings`() {
        val cborObject = CBORObject.FromObject(" test ")
        val cborArray = CBORObject.FromObject(listOf(cborObject))
        val cborMap = CBORObject.FromObject(mapOf(Pair(" array ", cborArray)))

        val trimmedCborMap = cborMap.trimAllStrings()
        trimmedCborMap.entries.forEach {
            assertEquals(it.key.AsString(), "array")
            assertEquals(it.value[0].AsString(), "test")
        }
    }
}
