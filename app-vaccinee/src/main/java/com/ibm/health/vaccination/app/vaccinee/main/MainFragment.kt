package com.ibm.health.vaccination.app.vaccinee.main

import android.os.Bundle
import android.view.View
import android.view.View.MeasureSpec
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.ensody.reactivestate.android.autoRun
import com.ensody.reactivestate.get
import com.google.android.material.tabs.TabLayoutMediator
import com.ibm.health.common.android.utils.buildState
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.vaccination.app.BaseFragment
import com.ibm.health.common.vaccination.app.OpenSourceLicenseFragmentNav
import com.ibm.health.common.vaccination.app.dialog.DialogModel
import com.ibm.health.common.vaccination.app.dialog.showDialog
import com.ibm.health.vaccination.app.vaccinee.R
import com.ibm.health.vaccination.app.vaccinee.add.AddVaccinationCertificateFragmentNav
import com.ibm.health.vaccination.app.vaccinee.databinding.VaccineeMainBinding
import com.ibm.health.vaccination.app.vaccinee.dependencies.vaccineeDeps
import com.ibm.health.vaccination.app.vaccinee.detail.DetailCallback
import com.ibm.health.vaccination.app.vaccinee.storage.GroupedCertificatesList
import kotlinx.parcelize.Parcelize

@Parcelize
class MainFragmentNav : FragmentNav(MainFragment::class)

internal class MainFragment : BaseFragment(), DetailCallback {

    private val state by buildState { MainState(scope) }
    private val binding by viewBinding(VaccineeMainBinding::inflate)
    private lateinit var fragmentStateAdapter: CertificateFragmentStateAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        autoRun {
            updateCertificates(get(vaccineeDeps.storage.certs), state.selectedCertId)
        }
    }

    private fun setupViews() {
        binding.mainAddButton.setOnClickListener { showAddVaccinationCertificatePopup() }
        binding.mainSettingsImagebutton.setOnClickListener { findNavigator().push(OpenSourceLicenseFragmentNav()) }
        fragmentStateAdapter = CertificateFragmentStateAdapter(this)
        binding.mainViewPager.adapter = fragmentStateAdapter
        TabLayoutMediator(binding.mainTabLayout, binding.mainViewPager) { tab, position ->
            // no special tab config necessary
        }.attach()
        prepareViewpagerForDifferentSizes()
    }

    private fun prepareViewpagerForDifferentSizes() {
        binding.mainViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                state.onPageSelected(position)
                fragmentStateAdapter.createFragment(position).view?.let { view ->
                    view.post {
                        val widthMeasureSpec = MeasureSpec.makeMeasureSpec(view.width, MeasureSpec.EXACTLY)
                        val heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                        view.measure(widthMeasureSpec, heightMeasureSpec)

                        if (binding.mainViewPager.layoutParams.height != view.measuredHeight) {
                            binding.mainViewPager.layoutParams =
                                (binding.mainViewPager.layoutParams as LinearLayout.LayoutParams).also { params ->
                                    params.height = view.measuredHeight
                                }
                        }
                    }
                }
            }
        })
    }

    private fun updateCertificates(certificateList: GroupedCertificatesList, selectedCertId: String?) {
        if (certificateList.certificates.isEmpty()) {
            binding.mainEmptyCardview.isVisible = true
            binding.mainViewPagerContainer.isVisible = false
        } else {
            fragmentStateAdapter.createFragments(certificateList)
            binding.mainEmptyCardview.isVisible = false
            binding.mainViewPagerContainer.isVisible = true
            selectedCertId?.let {
                binding.mainViewPager.setCurrentItem(fragmentStateAdapter.getItemPosition(it), false)
            }
        }
    }

    override fun onDeletionCompleted() {
        val dialogModel = DialogModel(
            titleRes = R.string.main_delete_dialog_header,
            messageRes = R.string.main_delete_dialog_message,
            positiveButtonTextRes = R.string.main_delete_dialog_positive,
        )
        showDialog(dialogModel, childFragmentManager)
    }

    private fun showAddVaccinationCertificatePopup() {
        findNavigator().push(AddVaccinationCertificateFragmentNav())
    }
}
