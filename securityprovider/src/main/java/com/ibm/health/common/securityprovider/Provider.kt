/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.securityprovider

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
    // Just in case we also integrate Bouncy Castle, disable patented algorithms
    System.setProperty("org.bouncycastle.ec.disable_mqv", "true")
    Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
    Security.addProvider(BouncyCastleProvider())
    Security.insertProviderAt(Conscrypt.newProvider(), 1)
}
