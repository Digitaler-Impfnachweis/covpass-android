package com.ibm.health.common.http.util

public interface UrlWhitelist {
    public fun isWhitelisted(url: String): Boolean
}

public object AllowAllUrlWhitelist : UrlWhitelist {
    override fun isWhitelisted(url: String): Boolean =
        true
}

public class HostBasedUrlWhitelist(whitelist: Collection<String>) : UrlWhitelist {
    private val whitelist: Set<String> = whitelist.toSet()

    override fun isWhitelisted(url: String): Boolean =
        whitelist.any { isSubdomainOf(url = url, parent = it) }
}
