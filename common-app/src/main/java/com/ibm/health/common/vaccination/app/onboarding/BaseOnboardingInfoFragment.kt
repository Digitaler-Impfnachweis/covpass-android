package com.ibm.health.common.vaccination.app.onboarding

import android.os.Bundle
import android.view.View
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.vaccination.app.BaseFragment
import com.ibm.health.common.vaccination.app.databinding.OnboardingInfoBinding

public abstract class BaseOnboardingInfoFragment : BaseFragment() {

    public abstract val titleRes: Int
    public abstract val textRes: Int
    public abstract val imageRes: Int
    public abstract val buttonTextRes: Int

    private val binding by viewBinding(OnboardingInfoBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.onboardingInfoHeaderTextview.setText(titleRes)
        binding.onboardingInfoTextview.setText(textRes)
        binding.onboardingImageview.setImageResource(imageRes)
    }
}
