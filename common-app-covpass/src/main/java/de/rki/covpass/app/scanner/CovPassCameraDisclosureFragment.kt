/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.scanner

import android.os.Bundle
import android.view.View
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.app.R
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.commonapp.databinding.CameraDisclosurePopupContentBinding
import kotlinx.parcelize.Parcelize

@Parcelize
internal class CovPassCameraDisclosureFragmentNav : FragmentNav(CovPassCameraDisclosureFragment::class)

/**
 * Fragment which shows a disclosure for the camera permission.
 */
internal class CovPassCameraDisclosureFragment : BaseBottomSheet() {

    private val binding by viewBinding(CameraDisclosurePopupContentBinding::inflate)
    override val buttonTextRes = R.string.certificate_add_dialog_camera_access_action_button
    override val announcementAccessibilityRes: Int = R.string.accessibility_scan_dialog_camera_access_announce

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetBinding.bottomSheetTitle.text = getString(R.string.certificate_add_dialog_camera_access_title)
        binding.cameraDisclosureContent.text = getString(R.string.certificate_add_dialog_camera_access_message)
    }

    override fun onCloseButtonClicked() {
        findNavigator().popAll()
    }

    override fun onActionButtonClicked() {
        findNavigator().popAll()
        findNavigator().push(CovPassQRScannerFragment())
    }
}
