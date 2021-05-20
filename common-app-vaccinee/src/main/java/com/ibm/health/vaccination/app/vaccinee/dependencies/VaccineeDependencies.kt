/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.vaccination.app.vaccinee.dependencies

import com.ibm.health.common.vaccination.app.utils.CborSharedPrefsStore
import com.ibm.health.vaccination.app.vaccinee.common.ToggleFavoriteUseCase
import com.ibm.health.vaccination.app.vaccinee.storage.CertRepository

/**
 * Global var for making the [VaccineeDependencies] accessible.
 */
internal lateinit var vaccineeDeps: VaccineeDependencies

/**
 * Access to various dependencies for app-vaccinee module.
 */
internal abstract class VaccineeDependencies {

    val certRepository: CertRepository = CertRepository(CborSharedPrefsStore("vaccinee_prefs"))

    val toggleFavoriteUseCase by lazy { ToggleFavoriteUseCase(certRepository) }
}
