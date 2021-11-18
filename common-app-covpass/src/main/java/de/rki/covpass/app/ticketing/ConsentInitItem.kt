/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.ticketing

public sealed class ConsentInitItem {

    public class TicketingData(
        public val header: String,
        public val value: String,
    ) : ConsentInitItem()

    public class Note(
        public val text: String,
        public val bulletPoint: Boolean = false,
    ) : ConsentInitItem()

    public class Infobox(
        public val title: String,
        public val description: String,
        public val list: List<String>,
    ) : ConsentInitItem()

    public class DataProtection(
        public val note: String,
        public val linkTitle: Int,
        public val link: String,
    ) : ConsentInitItem()
}
