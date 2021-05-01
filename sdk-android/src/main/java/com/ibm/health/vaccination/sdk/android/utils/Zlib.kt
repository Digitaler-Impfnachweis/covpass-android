package com.ibm.health.vaccination.sdk.android.utils

import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.Inflater

/**
 * Used to compress or decompress a byte array with zlib.
 */
public object Zlib {
    private const val BUFFER_SIZE: Int = 512

    /**
     * Decompresses a [value]
     * @return decompressed byte array
     */
    public fun decompress(value: ByteArray): ByteArray {
        // Create an expandable byte array to hold the decompressed data
        val bos = ByteArrayOutputStream(value.size)
        Inflater().run {
            try {
                setInput(value)
                val buf = ByteArray(BUFFER_SIZE)
                while (!finished()) {
                    val count = inflate(buf)
                    bos.write(buf, 0, count)
                }
            } finally {
                end()
            }
        }
        return bos.toByteArray()
    }

    /**
     * Compresses an [input]
     * @return compressed byte array
     */
    public fun compress(input: ByteArray): ByteArray {
        Deflater().run {
            return try {
                setInput(input)
                finish()
                val buffer = ByteArray(Short.MAX_VALUE.toInt())
                val sizeAfterCompression = deflate(buffer)
                val output = ByteArray(sizeAfterCompression)
                System.arraycopy(buffer, 0, output, 0, sizeAfterCompression)
                output
            } finally {
                end()
            }
        }
    }
}
