/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

public class ZlibTest {

    @Test
    public fun `test compressing and decompressing content`() {
        val input = TEST_STRING.toByteArray(charset(Charsets.UTF_8.name()))
        val decompressedByteArray = Zlib.decompress(Zlib.compress(input))
        assertEquals(TEST_STRING, decompressedByteArray.toString(charset(Charsets.UTF_8.name())))
    }

    @Test
    public fun `test compressing and decompressing size`() {
        val input = TEST_STRING.toByteArray(charset(Charsets.UTF_8.name()))
        val compressedByteArray = Zlib.compress(input)
        assertTrue(input.size > compressedByteArray.size)
        val decompressedByteArray = Zlib.decompress(compressedByteArray)
        assertEquals(input.size, decompressedByteArray.size)
    }

    public companion object {
        public const val TEST_STRING: String = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, " +
            "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. " +
            "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo " +
            "consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat " +
            "nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt " +
            "mollit anim id est laborum."
    }
}
