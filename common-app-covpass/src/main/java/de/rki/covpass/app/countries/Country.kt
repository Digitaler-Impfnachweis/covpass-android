/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.countries

import androidx.annotation.StringRes

public data class Country(
    @StringRes val nameRes: Int,
    val countryCode: String,
)
