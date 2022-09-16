/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.android.utils

import android.text.Spanned
import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat
import com.ensody.reactivestate.DependencyAccessor
import de.rki.covpass.logging.Lumber
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.TemporalAccessor

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

/**
 * A regular expression which is used to replace phone numbers with clickable links.
 */
private val phoneNumberRegex = Regex("""#(\+?[\d\s\-/]+)(?:::\$0)?#""")

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
public fun getSpanned(twineString: String, vararg values: Any, boldLinks: Boolean = true): Spanned {
    val textWithPhone = phoneNumberRegex.replace(twineString) { match ->
        val phone = match.groups[1]!!.value
        "#$phone::tel:$phone#"
    }
    val interpolatedText = interpolationRegex.replace(textWithPhone) { match ->
        try {
            val index = match.groups[1]!!.value.toInt()
            values.getOrNull(index)?.toString() ?: ""
        } catch (e: Throwable) {
            Lumber.e(e) { "Bad format string or index" }
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
 * Formats a string of the following formats:
 * Ich habe die #Datenschutzerklärung::linkUrl# zur Kenntnis genommen.
 *
 * @param stringRes - string res id to be transformed.
 *
 * @return the [Spanned] string.
 */
public fun getSpanned(@StringRes stringRes: Int, vararg values: Any, boldLinks: Boolean = true): Spanned =
    getSpanned(getString(stringRes, values = values), values = values, boldLinks = boldLinks)

@OptIn(DependencyAccessor::class)
@Suppress("SpreadOperator")
public fun getString(@StringRes stringRes: Int, vararg values: Any): String =
    androidDeps.resourceProvider.getString(stringRes, *values)

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
            colorRegex,
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

private fun DateTimeFormatter.parseOrNull(input: String): TemporalAccessor? = try {
    parse(input)
} catch (dtpe: DateTimeParseException) {
    null
}

public fun String.toLocalDateOrNull(formatter: DateTimeFormatter): LocalDate? =
    formatter.parseOrNull(this)?.let(LocalDate::from)

public fun String.toLocalTimeOrNull(formatter: DateTimeFormatter): LocalTime? =
    formatter.parseOrNull(this)?.let(LocalTime::from)
