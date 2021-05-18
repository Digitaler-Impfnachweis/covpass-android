package com.ibm.health.vaccination.app.vaccinee.errorhandling

import COSE.CoseException
import com.ibm.health.common.base45.Base45DecodeException
import com.ibm.health.common.vaccination.app.dialog.DialogModel
import com.ibm.health.common.vaccination.app.errorhandling.CommonErrorHandler
import com.ibm.health.vaccination.app.vaccinee.R
import com.ibm.health.vaccination.sdk.android.cert.BadCoseSignatureException
import com.ibm.health.vaccination.sdk.android.cert.ExpiredCwtException
import com.ibm.health.vaccination.sdk.android.cert.UnsupportedDgcVersionException
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
            is UnsupportedDgcVersionException -> DialogModel(
                titleRes = R.string.error_scan_present_data_is_not_supported_title,
                messageRes = R.string.error_scan_present_data_is_not_supported_message,
                positiveButtonTextRes = R.string.error_scan_present_data_is_not_supported_button_title,
                tag = TAG_ERROR_UNSUPPORTED_VERSION_OF_CERTIFICATE_DATA
            )
            is BadCoseSignatureException -> DialogModel(
                titleRes = R.string.error_scan_qrcode_without_seal_title,
                messageRes = R.string.error_scan_qrcode_without_seal_message,
                positiveButtonTextRes = R.string.error_scan_qrcode_without_seal_button_title,
                tag = TAG_ERROR_BAD_CERTIFICATE_SIGNATURE
            )
            is Base45DecodeException,
            is CoseException,
            -> DialogModel(
                titleRes = R.string.error_scan_qrcode_cannot_be_parsed_title,
                messageRes = R.string.error_scan_qrcode_cannot_be_parsed_message,
                positiveButtonTextRes = R.string.error_scan_qrcode_cannot_be_parsed_button_title,
                tag = TAG_ERROR_UNREADABLE_CERTIFICATE
            )
            else -> null
        }

    companion object {
        const val TAG_ERROR_DUPLICATE_CERTIFICATE: String = "error_duplicate_certificate"
        const val TAG_ERROR_EXPIRED_CERTIFICATE: String = "error_expired_certificate"
        const val TAG_ERROR_BAD_CERTIFICATE_SIGNATURE: String = "error_bad_signature_certificate"
        const val TAG_ERROR_UNREADABLE_CERTIFICATE: String = "error_unreadable_certificate"
        const val TAG_ERROR_UNSUPPORTED_VERSION_OF_CERTIFICATE_DATA: String =
            "error_unsupported_version_of_certificate_data"
    }
}
