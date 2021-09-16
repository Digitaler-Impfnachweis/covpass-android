/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LanguageUtilsTest {

    @Test
    fun `test en locale`() {
        Locale.setDefault(Locale.ENGLISH)
        assertEquals(getDescriptionLanguage(), "en")
    }

    @Test
    fun `test de locale`() {
        Locale.setDefault(Locale.GERMAN)
        assertEquals(getDescriptionLanguage(), "de")
    }

    @Test
    fun `test fr locale`() {
        Locale.setDefault(Locale.FRANCE)
        assertEquals(getDescriptionLanguage(), "en")
    }
}
