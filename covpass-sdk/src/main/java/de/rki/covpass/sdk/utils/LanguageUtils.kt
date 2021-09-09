package de.rki.covpass.sdk.utils

import java.util.*

public fun getDescriptionLanguage(): String = when (Locale.getDefault().language) {
    Locale.GERMAN.language -> DescriptionLanguage.GERMAN.languageCode
    else -> DescriptionLanguage.ENGLISH.languageCode
}

private enum class DescriptionLanguage(val languageCode: String) {
    GERMAN("de"),
    ENGLISH("en")
}
