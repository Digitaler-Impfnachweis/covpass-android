/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.onboarding

import de.rki.covpass.app.R
import de.rki.covpass.commonapp.onboarding.BaseOnboardingConsentFragment
import de.rki.covpass.commonapp.onboarding.BaseOnboardingInfoFragment

/**
 * Common base fragment which sets the [buttonTextRes]
 */
internal abstract class CommonOnboardingInfoFragment : BaseOnboardingInfoFragment() {
    override val buttonTextRes: Int = R.string.next_onboarding_page_button_title
}

/**
 * Fragment which holds information for the first page of the Onboarding flow
 */
internal class OnboardingInfo1Fragment : CommonOnboardingInfoFragment() {
    override val titleRes = R.string.vaccination_first_onboarding_page_title
    override val textRes = R.string.vaccination_first_onboarding_page_message
    override val imageRes = R.drawable.onboarding_info_1
}

/**
 * Fragment which holds information for the second page of the Onboarding flow
 */
internal class OnboardingInfo2Fragment : CommonOnboardingInfoFragment() {
    override val titleRes = R.string.vaccination_second_onboarding_page_title
    override val textRes = R.string.vaccination_second_onboarding_page_message
    override val imageRes = R.drawable.onboarding_info_2
}

/**
 * Fragment which holds information for the third page of the Onboarding flow
 */
internal class OnboardingInfo3Fragment : CommonOnboardingInfoFragment() {
    override val titleRes = R.string.vaccination_third_onboarding_page_title
    override val textRes = R.string.vaccination_third_onboarding_page_message
    override val imageRes = R.drawable.onboarding_info_3
}

/**
 * Fragment which holds information for the consent page of the Onboarding flow
 */
internal class OnboardingConsentFragment : BaseOnboardingConsentFragment() {
    override val titleRes = R.string.vaccination_fourth_onboarding_page_title
    override val imageRes = R.drawable.onboarding_consent
    override val buttonTextRes = R.string.vaccination_fourth_onboarding_page_button_title
    override val dataProtectionLinkRes = R.string.on_boarding_consent_data_protection_link
    override val contentItemsRes = listOf(
        R.string.vaccination_fourth_onboarding_page_first_list_item,
        R.string.vaccination_fourth_onboarding_page_second_list_item,
        R.string.vaccination_fourth_onboarding_page_third_list_item,
        R.string.vaccination_fourth_onboarding_page_fourth_list_item
    )
}
