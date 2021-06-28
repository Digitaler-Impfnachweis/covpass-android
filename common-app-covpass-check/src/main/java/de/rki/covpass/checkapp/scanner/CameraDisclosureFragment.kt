/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.scanner

import android.os.Bundle
import android.view.View
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.checkapp.R
import de.rki.covpass.checkapp.databinding.CameraDisclosurePopupContentBinding
import de.rki.covpass.commonapp.BaseBottomSheet
import kotlinx.parcelize.Parcelize

@Parcelize
internal class CameraDisclosureFragmentNav : FragmentNav(CameraDisclosureFragment::class)

/**
 * Fragment which shows a disclosure for the camera permission.
 */
internal class CameraDisclosureFragment : BaseBottomSheet() {

    override val buttonTextRes = R.string.scan_dialog_camera_access_action_button

    init {
        viewBinding(CameraDisclosurePopupContentBinding::inflate)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetBinding.bottomSheetTitle.text = getString(R.string.scan_dialog_camera_access_title)
    }

    override fun onActionButtonClicked() {
        findNavigator().pop()
        findNavigator().push(CovPassCheckQRScannerFragment())
    }
}
