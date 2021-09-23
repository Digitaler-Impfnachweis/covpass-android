/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.updateinfo

import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.app.R
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.updateinfo.UpdateInfoFragment
import de.rki.covpass.commonapp.updateinfo.UpdateInfoRepository
import kotlinx.parcelize.Parcelize

internal interface UpdateInfoCallback {
    fun onUpdateInfoFinish()
}

@Parcelize
public class UpdateInfoCovpassFragmentNav : FragmentNav(UpdateInfoCovpassFragment::class)

public class UpdateInfoCovpassFragment : UpdateInfoFragment() {
    override val updateInfoPath: Int = R.string.update_info_path
    override val updateInfoButton: Int = R.string.vaccination_fourth_onboarding_page_button_title

    override fun onActionButtonClicked() {
        launchWhenStarted {
            commonDeps.updateInfoRepository.updateInfoVersionShown.set(UpdateInfoRepository.CURRENT_UPDATE_VERSION)
            findNavigator().popUntil<UpdateInfoCallback>()?.onUpdateInfoFinish()
        }
    }
}
