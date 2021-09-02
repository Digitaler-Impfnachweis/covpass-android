/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.updateinfo

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.commonapp.databinding.UpdateInfoBinding
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.updateinfo.UpdateInfoRepository.Companion.CURRENT_UPDATE_VERSION

public abstract class UpdateInfoFragment : BaseBottomSheet() {

    private val binding by viewBinding(UpdateInfoBinding::inflate)

    @get:StringRes
    public abstract val updateInfoPath: Int

    @get:StringRes
    public abstract val updateInfoButton: Int

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.bottomSheetActionButton.text = getString(updateInfoButton)
        bottomSheetBinding.bottomSheetHeader.isVisible = false
        bottomSheetBinding.bottomSheetClose.isVisible = false

        binding.updateInfoWebView.loadUrl(getString(updateInfoPath))
    }

    override fun onActionButtonClicked() {
        launchWhenStarted {
            commonDeps.updateInfoRepository.updateInfoVersionShown.set(CURRENT_UPDATE_VERSION)
            findNavigator().pop()
        }
    }

    override fun onClickOutside() {
        super.onClickOutside()
        launchWhenStarted {
            commonDeps.updateInfoRepository.updateInfoVersionShown.set(CURRENT_UPDATE_VERSION)
        }
    }
}
