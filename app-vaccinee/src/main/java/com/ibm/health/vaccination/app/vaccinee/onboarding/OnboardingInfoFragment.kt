package com.ibm.health.vaccination.app.vaccinee.onboarding

import com.ibm.health.common.vaccination.app.onboarding.BaseOnboardingConsentFragment
import com.ibm.health.common.vaccination.app.onboarding.BaseOnboardingInfoFragment
import com.ibm.health.vaccination.app.vaccinee.R

abstract class CommonOnboardingInfoFragment : BaseOnboardingInfoFragment() {
    override val buttonTextRes = R.string.onboarding_continue_button_text
}

class OnboardingInfo1Fragment : CommonOnboardingInfoFragment() {
    override val titleRes = R.string.onboarding_info_title_1
    override val textRes = R.string.onboarding_info_text_1
    override val imageRes = R.drawable.onboarding_info_1
}

class OnboardingInfo2Fragment : CommonOnboardingInfoFragment() {
    override val titleRes = R.string.onboarding_info_title_2
    override val textRes = R.string.onboarding_info_text_2
    override val imageRes = R.drawable.onboarding_info_2
}

class OnboardingInfo3Fragment : CommonOnboardingInfoFragment() {
    override val titleRes = R.string.onboarding_info_title_3
    override val textRes = R.string.onboarding_info_text_3
    override val imageRes = R.drawable.onboarding_info_3
}

class OnboardingConsentFragment : BaseOnboardingConsentFragment() {
    override val titleRes = R.string.onboarding_consent_title
    override val textRes = R.string.onboarding_consent_text
    override val imageRes = R.drawable.onboarding_consent
    override val buttonTextRes = R.string.onboarding_consent_continue_button_text
}
