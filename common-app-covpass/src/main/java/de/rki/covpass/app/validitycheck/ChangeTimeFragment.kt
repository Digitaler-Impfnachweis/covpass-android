/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.validitycheck

import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.ChangeTimePopupContentBinding
import de.rki.covpass.commonapp.BaseBottomSheet
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Interface to communicate events from [ChangeDateFragment] back to other fragments.
 */
internal interface ChangeDateTimeCallback {
    fun updateDate(dateTime: LocalDateTime)
}

@Parcelize
internal class ChangeTimeFragmentNav(var dateTime: LocalDateTime) : FragmentNav(ChangeTimeFragment::class)

/**
 * Fragment to change date for the validity
 */
internal class ChangeTimeFragment : BaseBottomSheet() {

    private val args: ChangeTimeFragmentNav by lazy { getArgs() }
    val binding by viewBinding(ChangeTimePopupContentBinding::inflate)

    override val announcementAccessibilityRes: Int =
        R.string.accessibility_certificate_check_validity_selection_time_announce
    override val closingAnnouncementAccessibilityRes: Int =
        R.string.accessibility_certificate_check_validity_selection_time_closing_announce

    private val timePicker by lazy { binding.timePicker }
    private val hourPicker: NumberPicker by lazy {
        timePicker.findViewById(Resources.getSystem().getIdentifier("hour", "id", "android"))
    }
    private val minutePicker: NumberPicker by lazy {
        timePicker.findViewById(Resources.getSystem().getIdentifier("minute", "id", "android"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetBinding.bottomSheetTitle.setText(R.string.certificate_check_validity_selection_time_title)
        bottomSheetBinding.bottomSheetActionButton.setText(
            R.string.certificate_check_validity_selection_time_action_button,
        )

        startPickers()
    }

    private fun startPickers() {
        // Set time in time picker
        binding.timePicker.setIs24HourView(true)
        binding.timePicker.hour = args.dateTime.hour
        binding.timePicker.minute = args.dateTime.minute

        if (args.dateTime.toLocalDate().isEqual(LocalDate.now())) {
            hourPicker.minValue = LocalDateTime.now().hour
            // Hour picker listener to update minimum minute depending on the hour
            hourPicker.setOnValueChangedListener { picker, _, newVal ->
                picker.minValue = LocalDateTime.now().hour
                if (newVal <= LocalDateTime.now().hour) {
                    minutePicker.minValue = LocalDateTime.now().minute
                } else {
                    minutePicker.minValue = 0
                }
            }
            if (args.dateTime <= LocalDateTime.now()) {
                minutePicker.minValue = LocalDateTime.now().minute
            }
        }
        // remove listener to not allow hour change by changing minutes
        minutePicker.setOnValueChangedListener(null)
    }

    override fun onActionButtonClicked() {
        val newDateTime = args.dateTime.withHour(binding.timePicker.hour).withMinute(binding.timePicker.minute)
        findNavigator().popUntil<ChangeDateTimeCallback>()?.updateDate(newDateTime)
    }
}
