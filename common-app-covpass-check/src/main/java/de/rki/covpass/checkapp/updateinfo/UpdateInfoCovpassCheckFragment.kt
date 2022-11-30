/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.updateinfo

import android.os.Bundle
import android.view.View
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.checkapp.R
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.updateinfo.UpdateInfoFragment
import de.rki.covpass.commonapp.updateinfo.UpdateInfoRepository
import kotlinx.parcelize.Parcelize

internal interface UpdateInfoCallback {
    fun onUpdateInfoFinish()
}

@Parcelize
public class UpdateInfoCovpassCheckFragmentNav : FragmentNav(UpdateInfoCovpassCheckFragment::class)

public class UpdateInfoCovpassCheckFragment : UpdateInfoFragment() {
    override val updateInfoPath: Int = R.string.update_info_path
    override val updateInfoButton: Int = R.string.dialog_update_info_notification_action_button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.bottomSheetTitle.text = getString(R.string.whats_new_screen_title)
    }

    override fun onActionButtonClicked() {
        launchWhenStarted {
            if (isStopNotificationChecked()) {
                commonDeps.updateInfoRepository.updateInfoNotificationActive.set(false)
            }
            commonDeps.updateInfoRepository.updateInfoVersionShown.set(UpdateInfoRepository.CURRENT_UPDATE_VERSION)
            findNavigator().popUntil<UpdateInfoCallback>()?.onUpdateInfoFinish()
        }
    }
}
