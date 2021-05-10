package com.ibm.health.common.android.utils

import android.text.Spanned
import androidx.core.text.HtmlCompat

/**
 * A regular expression which is used to replace every match of the following format:
 * Lorem ipsum #text_to_be_displayed::action:arg_to_be_passed_to_the_action# dolores it.
 * with:
 * Lorem ipsum <a href=\"action:arg_to_be_passed_to_the_action\">text_to_be_displayed</a> dolores it.
 * and makes it clickable.
 *
 * Action could be anything like: `link`, etc.
 */
private val linkRegex = Regex("#(.*)::(.*)#")

/** Used for index-based string interpolation. */
private val interpolationRegex = Regex("""\$(\d+)""")

/**
 * Formats a string of the following formats:
 * Ich habe die #Datenschutzerklärung::linkUrl# zur Kenntnis genommen.
 *
 * @param twineString - string to be transformed.
 *
 * @return the [Spanned] string.
 */
public fun getSpanned(twineString: String, vararg values: String, boldLinks: Boolean = true): Spanned {
    val interpolatedText = interpolationRegex.replace(twineString) { match ->
        try {
            val index = match.groups[1]!!.value.toInt()
            values[index]
        } catch (e: Throwable) {
            // Bad format string or index
            return@replace ""
        }
    }
    return linkRegex
        .replace(interpolatedText) { match ->
            val (title, url) = match.destructured
            val cleanUrl = if (url.isBlank() && title.startsWith("https:")) title else url
            val link = "<a href=\"$cleanUrl\">$title</a>"
            if (boldLinks) "<b>$link</b>" else link
        }
        .convertStyleTags()
        .toSpanned()
}

/**
 * Converts the bold and new line tags to their HTML tags ignoring the case.
 *
 * @return The transformed string.
 */
internal fun String.convertStyleTags(): String {
    val colorRegex = Regex("\\[c=(.*?)](.*?)\\[/c]")

    return this
        .replace("[b]", "<b>", true)
        .replace("[/b]", "</b>", true)
        .replace("\n", "<br>", true)
        .replace("[i]", "<i>", true)
        .replace("[/i]", "</i>", true)
        .replace("[u]", "<u>", true)
        .replace("[/u]", "</u>", true)
        .replace(
            colorRegex
        ) {
            "<font color=\"#${it.groupValues[1]}\">${it.groupValues[2]}</font>"
        }
}

/**
 * Invokes Html.fromHtml(String, int) on API 24 and newer,
 * otherwise flags are ignored and Html.fromHtml(String) is used.
 *
 * @return [Spanned] string.
 */
public fun String.toSpanned(): Spanned = HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY)
