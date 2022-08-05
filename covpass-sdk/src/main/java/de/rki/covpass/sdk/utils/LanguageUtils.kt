package de.rki.covpass.sdk.utils

import java.util.Locale

public fun getDescriptionLanguage(): String = when (Locale.getDefault().language) {
    Locale.GERMAN.language -> DescriptionLanguage.GERMAN.languageCode
    else -> DescriptionLanguage.ENGLISH.languageCode
}

public enum class DescriptionLanguage(public val languageCode: String) {
    GERMAN("de"),
    ENGLISH("en")
}
