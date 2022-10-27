/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.scanner

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.checkapp.R
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.commonapp.databinding.CameraDisclosurePopupContentBinding
import de.rki.covpass.commonapp.utils.isLandscapeMode
import kotlinx.parcelize.Parcelize

@Parcelize
internal class CovPassCheckCameraDisclosureFragmentNav : FragmentNav(CovPassCheckCameraDisclosureFragment::class)

/**
 * Fragment which shows a disclosure for the camera permission.
 */
internal class CovPassCheckCameraDisclosureFragment : BaseBottomSheet() {

    private val binding by viewBinding(CameraDisclosurePopupContentBinding::inflate)
    override val buttonTextRes = R.string.scan_dialog_camera_access_action_button
    override val announcementAccessibilityRes: Int = R.string.accessibility_scan_dialog_camera_access_announce

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetBinding.bottomSheetTitle.text = getString(R.string.scan_dialog_camera_access_title)
        binding.cameraDisclosureIllustration.isGone = resources.isLandscapeMode()
        binding.cameraDisclosureContent.text = getString(R.string.scan_dialog_camera_access_message)
    }

    override fun onActionButtonClicked() {
        findNavigator().pop()
        findNavigator().push(CovPassCheckQRScannerFragmentNav())
    }
}
