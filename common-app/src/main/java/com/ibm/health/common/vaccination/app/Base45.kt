package com.ibm.health.common.vaccination.app

// Note: Unbelievably, we couldn't find a base45 encoder/decoder for Kotlin or Java, so we had to implement our own.
// GitHub and Google seemed to only list implementations in Rust, Go, Python, JavaScript, Haskell.

internal val ENCODING_CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:".encodeToByteArray()
private val DECODING_CHARSET = ByteArray(256).also {
    ENCODING_CHARSET.forEachIndexed { index, byte ->
        it[byte.toInt()] = index.toByte()
    }
}

/** A base45 [encode]r and [decode]r. */
public object Base45 {
    /** Encodes the given [input] [ByteArray] with base45 and returns the encoded [ByteArray]. */
    public fun encode(input: ByteArray): ByteArray {
        var inIndex = 0
        fun read() = input[inIndex++].toUByte().toInt()

        val result = ByteArray((input.size * 3 + 1) / 2)
        var outIndex = 0
        fun write(num: Int): Int {
            result[outIndex++] = ENCODING_CHARSET[num % 45]
            return num / 45
        }

        // 2 chars -> 3 chars case
        while (inIndex + 1 < input.size) {
            write(write(write(read() * 256 + read())))
        }
        // (remaining) 1 char -> 2 chars case
        if (inIndex < input.size) {
            write(write(read()))
        }
        return result
    }

    /**
     * Decodes the given base45 encoded [input] [ByteArray] and returns the decoded [ByteArray].
     *
     * @throws Base45DecodeException if the input is not correctly base45 encoded.
     */
    public fun decode(input: ByteArray): ByteArray {
        if (!input.all { it in ENCODING_CHARSET }) throw Base45DecodeException("Invalid characters in input.")

        var inIndex = 0
        fun read() = DECODING_CHARSET[input[inIndex++].toInt()]

        val result = ByteArray((input.size * 2 + 1) / 3)
        var outIndex = 0
        fun write(num: Int) {
            if (num !in 0..255) throw Base45DecodeException("Invalid character sequence.")
            result[outIndex++] = num.toByte()
        }

        // 3 chars -> 2 chars case
        while (inIndex + 2 < input.size) {
            val x = read() + read() * 45 + read() * 45 * 45
            write(x / 256)
            write(x % 256)
        }
        // (remaining) 2 chars -> 1 char case (we check above that we never have a 1 char case)
        if (inIndex + 1 < input.size) {
            write(read() + read() * 45)
        }
        if (inIndex < input.size) throw Base45DecodeException("Invalid input length.")
        return result
    }
}

/** Thrown when [Base45.decode] can't decode the input data. */
public class Base45DecodeException(message: String) : IllegalArgumentException(message)
