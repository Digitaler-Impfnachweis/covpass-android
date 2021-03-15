package com.ibm.health.common.securityprovider

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
    Security.insertProviderAt(Conscrypt.newProvider(), 1)
}
