/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.securityprovider

import de.rki.covpass.logging.Lumber
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.conscrypt.Conscrypt
import java.security.Security

/**
 * Initializes and installs our required security provider(s).
 *
 * This function should be called as early as possible, so all TLS connections, crypto routines, SecureRandom
 * and other features can automatically use the correct provider(s).
 */
public fun initSecurityProvider() {
    lazySecurityProviderInstaller
}

private val lazySecurityProviderInstaller by lazy {
    // Disable patented algorithms in Bouncy Castle
    System.setProperty("org.bouncycastle.ec.disable_mqv", "true")
    try {
        Security.removeProvider("BC")
    } catch (e: Throwable) {
        Lumber.w { "Provider BC not found. Removal failed." }
    }
    Security.addProvider(BouncyCastleProvider())
    Security.insertProviderAt(Conscrypt.newProvider(), 1)
}
