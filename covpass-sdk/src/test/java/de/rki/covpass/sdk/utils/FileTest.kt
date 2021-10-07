/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import kotlin.test.Test
import kotlin.test.assertEquals

internal class FileTest {

    @Test
    fun `test sanitization`() {
        assertEquals("a-b.pdf", "a//\\.b.pdf".sanitizeFileName())
    }
}
