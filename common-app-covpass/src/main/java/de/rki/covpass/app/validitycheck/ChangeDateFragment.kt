/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.validitycheck

import android.os.Bundle
import android.view.View
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.ChangeDatePopupContentBinding
import de.rki.covpass.commonapp.BaseBottomSheet
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.time.LocalDateTime

@Parcelize
internal class ChangeDateFragmentNav(var dateTime: LocalDateTime) : FragmentNav(ChangeDateFragment::class)

/**
 * Fragment to change date for the validity
 */
internal class ChangeDateFragment : BaseBottomSheet() {

    private val args: ChangeDateFragmentNav by lazy { getArgs() }
    val binding by viewBinding(ChangeDatePopupContentBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.bottomSheetTitle.setText(R.string.certificate_check_validity_selection_date_title)
        bottomSheetBinding.bottomSheetActionButton.setText(
            R.string.certificate_check_validity_selection_date_action_button
        )
        val dateTime = args.dateTime
        binding.datePicker.minDate = Instant.now().toEpochMilli()
        binding.datePicker.updateDate(dateTime.year, dateTime.monthValue - 1, dateTime.dayOfMonth)
    }

    override fun onActionButtonClicked() {
        findNavigator().push(
            ChangeTimeFragmentNav(
                args.dateTime.withYear(binding.datePicker.year)
                    .withMonth(binding.datePicker.month + 1)
                    .withDayOfMonth(binding.datePicker.dayOfMonth)
            )
        )
    }
}
