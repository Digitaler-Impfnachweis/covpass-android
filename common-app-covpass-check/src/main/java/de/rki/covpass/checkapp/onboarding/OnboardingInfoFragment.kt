/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.onboarding

import de.rki.covpass.checkapp.R
import de.rki.covpass.commonapp.onboarding.BaseOnboardingConsentFragment
import de.rki.covpass.commonapp.onboarding.BaseOnboardingInfoFragment

/**
 * Common base fragment which sets the [buttonTextRes]
 */
internal abstract class CommonOnboardingInfoFragment : BaseOnboardingInfoFragment() {
    override val buttonTextRes = R.string.next_onboarding_page_button_title
}

/**
 * Fragment which holds information for the first page of the onboarding flow
 */
internal class OnboardingInfo1Fragment : CommonOnboardingInfoFragment() {
    override val titleRes = R.string.validation_first_onboarding_page_title
    override val textRes = R.string.validation_first_onboarding_page_message
    override val imageRes = R.drawable.onboarding_info_1
}

/**
 * Fragment which holds information for the second page of the onboarding flow
 */
internal class OnboardingInfo2Fragment : CommonOnboardingInfoFragment() {
    override val titleRes = R.string.validation_second_onboarding_page_title
    override val textRes = R.string.validation_second_onboarding_page_message
    override val imageRes = R.drawable.onboarding_info_2
}

/**
 * Fragment which holds information for the consent page of the onboarding flow
 */
internal class OnboardingConsentFragment : BaseOnboardingConsentFragment() {
    override val titleRes = R.string.validation_fourth_onboarding_page_title
    override val imageRes = R.drawable.onboarding_consent
    override val buttonTextRes = R.string.confirmation_fourth_onboarding_page_button_title
    override val dataProtectionLinkRes = R.string.on_boarding_consent_data_protection_link
    override val contentItemsRes = listOf(
        R.string.validation_fourth_onboarding_first_list_item,
        R.string.validation_fourth_onboarding_second_list_item,
        R.string.validation_fourth_onboarding_third_list_item,
        R.string.validation_fourth_onboarding_fourth_list_item
    )
}
