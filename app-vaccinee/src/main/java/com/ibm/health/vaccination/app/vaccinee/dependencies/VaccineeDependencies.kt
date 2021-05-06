package com.ibm.health.vaccination.app.vaccinee.dependencies

import com.ibm.health.common.vaccination.app.utils.CborSharedPrefsStore
import com.ibm.health.vaccination.app.vaccinee.common.ToggleFavoriteUseCase
import com.ibm.health.vaccination.app.vaccinee.main.CertRefreshService
import com.ibm.health.vaccination.app.vaccinee.storage.CertRepository
import com.ibm.health.vaccination.sdk.android.dependencies.sdkDeps

/**
 * Global var for making the [VaccineeDependencies] accessible.
 */
lateinit var vaccineeDeps: VaccineeDependencies

/**
 * Access to various dependencies for app-vaccinee module.
 */
abstract class VaccineeDependencies {

    val certRepository: CertRepository = CertRepository(CborSharedPrefsStore("vaccinee_prefs"))

    val toggleFavoriteUseCase by lazy { ToggleFavoriteUseCase(certRepository) }

    val certRefreshService by lazy { CertRefreshService(sdkDeps.mainScope, sdkDeps.certService, certRepository.certs) }
}
