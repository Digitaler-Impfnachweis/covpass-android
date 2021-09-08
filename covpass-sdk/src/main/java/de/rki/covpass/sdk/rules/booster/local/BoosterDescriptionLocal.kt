package de.rki.covpass.sdk.rules.booster.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "descriptions",
    foreignKeys = [
        ForeignKey(
            entity = BoosterRuleLocal::class,
            parentColumns = arrayOf("ruleId"),
            childColumns = arrayOf("ruleContainerId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
public data class BoosterDescriptionLocal(
    @PrimaryKey(autoGenerate = true)
    val descriptionId: Long = 0,
    val ruleContainerId: Long = 0,
    val lang: String,
    val desc: String
)
