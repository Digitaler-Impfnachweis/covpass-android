/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.local.rules.eu

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "rules_descriptions",
    foreignKeys = [
        ForeignKey(
            entity = CovPassEuRuleLocal::class,
            parentColumns = arrayOf("ruleId"),
            childColumns = arrayOf("ruleContainerId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
public data class CovPassEuRuleDescriptionLocal(
    @PrimaryKey(autoGenerate = true)
    val descriptionId: Long = 0,
    val ruleContainerId: Long = 0,
    val lang: String,
    val desc: String
)
