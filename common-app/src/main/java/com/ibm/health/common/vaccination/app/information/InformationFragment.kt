package com.ibm.health.common.vaccination.app.information

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ibm.health.common.android.utils.appVersion
import com.ibm.health.common.android.utils.getSpanned
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

public abstract class InformationFragment : BaseFragment() {

    private val binding by viewBinding(InformationBinding::inflate)

    @SuppressLint("StringFormatInvalid")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBar()
        binding.informationAppVersionLabel.text = getString(R.string.app_information_version_label, appVersion)
        binding.informationFieldFaq.apply {
            text = getSpanned(
                getString(R.string.app_information_title_faq_linked),
                getString(getFAQLinkRes())
            )
            movementMethod = LinkMovementMethod.getInstance()
            stripUnderlines()
        }
        binding.informationFieldDataSecurityPolicy.apply {
            text = getSpanned(
                getString(R.string.app_information_title_datenschutz_linked),
                getString(getDataSecurityPolicyLinkRes())
            )
            movementMethod = LinkMovementMethod.getInstance()
            stripUnderlines()
        }
        binding.informationFieldImprint.apply {
            text = getSpanned(
                getString(R.string.app_information_title_company_details_linked),
                getString(getImprintLinkRes())
            )
            movementMethod = LinkMovementMethod.getInstance()
            stripUnderlines()
        }

        binding.informationFieldOpenSourceLicenses.apply {
            text = getString(R.string.app_information_title_open_source)
            setOnClickListener {
                findNavigator().push(OpenSourceLicenseFragmentNav())
            }
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
            binding.informationToolbar.setTitle(R.string.app_information_title)
        }
    }
    protected abstract fun getFAQLinkRes(): Int
    protected abstract fun getDataSecurityPolicyLinkRes(): Int
    protected abstract fun getImprintLinkRes(): Int
}
