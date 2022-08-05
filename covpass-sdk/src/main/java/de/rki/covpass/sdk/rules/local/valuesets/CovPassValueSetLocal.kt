/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.local.valuesets

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rki.covpass.sdk.cert.models.EUValueSet
import de.rki.covpass.sdk.dependencies.defaultJson
import de.rki.covpass.sdk.utils.formatDateInternational
import kotlinx.serialization.decodeFromString
import java.time.LocalDate

@Entity(tableName = "covpass_valuesets")
public data class CovPassValueSetLocal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val valueSetId: String,
    val valueSetDate: LocalDate,
    val valueSetValues: String,
    val hash: String,
)

public fun CovPassValueSetLocal.toEuValueSet(): EUValueSet =
    EUValueSet(
        valueSetId = valueSetId,
        valueSetDate = valueSetDate.formatDateInternational(),
        valueSetValues = defaultJson.decodeFromString(valueSetValues),
    )
