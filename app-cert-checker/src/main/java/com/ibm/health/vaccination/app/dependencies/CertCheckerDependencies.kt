package com.ibm.health.vaccination.app.dependencies

import com.ibm.health.vaccination.app.storage.Storage

/**
 * Global var for making the [CertCheckerDependencies] accessible.
 */
lateinit var certCheckerDeps: CertCheckerDependencies

/**
 * Access to various dependencies for app-cert-checker module.
 */
abstract class CertCheckerDependencies {

    /**
     * The [Storage].
     */
    open val storage: Storage = Storage
}
