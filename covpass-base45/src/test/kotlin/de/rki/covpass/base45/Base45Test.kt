/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.base45

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.test.fail

internal class Base45Test {
    /** Makes sure we have *some* kind of (reversible) encoding, but not necessarily the correct one. */
    @Test
    fun `for all possible 0-2 char inputs X, decode(encode(X)) = X`() {
        // It's enough to check all 0-2 char inputs because base45 encodes 2 chars -> 3 chars or 1 char -> 2 chars.
        // There are no other possible encodings. Longer sequences are just concatenations of the 1-2 chars problem.
        val values = sequence {
            yield(listOf())
            for (x in 0..255) {
                yield(listOf(x.toByte()))
                for (y in 0..255) {
                    yield(listOf(x.toByte(), y.toByte()))
                }
            }
        }
        val validEncodings = mutableSetOf<String>()
        for (data in values) {
            val encoded = Base45.encode(data.toByteArray())
            // The length is correct.
            val expectedLength = when (data.size) {
                0 -> 0
                1 -> 2
                2 -> 3
                else -> fail("Invalid data input size")
            }
            assertEquals(expectedLength, encoded.size)
            // The encoded output only uses chars from ENCODING_CHARSET.
            assertTrue(encoded.all { it in ENCODING_CHARSET })
            // The encoding is reversible.
            assertEquals(data, Base45.decode(encoded).toList())
            validEncodings.add(String(encoded))
        }

        // Even allowed chars can result in invalid sequences. We detect this as an error.
        // Checking for all ~16M char sequences takes a little bit too long, so we limit this test to the allowed ones
        // and check in another test that invalid chars are also detected.
        // Also, I swear I ran the test against the ~16M once and it succeeded. ;)
        val allEncodings = sequence {
            yield(listOf())
            for (x in ENCODING_CHARSET) {
                yield(listOf(x))
                for (y in ENCODING_CHARSET) {
                    yield(listOf(x, y))
                    for (z in ENCODING_CHARSET) {
                        yield(listOf(x, y, z))
                    }
                }
            }
        }
        for (x in allEncodings) {
            val data = x.toByteArray()
            if (String(data) in validEncodings) {
                continue
            }
            assertFailsWith<Base45DecodeException> { String(Base45.decode(data)) }
        }
    }

    /**
     * Makes sure we have a correct encoding for *some* inputs.
     *
     * Together with the reversibility check we should have sufficient correctness guarantees.
     */
    @Test
    fun `encoding and decoding`() {
        for ((raw, base45) in rawToBase45) {
            assertEquals(base45, String(Base45.encode(raw.toByteArray())))
            assertEquals(raw, String(Base45.decode(base45.toByteArray())))
        }
    }

    @Test
    fun `invalid base45 strings`() {
        for (x in listOf("::", ":::", "___", "_", "a", "aa", "aaa", "A", "ZZ", "ZZZ")) {
            assertFailsWith<Base45DecodeException> { String(Base45.decode(x.toByteArray())) }
        }
    }

    companion object {
        val rawToBase45 = mapOf(
            // Taken from https://tools.ietf.org/id/draft-faltstrom-base45-02.html
            // 4.1 encoding example 1
            "AB" to "BB8",
            // 4.1 encoding example 2
            "Hello!!" to "%69 VD92EX0",
            // 4.1 encoding example 3
            "base-45" to "UJCLQE7W581",
            // 4.2 decoding example 1
            "ietf!" to "QED8WEX0",

            // Make sure edge cases are taken into account (also cross-checked expected results with other encoders)
            // 1 char
            "x" to "U2",
            // empty string
            "" to "",
            // bytes > 127 (represented as negative numbers in ByteArray because Byte is a signed number)
            "öäüß" to "HXO:WONXO*WO",
        )
    }
}
