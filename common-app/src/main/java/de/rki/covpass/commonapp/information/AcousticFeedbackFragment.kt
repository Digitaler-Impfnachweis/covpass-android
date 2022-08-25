/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.information

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ibm.health.common.android.utils.attachToolbar
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.R
import de.rki.covpass.commonapp.databinding.AcousticFeedbackStatusBinding
import de.rki.covpass.commonapp.dependencies.commonDeps
import kotlinx.parcelize.Parcelize

@Parcelize
public class AcousticFeedbackFragmentNav : FragmentNav(AcousticFeedbackFragment::class)

/**
 * Displays the contacts
 */
public class AcousticFeedbackFragment : BaseFragment() {

    private val binding by viewBinding(AcousticFeedbackStatusBinding::inflate)

    private lateinit var mp: MediaPlayer

    override val announcementAccessibilityRes: Int = R.string.accessibility_settings_beep_announce_open

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBar()
        binding.acousticFeedbackText.text = getString(R.string.app_information_beep_when_checking_message)
        binding.acousticFeedbackField.apply {
            updateTitle(R.string.app_information_beep_when_checking_title)
            updateToggle(commonDeps.acousticFeedbackRepository.acousticFeedbackStatus.value)
            setOnClickListener {
                updateToggle(!binding.acousticFeedbackField.isChecked())
                updateAcousticFeedbackState()
            }
        }

        mp = MediaPlayer.create(requireContext(), R.raw.covpass_check_certificate_activated)
    }

    private fun updateAcousticFeedbackState() {
        launchWhenStarted {
            commonDeps.acousticFeedbackRepository.acousticFeedbackStatus.set(
                binding.acousticFeedbackField.isChecked(),
            )
            if (binding.acousticFeedbackField.isChecked()) {
                mp.start()
            }
        }
    }

    private fun setupActionBar() {
        attachToolbar(binding.informationToolbar)
        (activity as? AppCompatActivity)?.run {
            supportActionBar?.run {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.back_arrow)
                setHomeActionContentDescription(R.string.accessibility_settings_beep_backlabel)
            }
            binding.informationToolbar.setTitle(R.string.app_information_beep_when_checking_title)
        }
    }
}
