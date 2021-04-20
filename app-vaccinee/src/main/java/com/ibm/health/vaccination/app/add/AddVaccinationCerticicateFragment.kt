package com.ibm.health.vaccination.app.add

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ScrollView
import android.widget.TextView
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.OverlayNavigation
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.vaccination.app.BaseFragment
import com.ibm.health.common.vaccination.app.databinding.BottomSheetViewBinding
import com.ibm.health.common.vaccination.app.extensions.stripUnderlines
import com.ibm.health.vaccination.app.vaccinee.R
import com.ibm.health.vaccination.app.vaccinee.main.MainActivity
import kotlinx.parcelize.Parcelize

@Parcelize
class AddVaccinationCertificateFragmentNav : FragmentNav(AddVaccinationCertificateFragment::class)

class AddVaccinationCertificateFragment : BaseFragment(), OverlayNavigation {

    private val binding by viewBinding(BottomSheetViewBinding::inflate)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)?.apply {
            val contentView = inflater.inflate(
                R.layout.add_vaccination_cert_popup_content,
                container,
                false
            )
            findViewById<ScrollView>(R.id.bottom_sheet_content).addView(contentView)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bottomSheetContent.findViewById<TextView>(R.id.add_vaccination_cert_faq).run {
            movementMethod = LinkMovementMethod.getInstance()
            stripUnderlines()
        }
        binding.bottomSheetTitle.text = getString(R.string.add_vaccination_cert_title)
        binding.bottomSheetClose.setOnClickListener { findNavigator().pop() }
        binding.bottomSheetActionButton.setText(R.string.add_vaccination_cert_action_button_text)
        binding.bottomSheetActionButton.setOnClickListener {
            (requireActivity() as? MainActivity)?.launchScanner()
            findNavigator().pop()
        }

        /**
         * Adds padding to the content view equal to bottomSheetBottomView.height
         */
        binding.bottomSheetBottomView.apply {
            viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val bottomViewHeight = height
                    binding.bottomSheetContent.getChildAt(0)?.run {
                        setPadding(paddingLeft, paddingTop, paddingRight, bottomViewHeight)
                    }
                }
            })
        }
    }
}
