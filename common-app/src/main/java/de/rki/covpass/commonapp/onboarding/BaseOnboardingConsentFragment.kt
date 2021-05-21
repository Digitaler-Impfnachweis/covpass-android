/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.onboarding

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.ensody.reactivestate.MutableValueFlow
import com.ibm.health.common.android.utils.viewBinding
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.R
import de.rki.covpass.commonapp.databinding.OnboardingConsentBinding

/**
 * Common base fragment for displaying a data privacy consent to the user. Both apps use basically the same fragment,
 * only the different texts and icons are defined in the app-specific fragments.
 */
public abstract class BaseOnboardingConsentFragment : BaseFragment() {

    public abstract val titleRes: Int
    public abstract val textRes: Int
    public abstract val imageRes: Int
    public abstract val buttonTextRes: Int
    public abstract val dataProtectionLinkRes: Int

    private val binding by viewBinding(OnboardingConsentBinding::inflate)

    public val isFormValid: MutableValueFlow<Boolean> = MutableValueFlow(false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.onboardingInfoHeaderTextview.setText(titleRes)
        binding.onboardingInfoTextview.setText(textRes)
        binding.onboardingImageview.setImageResource(imageRes)
        binding.dataProtectionCheckbox.setLinkedText(
            R.string.fourth_onboarding_page_second_selection_linked,
            dataProtectionLinkRes
        )
        updateFormValidity()

        binding.dataProtectionCheckbox.addOnCheckListener { _: CompoundButton, _: Boolean ->
            updateFormValidity()
        }
    }

    private fun updateFormValidity() {
        isFormValid.value = binding.dataProtectionCheckbox.isChecked()
    }
}
