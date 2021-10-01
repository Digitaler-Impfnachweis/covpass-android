/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.detail.adapter

import de.rki.covpass.sdk.cert.models.CertValidationResult
import de.rki.covpass.sdk.cert.models.DGCEntryType

/**
 * Sealed class which represents the possible data sources for the [DetailAdapter]
 */
public sealed class DetailItem {

    public class Name(
        public val fullname: String,
    ) : DetailItem()

    public class Widget(
        public val title: String,
        public val statusIcon: Int,
        public val message: String,
        public val buttonText: String,
        public val isExpiredOrInvalid: Boolean = false,
    ) : DetailItem()

    public class Header(
        public val title: String,
        public val titleAccessibleDescription: String
    ) : DetailItem()

    public class Personal(
        public val title: String,
        public val titleAccessibleDescription: String,
        public val subtitle: String?,
    ) : DetailItem()

    public class Infobox(
        public val title: String,
        public val description: String
    ) : DetailItem()

    public class Certificate(
        public val id: String,
        public val type: DGCEntryType,
        public val title: String,
        public val subtitle: String,
        public val date: String,
        public val isActual: Boolean = false,
        public val certStatus: CertValidationResult = CertValidationResult.Valid,
    ) : DetailItem()

    public class Notification(
        public val titleRes: Int,
        public val description: String,
        public val ruleId: String,
        public val iconBackgroundRes: Int?,
        public val iconTextRes: Int?,
    ) : DetailItem()
}
