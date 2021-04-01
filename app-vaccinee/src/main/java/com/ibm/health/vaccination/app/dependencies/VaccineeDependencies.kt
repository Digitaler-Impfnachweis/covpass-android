package com.ibm.health.vaccination.app.dependencies

import com.ibm.health.vaccination.app.storage.Storage

/**
 * Global var for making the [VaccineeDependencies] accessible.
 */
lateinit var vaccineeDeps: VaccineeDependencies

/**
 * Access to various dependencies for app-vaccinee module.
 */
abstract class VaccineeDependencies {

    /**
     * The [Storage].
     */
    open val storage: Storage = Storage
}
