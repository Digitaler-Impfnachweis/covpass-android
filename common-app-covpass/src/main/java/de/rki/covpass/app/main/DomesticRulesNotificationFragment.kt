/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.main

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.DomesticRulesNotificationBinding
import de.rki.covpass.commonapp.storage.CheckContextRepository.Companion.CURRENT_CHECK_CONTEXT_NOTIFICATION_VERSION
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.commonapp.dependencies.commonDeps
import kotlinx.parcelize.Parcelize

internal interface DomesticRulesNotificationCallback {
    fun onDomesticRulesNotificationFinish()
}

@Parcelize
public class DomesticRulesNotificationFragmentNav : FragmentNav(DomesticRulesNotificationFragment::class)

public class DomesticRulesNotificationFragment : BaseBottomSheet() {

    private val binding by viewBinding(DomesticRulesNotificationBinding::inflate)
    override val buttonTextRes: Int = R.string.dialog_local_rulecheck_button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startView()
    }

    private fun startView() {
        bottomSheetBinding.bottomSheetHeader.isVisible = false
        bottomSheetBinding.bottomSheetClose.isVisible = false
        binding.domesticRulesNotificationTitle.setText(R.string.dialog_local_rulecheck_title)
        binding.domesticRulesNotificationNoteTitle.setText(R.string.dialog_local_rulecheck_subtitle)
        binding.domesticRulesNotificationNote.setText(R.string.dialog_local_rulecheck_copy)
    }

    override fun onActionButtonClicked() {
        launchWhenStarted {
            commonDeps.checkContextRepository.checkContextNotificationVersionShown.set(
                CURRENT_CHECK_CONTEXT_NOTIFICATION_VERSION
            )
            findNavigator().pop()
        }
    }
}
