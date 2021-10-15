/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.local.countries

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "countries")
public data class CountryLocal(
    @PrimaryKey(autoGenerate = true)
    val countryId: Long = 0,
    val countryCode: String,
)
