package com.ibm.health.vaccination.app.vaccinee.dependencies

import com.ibm.health.vaccination.app.vaccinee.common.ToggleFavoriteUseCase
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
    val storage: Storage = Storage()

    val toggleFavoriteUseCase by lazy { ToggleFavoriteUseCase(storage) }
}
