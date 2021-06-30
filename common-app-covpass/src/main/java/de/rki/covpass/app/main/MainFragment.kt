/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.main

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.ensody.reactivestate.android.autoRun
import com.ensody.reactivestate.android.onDestroyView
import com.ensody.reactivestate.android.reactiveState
import com.ensody.reactivestate.get
import com.ensody.reactivestate.validUntil
import com.google.android.material.tabs.TabLayoutMediator
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.app.R
import de.rki.covpass.app.add.AddCovCertificateFragmentNav
import de.rki.covpass.app.databinding.CovpassMainBinding
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.app.detail.DetailCallback
import de.rki.covpass.app.information.CovPassInformationFragmentNav
import de.rki.covpass.app.validitycheck.ValidityCheckFragmentNav
import de.rki.covpass.sdk.cert.models.GroupedCertificates
import de.rki.covpass.sdk.cert.models.GroupedCertificatesList
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.dialog.DialogModel
import de.rki.covpass.commonapp.dialog.showDialog
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import kotlinx.parcelize.Parcelize

@Parcelize
internal class MainFragmentNav : FragmentNav(MainFragment::class)

/**
 * The main fragment hosts a [ViewPager2] to display all [GroupedCertificates] and serves as entry point for further
 * actions (e.g. add new certificate, show settings screen, show selected certificate)
 */
internal class MainFragment : BaseFragment(), DetailCallback {

    private val viewModel by reactiveState { MainViewModel(scope) }
    private val binding by viewBinding(CovpassMainBinding::inflate)
    private var fragmentStateAdapter: CertificateFragmentStateAdapter by validUntil(::onDestroyView)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        autoRun {
            updateCertificates(get(covpassDeps.certRepository.certs), viewModel.selectedCertId)
        }
    }

    private fun setupViews() {
        binding.mainAddButton.setOnClickListener { showAddCovCertificatePopup() }
        binding.mainValidityCheckTextview.setOnClickListener { findNavigator().push(ValidityCheckFragmentNav()) }
        binding.mainSettingsImagebutton.setOnClickListener { findNavigator().push(CovPassInformationFragmentNav()) }
        fragmentStateAdapter = CertificateFragmentStateAdapter(this)
        fragmentStateAdapter.attachTo(binding.mainViewPager)
        TabLayoutMediator(binding.mainTabLayout, binding.mainViewPager) { _, _ ->
            // no special tab config necessary
        }.attach()
        setupPageChangeCallback()
    }

    private fun setupPageChangeCallback() {
        binding.mainViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.onPageSelected(position)
            }
        })
    }

    private fun updateCertificates(certificateList: GroupedCertificatesList, selectedCertId: GroupedCertificatesId?) {
        if (certificateList.certificates.isEmpty()) {
            binding.mainEmptyCardview.isVisible = true
            binding.mainViewPager.isVisible = false
        } else {
            fragmentStateAdapter.createFragments(certificateList)
            binding.mainEmptyCardview.isVisible = false
            binding.mainViewPager.isVisible = true
            selectedCertId?.let {
                binding.mainViewPager.setCurrentItem(fragmentStateAdapter.getItemPosition(it), isResumed)
            }
        }
        binding.mainTabLayout.isVisible = certificateList.certificates.size > 1
    }

    override fun onDeletionCompleted() {
        val dialogModel = DialogModel(
            titleRes = R.string.delete_result_dialog_header,
            messageRes = R.string.delete_result_dialog_message,
            positiveButtonTextRes = R.string.delete_result_dialog_positive_button_text,
        )
        showDialog(dialogModel, childFragmentManager)
    }

    override fun displayCert(certId: GroupedCertificatesId) {
        viewModel.selectedCertId = certId
        binding.mainViewPager.setCurrentItem(fragmentStateAdapter.getItemPosition(certId), isResumed)
    }

    private fun showAddCovCertificatePopup() {
        findNavigator().push(AddCovCertificateFragmentNav())
    }
}
