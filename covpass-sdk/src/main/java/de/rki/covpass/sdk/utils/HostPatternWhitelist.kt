package de.rki.covpass.sdk.utils

import io.ktor.http.*

public class HostPatternWhitelist(public val whitelist: Set<String>) {

    public fun isWhitelisted(url: String): Boolean {
        val hostname = Url(url).host
        return whitelist.any { matchesHostname(it, hostname) }
    }
}

private fun matchesHostname(pattern: String, hostname: String): Boolean {
    return when {
        pattern.startsWith("**.") -> {
            // With ** empty prefixes match so exclude the dot from regionMatches().
            val suffixLength = pattern.length - 3
            val prefixLength = hostname.length - suffixLength
            hostname.regionMatches(hostname.length - suffixLength, pattern, 3, suffixLength) &&
                (prefixLength == 0 || hostname[prefixLength - 1] == '.')
        }
        pattern.startsWith("*.") -> {
            // With * there must be a prefix so include the dot in regionMatches().
            val suffixLength = pattern.length - 1
            val prefixLength = hostname.length - suffixLength
            hostname.regionMatches(hostname.length - suffixLength, pattern, 1, suffixLength) &&
                hostname.lastIndexOf('.', prefixLength - 1) == -1
        }
        else -> hostname == pattern
    }
}
