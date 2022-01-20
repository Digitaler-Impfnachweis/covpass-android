/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.errorhandling

import COSE.CoseException
import androidx.fragment.app.FragmentManager
import com.ibm.health.common.android.utils.getString
import com.upokecenter.cbor.CBORException
import de.rki.covpass.base45.Base45DecodeException
import de.rki.covpass.commonapp.R
import de.rki.covpass.commonapp.dialog.DialogModel
import de.rki.covpass.commonapp.dialog.showDialog
import de.rki.covpass.logging.Lumber
import de.rki.covpass.sdk.cert.BadCoseSignatureException
import de.rki.covpass.sdk.cert.ExpiredCwtException
import de.rki.covpass.sdk.cert.NoMatchingExtendedKeyUsageException

/**
 * Common abstract base class for the app-specific error handlers. This one covers handling of errors that can occur in
 * both apps. Errors that only occur in one app should be handled inside the app-specific error handlers.
 */
public abstract class CommonErrorHandler {

    public fun handleError(error: Throwable, fragmentManager: FragmentManager) {
        Lumber.e(error)
        val dialogModel = getSpecificDialogModel(error) ?: getCommonDialogModel(error)
        showDialog(dialogModel, fragmentManager)
    }

    protected abstract fun getSpecificDialogModel(error: Throwable): DialogModel?

    private fun getCommonDialogModel(error: Throwable): DialogModel =
        when (error) {
            is Base45DecodeException,
            is CoseException, is CBORException,
            -> DialogModel(
                titleRes = R.string.error_scan_qrcode_cannot_be_parsed_title,
                messageString = "${getString(R.string.error_scan_qrcode_cannot_be_parsed_message)} (Error " +
                    "${getDecodingExceptionType(error)})",
                positiveButtonTextRes = R.string.error_scan_qrcode_cannot_be_parsed_button_title,
                tag = TAG_ERROR_UNREADABLE_CERTIFICATE
            )
            else -> if (isConnectionError(error)) {
                DialogModel(
                    titleRes = R.string.error_no_connection_to_server_title,
                    messageString = getString(R.string.error_no_connection_to_server_message) +
                        " (Error $ERROR_CODE_CONNECTION_ERROR)",
                    positiveButtonTextRes = R.string.error_no_connection_to_server_button_title,
                    tag = TAG_ERROR_CONNECTION,
                )
            } else {
                DialogModel(
                    titleRes = R.string.error_standard_unexpected_title,
                    messageString = getString(R.string.error_standard_unexpected_message) +
                        " (Error ${getGeneralErrorCode(error)})",
                    positiveButtonTextRes = R.string.error_standard_unexpected_button_title,
                    tag = TAG_ERROR_GENERAL,
                )
            }
        }

    private fun getDecodingExceptionType(error: Throwable) = when (error) {
        is Base45DecodeException -> ERROR_CODE_BASE45DECODING
        is CoseException -> ERROR_CODE_COSE_DECODING
        else -> ERROR_CODE_CBOR_DECODING
    }

    private fun getGeneralErrorCode(error: Throwable) = when (error) {
        is ExpiredCwtException -> ERROR_CODE_EXPIRED_CERTIFICATE
        is BadCoseSignatureException -> ERROR_CODE_COSE_BAD_SIGNATURE
        is NoMatchingExtendedKeyUsageException -> ERROR_CODE_ILLEGAL_KEY
        else -> ERROR_CODE_GENERAL
    }

    public companion object {
        public const val TAG_ERROR_GENERAL: String = "error_general"
        public const val TAG_ERROR_CONNECTION: String = "error_connection"
        public const val TAG_ERROR_UNREADABLE_CERTIFICATE: String = "error_unreadable_certificate"

        // Error codes
        public const val ERROR_CODE_CONNECTION_ERROR: Int = 105
        public const val ERROR_CODE_BASE45DECODING: Int = 301
        public const val ERROR_CODE_COSE_DECODING: Int = 404
        public const val ERROR_CODE_COSE_BAD_SIGNATURE: Int = 405
        public const val ERROR_CODE_ILLEGAL_KEY: Int = 413
        public const val ERROR_CODE_CBOR_DECODING: Int = 414
        public const val ERROR_CODE_EXPIRED_CERTIFICATE: Int = 422
        public const val ERROR_CODE_GENERAL: Int = 902
        public const val ERROR_CODE_QR_CODE_DUPLICATED: Int = 201
    }
}
