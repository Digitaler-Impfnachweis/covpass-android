/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.main

import android.os.Bundle
import android.view.View
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.ensody.reactivestate.android.autoRun
import com.ensody.reactivestate.android.onDestroyView
import com.ensody.reactivestate.android.reactiveState
import com.ensody.reactivestate.get
import com.ensody.reactivestate.validUntil
import com.google.android.material.tabs.TabLayoutMediator
import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.app.R
import de.rki.covpass.app.add.AddCovCertificateFragmentNav
import de.rki.covpass.app.checkerremark.CheckRemarkCallback
import de.rki.covpass.app.checkerremark.CheckerRemarkFragmentNav
import de.rki.covpass.app.databinding.CovpassMainBinding
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.app.detail.DetailCallback
import de.rki.covpass.app.information.CovPassInformationFragmentNav
import de.rki.covpass.app.updateinfo.UpdateInfoCallback
import de.rki.covpass.app.updateinfo.UpdateInfoCovpassFragmentNav
import de.rki.covpass.app.validitycheck.ValidityCheckFragmentNav
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.dialog.DialogAction
import de.rki.covpass.commonapp.dialog.DialogListener
import de.rki.covpass.commonapp.dialog.DialogModel
import de.rki.covpass.commonapp.dialog.showDialog
import de.rki.covpass.sdk.cert.models.GroupedCertificates
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import de.rki.covpass.sdk.cert.models.GroupedCertificatesList
import kotlinx.parcelize.Parcelize

@Parcelize
internal class MainFragmentNav : FragmentNav(MainFragment::class)

internal interface NotificationEvents : BaseEvents {
    fun showExpiryNotification()
    fun showNewUpdateInfo()
    fun showCheckerRemark()
    fun showBoosterNotification()
}

/**
 * The main fragment hosts a [ViewPager2] to display all [GroupedCertificates] and serves as entry point for further
 * actions (e.g. add new certificate, show settings screen, show selected certificate)
 */
internal class MainFragment :
    BaseFragment(),
    DetailCallback,
    DialogListener,
    UpdateInfoCallback,
    CheckRemarkCallback,
    NotificationEvents {

    private val viewModel by reactiveState { MainViewModel(scope) }
    private val binding by viewBinding(CovpassMainBinding::inflate)
    private var fragmentStateAdapter: CertificateFragmentStateAdapter by validUntil(::onDestroyView)
    override val announcementAccessibilityRes: Int = R.string.accessibility_start_screen_info_announce

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        autoRun {
            val certs = get(covpassDeps.certRepository.certs)
            updateCertificates(certs, viewModel.selectedCertId)
        }
    }

    private fun setupViews() {
        ViewCompat.setAccessibilityDelegate(
            binding.mainEmptyHeaderTextview,
            object : AccessibilityDelegateCompat() {
                override fun onInitializeAccessibilityNodeInfo(host: View?, info: AccessibilityNodeInfoCompat) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info.isHeading = true
                }
            }
        )
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
        binding.mainValidityCheckTextview.isVisible = certificateList.certificates.size > 0
    }

    override fun onDeletionCompleted() {
        val dialogModel = DialogModel(
            titleRes = R.string.delete_result_dialog_header,
            messageString = getString(R.string.delete_result_dialog_message),
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

    override fun onUpdateInfoFinish() {
        viewModel.validateNotifications()
    }

    override fun onCheckRemarkFinish() {
        viewModel.validateNotifications()
    }

    override fun onDialogAction(tag: String, action: DialogAction) {
        if (tag == EXPIRED_DIALOG_TAG) {
            launchWhenStarted {
                covpassDeps.certRepository.certs.update { groupedCertificateList ->
                    groupedCertificateList.certificates.forEach {
                        it.hasSeenExpiryNotification = true
                    }
                }
                viewModel.validateNotifications()
            }
        }
    }

    companion object {
        private const val EXPIRED_DIALOG_TAG = "expired_dialog"
    }

    override fun showExpiryNotification() {
        val dialogModel = DialogModel(
            titleRes = R.string.error_validity_check_certificates_title,
            messageString = getString(R.string.error_validity_check_certificates_message),
            positiveButtonTextRes = R.string.error_validity_check_certificates_button_title,
            tag = EXPIRED_DIALOG_TAG,
        )
        showDialog(dialogModel, childFragmentManager)
    }

    override fun showNewUpdateInfo() {
        findNavigator().push(UpdateInfoCovpassFragmentNav())
    }

    override fun showCheckerRemark() {
        findNavigator().push(CheckerRemarkFragmentNav())
    }

    override fun showBoosterNotification() {
        findNavigator().push(BoosterNotificationFragmentNav())
    }
}
