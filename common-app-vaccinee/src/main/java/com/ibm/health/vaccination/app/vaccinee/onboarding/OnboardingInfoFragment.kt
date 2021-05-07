package com.ibm.health.vaccination.app.vaccinee.onboarding

import com.ibm.health.common.vaccination.app.onboarding.BaseOnboardingConsentFragment
import com.ibm.health.common.vaccination.app.onboarding.BaseOnboardingInfoFragment
import com.ibm.health.vaccination.app.vaccinee.R

internal abstract class CommonOnboardingInfoFragment : BaseOnboardingInfoFragment() {
    override val buttonTextRes: Int = R.string.next_onboarding_page_button_title
}

internal class OnboardingInfo1Fragment : CommonOnboardingInfoFragment() {
    override val titleRes = R.string.vaccination_first_onboarding_page_title
    override val textRes = R.string.vaccination_first_onboarding_page_message
    override val imageRes = R.drawable.onboarding_info_1
}

internal class OnboardingInfo2Fragment : CommonOnboardingInfoFragment() {
    override val titleRes = R.string.vaccination_second_onboarding_page_title
    override val textRes = R.string.vaccination_second_onboarding_page_message
    override val imageRes = R.drawable.onboarding_info_2
}

internal class OnboardingInfo3Fragment : CommonOnboardingInfoFragment() {
    override val titleRes = R.string.vaccination_third_onboarding_page_title
    override val textRes = R.string.vaccination_third_onboarding_page_message
    override val imageRes = R.drawable.onboarding_info_3
}

internal class OnboardingConsentFragment : BaseOnboardingConsentFragment() {
    override val titleRes = R.string.vaccination_fourth_onboarding_page_title
    override val textRes = R.string.vaccination_fourth_onboarding_page_message
    override val imageRes = R.drawable.onboarding_consent
    override val buttonTextRes = R.string.confirmation_fourth_onboarding_page_button_title
    override val dataProtectionLinkRes = R.string.on_boarding_consent_data_protection_link
}
