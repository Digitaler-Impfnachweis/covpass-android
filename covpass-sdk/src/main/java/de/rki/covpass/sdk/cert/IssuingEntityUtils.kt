/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert

import de.rki.covpass.sdk.storage.IssuingEntityRepository
import java.math.BigInteger
import java.security.MessageDigest

public fun validateEntity(uvci: String) {
    IssuingEntityRepository.entityBlacklist.forEach { blacklistedEntitySHA512 ->
        extractEntity(uvci)?.let { entity ->
            val entitySHA512 = getSHA512(entity)
            if (blacklistedEntitySHA512 == entitySHA512) {
                throw BlacklistedEntityException()
            }
        }
    }
}

private fun getSHA512(input: String): String {
    val md = MessageDigest.getInstance("SHA-512")
    val messageDigest = md.digest(input.toByteArray())
    val no = BigInteger(1, messageDigest)
    var hashText = no.toString(16)

    while (hashText.length < 32) {
        hashText = "0$hashText"
    }

    return hashText
}

private fun extractEntity(uvci: String): String? {
    val regex = "[a-zA-Z]{2}/.+?(?=/)".toRegex()
    return regex.find(uvci)?.value
}

/**
 * This exception is thrown when a entity is blacklisted.
 */
public class BlacklistedEntityException : RuntimeException("Blacklisted Issuing Entity")
