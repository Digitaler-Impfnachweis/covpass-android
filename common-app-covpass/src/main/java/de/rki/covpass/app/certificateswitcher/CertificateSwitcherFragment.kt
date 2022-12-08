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
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
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
    override val announcementAccessibilityRes: Int =
        R.string.accessibility_infschg_modal_view_announce
    override val closingAnnouncementAccessibilityRes: Int =
        R.string.accessibility_infschg_modal_view_announce_closing

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()
        setupOnPageChangeCallback()
        setupButtons()
        autoRun {
            updateCertificates(get(covpassDeps.certRepository.certs).getGroupedCertificates(args.certId))
        }

        binding.fragmentContainer.setBackgroundResource(backgroundColor)
        binding.mainTabIndicator.setBackgroundResource(backgroundColor)
        binding.mainTabIndicatorGreen.setBackgroundResource(backgroundColor)
        binding.tabNextButton.setOnClickListener {
            nextPage()
        }
        binding.tabBackButton.setOnClickListener {
            previousPage()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val maskStatus = covpassDeps.certRepository.certs.value.getGroupedCertificates(
            args.certId,
        )?.maskStatusWrapper?.maskStatus
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
        setupTabNavigationButtons()
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
        binding.mainTabLayout.isFocusable = false
        (binding.mainViewPager.getChildAt(0) as RecyclerView).isFocusable = false
        TabLayoutMediator(binding.mainTabIndicator, binding.mainViewPager) { _, _ ->
            // no special tab config necessary
        }.attach()
        TabLayoutMediator(binding.mainTabIndicatorGreen, binding.mainViewPager) { _, _ ->
            // no special tab config necessary
        }.attach()
    }

    private fun setupOnPageChangeCallback() {
        binding.mainViewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    showTabNavigationButtons(position)
                }
            },
        )
    }

    private fun nextPage() {
        val certSize = covpassDeps.certRepository.certs.value.getGroupedCertificates(args.certId)
            ?.getListOfImportantCerts()?.size ?: 0
        val currentPage = binding.mainViewPager.currentItem
        val nextPage = if (currentPage + 1 >= certSize) {
            0
        } else {
            currentPage + 1
        }

        binding.mainViewPager.setCurrentItem(nextPage, true)
    }

    private fun previousPage() {
        val currentPage = binding.mainViewPager.currentItem
        val previousPage = if (currentPage - 1 < 0) {
            0
        } else {
            currentPage - 1
        }

        binding.mainViewPager.setCurrentItem(previousPage, true)
    }

    private fun setupTabNavigationButtons() {
        if (backgroundColor == R.color.info70) {
            binding.tabBackButton.setImageResource(R.drawable.arrow_left_on_blue)
            binding.tabNextButton.setImageResource(R.drawable.arrow_right_on_blue)
        } else {
            binding.tabBackButton.setImageResource(R.drawable.arrow_left_on_green)
            binding.tabNextButton.setImageResource(R.drawable.arrow_right_on_green)
        }
    }

    private fun showTabNavigationButtons(position: Int) {
        val certSize = covpassDeps.certRepository.certs.value.getGroupedCertificates(args.certId)
            ?.getListOfImportantCerts()?.size ?: return

        if (position == 0) {
            binding.tabBackButton.isVisible = false
        }
        if (position > 0) {
            binding.tabBackButton.isVisible = true
        }
        binding.tabNextButton.isVisible = position != certSize - 1
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
            binding.mainTabLayout.isVisible = list.size > 1
            if (backgroundColor == R.color.info70) {
                binding.mainTabIndicator.isVisible = list.size > 1
                binding.mainTabIndicatorGreen.isVisible = false
            } else {
                binding.mainTabIndicator.isVisible = false
                binding.mainTabIndicatorGreen.isVisible = list.size > 1
            }
            binding.certificateNoteTextview.text = getString(R.string.modal_subline, list.size)
            fragmentStateAdapter.createFragments(args.certId, list)
        }
    }
}
