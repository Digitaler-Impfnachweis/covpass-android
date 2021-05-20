/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.base45

// Lookup tables for faster processing
internal val ENCODING_CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:".encodeToByteArray()
private val DECODING_CHARSET = ByteArray(256) { -1 }.also { charset ->
    ENCODING_CHARSET.forEachIndexed { index, byte ->
        charset[byte.toInt()] = index.toByte()
    }
}

/** A base45 [encoder][encode] and [decoder][decode]. */
public object Base45 {
    /** Encodes the given [input] [ByteArray] with base45 and returns the encoded [ByteArray]. */
    public fun encode(input: ByteArray): ByteArray =
        input.asSequence().chunked(2) { chunk ->
            // Encoding turns 2 into 3 chars and 1 into 2 chars, so size increases by 1.
            chunk.toInt(256).toBase(base = 45, count = chunk.size + 1)
        }.flatten().map { ENCODING_CHARSET[it.toUByte().toInt()] }.toList().toByteArray()

    /**
     * Decodes the given base45 encoded [input] [ByteArray] and returns the decoded [ByteArray].
     *
     * @throws Base45DecodeException if the input is not correctly base45 encoded.
     */
    public fun decode(input: ByteArray): ByteArray =
        input.asSequence().map {
            DECODING_CHARSET[it.toInt()].also { index ->
                if (index < 0) throw Base45DecodeException("Invalid characters in input.")
            }
        }.chunked(3) { chunk ->
            if (chunk.size < 2) throw Base45DecodeException("Invalid input length.")
            chunk.reversed().toInt(45).toBase(base = 256, count = chunk.size - 1).reversed()
        }.flatten().toList().toByteArray()

    /** Converts integer to a list of [count] integers in the given [base]. */
    private fun Int.toBase(base: Int, count: Int): List<Byte> =
        mutableListOf<Byte>().apply {
            var tmp = this@toBase
            repeat(count) {
                add((tmp % base).toByte())
                tmp /= base
            }
            if (tmp != 0) throw Base45DecodeException("Invalid character sequence.")
        }

    /** Converts list of bytes in given [base] to an integer. */
    private fun List<Byte>.toInt(base: Int): Int =
        fold(0) { acc, i -> acc * base + i.toUByte().toInt() }
}

/** Thrown when [Base45.decode] can't decode the input data. */
public class Base45DecodeException(message: String) : IllegalArgumentException(message)
