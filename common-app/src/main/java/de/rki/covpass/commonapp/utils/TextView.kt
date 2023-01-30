/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.utils

import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ImageSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import de.rki.covpass.commonapp.R

/**
 * Strips the underlines and sets a external link image at the end of this [TextView].
 */
public fun TextView.stripUnderlinesAndSetExternalLinkImage() {
    val spString = SpannableStringBuilder(text)
    val spans = spString.getSpans(0, spString.length, URLSpan::class.java)
    for (span in spans) {
        val start = spString.getSpanStart(span)
        val end = spString.getSpanEnd(span)
        spString.removeSpan(span)
        val noUnderlineSpan = URLSpanNoUnderline(span.url)
        val image = ImageSpan(context, R.drawable.ic_external_link, ImageSpan.ALIGN_BOTTOM)
        // Added empty character which will be replaced with external link image
        spString.insert(end, " ")
        spString.setSpan(noUnderlineSpan, start, end + 1, 0)
        spString.setSpan(image, end, end + 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
    }
    text = spString
}

public fun TextView.underlinedClickable(onClick: () -> Unit) {
    val spString = SpannableString(text)
    val spans = spString.getSpans(0, spString.length, URLSpan::class.java)
    for (span in spans) {
        val start = spString.getSpanStart(span)
        val end = spString.getSpanEnd(span)
        spString.removeSpan(span)
        val clickableSpan = URLSpanClickable(span.url, onClick)
        spString.setSpan(clickableSpan, start, end, 0)
    }
    text = spString
}

/**
 * Utility class for making an URL not underlined.
 *
 * @param url The url to be spanned.
 */
private class URLSpanNoUnderline(url: String) : URLSpan(url) {

    /** @suppress */
    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.isUnderlineText = false
    }
}

private class URLSpanClickable(
    url: String,
    val onClick: () -> Unit,
) : URLSpan(url) {

    override fun onClick(widget: View) {
        onClick()
    }
}
