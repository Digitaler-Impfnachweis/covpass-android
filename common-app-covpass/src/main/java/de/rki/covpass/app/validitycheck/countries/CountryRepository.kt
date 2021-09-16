/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.validitycheck.countries

import com.ibm.health.common.android.utils.getString
import de.rki.covpass.app.R

public object CountryRepository {

    public val defaultCountry: Country = Country(
        R.string.DE,
        "DE",
        R.drawable.flag_de
    )

    private val countryList: List<Country> = listOf(
        Country(
            R.string.IT,
            "IT",
            R.drawable.flag_it
        ),
        Country(
            R.string.LT,
            "LT",
            R.drawable.flag_lt
        ),
        Country(
            R.string.DK,
            "DK",
            R.drawable.flag_dk
        ),
        Country(
            R.string.GR,
            "GR",
            R.drawable.flag_gr
        ),
        Country(
            R.string.CZ,
            "CZ",
            R.drawable.flag_cz
        ),
        Country(
            R.string.HR,
            "HR",
            R.drawable.flag_hr
        ),
        Country(
            R.string.IS,
            "IS",
            R.drawable.flag_is
        ),
        Country(
            R.string.PT,
            "PT",
            R.drawable.flag_pt
        ),
        Country(
            R.string.PL,
            "PL",
            R.drawable.flag_pl
        ),
        Country(
            R.string.BE,
            "BE",
            R.drawable.flag_be
        ),
        Country(
            R.string.BG,
            "BG",
            R.drawable.flag_bg
        ),
        defaultCountry,
        Country(
            R.string.LU,
            "LU",
            R.drawable.flag_lu
        ),
        Country(
            R.string.EE,
            "EE",
            R.drawable.flag_ee
        ),
        Country(
            R.string.CY,
            "CY",
            R.drawable.flag_cy
        ),
        Country(
            R.string.ES,
            "ES",
            R.drawable.flag_es
        ),
        Country(
            R.string.NL,
            "NL",
            R.drawable.flag_nl
        ),
        Country(
            R.string.AT,
            "AT",
            R.drawable.flag_at
        ),
        Country(
            R.string.LV,
            "LV",
            R.drawable.flag_lv
        ),
        Country(
            R.string.LI,
            "LI",
            R.drawable.flag_li
        ),
        Country(
            R.string.FI,
            "FI",
            R.drawable.flag_fi
        ),
        Country(
            R.string.SE,
            "SE",
            R.drawable.flag_se
        ),
        Country(
            R.string.SI,
            "SI",
            R.drawable.flag_si
        ),
        Country(
            R.string.RO,
            "RO",
            R.drawable.flag_ro
        ),
        Country(
            R.string.NO,
            "NO",
            R.drawable.flag_no
        ),
        Country(
            R.string.SK,
            "SK",
            R.drawable.flag_sk
        ),
        Country(
            R.string.FR,
            "FR",
            R.drawable.flag_fr
        ),
        Country(
            R.string.MT,
            "MT",
            R.drawable.flag_mt
        ),
        Country(
            R.string.HU,
            "HU",
            R.drawable.flag_hu
        ),
        Country(
            R.string.IE,
            "IE",
            R.drawable.flag_ie
        ),
        Country(
            R.string.CH,
            "CH",
            R.drawable.flag_ch
        ),
        Country(
            R.string.UA,
            "UA",
            R.drawable.flag_ua
        )
    )

    public fun getSortedCountryList(): List<Country> {
        return countryList.sortedBy { getString(it.nameRes) }
    }

    private fun getCountryByCode(countryCode: String): Country =
        countryList.find {
            country ->
            country.countryCode.equals(countryCode, true)
        } ?: defaultCountry

    public fun getCountryLocalized(countryCode: String): String =
        getString(getCountryByCode(countryCode).nameRes)
}
