/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.main

import android.os.Bundle
import android.view.View
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.checkapp.R
import de.rki.covpass.checkapp.databinding.NewRegulationNotificationPopupBinding
import de.rki.covpass.checkapp.dependencies.covpassCheckDeps
import de.rki.covpass.commonapp.BaseBottomSheet
import kotlinx.parcelize.Parcelize

@Parcelize
internal class NewRegulationNotificationFragmentNav : FragmentNav(NewRegulationNotificationFragment::class)

public class NewRegulationNotificationFragment : BaseBottomSheet() {

    private val binding by viewBinding(NewRegulationNotificationPopupBinding::inflate)

    override val buttonTextRes: Int = R.string.infschg_info_button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.bottomSheetTitle.setText(R.string.infschg_info_title)
        binding.newRegulationNote1.setText(R.string.infschg_info_copy_1)
        binding.newRegulationNote2.setText(R.string.infschg_info_copy_2)
        binding.newRegulationNote3.setText(R.string.infschg_info_copy_3)
    }

    override fun onActionButtonClicked() {
        launchWhenStarted {
            covpassCheckDeps.checkAppRepository.newRegulationNotificationShown.set(true)
            findNavigator().pop()
        }
    }
}
