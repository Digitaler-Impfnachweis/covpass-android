package com.ibm.health.common.vaccination.app.information

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ibm.health.common.android.utils.appVersion
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.vaccination.app.BaseFragment
import com.ibm.health.common.vaccination.app.OpenSourceLicenseFragmentNav
import com.ibm.health.common.vaccination.app.R
import com.ibm.health.common.vaccination.app.databinding.InformationBinding
import com.ibm.health.common.vaccination.app.utils.stripUnderlines
import kotlinx.parcelize.Parcelize

@Parcelize
public class InformationFragmentNav : FragmentNav(InformationFragment::class)

public class InformationFragment : BaseFragment() {

    private val binding by viewBinding(InformationBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBar()
        binding.informationAppVersionLabel.text = getString(R.string.information_app_version_label_text, appVersion)
        binding.informationFieldFaq.apply {
            movementMethod = LinkMovementMethod.getInstance()
            stripUnderlines()
        }
        binding.informationFieldDataSecurityPolicy.apply {
            movementMethod = LinkMovementMethod.getInstance()
            stripUnderlines()
        }
        binding.informationFieldTermsAndConditions.apply {
            movementMethod = LinkMovementMethod.getInstance()
            stripUnderlines()
        }
        binding.informationFieldImprint.apply {
            movementMethod = LinkMovementMethod.getInstance()
            stripUnderlines()
        }
        binding.informationFieldOpenSourceLicenses.setOnClickListener {
            findNavigator().push(OpenSourceLicenseFragmentNav())
        }
    }

    private fun setupActionBar() {
        (activity as? AppCompatActivity)?.run {
            setSupportActionBar(binding.informationToolbar)
            supportActionBar?.run {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.back_arrow)
            }
            binding.informationToolbar.title = getString(R.string.information_header)
        }
    }
}
