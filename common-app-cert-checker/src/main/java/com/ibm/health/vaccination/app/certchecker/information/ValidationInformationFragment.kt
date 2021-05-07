package com.ibm.health.vaccination.app.certchecker.information

import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.vaccination.app.information.InformationFragment
import com.ibm.health.vaccination.app.certchecker.R
import kotlinx.parcelize.Parcelize

@Parcelize
internal class ValidationInformationFragmentNav : FragmentNav(ValidationInformationFragment::class)

internal class ValidationInformationFragment : InformationFragment() {

    override fun getFAQLinkRes() = R.string.information_faq_link

    override fun getDataSecurityPolicyLinkRes() = R.string.information_data_security_policy_link

    override fun getImprintLinkRes() = R.string.information_imprint_link
}
