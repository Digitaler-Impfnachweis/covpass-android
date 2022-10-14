/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.information

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ensody.reactivestate.android.autoRun
import com.ensody.reactivestate.android.reactiveState
import com.ensody.reactivestate.get
import com.ibm.health.common.android.utils.attachToolbar
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.R
import de.rki.covpass.commonapp.databinding.FederalStateSettingsBinding
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.federalstate.ChangeFederalStateCallBack
import de.rki.covpass.commonapp.federalstate.ChangeFederalStateFragmentNav
import de.rki.covpass.commonapp.utils.FederalStateResolver
import kotlinx.parcelize.Parcelize

@Parcelize
public class FederalStateSettingFragmentNav : FragmentNav(FederalStateSettingFragment::class)

public class FederalStateSettingFragment : BaseFragment(), ChangeFederalStateCallBack {

    private val binding by viewBinding(FederalStateSettingsBinding::inflate)
    private val viewModel by reactiveState { FederalStateSettingsViewModel(scope) }

    // TODO change text
    override val announcementAccessibilityRes: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBar()

        binding.federalStateElement.setOnClickListener {
            findNavigator().push(
                ChangeFederalStateFragmentNav(commonDeps.federalStateRepository.federalState.value),
            )
        }
        autoRun {
            FederalStateResolver.getFederalStateByCode(
                get(commonDeps.federalStateRepository.federalState),
            )?.let {
                binding.federalStateElement.updateText(
                    getString(it.nameRes),
                )
            }
        }
    }

    private fun setupActionBar() {
        attachToolbar(binding.federalStateSettingsToolbar)
        (activity as? AppCompatActivity)?.run {
            supportActionBar?.run {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.back_arrow)
                setHomeActionContentDescription(R.string.accessibility_app_information_contact_label_back)
            }
            binding.federalStateSettingsToolbar.setTitle(R.string.infschg_settings_federal_state_title)
        }
    }

    override fun onChangeDone() {
        viewModel.updateCertificatesStatus()
    }
}
