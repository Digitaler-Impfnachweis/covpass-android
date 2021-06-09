package de.rki.covpass.sdk.cert.models

/**
 * Interface for all possible entries of a Digital Green Certificate.
 */
public interface DGCEntry {
    public val id: String
    public val idWithoutPrefix: String
        get() = id.removePrefix("URN:UVCI:")
}
