/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert

import de.rki.covpass.sdk.storage.IssuingEntityRepository
import de.rki.covpass.sdk.utils.sha512
import de.rki.covpass.sdk.utils.toHex

public fun validateEntity(uvci: String) {
    IssuingEntityRepository.entityBlacklist.forEach { blacklistedEntitySHA512 ->
        extractEntity(uvci)?.let { entity ->
            val entitySHA512 = entity.sha512().toHex()
            if (blacklistedEntitySHA512 == entitySHA512) {
                throw BlacklistedEntityException()
            }
        }
    }
}

private fun extractEntity(uvci: String): String? {
    val regex = "[a-zA-Z]{2}/.+?(?=/)".toRegex()
    return regex.find(uvci)?.value
}

/**
 * This exception is thrown when a entity is blacklisted.
 */
public class BlacklistedEntityException : RuntimeException("Blacklisted Issuing Entity")
