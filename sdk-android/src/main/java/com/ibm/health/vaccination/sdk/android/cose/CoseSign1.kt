package com.ibm.health.vaccination.sdk.android.cose

import com.google.iot.cbor.CborArray
import com.google.iot.cbor.CborObject

/**
 * Data class to keep COSE object of type cose-sign1 (CBOR tag 18).
 */
public data class CoseSign1(
    val protected: ByteArray,
    val unprotected: Any?,
    val payload: ByteArray,
    val signature: ByteArray,
) {

    public companion object {
        /**
         * Creates CoseSign1 instance from given [byteArray].
         */
        public fun fromByteArray(byteArray: ByteArray): CoseSign1 {
            val cborArray = CborArray.createFromCborByteArray(byteArray)
            if (cborArray.majorType != MAJOR_TYPE_ARRAY_OF_DATA_ITEMS)
                throw CoseSign1Exception(
                    "Wrong major type. " +
                        "Expected type is ($MAJOR_TYPE_ARRAY_OF_DATA_ITEMS) - an array of data items. " +
                        "Actual type is (${cborArray.majorType})"
                )
            val list: List<CborObject> = (cborArray as Iterable<CborObject>).toList()
            if (list.size != EXPECTED_CBOR_ARRAY_SIZE)
                throw CoseSign1Exception(
                    "Wrong size of the object array. Expected size is ($EXPECTED_CBOR_ARRAY_SIZE). " +
                        "Actual size is (${list.size})"
                )
            val (protected, unprotected, payload, signature) = list
            return CoseSign1(
                protected.toJavaObject() as ByteArray,
                unprotected.toJavaObject(),
                payload.toJavaObject() as ByteArray,
                signature.toJavaObject() as ByteArray
            )
        }

        private const val EXPECTED_CBOR_ARRAY_SIZE: Int = 4

        /**
         * According to the specification.
         * https://tools.ietf.org/html/rfc7049#section-2.1
         */
        private const val MAJOR_TYPE_ARRAY_OF_DATA_ITEMS: Int = 4
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CoseSign1

        if (!protected.contentEquals(other.protected)) return false
        if (unprotected != other.unprotected) return false
        if (!payload.contentEquals(other.payload)) return false
        if (!signature.contentEquals(other.signature)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = protected.contentHashCode()
        result = 31 * result + unprotected.hashCode()
        result = 31 * result + payload.contentHashCode()
        result = 31 * result + signature.contentHashCode()
        return result
    }
}

/** Thrown when [CoseSign1.fromByteArray] can't create an instance of CoseSign1. */
public class CoseSign1Exception(message: String) : IllegalArgumentException(message)
