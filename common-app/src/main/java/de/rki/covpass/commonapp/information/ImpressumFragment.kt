package de.rki.covpass.commonapp.information

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ibm.health.common.android.utils.attachToolbar
import com.ibm.health.common.android.utils.getSpanned
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.R
import de.rki.covpass.commonapp.databinding.ImpressumBinding
import de.rki.covpass.commonapp.utils.setExternalLinkImage
import kotlinx.parcelize.Parcelize

@Parcelize
public class ImpressumFragmentNav : FragmentNav(ImpressumFragment::class)

public class ImpressumFragment : BaseFragment() {

    private val binding by viewBinding(ImpressumBinding::inflate)

    override val announcementAccessibilityRes: Int =
        R.string.accessibility_app_information_title_legal_information_announce
    override val closingAnnouncementAccessibilityRes: Int =
        R.string.accessibility_app_information_title_legal_information_announce_closing

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBar()
        binding.impressumEmail.apply {
            text = getSpanned(R.string.app_imprint_contact_mail)
            movementMethod = LinkMovementMethod.getInstance()
            setExternalLinkImage()
        }
        binding.impressumForm.apply {
            text = getSpanned(R.string.app_imprint_contact_form)
            movementMethod = LinkMovementMethod.getInstance()
            setExternalLinkImage()
        }
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
            binding.informationToolbar.setTitle(R.string.app_information_title_company_details)
        }
    }
}
