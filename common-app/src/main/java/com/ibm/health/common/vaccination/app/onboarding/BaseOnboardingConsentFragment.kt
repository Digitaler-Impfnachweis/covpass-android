package com.ibm.health.common.vaccination.app.onboarding

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.ensody.reactivestate.MutableValueFlow
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.vaccination.app.BaseFragment
import com.ibm.health.common.vaccination.app.R
import com.ibm.health.common.vaccination.app.databinding.OnboardingConsentBinding

public abstract class BaseOnboardingConsentFragment : BaseFragment() {

    public abstract val titleRes: Int
    public abstract val textRes: Int
    public abstract val imageRes: Int
    public abstract val buttonTextRes: Int

    private val binding by viewBinding(OnboardingConsentBinding::inflate)

    public val isFormValid: MutableValueFlow<Boolean> = MutableValueFlow(false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.onboardingInfoHeaderTextview.setText(titleRes)
        binding.onboardingInfoTextview.setText(textRes)
        binding.onboardingImageview.setImageResource(imageRes)
        binding.dataProtectionCheckbox.setText(R.string.onboarding_consent_data_protection)
        updateFormValidity()

        binding.dataProtectionCheckbox.addOnCheckListener { _: CompoundButton, _: Boolean ->
            updateFormValidity()
        }
    }

    private fun updateFormValidity() {
        isFormValid.value = binding.dataProtectionCheckbox.isChecked()
    }
}
