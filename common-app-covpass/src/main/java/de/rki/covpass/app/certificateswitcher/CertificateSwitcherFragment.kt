/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.certificateswitcher

import android.content.Context
import android.gesture.GestureOverlayView.ORIENTATION_HORIZONTAL
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
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
import kotlinx.parcelize.Parcelize
import kotlin.math.abs

@Parcelize
internal class CertificateSwitcherFragmentNav(val certId: GroupedCertificatesId) :
    FragmentNav(CertificateSwitcherFragment::class)

internal class CertificateSwitcherFragment : BaseFragment() {

    private val args: CertificateSwitcherFragmentNav by lazy { getArgs() }
    private var fragmentStateAdapter: CertificateSwitcherFragmentStateAdapter by validUntil(::onDestroyView)
    private val binding by viewBinding(CertificateSwitcherBinding::inflate)
    private var navigationBarColor: Int? = null
    private var statusBarColor: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()
        setupViewPager()
        setupButtons()
        autoRun {
            updateCertificates(get(covpassDeps.certRepository.certs).getGroupedCertificates(args.certId))
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
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
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.info70)
        requireActivity().window.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.info70)
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
    }

    private fun setupViewPager() {
        with(binding.mainViewPager) {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
        }

        val pageMarginPx = resources.getDimensionPixelOffset(R.dimen.grid_three)
        val offsetPx = resources.getDimensionPixelOffset(R.dimen.grid_three)
        binding.mainViewPager.setPageTransformer { page, position ->
            val viewPager = page.parent.parent as ViewPager2
            val offset = position * -(2 * offsetPx + pageMarginPx)
            if (viewPager.orientation == ORIENTATION_HORIZONTAL) {
                if (ViewCompat.getLayoutDirection(viewPager) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                    page.translationX = -offset
                } else {
                    page.translationX = offset
                }
            } else {
                page.translationY = offset
            }
            val alpha = CERTIFICATE_SWITCHER_CARD_MAX_WITH_OFFSET_ALPHA - abs(position)
            page.alpha = when {
                alpha > CERTIFICATE_SWITCHER_CARD_MAX_ALPHA -> {
                    CERTIFICATE_SWITCHER_CARD_MAX_ALPHA
                }
                alpha < CERTIFICATE_SWITCHER_CARD_MIN_ALPHA -> {
                    CERTIFICATE_SWITCHER_CARD_MIN_ALPHA
                }
                else -> {
                    alpha
                }
            }
        }
    }

    private fun updateCertificates(groupedCertificates: GroupedCertificates?) {
        binding.certificateNameTextview.text =
            groupedCertificates?.certificates?.first()?.covCertificate?.fullName
        binding.certificateNoteTextview.text = getString(R.string.close)
        val showBoosterNotification = groupedCertificates?.hasSeenBoosterDetailNotification == false &&
            groupedCertificates.boosterNotification.result == BoosterResult.Passed
        binding.actionButtonIcon.setImageResource(
            if (showBoosterNotification) {
                R.drawable.certificate_switcher_manage_certificates_notification
            } else {
                R.drawable.certificate_switcher_manage_certificates
            }
        )

        groupedCertificates?.getListOfImportantCerts()?.let { list ->
            binding.mainTabLayout.isVisible = list.size > 1
            binding.certificateNoteTextview.text = getString(R.string.modal_subline, list.size)
            fragmentStateAdapter.createFragments(args.certId, list)
        }
    }

    private companion object {
        const val CERTIFICATE_SWITCHER_CARD_MAX_ALPHA = 1.0f
        const val CERTIFICATE_SWITCHER_CARD_MIN_ALPHA = 0.4f
        const val CERTIFICATE_SWITCHER_CARD_MAX_WITH_OFFSET_ALPHA = 1.4f
    }
}
