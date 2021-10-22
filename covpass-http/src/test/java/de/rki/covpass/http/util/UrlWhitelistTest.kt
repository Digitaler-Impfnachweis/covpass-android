/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.http.util

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class UrlWhitelistTest {
    @Test
    fun `empty host whitelist`() {
        val whitelist = HostBasedUrlWhitelist(setOf())
        assertFalse(whitelist.isWhitelisted("https://www.ibm.com/some/path"))
    }

    @Test
    fun `path based host whitelist`() {
        val whitelist = HostBasedUrlWhitelist(setOf("ibm.com/some", "www.foo.com"))
        assertTrue(whitelist.isWhitelisted("https://www.ibm.com/some"))
        assertTrue(whitelist.isWhitelisted("https://www.ibm.com/some/path"))
        assertFalse(whitelist.isWhitelisted("https://www.ibm.com/someother/path"))
        assertFalse(whitelist.isWhitelisted("https://www.ibm.com/other/path"))

        assertTrue(whitelist.isWhitelisted("https://www.foo.com/any/path"))
        assertTrue(whitelist.isWhitelisted("https://www.foo.com"))
        assertFalse(whitelist.isWhitelisted("https://bar.foo.com/any/path"))
    }
}
