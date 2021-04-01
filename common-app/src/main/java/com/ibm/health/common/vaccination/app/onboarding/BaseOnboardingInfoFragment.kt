package com.ibm.health.common.vaccination.app.onboarding

import android.os.Bundle
import android.view.View
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.vaccination.app.BaseFragment
import com.ibm.health.common.vaccination.app.databinding.OnboardingInfoBinding

public abstract class BaseOnboardingInfoFragment : BaseFragment() {

    private val binding by viewBinding(OnboardingInfoBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.onboardingInfoHeaderTextview.setText(getTitleRes())
        binding.onboardingInfoTextview.setText(getTextRes())
        binding.onboardingImageview.setImageResource(getImageRes())
    }

    protected abstract fun getTitleRes(): Int

    protected abstract fun getTextRes(): Int

    protected abstract fun getImageRes(): Int
}
