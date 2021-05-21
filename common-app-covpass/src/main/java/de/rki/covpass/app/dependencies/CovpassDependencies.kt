/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.dependencies

import de.rki.covpass.commonapp.utils.CborSharedPrefsStore
import de.rki.covpass.app.common.ToggleFavoriteUseCase
import de.rki.covpass.app.storage.CertRepository

/**
 * Global var for making the [CovpassDependencies] accessible.
 */
internal lateinit var covpassDeps: CovpassDependencies

/**
 * Access to various dependencies for common-app-covpass module.
 */
internal abstract class CovpassDependencies {

    val certRepository: CertRepository = CertRepository(CborSharedPrefsStore("covpass_prefs"))

    val toggleFavoriteUseCase by lazy { ToggleFavoriteUseCase(certRepository) }
}
