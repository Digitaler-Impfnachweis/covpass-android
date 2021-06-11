package de.rki.covpass.sdk.cert.models

/**
 * Interface for all possible entries of a Digital Green Certificate.
 */
public sealed interface DGCEntry {
    public val id: String
    public val idWithoutPrefix: String
        get() = id.removePrefix("URN:UVCI:")
    public val type: DGCEntryType
}

/**
 * Sealed interface to mark the type of a [DGCEntry], e.g. as VACCINATION_FULL_PROTECTION.
 */
public sealed interface DGCEntryType
