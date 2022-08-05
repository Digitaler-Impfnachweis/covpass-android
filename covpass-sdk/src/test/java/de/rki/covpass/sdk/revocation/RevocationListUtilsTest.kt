/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.revocation

import com.upokecenter.cbor.CBORObject
import de.rki.covpass.sdk.revocation.database.RevocationKidLocal
import kotlin.test.Test
import kotlin.test.assertEquals

internal class RevocationListUtilsTest {

    @Test
    fun `test toListOfByteArrays`() {
        val list = listOf(byteArrayOf(0x0a, 0x0b), byteArrayOf(0x0b, 0x0c))
        val cbor = CBORObject.FromObject(list)

        val result = cbor.toListOfByteArrays()
        assertEquals(list, result)
    }

    @Test
    fun `test toKidList`() {
        val map = mapOf(
            Pair(
                byteArrayOf(0x0a, 0x0b),
                mapOf(
                    Pair(byteArrayOf(0x0a), 4),
                ),
            ),
            Pair(
                byteArrayOf(0x0b, 0x0c),
                mapOf(
                    Pair(byteArrayOf(0x0a), 4),
                    Pair(byteArrayOf(0x0b), 2),
                ),
            ),
        )
        val expectedResult = listOf(
            RevocationKidEntry(
                byteArrayOf(0x0a, 0x0b),
                mapOf(
                    Pair(0x0a, 4),
                ),
            ),
            RevocationKidEntry(
                byteArrayOf(0x0b, 0x0c),
                mapOf(
                    Pair(0x0a, 4),
                    Pair(0x0b, 2),
                ),
            ),
        )
        val cbor = CBORObject.FromObject(map)

        val result = cbor.toKidList()
        assertEquals(expectedResult, result)
    }

    @Test
    fun `test toIndexResponse`() {
        val map = mapOf(
            Pair(
                byteArrayOf(0x0a),
                arrayOf(
                    10839741L,
                    1,
                    mapOf(
                        Pair(
                            byteArrayOf(0x0a),
                            arrayOf(10839741L, 3),
                        ),
                    ),
                ),
            ),
            Pair(
                byteArrayOf(0x0b),
                arrayOf(
                    10839741L,
                    1,
                    mapOf(
                        Pair(
                            byteArrayOf(0x0c),
                            arrayOf(10839741L, 2),
                        ),
                    ),
                ),
            ),
        )
        val expectedResult = mapOf<Byte, RevocationIndexEntry>(
            Pair(
                0x0a,
                RevocationIndexEntry(
                    10839741L,
                    1,
                    mapOf(
                        Pair(
                            0x0a,
                            RevocationIndexByte2Entry(
                                10839741L,
                                3,
                            ),
                        ),
                    ),
                ),
            ),
            Pair(
                0x0b,
                RevocationIndexEntry(
                    10839741L,
                    1,
                    mapOf(
                        Pair(
                            0x0c,
                            RevocationIndexByte2Entry(
                                10839741L,
                                2,
                            ),
                        ),
                    ),
                ),
            ),
        )

        val cbor = CBORObject.FromObject(map)

        val result = cbor.toIndexResponse()
        assertEquals(expectedResult, result)
    }

    @Test
    fun `test toListOfRevocationKidEntry`() {
        val list = listOf(
            RevocationKidLocal(
                byteArrayOf(0x0a, 0x0b),
                mapOf(
                    Pair(0x0a, 5),
                    Pair(0x0b, 7),
                ),
            ),
            RevocationKidLocal(
                byteArrayOf(0x0a, 0x0b),
                mapOf(
                    Pair(0x0a, 3),
                    Pair(0x0b, 2),
                ),
            ),
        )
        val expectedResult = listOf(
            RevocationKidEntry(
                byteArrayOf(0x0a, 0x0b),
                mapOf(
                    Pair(0x0a, 5),
                    Pair(0x0b, 7),
                ),
            ),
            RevocationKidEntry(
                byteArrayOf(0x0a, 0x0b),
                mapOf(
                    Pair(0x0a, 3),
                    Pair(0x0b, 2),
                ),
            ),
        )

        val result = list.toListOfRevocationKidEntry()
        assertEquals(expectedResult, result)
    }
}
