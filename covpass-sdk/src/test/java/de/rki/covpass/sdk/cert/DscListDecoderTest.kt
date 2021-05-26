/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert

import assertk.assertThat
import assertk.assertions.isNotEmpty
import de.rki.covpass.sdk.crypto.readPemKeys
import de.rki.covpass.sdk.utils.BaseSdkTest
import de.rki.covpass.sdk.utils.readTextAssetFromTest
import org.junit.Test

internal class DscListDecoderTest : BaseSdkTest() {
    val key by lazy { readPemKeys(readTextAssetFromTest("covpass-sdk/dsc-list-signing-key.pem")).first() }
    val rawDscList by lazy { readTextAssetFromTest("covpass-sdk/dsc-list.json") }

    val decoder by lazy { DscListDecoder(key) }

    @Test
    fun `validate dsc list`() {
        // Decoding validates the signature, so if we don't get an exception here
        // we should be fine.
        assertThat(decoder.decodeDscList(rawDscList).certificates).isNotEmpty()
    }
}
