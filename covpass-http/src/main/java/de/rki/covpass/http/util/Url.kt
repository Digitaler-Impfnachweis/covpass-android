/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.http.util

import android.net.Uri
import io.ktor.http.*
import java.net.MalformedURLException
import java.net.URI
import java.net.URL
import java.util.*

/** Returns true, if the given [url] is a subdomain of [parent], else false. */
public fun isSubdomainOf(url: String, parent: String): Boolean {
    val host = try {
        URL(url).host
    } catch (e: MalformedURLException) {
        url
    }.toLowerCase(Locale.ROOT)
    return isHostSubdomainOf(host, parent.toLowerCase(Locale.ROOT))
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
