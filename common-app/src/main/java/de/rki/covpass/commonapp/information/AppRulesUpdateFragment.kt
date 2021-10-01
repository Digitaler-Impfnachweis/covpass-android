/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.information

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.ensody.reactivestate.android.autoRun
import com.ensody.reactivestate.android.reactiveState
import com.ensody.reactivestate.get
import com.ibm.health.common.android.utils.attachToolbar
import com.ibm.health.common.android.utils.viewBinding
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.R
import de.rki.covpass.commonapp.databinding.AppRulesUpdateBinding
import de.rki.covpass.commonapp.uielements.showInfo
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.utils.formatDateTime
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

public class AppRulesUpdateFragment : BaseFragment() {

    private val binding by viewBinding(AppRulesUpdateBinding::inflate)
    private val appUpdateViewModel by reactiveState { AppUpdateViewModel(scope) }
    private val dscRepository get() = sdkDeps.dscRepository
    private val rulesUpdateRepository get() = sdkDeps.rulesUpdateRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupActionBar()
        binding.updateButton.setOnClickListener {
            appUpdateViewModel.updateRulesAndCertificates()
        }

        autoRun {
            updateTimeDisplay(
                get(dscRepository.lastUpdate),
                get(rulesUpdateRepository.lastRulesUpdate)
            )
        }
    }

    override fun setLoading(isLoading: Boolean) {
        binding.loadingLayout.isVisible = isLoading
        binding.updateInfoElement.isGone = isLoading
        binding.updateButton.isGone = isLoading
    }

    @SuppressLint("StringFormatInvalid")
    private fun updateTimeDisplay(lastUpdate: Instant, lastRulesUpdate: Instant) {
        binding.updateNote.setText(R.string.validation_start_screen_offline_modus_information)
        val certText = getString(
            R.string.validation_start_screen_offline_modus_certificates,
            LocalDateTime.ofInstant(lastUpdate, ZoneId.systemDefault()).formatDateTime()
        )
        val ruleText = getString(
            R.string.validation_start_screen_offline_modus_rules,
            LocalDateTime.ofInstant(lastRulesUpdate, ZoneId.systemDefault()).formatDateTime()
        )
        binding.updateInfoElement.showInfo(
            title = getString(R.string.validation_start_screen_offline_modus_note_update),
            subtitle = certText,
            subtitleStyle = R.style.DefaultText_OnBackground70,
            subtitleTopMarginDimenRes = R.dimen.grid_one,
            description = ruleText,
            descriptionTopMarginDimenRes = R.dimen.grid_half,
            descriptionStyle = R.style.DefaultText_OnBackground70,
            iconRes = R.drawable.info_icon_update_app,
        )
    }

    private fun setupActionBar() {
        attachToolbar(binding.informationToolbar)
        (activity as? AppCompatActivity)?.run {
            supportActionBar?.run {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.back_arrow)
                setHomeActionContentDescription(R.string.accessibility_app_information_contact_label_back)
            }
            binding.informationToolbar.setTitle(R.string.validation_start_screen_offline_modus_information_title)
        }
    }
}
