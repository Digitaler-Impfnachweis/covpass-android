/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.federalstate

import android.os.Parcelable
import androidx.annotation.StringRes
import com.ibm.health.common.android.utils.getString
import de.rki.covpass.checkapp.R
import kotlinx.parcelize.Parcelize

public object FederalStateResolver {

    public val defaultFederalState: FederalState =
        FederalState(
            R.string.DE_BW,
            "BW",
        )

    private val federalStateList: Map<String, FederalState> = listOf(
        FederalState(
            R.string.DE_BY,
            "BY",
        ),
        FederalState(
            R.string.DE_BB,
            "BB",
        ),
        FederalState(
            R.string.DE_BE,
            "BE",
        ),
        defaultFederalState,
        FederalState(
            R.string.DE_HB,
            "HB",
        ),
        FederalState(
            R.string.DE_HH,
            "HH",
        ),
        FederalState(
            R.string.DE_HE,
            "HE",
        ),
        FederalState(
            R.string.DE_MV,
            "MV",
        ),
        FederalState(
            R.string.DE_NI,
            "NI",
        ),
        FederalState(
            R.string.DE_NW,
            "NW",
        ),
        FederalState(
            R.string.DE_RP,
            "RP",
        ),
        FederalState(
            R.string.DE_SN,
            "SN",
        ),
        FederalState(
            R.string.DE_ST,
            "ST",
        ),
        FederalState(
            R.string.DE_SL,
            "SL",
        ),
        FederalState(
            R.string.DE_SH,
            "SH",
        ),
        FederalState(
            R.string.DE_TH,
            "TH",
        ),
    ).associateBy { it.regionId.uppercase() }

    public fun getSortedFederalStateList(): List<FederalState> {
        return federalStateList.mapNotNull { it.value }.sortedBy { getString(it.nameRes) }
    }

    public fun getFederalStateByCode(regionId: String): FederalState? = federalStateList[regionId]
}

@Parcelize
public data class FederalState(
    @StringRes val nameRes: Int,
    val regionId: String,
) : Parcelable
