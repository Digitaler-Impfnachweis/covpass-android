package com.ibm.health.vaccination.app.vaccinee.dependencies

import com.ibm.health.vaccination.app.vaccinee.storage.Storage

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
