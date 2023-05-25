package de.rki.covpass.commonapp.information

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ensody.reactivestate.android.autoRun
import com.ensody.reactivestate.android.reactiveState
import com.ensody.reactivestate.get
import com.ibm.health.common.android.utils.attachToolbar
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.R
import de.rki.covpass.commonapp.databinding.CheckSettingsBinding
import kotlinx.parcelize.Parcelize

@Parcelize
public class SettingsFragmentNav(
    public val isCovPassCheck: Boolean,
) : FragmentNav(SettingsFragment::class)

public class SettingsFragment : BaseFragment() {

    private val binding by viewBinding(CheckSettingsBinding::inflate)
    private val args by lazy { getArgs<SettingsFragmentNav>() }
    private val settingsUpdateViewModel by reactiveState {
        SettingsUpdateViewModel(scope)
    }

    override var announcementAccessibilityRes: Int? = null

    override var closingAnnouncementAccessibilityRes: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBar()

        if (args.isCovPassCheck) {
            announcementAccessibilityRes = R.string.accessibility_app_information_title_local_rules
        } else {
            announcementAccessibilityRes =
                R.string.accessibility_app_information_title_checking_rules_announce
            closingAnnouncementAccessibilityRes =
                R.string.accessibility_app_information_title_checking_rules_closing_announce
        }
        initCommonContent()
    }

    private fun initCommonContent() {
        val adapter = SettingsAdapter(this)
        adapter.attachTo(binding.settingsRecyclerview)
        autoRun {
            adapter.updateList(
                get(settingsUpdateViewModel.settingItems),
            )
        }
    }

    private fun setupActionBar() {
        attachToolbar(binding.settingsToolbar)
        (activity as? AppCompatActivity)?.run {
            supportActionBar?.run {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.back_arrow)
                setHomeActionContentDescription(R.string.accessibility_app_information_contact_label_back)
            }
            binding.settingsToolbar.setTitle(R.string.app_information_title_update)
        }
    }
}
