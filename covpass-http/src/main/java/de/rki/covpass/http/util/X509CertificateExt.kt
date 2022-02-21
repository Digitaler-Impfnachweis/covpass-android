/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.http.util

import java.security.cert.X509Certificate

/**
 * Extract the SAN from [X509Certificate]
 * @return [Set] which contains SubjectAlternativeNames as Strings
 */
public fun X509Certificate.getDnsSubjectAlternativeNames(): Set<String> {
    return subjectAlternativeNames.filter { san ->
        san.size >= 2 && san[0] == 2 && san[1] is String
    }.mapNotNull { it[1] as String }.toSet()
}
