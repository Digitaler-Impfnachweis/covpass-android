/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.detail.adapter

import de.rki.covpass.commonapp.utils.CertificateType

/**
 * Sealed class which represents the possible data sources for the [DetailAdapter]
 */
public sealed class DetailItem {

    public class Name(
        public val fullname: String
    ) : DetailItem()

    public class Widget(
        public val title: String,
        public val statusIcon: Int,
        public val message: String,
        public val buttonText: String
    ) : DetailItem()

    public class Header(
        public val title: String
    ) : DetailItem()

    public class Personal(
        public val title: String,
        public val subtitle: String?
    ) : DetailItem()

    public class Certificate(
        public val id: String,
        public val type: CertificateType,
        public val title: String,
        public val subtitle: String,
        public val date: String,
        public val isActual: Boolean = false
    ) : DetailItem()
}
