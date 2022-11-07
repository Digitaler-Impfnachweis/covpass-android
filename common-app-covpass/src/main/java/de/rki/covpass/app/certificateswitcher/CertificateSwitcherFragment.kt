/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.certificateswitcher

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.ensody.reactivestate.android.autoRun
import com.ensody.reactivestate.android.onDestroyView
import com.ensody.reactivestate.get
import com.ensody.reactivestate.validUntil
import com.google.android.material.tabs.TabLayoutMediator
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.CertificateSwitcherBinding
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.app.detail.DetailFragmentNav
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.sdk.cert.models.BoosterResult
import de.rki.covpass.sdk.cert.models.GroupedCertificates
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import de.rki.covpass.sdk.cert.models.MaskStatus
import kotlinx.parcelize.Parcelize

@Parcelize
internal class CertificateSwitcherFragmentNav(
    val certId: GroupedCertificatesId,
) : FragmentNav(CertificateSwitcherFragment::class)

internal class CertificateSwitcherFragment : BaseFragment() {

    private val args: CertificateSwitcherFragmentNav by lazy { getArgs() }
    private var fragmentStateAdapter: CertificateSwitcherFragmentStateAdapter by validUntil(::onDestroyView)
    private val binding by viewBinding(CertificateSwitcherBinding::inflate)
    private var navigationBarColor: Int? = null
    private var statusBarColor: Int? = null
    private var backgroundColor = R.color.info70
    override val announcementAccessibilityRes: Int = R.string.accessibility_infschg_modal_view_announce
    override val closingAnnouncementAccessibilityRes: Int =
        R.string.accessibility_infschg_modal_view_announce_closing

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()
        setupButtons()
        autoRun {
            updateCertificates(get(covpassDeps.certRepository.certs).getGroupedCertificates(args.certId))
        }

        binding.fragmentContainer.setBackgroundResource(backgroundColor)
        binding.mainTabLayout.setBackgroundResource(backgroundColor)
        binding.mainTabLayoutGreen.setBackgroundResource(backgroundColor)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val maskStatus = covpassDeps.certRepository.certs.value.getGroupedCertificates(args.certId)?.maskStatus
        if (maskStatus == MaskStatus.NotRequired) {
            backgroundColor = R.color.full_immunization_green
        }
        changeTopAndBottomBarColor()
    }

    override fun onResume() {
        super.onResume()
        changeTopAndBottomBarColor()
    }

    override fun onPause() {
        super.onPause()
        revertTopAndBottomBarColor()
    }

    private fun changeTopAndBottomBarColor() {
        if (navigationBarColor == null) navigationBarColor = requireActivity().window.navigationBarColor
        if (statusBarColor == null) statusBarColor = requireActivity().window.statusBarColor
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), backgroundColor)
        requireActivity().window.navigationBarColor = ContextCompat.getColor(requireContext(), backgroundColor)
    }

    private fun revertTopAndBottomBarColor() {
        navigationBarColor?.let { requireActivity().window.navigationBarColor = it }
        statusBarColor?.let { requireActivity().window.statusBarColor = it }
        navigationBarColor = null
        statusBarColor = null
    }

    private fun setupButtons() {
        binding.actionButtonText.setText(R.string.modal_button)
        binding.actionButton.setOnClickListener {
            findNavigator().push(DetailFragmentNav(args.certId))
        }
        binding.closeButton.setOnClickListener {
            findNavigator().popAll()
        }
    }

    private fun setupAdapter() {
        fragmentStateAdapter = CertificateSwitcherFragmentStateAdapter(this)
        fragmentStateAdapter.attachTo(binding.mainViewPager)
        TabLayoutMediator(binding.mainTabLayout, binding.mainViewPager) { _, _ ->
            // no special tab config necessary
        }.attach()
        TabLayoutMediator(binding.mainTabLayoutGreen, binding.mainViewPager) { _, _ ->
            // no special tab config necessary
        }.attach()
    }

    private fun updateCertificates(groupedCertificates: GroupedCertificates?) {
        binding.certificateNameTextview.text =
            groupedCertificates?.certificates?.first()?.covCertificate?.fullName
        binding.certificateNoteTextview.text = getString(R.string.close)
        val showBoosterNotification = groupedCertificates?.hasSeenBoosterDetailNotification == false &&
            groupedCertificates.boosterNotification.result == BoosterResult.Passed
        val showDetailReissueNotification = groupedCertificates?.hasSeenReissueDetailNotification == false &&
            (groupedCertificates.isBoosterReadyForReissue() || groupedCertificates.isExpiredReadyForReissue())

        binding.actionButtonIcon.setImageResource(
            if (showBoosterNotification || showDetailReissueNotification) {
                R.drawable.certificate_switcher_manage_certificates_notification
            } else {
                R.drawable.certificate_switcher_manage_certificates
            },
        )

        groupedCertificates?.getListOfImportantCerts()?.let { list ->
            if (backgroundColor == R.color.info70) {
                binding.mainTabLayout.isVisible = list.size > 1
                binding.mainTabLayoutGreen.isVisible = false
            } else {
                binding.mainTabLayout.isVisible = false
                binding.mainTabLayoutGreen.isVisible = list.size > 1
            }
            binding.certificateNoteTextview.text = getString(R.string.modal_subline, list.size)
            fragmentStateAdapter.createFragments(args.certId, list)
        }
    }
}
