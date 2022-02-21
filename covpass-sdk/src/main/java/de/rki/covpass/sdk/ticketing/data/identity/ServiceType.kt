/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.ticketing.data.identity

public enum class ServiceType(public val type: String) {
    VALIDATION_SERVICE("ValidationService"),
    ACCESS_TOKEN_SERVICE("AccessTokenService"),
    CANCELLATION_SERVICE("CancellationService")
}
