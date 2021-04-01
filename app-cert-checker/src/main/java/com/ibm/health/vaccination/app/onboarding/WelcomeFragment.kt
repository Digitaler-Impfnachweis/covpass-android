package com.ibm.health.vaccination.app.onboarding

import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.vaccination.app.onboarding.BaseWelcomeFragment
import com.ibm.health.vaccination.app.R
import kotlinx.parcelize.Parcelize

@Parcelize
class WelcomeFragmentNav : FragmentNav(WelcomeFragment::class)

class WelcomeFragment : BaseWelcomeFragment() {

    override fun getHeaderTextRes() = R.string.onboarding_welcome_header

    override fun getSubheaderTextRes() = R.string.onboarding_welcome_subheader

    override fun getEncryptionHeaderTextRes() = R.string.onboarding_welcome_encryption_header

    override fun getEncryptionTextRes() = R.string.onboarding_welcome_encryption_text

    override fun getMainImageRes() = R.drawable.onboarding_welcome

    override fun getOnboardingNav(): FragmentNav = OnboardingContainerFragmentNav()
}
