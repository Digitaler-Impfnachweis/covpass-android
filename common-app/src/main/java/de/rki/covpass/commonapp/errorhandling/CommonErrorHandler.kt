/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.errorhandling

import COSE.CoseException
import androidx.fragment.app.FragmentManager
import de.rki.covpass.base45.Base45DecodeException
import de.rki.covpass.logging.Lumber
import de.rki.covpass.commonapp.R
import de.rki.covpass.commonapp.dialog.DialogModel
import de.rki.covpass.commonapp.dialog.showDialog
import de.rki.covpass.sdk.android.cert.UnsupportedDgcVersionException

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
            is UnsupportedDgcVersionException -> DialogModel(
                titleRes = R.string.error_scan_present_data_is_not_supported_title,
                messageRes = R.string.error_scan_present_data_is_not_supported_message,
                positiveButtonTextRes = R.string.error_scan_present_data_is_not_supported_button_title,
                tag = TAG_ERROR_UNSUPPORTED_VERSION_OF_CERTIFICATE_DATA
            )
            is Base45DecodeException,
            is CoseException -> DialogModel(
                titleRes = R.string.error_scan_qrcode_cannot_be_parsed_title,
                messageRes = R.string.error_scan_qrcode_cannot_be_parsed_message,
                positiveButtonTextRes = R.string.error_scan_qrcode_cannot_be_parsed_button_title,
                tag = TAG_ERROR_UNREADABLE_CERTIFICATE
            )
            else -> if (isConnectionError(error)) {
                DialogModel(
                    titleRes = R.string.error_no_connection_to_server_title,
                    messageRes = R.string.error_no_connection_to_server_message,
                    positiveButtonTextRes = R.string.error_no_connection_to_server_button_title,
                    tag = TAG_ERROR_CONNECTION,
                )
            } else {
                DialogModel(
                    titleRes = R.string.error_standard_unexpected_title,
                    messageRes = R.string.error_standard_unexpected_message,
                    positiveButtonTextRes = R.string.error_standard_unexpected_button_title,
                    tag = TAG_ERROR_GENERAL,
                )
            }
        }

    public companion object {
        public const val TAG_ERROR_GENERAL: String = "error_general"
        public const val TAG_ERROR_CONNECTION: String = "error_connection"
        public const val TAG_ERROR_UNREADABLE_CERTIFICATE: String = "error_unreadable_certificate"
        public const val TAG_ERROR_UNSUPPORTED_VERSION_OF_CERTIFICATE_DATA: String =
            "error_unsupported_version_of_certificate_data"
    }
}
