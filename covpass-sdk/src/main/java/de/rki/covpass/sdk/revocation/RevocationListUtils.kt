/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.revocation

public fun validateKid(kid: ByteArray, kidList: List<RevocationKidEntry>): Boolean {
    return kidList.any { kid.contentEquals(it.kid) }
}
