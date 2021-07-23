/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.errorhandling

import com.ibm.health.common.android.utils.getString
import de.rki.covpass.app.R
import de.rki.covpass.sdk.cert.models.CertAlreadyExistsException
import de.rki.covpass.sdk.cert.models.CertTestPositiveException
import de.rki.covpass.commonapp.dialog.DialogModel
import de.rki.covpass.commonapp.errorhandling.CommonErrorHandler
import de.rki.covpass.sdk.cert.BadCoseSignatureException
import de.rki.covpass.sdk.cert.NoMatchingExtendedKeyUsageException

/**
 * Covpass specific Error handling. Overrides the abstract functions from [CommonErrorHandler].
 */
internal class ErrorHandler : CommonErrorHandler() {

    override fun getSpecificDialogModel(error: Throwable): DialogModel? =
        when (error) {
            is CertAlreadyExistsException -> DialogModel(
                titleRes = R.string.duplicate_certificate_dialog_header,
                messageString = getString(R.string.duplicate_certificate_dialog_message) +
                    " (Error $ERROR_CODE_QR_CODE_DUPLICATED)",
                positiveButtonTextRes = R.string.duplicate_certificate_dialog_button_title,
                tag = TAG_ERROR_DUPLICATE_CERTIFICATE
            )
            is CertTestPositiveException -> DialogModel(
                titleRes = R.string.error_test_certificate_not_valid_title,
                messageString = getString(R.string.error_test_certificate_not_valid_message) +
                    " (Error $ERROR_CODE_CERTIFICATE_POSITIVE_RESULT)",
                positiveButtonTextRes = R.string.error_test_certificate_not_valid_button_title,
                tag = TAG_ERROR_POSITIVE_CERTIFICATE
            )
            is BadCoseSignatureException,
            is NoMatchingExtendedKeyUsageException -> DialogModel(
                titleRes = R.string.error_scan_qrcode_without_seal_title,
                messageString = getString(R.string.error_scan_qrcode_without_seal_message) +
                    " (Error $ERROR_CODE_CERTIFICATE_BAD_SIGNATURE)",
                positiveButtonTextRes = R.string.error_scan_qrcode_without_seal_button_title,
                tag = TAG_ERROR_BAD_CERTIFICATE_SIGNATURE
            )
            else -> null
        }

    companion object {
        const val TAG_ERROR_DUPLICATE_CERTIFICATE: String = "error_duplicate_certificate"
        const val TAG_ERROR_POSITIVE_CERTIFICATE: String = "error_positive_certificate"
        const val TAG_ERROR_BAD_CERTIFICATE_SIGNATURE: String = "error_bad_signature_certificate"

        // Error codes
        const val ERROR_CODE_QR_CODE_DUPLICATED: Int = 201
        const val ERROR_CODE_CERTIFICATE_BAD_SIGNATURE: Int = 413
        const val ERROR_CODE_CERTIFICATE_POSITIVE_RESULT: Int = 421
    }
}
