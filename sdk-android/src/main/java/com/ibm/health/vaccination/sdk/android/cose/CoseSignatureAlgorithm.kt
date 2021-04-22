package com.ibm.health.vaccination.sdk.android.cose

import org.apache.kerby.asn1.type.Asn1Integer
import org.apache.kerby.asn1.type.Asn1Sequence
import java.math.BigInteger
import java.security.GeneralSecurityException
import java.security.PublicKey
import java.security.Signature

internal enum class CoseSignatureAlgorithm(
    val validator: CoseSignatureValidator,
) {
    ECDSA_256(CoseEcdsaSignatureValidator(-7, "SHA256withECDSA")),
    ECDSA_384(CoseEcdsaSignatureValidator(-35, "SHA384withECDSA")),
    ECDSA_512(CoseEcdsaSignatureValidator(-36, "SHA512withECDSA")),
    // TODO: Add support for EdDSA (not supported by Conscrypt and COSE-JAVA)
    //  https://cose-wg.github.io/cose-spec/#rfc.section.8.2
    ;

    companion object {
        fun fromId(id: Int?): CoseSignatureAlgorithm? =
            values().find { it.validator.id == id }
    }
}

internal interface CoseSignatureValidator {
    val id: Int

    fun validate(data: ByteArray, publicKey: PublicKey, signature: ByteArray)
}

internal abstract class BaseCoseSignatureValidator : CoseSignatureValidator {
    override fun validate(data: ByteArray, publicKey: PublicKey, signature: ByteArray) {
        val signatureValidator = toSignature().apply {
            initVerify(publicKey)
            update(data)
        }
        if (!signatureValidator.verify(signature)) {
            throw CoseValidationException()
        }
    }

    abstract fun toSignature(): Signature
}

internal class CoseEcdsaSignatureValidator(
    override val id: Int,
    val algorithmName: String,
) : BaseCoseSignatureValidator() {

    override fun validate(data: ByteArray, publicKey: PublicKey, signature: ByteArray) {
        val splitAt = signature.size / 2
        val encodedSignature = Asn1Sequence().apply {
            value.add(Asn1Integer(BigInteger(1, signature.sliceArray(0 until splitAt))))
            value.add(Asn1Integer(BigInteger(1, signature.sliceArray(splitAt until signature.size))))
        }.encode()
        super.validate(data, publicKey, encodedSignature)
    }

    override fun toSignature(): Signature = Signature.getInstance(algorithmName)
}

/** Thrown when the COSE signature validation failed. */
public open class CoseValidationException : GeneralSecurityException()
