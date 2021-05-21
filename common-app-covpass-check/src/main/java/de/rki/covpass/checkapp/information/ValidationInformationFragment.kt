/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.information

import com.ibm.health.common.navigation.android.FragmentNav
import de.rki.covpass.checkapp.R
import de.rki.covpass.commonapp.information.InformationFragment
import kotlinx.parcelize.Parcelize

@Parcelize
internal class ValidationInformationFragmentNav : FragmentNav(ValidationInformationFragment::class)

/**
 * Fragment to display the faq, imprint etc. This subclass only defines the links, the rest is included in
 * [InformationFragment].
 */
internal class ValidationInformationFragment : InformationFragment() {

    override fun getFAQLinkRes() = R.string.information_faq_link

    override fun getDataSecurityPolicyLinkRes() = R.string.information_data_security_policy_link

    override fun getImprintLinkRes() = R.string.information_imprint_link
}
