/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.http.util

/** Provides functionality to check if a url is whitelisted. */
public interface UrlWhitelist {

    /** Return true if the given [url] is whitelisted, else false. */
    public fun isWhitelisted(url: String): Boolean
}

/** [UrlWhitelist] which whitelists every url. */
public object AllowAllUrlWhitelist : UrlWhitelist {
    override fun isWhitelisted(url: String): Boolean =
        true
}

/** [UrlWhitelist] which whitelists every url that is a subdomain of a host contained in the given [whitelist]. */
public class HostBasedUrlWhitelist(whitelist: Collection<String>) : UrlWhitelist {
    private val whitelist: Set<String> = whitelist.toSet()

    override fun isWhitelisted(url: String): Boolean =
        whitelist.any { isSubUrlOf(url = url, parent = it) }
}
