/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import java.util.zip.Deflater
import java.util.zip.DeflaterInputStream
import java.util.zip.InflaterInputStream

/**
 * Used to compress or decompress a byte array with zlib.
 */
public object Zlib {
    /**
     * Decompresses the given [input].
     *
     * @return decompressed byte array
     */
    public fun decompress(input: ByteArray): ByteArray =
        InflaterInputStream(input.inputStream()).readBytes()

    /**
     * Compresses the given [input] at compression [level].
     *
     * @return compressed byte array
     */
    public fun compress(input: ByteArray, level: Int = Deflater.DEFAULT_COMPRESSION): ByteArray =
        DeflaterInputStream(input.inputStream(), Deflater(level)).readBytes()
}
