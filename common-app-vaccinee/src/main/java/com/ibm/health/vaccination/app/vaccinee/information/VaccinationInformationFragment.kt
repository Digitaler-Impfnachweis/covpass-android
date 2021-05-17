package com.ibm.health.vaccination.app.vaccinee.information

import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.vaccination.app.information.InformationFragment
import com.ibm.health.vaccination.app.vaccinee.R
import kotlinx.parcelize.Parcelize

@Parcelize
internal class VaccinationInformationFragmentNav : FragmentNav(VaccinationInformationFragment::class)

/**
 * Vaccinee specific Information screen. Overrides the abstract functions from [InformationFragment].
 */
internal class VaccinationInformationFragment : InformationFragment() {

    override fun getFAQLinkRes() = R.string.information_faq_link

    override fun getDataSecurityPolicyLinkRes() = R.string.information_data_security_policy_link

    override fun getImprintLinkRes() = R.string.information_imprint_link
}
