package de.rki.covpass.sdk.rules.local.rules

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "rules_descriptions",
    foreignKeys = [
        ForeignKey(
            entity = CovPassRuleLocal::class,
            parentColumns = arrayOf("ruleId"),
            childColumns = arrayOf("ruleContainerId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
public data class CovPassRuleDescriptionLocal(
    @PrimaryKey(autoGenerate = true)
    val descriptionId: Long = 0,
    val ruleContainerId: Long = 0,
    val lang: String,
    val desc: String
)
