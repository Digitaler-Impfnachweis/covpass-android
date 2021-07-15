/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.validitycheck.countries

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize

@Parcelize
public data class Country(
    @StringRes val nameRes: Int,
    val countryCode: String,
    @DrawableRes val flag: Int
) : Parcelable
