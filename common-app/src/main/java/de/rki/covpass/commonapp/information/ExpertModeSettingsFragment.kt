package de.rki.covpass.commonapp.information

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ibm.health.common.android.utils.attachToolbar
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.R
import de.rki.covpass.commonapp.databinding.ExpertModeSettingsBinding
import de.rki.covpass.commonapp.dependencies.commonDeps
import kotlinx.parcelize.Parcelize

@Parcelize
public class ExpertModeSettingsFragmentNav : FragmentNav(ExpertModeSettingsFragment::class)

/**
 * Displays the Expert mode
 */
public class ExpertModeSettingsFragment : BaseFragment() {

    private val binding by viewBinding(ExpertModeSettingsBinding::inflate)

    override val announcementAccessibilityRes: Int = R.string.accessibility_app_information_title_local_rules

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBar()
        val isExpertModeOn = commonDeps.checkContextRepository.isExpertModeOn.value
        binding.expertModeSettingsNote.setText(R.string.revocation_copy)
        binding.expertModeSettingsToggle.apply {
            updateTitle(R.string.revocation_toggle_text)
            updateToggle(isExpertModeOn)
            setOnClickListener {
                updateToggle(!binding.expertModeSettingsToggle.isChecked())
                updateRulesState()
            }
        }
    }

    private fun updateRulesState() {
        launchWhenStarted {
            commonDeps.checkContextRepository.isExpertModeOn.set(
                binding.expertModeSettingsToggle.isChecked()
            )
        }
    }

    private fun setupActionBar() {
        attachToolbar(binding.expertModeToolbar)
        (activity as? AppCompatActivity)?.run {
            supportActionBar?.run {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.back_arrow)
                // TODO add accessibility title
//                setHomeActionContentDescription(R.string.accessibility_app_information_contact_label_back)
            }
            binding.expertModeToolbar.setTitle(R.string.revocation_headline)
        }
    }
}
