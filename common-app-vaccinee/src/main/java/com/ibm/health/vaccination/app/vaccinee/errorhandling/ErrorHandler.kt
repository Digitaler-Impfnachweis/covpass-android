/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.vaccination.app.vaccinee.errorhandling

import com.ibm.health.common.vaccination.app.dialog.DialogModel
import com.ibm.health.common.vaccination.app.errorhandling.CommonErrorHandler
import com.ibm.health.vaccination.app.vaccinee.R
import com.ibm.health.vaccination.sdk.android.cert.BadCoseSignatureException
import com.ibm.health.vaccination.sdk.android.cert.ExpiredCwtException
import com.ibm.health.vaccination.sdk.android.cert.models.CertAlreadyExistsException

/**
 * Vaccinee specific Error handling. Overrides the abstract functions from [CommonErrorHandler].
 */
internal class ErrorHandler : CommonErrorHandler() {

    override fun getSpecificDialogModel(error: Throwable): DialogModel? =
        when (error) {
            is CertAlreadyExistsException -> DialogModel(
                titleRes = R.string.duplicate_certificate_dialog_header,
                messageRes = R.string.duplicate_certificate_dialog_message,
                positiveButtonTextRes = R.string.duplicate_certificate_dialog_button_title,
                tag = TAG_ERROR_DUPLICATE_CERTIFICATE
            )
            is ExpiredCwtException -> DialogModel(
                titleRes = R.string.vaccination_certificate_expired_title,
                messageRes = R.string.error_vaccination_certificate_expired_message,
                positiveButtonTextRes = R.string.error_vaccination_certificate_expired_title,
                tag = TAG_ERROR_EXPIRED_CERTIFICATE
            )
            is BadCoseSignatureException -> DialogModel(
                titleRes = R.string.error_scan_qrcode_without_seal_title,
                messageRes = R.string.error_scan_qrcode_without_seal_message,
                positiveButtonTextRes = R.string.error_scan_qrcode_without_seal_button_title,
                tag = TAG_ERROR_BAD_CERTIFICATE_SIGNATURE
            )
            else -> null
        }

    companion object {
        const val TAG_ERROR_DUPLICATE_CERTIFICATE: String = "error_duplicate_certificate"
        const val TAG_ERROR_EXPIRED_CERTIFICATE: String = "error_expired_certificate"
        const val TAG_ERROR_BAD_CERTIFICATE_SIGNATURE: String = "error_bad_signature_certificate"
    }
}
