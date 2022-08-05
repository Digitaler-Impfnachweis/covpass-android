/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.http.util

import android.net.Uri
import io.ktor.http.Url
import io.ktor.http.toURI
import java.net.MalformedURLException
import java.net.URI
import java.net.URL

/** Returns true, if the given [url] is a sub-URL of [parent], else false. */
public fun isSubUrlOf(url: String, parent: String): Boolean {
    val currentUrl = try {
        URL(url)
    } catch (e: MalformedURLException) {
        return false
    }
    val parentHost = parent.substringBefore("/")
    val parentPath = parent.substring(parentHost.length)
    return isSubdomainOf(url, parentHost) &&
        (currentUrl.path == parentPath || currentUrl.path.startsWith("$parentPath/"))
}

/** Returns true, if the given [url] is a subdomain of [parent], else false. */
public fun isSubdomainOf(url: String, parent: String): Boolean {
    val host = try {
        URL(url).host
    } catch (e: MalformedURLException) {
        url
    }.lowercase()
    return isHostSubdomainOf(host, parent.lowercase())
}

private fun isHostSubdomainOf(host: String, parent: String) =
    host == parent || host.endsWith(".$parent")

/** Returns the given [String] converted to a [Url]. If the conversion fails, null is returned. */
public fun parseUrl(url: String): Url? =
    runCatching {
        Url(url)
    }.getOrNull()

/** Returns the given [String] converted to a [URI]. If the conversion fails, null is returned. */
public fun parseURI(uri: String): URI? =
    runCatching {
        Url(uri).toURI()
    }.getOrNull()

/** Returns the given [String] converted to a [Uri]. If the conversion fails, null is returned. */
public fun parseUri(uri: String): Uri? =
    runCatching {
        Uri.parse(uri)
    }.getOrNull()

/** Returns true, if the [hostname] matches the [pattern] */
public fun matchesHostname(pattern: String, hostname: String): Boolean {
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
