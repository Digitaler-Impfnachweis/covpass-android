package com.ibm.health.common.http.utils

import android.net.Uri
import io.ktor.http.*
import java.net.MalformedURLException
import java.net.URI
import java.net.URL
import java.util.*

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

public fun parseUrl(url: String): Url? =
    runCatching {
        Url(url)
    }.getOrNull()

public fun parseURI(uri: String): URI? =
    runCatching {
        Url(uri).toURI()
    }.getOrNull()

public fun parseUri(uri: String): Uri? =
    runCatching {
        Uri.parse(uri)
    }.getOrNull()
