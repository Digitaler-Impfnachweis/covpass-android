/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import de.rki.covpass.sdk.cert.CertValidator
import de.rki.covpass.sdk.cert.DscListService
import de.rki.covpass.sdk.cert.toTrustedCerts
import de.rki.covpass.sdk.storage.DscRepository

public class DscListUpdater(
    public val dscListService: DscListService,
    public val dscRepository: DscRepository,
    public val certValidator: CertValidator
) {

    public suspend fun update() {
        val dscList = dscListService.getTrustedList(dscRepository.dscList.value)
        certValidator.updateTrustedCerts(dscList.toTrustedCerts())
        dscRepository.updateDscList(dscList)
    }
}
