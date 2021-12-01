/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.ticketing

import com.ibm.health.common.annotations.Abort
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.app.R
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.commonapp.dialog.DialogAction
import de.rki.covpass.commonapp.dialog.DialogListener
import de.rki.covpass.commonapp.dialog.DialogModel
import de.rki.covpass.commonapp.dialog.showDialog

public abstract class BaseTicketingFragment : BaseBottomSheet(), DialogListener {

    public open val cancelProcess: Boolean = false

    private fun cancelTicketing() {
        // TODO cancellation request will be added after
//        if (cancelProcess) {
//        }
        findNavigator().popAll()
    }

    override fun onCloseButtonClicked() {
        val dialogModel = DialogModel(
            titleRes = R.string.cancellation_share_certificate_title,
            positiveButtonTextRes = R.string.cancellation_share_certificate_action_button_yes,
            negativeButtonTextRes = R.string.cancellation_share_certificate_action_button_no,
            tag = CANCEL_TICKETING,
        )
        showDialog(dialogModel, childFragmentManager)
    }

    override fun onClickOutside() {
        onCloseButtonClicked()
    }

    override fun onBackPressed(): Abortable {
        onCloseButtonClicked()
        return Abort
    }

    override fun onDialogAction(tag: String, action: DialogAction) {
        if (action == DialogAction.POSITIVE) {
            cancelTicketing()
        }
    }

    public companion object {
        public const val CANCEL_TICKETING: String = "cancel_ticketing"
    }
}
