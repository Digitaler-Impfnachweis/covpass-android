/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.revocation

import android.util.Base64
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.utils.sha256

public suspend fun validateRevocation(
    covCertificate: CovCertificate,
    revocationListRepository: RevocationListRepository,
): Boolean {
    val kid = Base64.decode(covCertificate.kid, Base64.DEFAULT)
    val kidList: List<RevocationKidEntry> = revocationListRepository.getKidList()
    if (kidList.isEmpty() || !validateKid(kid, kidList)) {
        return false
    }

    val hashVariants = kidList.first { it.kid.contentEquals(kid) }.hashVariants
    for (hashVariant in hashVariants) {
        val hash = when (hashVariant.key) {
            HashVariant.SIGNATURE.byte -> {
                covCertificate.getRValueByteArray.sha256()
            }
            HashVariant.UCI.byte -> {
                covCertificate.dgcEntry.id.sha256()
            }
            HashVariant.COUNTRY_UCI.byte -> {
                (covCertificate.dgcEntry.id + covCertificate.issuer).sha256()
            }
            else -> {
                continue
            }
        }.sliceArray(0..15)

        val byte1 = hash[0]
        val byte2 = hash[1]
        val indexRequest = revocationListRepository.getIndex(kid, hashVariant.key)
        if (indexRequest.isEmpty() || !indexRequest.containsKey(byte1) ||
            indexRequest[byte1]?.byte2?.containsKey(byte2) == false
        ) {
            continue
        }

        val chunkListByte1: List<ByteArray> =
            revocationListRepository.getByteOneChunk(kid, hashVariant.key, byte1)
        val chunkListByte2: List<ByteArray> =
            revocationListRepository.getByteTwoChunk(kid, hashVariant.key, byte1, byte2)
        chunkListByte2.forEach { chunk ->
            if (hash.contentEquals(chunk)) {
                return true
            }
        }
        chunkListByte1.forEach { chunk ->
            if (hash.contentEquals(chunk)) {
                return true
            }
        }
    }
    return false
}

public enum class HashVariant(public val byte: Byte) {
    SIGNATURE(0x0a),
    UCI(0x0b),
    COUNTRY_UCI(0x0c),
}
