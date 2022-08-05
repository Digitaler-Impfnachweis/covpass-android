/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.local.rules.eu

import androidx.room.Embedded
import androidx.room.Relation

public data class CovPassEuRuleWithDescriptionsLocal(
    @Embedded val rule: CovPassEuRuleLocal,
    @Relation(
        parentColumn = "ruleId",
        entityColumn = "ruleContainerId",
    )
    val descriptions: List<CovPassEuRuleDescriptionLocal>,
)
