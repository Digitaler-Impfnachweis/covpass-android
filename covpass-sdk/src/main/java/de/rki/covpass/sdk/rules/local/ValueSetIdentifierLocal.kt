/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "value_set_identifier")
public data class ValueSetIdentifierLocal(
    @PrimaryKey(autoGenerate = true)
    val identifier: Int = 0,
    val id: String,
    val hash: String
)
