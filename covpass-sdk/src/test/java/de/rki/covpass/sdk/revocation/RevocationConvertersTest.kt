/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.revocation

import de.rki.covpass.sdk.revocation.database.RevocationConverters
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class RevocationConvertersTest {

    private val revocationConverters by lazy { RevocationConverters() }

    @Test
    fun `test byteArrayToString and stringToByteArray`() {
        val string = "0a0b"
        val expectedByteArray = byteArrayOf(0x0a, 0x0b)
        val byteArray = revocationConverters.stringToByteArray(string)
        val newString = revocationConverters.byteArrayToString(byteArray)

        assertEquals(string, newString)
        assertTrue { byteArray.contentEquals(expectedByteArray) }
    }

    @Test
    fun `test byteArrayListToString and stringToByteArrayList`() {
        val startList = listOf(byteArrayOf(0x0a, 0x0b), byteArrayOf(0x0b, 0x0c))

        val resultString = revocationConverters.byteArrayListToString(startList)
        val resultList = revocationConverters.stringToByteArrayList(resultString)

        assertTrue {
            startList[0].contentEquals(resultList[0]) &&
                startList[1].contentEquals(resultList[1])
        }
    }

    @Test
    fun `test stringToHashVariantMap and hashVariantMapToString`() {
        val map = mapOf<Byte, Int>(Pair(0x0a, 1), Pair(0x0b, 2))
        val expectedString = "{\"10\":1,\"11\":2}"
        val resultString = revocationConverters.hashVariantMapToString(map)
        val resultMap = revocationConverters.stringToHashVariantMap(resultString)

        assertEquals(expectedString, resultString)
        assertEquals(map, resultMap)
    }

    @Test
    fun `test stringToIndexMap and indexMapToString`() {
        val indexMap = mapOf<Byte, RevocationIndexEntry>(
            Pair(
                0x0a,
                RevocationIndexEntry(
                    10032498L,
                    1,
                    mapOf(
                        Pair(0x0a, RevocationIndexByte2Entry(10032498L, 1)),
                        Pair(0x0b, RevocationIndexByte2Entry(10032498L, 2))
                    )
                )
            ),
            Pair(
                0x0b,
                RevocationIndexEntry(
                    10032498L,
                    1,
                    mapOf(
                        Pair(0x0a, RevocationIndexByte2Entry(10032498L, 1)),
                        Pair(0x0b, RevocationIndexByte2Entry(10032498L, 2))
                    )
                )
            )
        )
        val expectedString = "{\"10\":{\"timestamp\":10032498,\"num\":1,\"byte2\":{\"10\":{\"timestamp\":10032498," +
            "\"num\":1},\"11\":{\"timestamp\":10032498,\"num\":2}}},\"11\":{\"timestamp\":10032498,\"num\":1," +
            "\"byte2\":{\"10\":{\"timestamp\":10032498,\"num\":1},\"11\":{\"timestamp\":10032498,\"num\":2}}}}"

        val resultString = revocationConverters.indexMapToString(indexMap)
        val resultIndexMap = revocationConverters.stringToIndexMap(resultString)

        assertEquals(expectedString, resultString)
        assertEquals(indexMap, resultIndexMap)
    }
}
