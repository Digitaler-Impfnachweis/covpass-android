package com.ibm.health.vaccination.app.vaccinee.errorhandling

import com.ibm.health.common.vaccination.app.dialog.DialogModel
import com.ibm.health.common.vaccination.app.errorhandling.CommonErrorHandler
import com.ibm.health.vaccination.app.vaccinee.R
import com.ibm.health.vaccination.sdk.android.cert.models.CertAlreadyExistsException

internal class ErrorHandler : CommonErrorHandler() {

    override fun getSpecificDialogModel(error: Throwable): DialogModel? =
        when (error) {
            is CertAlreadyExistsException -> DialogModel(
                titleRes = R.string.duplicate_certificate_dialog_header,
                messageRes = R.string.duplicate_certificate_dialog_message,
                positiveButtonTextRes = R.string.duplicate_certificate_dialog_positive_button_text,
                negativeButtonTextRes = R.string.duplicate_certificate_dialog_negative_button_text,
                isCancelable = false,
                tag = TAG_ERROR_DUPLICATE_CERTIFICATE
            )
            // TODO add more specific exceptions later on
            else -> null
        }

    companion object {
        const val TAG_ERROR_DUPLICATE_CERTIFICATE: String = "error_duplicate_certificate"
    }
}
