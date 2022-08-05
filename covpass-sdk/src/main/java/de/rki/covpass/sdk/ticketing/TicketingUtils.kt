/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.ticketing

import android.util.Base64
import de.rki.covpass.sdk.cert.models.CombinedCovCertificate
import de.rki.covpass.sdk.cert.models.DGCEntry
import de.rki.covpass.sdk.cert.models.GroupedCertificatesList
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.TestCert
import de.rki.covpass.sdk.cert.models.TestCertType
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.ticketing.encoding.JwtObject
import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

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

public fun String.parseJwtToken(): JwtObject {
    val tokens = split('.')
    val header = String(Base64.decode(tokens[0], Base64.NO_WRAP))
    val body = String(Base64.decode(tokens[1], Base64.NO_WRAP))
    return JwtObject(header, body)
}

public fun String.base64ToX509Certificate(): X509Certificate {
    val decoded = Base64.decode(this, Base64.NO_WRAP)
    val inputStream = ByteArrayInputStream(decoded)
    val x509Certificate = CertificateFactory.getInstance("X.509").generateCertificate(inputStream) as? X509Certificate
    return x509Certificate ?: throw IllegalStateException()
}

public fun String.createAuthHeader(): String = "Bearer $this"
