/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rule_identifier")
public data class RuleIdentifierLocal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val identifier: String,
    val version: String,
    val country: String,
    val hash: String
)
