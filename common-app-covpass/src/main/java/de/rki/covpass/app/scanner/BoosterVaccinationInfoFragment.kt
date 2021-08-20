/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.scanner

import android.os.Bundle
import android.view.View
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.annotations.Abort
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import com.ibm.health.common.navigation.android.triggerBackPress
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.BoosterVaccinationPopupContentBinding
import de.rki.covpass.app.detail.DetailFragmentNav
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import kotlinx.parcelize.Parcelize

@Parcelize
internal class BoosterVaccinationInfoFragmentNav(
    var certId: GroupedCertificatesId
) : FragmentNav(BoosterVaccinationInfoFragment::class)

/**
 * Fragment which show information about the Booster Vaccination
 */
internal class BoosterVaccinationInfoFragment : BaseBottomSheet() {

    private val args: BoosterVaccinationInfoFragmentNav by lazy { getArgs() }
    override val buttonTextRes = R.string.dialogue_add_booster_vaccination_action_button_title

    init {
        viewBinding(BoosterVaccinationPopupContentBinding::inflate)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetBinding.bottomSheetTitle.text = getString(R.string.dialogue_add_booster_vaccination_title)
    }

    override fun onBackPressed(): Abortable {
        findNavigator().popAll()
        findNavigator().push(DetailFragmentNav(args.certId))
        return Abort
    }

    override fun onActionButtonClicked() {
        triggerBackPress()
    }
}
