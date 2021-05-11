package com.ibm.health.vaccination.sdk.android.zlib

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import com.ibm.health.vaccination.sdk.android.utils.Zlib
import org.junit.Test

public class ZlibTest {

    @Test
    public fun `test compressing and decompressing content`() {
        val input = TEST_STRING.toByteArray(charset(Charsets.UTF_8.name()))
        val decompressedByteArray = Zlib.decompress(Zlib.compress(input))
        assertThat(decompressedByteArray.toString(charset(Charsets.UTF_8.name()))).isEqualTo(TEST_STRING)
    }

    @Test
    public fun `test compressing and decompressing size`() {
        val input = TEST_STRING.toByteArray(charset(Charsets.UTF_8.name()))
        val compressedByteArray = Zlib.compress(input)
        assertThat(input.size).isGreaterThan(compressedByteArray.size)
        val decompressedByteArray = Zlib.decompress(compressedByteArray)
        assertThat(decompressedByteArray).hasSize(input.size)
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
