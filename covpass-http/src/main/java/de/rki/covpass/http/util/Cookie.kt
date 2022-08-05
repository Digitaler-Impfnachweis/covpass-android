/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.http.util

import io.ktor.client.features.cookies.AcceptAllCookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.CookieEncoding
import io.ktor.http.Url

public data class CookieEntry(val url: Url, val cookie: Cookie)

public fun String.toKtorCookie(): Cookie {
    val parts = split("; ").map { it.split("=") }
    val name = parts[0][0]
    val value = parts[0][1]
    val path = parts.findLast { it[0] == "Path" }?.get(1)
    return Cookie(name = name, value = value, path = path, httpOnly = true, encoding = CookieEncoding.RAW)
}

public suspend fun AcceptAllCookiesStorage.addCookies(cookies: List<CookieEntry>) {
    for (entry in cookies) {
        addCookie(entry.url, entry.cookie)
    }
}

public suspend fun AcceptAllCookiesStorage.addCookies(url: Url, cookies: List<String>) {
    addCookies(cookies.map { CookieEntry(url, it.toKtorCookie()) })
}
