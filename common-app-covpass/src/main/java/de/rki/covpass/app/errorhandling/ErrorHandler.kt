/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.errorhandling

import com.ibm.health.common.android.utils.getString
import de.rki.covpass.app.R
import de.rki.covpass.app.ticketing.AccessTokenDecodingException
import de.rki.covpass.app.ticketing.InvalidValidationServiceProvider
import de.rki.covpass.app.ticketing.NoValidationServiceListed
import de.rki.covpass.commonapp.dialog.DialogModel
import de.rki.covpass.commonapp.errorhandling.CommonErrorHandler
import de.rki.covpass.sdk.cert.BadCoseSignatureException
import de.rki.covpass.sdk.cert.NoMatchingExtendedKeyUsageException
import de.rki.covpass.sdk.cert.models.CertAlreadyExistsException
import de.rki.covpass.sdk.cert.models.CertTestPositiveException
import de.rki.covpass.sdk.ticketing.*

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
            is BadCoseSignatureException -> DialogModel(
                titleRes = R.string.error_scan_qrcode_without_seal_title,
                messageString = getString(R.string.error_scan_qrcode_without_seal_message) +
                    " (Error $ERROR_CODE_CERTIFICATE_BAD_SIGNATURE)",
                positiveButtonTextRes = R.string.error_scan_qrcode_without_seal_button_title,
                tag = TAG_ERROR_BAD_CERTIFICATE_SIGNATURE
            )
            is NoMatchingExtendedKeyUsageException -> DialogModel(
                titleRes = R.string.error_scan_qrcode_cannot_be_parsed_title,
                messageString = getString(R.string.error_scan_qrcode_cannot_be_parsed_message) +
                    " (Error $ERROR_CODE_ILLEGAL_KEY_USAGE)",
                positiveButtonTextRes = R.string.error_scan_qrcode_cannot_be_parsed_button_title,
                tag = TAG_CODE_ILLEGAL_KEY_USAGE
            )
            is IdentityDocumentRequestException -> DialogModel(
                titleRes = R.string.error_share_certificate_provider_not_verified_title,
                messageString = getString(R.string.error_share_certificate_provider_not_verified_message) +
                    " (Error $ERROR_CODE_CONNECTION_ERROR)",
                positiveButtonTextRes = R.string.error_share_certificate_provider_not_verified_action_button,
                tag = TAG_ERROR_IDENTITY_DOCUMENT_REQUEST_FAILED
            )
            is AccessCredentialServiceEndpointNotFoundException,
            is NoValidationServiceListed -> DialogModel(
                titleRes = R.string.error_share_certificate_validation_partner_not_verified_title2,
                messageString = getString(R.string.error_share_certificate_validation_partner_not_verified_message2) +
                    " (Error $ERROR_NO_VALIDATION_SERVICE_LISTED)",
                positiveButtonTextRes = R.string.error_share_certificate_validation_partner_not_verified_action_button2,
                tag = TAG_ERROR_NO_VALIDATION_SERVICE_LISTED
            )
            is InvalidValidationServiceProvider -> DialogModel(
                titleRes = R.string.error_share_certificate_validation_partner_not_verified_title,
                messageString = getString(R.string.error_share_certificate_validation_partner_not_verified_message) +
                    " (Error $ERROR_INVALID_VALIDATION_SERVICE_PROVIDER)",
                positiveButtonTextRes = R.string.error_share_certificate_validation_partner_not_verified_action_button,
                tag = TAG_ERROR_INVALID_VALIDATION_SERVICE_PROVIDER
            )
            is IdentityDocumentValidationRequestException -> DialogModel(
                titleRes = R.string.error_share_certificate_validation_partner_not_verified_title2,
                messageString = getString(R.string.error_share_certificate_validation_partner_not_verified_message2) +
                    " (Error $ERROR_CODE_CONNECTION_ERROR)",
                positiveButtonTextRes = R.string.error_share_certificate_validation_partner_not_verified_action_button2,
                tag = TAG_ERROR_IDENTITY_DOCUMENT_VALIDATION_REQUEST
            )
            is AccessTokenRequestException -> DialogModel(
                titleRes = R.string.error_share_certificate_access_token_not_retrieved_title,
                messageString = getString(R.string.error_share_certificate_access_token_not_retrieved_message) +
                    " (Error $ERROR_CODE_CONNECTION_ERROR)",
                positiveButtonTextRes = R.string.error_share_certificate_validation_partner_not_verified_action_button2,
                tag = TAG_ERROR_ACCESS_TOKEN_REQUEST
            )
            is AccessTokenDecodingException -> DialogModel(
                titleRes = R.string.error_share_certificate_access_token_not_processed_title,
                messageString = getString(R.string.error_share_certificate_access_token_not_processed_message) +
                    " (Error $ERROR_CODE_CONNECTION_ERROR)",
                positiveButtonTextRes = R.string.error_share_certificate_access_token_not_processed_action_button,
                tag = TAG_ERROR_ACCESS_TOKEN_DECODING
            )
            is TicketingCertificatePreparationException -> DialogModel(
                titleRes = R.string.error_share_certificate_no_verification_possible_title,
                messageString = getString(R.string.error_share_certificate_no_verification_possible_message) +
                    " (Error $ERROR_CODE_CONNECTION_ERROR)",
                positiveButtonTextRes = R.string.error_share_certificate_no_verification_possible_action_button,
                tag = TAG_ERROR_TICKETING_CERTIFICATE_PREPARATION
            )
            is TicketingSendingCertificateException -> DialogModel(
                titleRes = R.string.error_share_certificate_no_verification_submission_possible_title,
                messageString =
                getString(R.string.error_share_certificate_no_verification_submission_possible_message) +
                    " (Error $ERROR_CODE_CONNECTION_ERROR)",
                positiveButtonTextRes =
                R.string.error_share_certificate_no_verification_submission_possible_action_button,
                tag = TAG_ERROR_TICKETING_SENDING_CERTIFICATE
            )
            else -> null
        }

    companion object {
        const val TAG_ERROR_DUPLICATE_CERTIFICATE: String = "error_duplicate_certificate"
        const val TAG_ERROR_POSITIVE_CERTIFICATE: String = "error_positive_certificate"
        const val TAG_ERROR_BAD_CERTIFICATE_SIGNATURE: String = "error_bad_signature_certificate"
        const val TAG_CODE_ILLEGAL_KEY_USAGE: String = "error_illegal_key_usage"
        const val TAG_ERROR_IDENTITY_DOCUMENT_REQUEST_FAILED: String = "error_identity_document_request_failed"
        const val TAG_ERROR_ACCESS_CREDENTIAL_SERVICE_ENDPOINT_NOT_FOUND: String =
            "error_access_credential_service_endpoint_not_found"
        const val TAG_ERROR_IDENTITY_DOCUMENT_VALIDATION_REQUEST: String = "error_identity_document_validation_request"
        const val TAG_ERROR_ACCESS_TOKEN_REQUEST: String = "error_access_token_request"
        const val TAG_ERROR_ACCESS_TOKEN_DECODING: String = "error_access_token_decoding"
        const val TAG_ERROR_TICKETING_CERTIFICATE_PREPARATION: String = "error_ticketing_certificate_preparation"
        const val TAG_ERROR_TICKETING_SENDING_CERTIFICATE: String = "error_ticketing_sending_certificate"
        const val TAG_ERROR_INVALID_VALIDATION_SERVICE_PROVIDER: String = "error_invalid_validation_service_provider"
        const val TAG_ERROR_NO_VALIDATION_SERVICE_LISTED: String = "error_no_validation_service_listed"

        // Error codes
        const val ERROR_NO_VALIDATION_SERVICE_LISTED = 103
        const val ERROR_INVALID_VALIDATION_SERVICE_PROVIDER = 106
        const val ERROR_CODE_QR_CODE_DUPLICATED: Int = 201
        const val ERROR_CODE_CERTIFICATE_BAD_SIGNATURE: Int = 412
        const val ERROR_CODE_CERTIFICATE_POSITIVE_RESULT: Int = 421
        const val ERROR_CODE_ILLEGAL_KEY_USAGE: Int =
            413 // Entity created a certificate which they are not allowed to create
    }
}
