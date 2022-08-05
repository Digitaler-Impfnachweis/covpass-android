/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.reissuing.local

public data class CertificateReissue(
    val certificate: String,
    val relations: List<Relation>,
)

public data class Relation(
    val action: CertificateReissueRelationType,
)
