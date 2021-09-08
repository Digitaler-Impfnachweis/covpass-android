/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.booster.local

import androidx.room.Embedded
import androidx.room.Relation

public data class BoosterRuleWithDescriptionsLocal(
    @Embedded val rule: BoosterRuleLocal,
    @Relation(
        parentColumn = "ruleId",
        entityColumn = "ruleContainerId"
    )
    val descriptions: List<BoosterDescriptionLocal>
)
