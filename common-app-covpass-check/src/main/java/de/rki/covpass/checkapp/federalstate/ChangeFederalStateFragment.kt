/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.federalstate

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.checkapp.R
import de.rki.covpass.checkapp.databinding.ChangeFederalStatePopupBinding
import de.rki.covpass.checkapp.dependencies.covpassCheckDeps
import de.rki.covpass.commonapp.BaseBottomSheet
import kotlinx.parcelize.Parcelize

@Parcelize
internal class ChangeFederalStateFragmentNav(val regionId: String) : FragmentNav(ChangeFederalStateFragment::class)

public class ChangeFederalStateFragment : BaseBottomSheet() {

    private val binding by viewBinding(ChangeFederalStatePopupBinding::inflate)

    private val args: ChangeFederalStateFragmentNav by lazy { getArgs() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.bottomSheetTitle.setText(R.string.infschg_module_choose_federal_state_title)
        bottomSheetBinding.bottomSheetBottomLayout.isVisible = false

        val adapter = ChangeFederalStateAdapter(this, args.regionId) { position ->
            val federalState = (binding.federalStateList.adapter as? ChangeFederalStateAdapter)?.getItem(position)
            launchWhenStarted {
                federalState?.let {
                    covpassCheckDeps.checkAppRepository.federalState.set(it.regionId)
                }
                findNavigator().popAll()
            }
        }
        adapter.apply {
            attachTo(binding.federalStateList)
            updateList(FederalStateResolver.getSortedFederalStateList())
        }
    }

    override fun onActionButtonClicked() {}
}
