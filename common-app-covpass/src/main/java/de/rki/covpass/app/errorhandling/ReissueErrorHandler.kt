/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.errorhandling

import com.ibm.health.common.android.utils.getString
import de.rki.covpass.app.R
import de.rki.covpass.commonapp.dialog.DialogModel
import de.rki.covpass.commonapp.errorhandling.CommonErrorHandler
import de.rki.covpass.sdk.reissuing.ReissuingInternalServerError
import de.rki.covpass.sdk.reissuing.ReissuingTooManyRequests

/**
 * Covpass specific Error handling. Overrides the abstract functions from [CommonErrorHandler].
 */
internal class ReissueErrorHandler : CommonErrorHandler() {

    override fun getSpecificDialogModel(error: Throwable): DialogModel? =
        when (error) {
            is ReissuingTooManyRequests -> DialogModel(
                titleRes = R.string.certificate_renewal_error_title,
                messageString =
                getString(R.string.certificate_renewal_error_copy, ERROR_TOO_MANY_REQUESTS),
                positiveButtonTextRes = R.string.certificate_renewal_error_button_primary,
                negativeButtonTextRes = R.string.certificate_renewal_error_button_secondary,
                tag = TAG_ERROR_REISSUING_TOO_MANY_REQUESTS,
            )
            is ReissuingInternalServerError -> DialogModel(
                titleRes = R.string.certificate_renewal_error_title,
                messageString =
                getString(R.string.certificate_renewal_error_copy, ERROR_INTERNAL_SERVER_ERROR),
                positiveButtonTextRes = R.string.certificate_renewal_error_button_primary,
                negativeButtonTextRes = R.string.certificate_renewal_error_button_secondary,
                tag = TAG_ERROR_REISSUING_INTERNAL_SERVER_ERROR,
            )
            else -> DialogModel(
                titleRes = R.string.certificate_renewal_error_title,
                messageString =
                getString(R.string.certificate_renewal_error_copy, ERROR_INTERNAL_ERROR),
                positiveButtonTextRes = R.string.certificate_renewal_error_button_primary,
                negativeButtonTextRes = R.string.certificate_renewal_error_button_secondary,
                tag = TAG_ERROR_REISSUING_INTERNAL_ERROR,
            )
        }

    companion object {
        const val TAG_ERROR_REISSUING_TOO_MANY_REQUESTS: String = "error_reissuing_too_many_requests"
        const val TAG_ERROR_REISSUING_INTERNAL_SERVER_ERROR: String = "error_reissuing_internal_server_error"
        const val TAG_ERROR_REISSUING_INTERNAL_ERROR: String = "error_reissuing_internal_error"

        // Error codes
        const val ERROR_TOO_MANY_REQUESTS: String = "R429"
        const val ERROR_INTERNAL_SERVER_ERROR: String = "R500"
        const val ERROR_INTERNAL_ERROR: String = "R000"
    }
}
