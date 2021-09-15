/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.local.rules

import androidx.room.Embedded
import androidx.room.Relation

public data class CovPassRuleWithDescriptionsLocal(
    @Embedded val rule: CovPassRuleLocal,
    @Relation(
        parentColumn = "ruleId",
        entityColumn = "ruleContainerId"
    )
    val descriptions: List<CovPassRuleDescriptionLocal>
)
