/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.information

import com.ibm.health.common.navigation.android.FragmentNav
import de.rki.covpass.app.R
import de.rki.covpass.commonapp.information.InformationFragment
import kotlinx.parcelize.Parcelize

@Parcelize
internal class VaccinationInformationFragmentNav : FragmentNav(VaccinationInformationFragment::class)

/**
 * Covpass specific Information screen. Overrides the abstract functions from [InformationFragment].
 */
internal class VaccinationInformationFragment : InformationFragment() {

    override fun getFAQLinkRes() = R.string.information_faq_link

    override fun getImprintLinkRes() = R.string.information_imprint_link
}
