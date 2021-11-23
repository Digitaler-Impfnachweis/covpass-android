/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.ticketing

import de.rki.covpass.sdk.cert.models.*

public fun GroupedCertificatesList.filterCertificates(
    types: Collection<TicketingType>,
    firstName: String?,
    lastName: String,
    birthDate: String,
): List<CombinedCovCertificate> =
    certificates
        .filter { groupedCovCertificate ->
            groupedCovCertificate.getMainCertificate().covCertificate.name.familyNameTransliterated == lastName &&
                groupedCovCertificate.getMainCertificate().covCertificate.name.givenNameTransliterated == firstName &&
                birthDate == groupedCovCertificate.getMainCertificate().covCertificate.birthDate
        }
        .flatMap { it.certificates }
        .filter {
            it.covCertificate.dgcEntry.certificateType.matches(types)
        }

public val DGCEntry.certificateType: TicketingType
    get() = when (this) {
        is Recovery -> TicketingType.Recovery
        is TestCert -> when (type) {
            TestCertType.POSITIVE_PCR_TEST,
            TestCertType.NEGATIVE_PCR_TEST,
            -> TicketingType.Test.Pcr
            TestCertType.POSITIVE_ANTIGEN_TEST,
            TestCertType.NEGATIVE_ANTIGEN_TEST,
            -> TicketingType.Test.Antigen
        }
        is Vaccination -> TicketingType.Vaccination
    }

public sealed class TicketingType(vararg codes: String) {
    public val codes: Set<String> = codes.toSet()

    public object Recovery : TicketingType("r")
    public object Vaccination : TicketingType("v")
    public sealed interface Test {
        public object Generic : TicketingType("t", "tp", "tr"), Test
        public object Pcr : TicketingType("tp"), Test
        public object Antigen : TicketingType("tr"), Test
    }

    public fun matches(type: TicketingType): Boolean =
        (codes intersect type.codes).isNotEmpty()

    public fun matches(types: Collection<TicketingType>): Boolean =
        types.any { matches(it) }

    public companion object {
        public val values: List<TicketingType> by lazy {
            TicketingType::class.sealedSubclasses.map { checkNotNull(it.objectInstance) }
        }

        public fun valueOfOrNull(code: String): TicketingType? = values.find { code in it.codes }
    }
}
