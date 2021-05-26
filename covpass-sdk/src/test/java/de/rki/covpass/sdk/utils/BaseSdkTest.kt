/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

internal abstract class BaseSdkTest {
    init {
        Security.addProvider(BouncyCastleProvider())
    }
}
