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
internal class CovPassCheckInformationFragmentNav : FragmentNav(CovPassCheckInformationFragment::class)

/**
 * Fragment to display the faq, imprint etc. This subclass only defines the links, the rest is included in
 * [InformationFragment].
 */
internal class CovPassCheckInformationFragment : InformationFragment() {
    override val activateAppRuleSet = true

    override fun getFAQLinkRes() = R.string.information_faq_link

    override fun getImprintLinkRes() = R.string.information_imprint_link

    override fun getEasyLanguageLinkRes(): Int = R.string.easy_language_link
}
