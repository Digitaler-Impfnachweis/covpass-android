/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.misuseprevention

import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.GroupedCertificates
import de.rki.covpass.sdk.utils.DccNameMatchingUtils.isHolderSame

public object MisusePreventionHelper {

    public fun getMisusePreventionStatus(
        groupedCertificates: List<GroupedCertificates>,
        covCertificate: CovCertificate,
    ): MisusePreventionStatus = when {
        groupedCertificates.size == FIRST_LIMITATION_COUNT && !isMatching(groupedCertificates, covCertificate) -> {
            MisusePreventionStatus.FIRST_LIMITATION_WARNING
        }
        groupedCertificates.size == SECOND_LIMITATION_COUNT && !isMatching(groupedCertificates, covCertificate) -> {
            MisusePreventionStatus.SECOND_LIMITATION_WARNING
        }
        groupedCertificates.size >= SAVE_BLOCKED_COUNT && !isMatching(groupedCertificates, covCertificate) -> {
            MisusePreventionStatus.SAVING_BLOCKED
        }
        else -> MisusePreventionStatus.ALL_GOOD
    }

    private fun isMatching(
        groupedCertificates: List<GroupedCertificates>,
        covCertificate: CovCertificate,
    ): Boolean = groupedCertificates.firstOrNull {
        isHolderSame(it.certificates.first().covCertificate, covCertificate)
    } != null

    private const val FIRST_LIMITATION_COUNT = 1
    private const val SECOND_LIMITATION_COUNT = 9
    private const val SAVE_BLOCKED_COUNT = 20
}

public enum class MisusePreventionStatus {
    ALL_GOOD,
    FIRST_LIMITATION_WARNING,
    SECOND_LIMITATION_WARNING,
    SAVING_BLOCKED
}
