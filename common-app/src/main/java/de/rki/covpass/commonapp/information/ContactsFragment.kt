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
import de.rki.covpass.commonapp.databinding.ContactsBinding
import de.rki.covpass.commonapp.utils.stripUnderlines
import kotlinx.parcelize.Parcelize

@Parcelize
public class ContactsFragmentNav : FragmentNav(ContactsFragment::class)

/**
 * Displays the contacts
 */
public class ContactsFragment : BaseFragment() {

    private val binding by viewBinding(ContactsBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBar()
        binding.titleTechnicalContacts.setText(R.string.app_information_message_contact_support)
        binding.textTechnicalEmail.apply {
            text = getSpanned(R.string.app_information_message_contact_support_mail)
            movementMethod = LinkMovementMethod.getInstance()
            stripUnderlines()
        }
        binding.textTechnicalPhone.apply {
            text = getSpanned(R.string.app_information_message_contact_support_phone)
            movementMethod = LinkMovementMethod.getInstance()
            stripUnderlines()
        }
        binding.titleGeneralContacts.setText(R.string.app_information_message_contact_info)
        binding.textGeneralEmail.apply {
            text = getSpanned(R.string.app_information_message_contact_info_mail)
            movementMethod = LinkMovementMethod.getInstance()
            stripUnderlines()
        }
    }

    private fun setupActionBar() {
        attachToolbar(binding.informationToolbar)
        (activity as? AppCompatActivity)?.run {
            supportActionBar?.run {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.back_arrow)
            }
            binding.informationToolbar.setTitle(R.string.app_information_title_contact)
        }
    }
}
