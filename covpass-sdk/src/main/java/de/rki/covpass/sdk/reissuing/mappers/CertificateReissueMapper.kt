/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.reissuing.mappers

import de.rki.covpass.sdk.reissuing.local.CertificateReissue
import de.rki.covpass.sdk.reissuing.local.CertificateReissueRelationType
import de.rki.covpass.sdk.reissuing.local.Relation
import de.rki.covpass.sdk.reissuing.remote.CertificateReissueRelation
import de.rki.covpass.sdk.reissuing.remote.CertificateReissueResponse

public fun CertificateReissueResponse.toCertificateReissue(): CertificateReissue =
    CertificateReissue(
        certificate = certificate,
        relations = relations.map { it.toRelation() }
    )

public fun CertificateReissueRelation.toRelation(): Relation =
    Relation(CertificateReissueRelationType.valueOf(action.uppercase()))
