/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.federalstate

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.ensody.reactivestate.android.reactiveState
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.commonapp.R
import de.rki.covpass.commonapp.databinding.ChangeFederalStatePopupBinding
import de.rki.covpass.commonapp.utils.FederalStateResolver
import kotlinx.parcelize.Parcelize

public interface ChangeFederalStateCallBack {
    public fun onChangeDone()
}

@Parcelize
public class ChangeFederalStateFragmentNav(
    public val regionId: String,
) : FragmentNav(
    ChangeFederalStateFragment::class,
)

public class ChangeFederalStateFragment : BaseBottomSheet(), ChangeFederalStateEvents {

    private val binding by viewBinding(ChangeFederalStatePopupBinding::inflate)
    private val args: ChangeFederalStateFragmentNav by lazy { getArgs() }
    private val viewModel by reactiveState { ChangeFederalStateViewModel(scope) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.bottomSheetTitle.setText(R.string.infschg_module_choose_federal_state_title)
        bottomSheetBinding.bottomSheetBottomLayout.isVisible = false

        val adapter = ChangeFederalStateAdapter(this, args.regionId) { position ->
            val federalState = (binding.federalStateList.adapter as? ChangeFederalStateAdapter)?.getItem(position)
            launchWhenStarted {
                federalState?.let {
                    viewModel.updateFederalState(it.regionId)
                }
            }
        }
        adapter.apply {
            attachTo(binding.federalStateList)
            updateList(FederalStateResolver.getSortedFederalStateList())
        }
    }

    override fun onUpdateDone() {
        findNavigator().popUntil<ChangeFederalStateCallBack>()?.onChangeDone()
    }

    override fun onActionButtonClicked() {}
}
