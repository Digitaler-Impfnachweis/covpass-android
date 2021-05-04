package com.ibm.health.vaccination.app.vaccinee.errorhandling

import com.ibm.health.common.vaccination.app.errorhandling.CommonErrorHandler
import com.ibm.health.common.vaccination.app.dialog.DialogModel
import com.ibm.health.vaccination.sdk.android.cert.models.CertAlreadyExistsException
import com.ibm.health.vaccination.app.vaccinee.R

class ErrorHandler : CommonErrorHandler() {

    override fun getSpecificDialogModel(error: Throwable): DialogModel? =
        when (error) {
            is CertAlreadyExistsException -> DialogModel(
                titleRes = R.string.scan_duplicate_warning_dialog_title,
                messageRes = R.string.scan_duplicate_warning_dialog_message,
                positiveButtonTextRes = R.string.scan_duplicate_warning_dialog_positive,
                negativeButtonTextRes = R.string.scan_duplicate_warning_dialog_negative,
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
