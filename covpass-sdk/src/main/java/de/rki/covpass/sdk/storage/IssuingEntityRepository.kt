/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.storage

import java.time.LocalDate

internal object IssuingEntityRepository {

    /* ktlint-disable max-line-length */
    @Suppress("MaxLineLength")
    val entityBlacklist: Map<String, LocalDate?> = mapOf(
        Pair(
            "75f6df21f51b4998740bf3e1cdaff1c76230360e1baf5ac0a2b9a383a1f9fa34dd77b6aa55a28cc5843d75b7c4a89bdbfc9a9177da244861c4068e76847dd150",
            null
        ),
        Pair(
            "81d51278c45f29dbba6b243c9c25cb0266b3d32e425b7a9db1fa6fcd58ad308c5b3857be6470a84403680d833a3f28fb02fb8c809324811b573c131d1ae52599",
            null
        ),
        Pair(
            "a398d7d9c57900ab502bd011ad9107f2aefb4e6d58dd4322ecc8a656c10028ce5391353854f81fb0a8a44d0715628aba29bdc4556caa0e6f763292e462906ad4",
            LocalDate.parse("2021-11-11")
        )
    )
    /* ktlint-enable max-line-length */
}
