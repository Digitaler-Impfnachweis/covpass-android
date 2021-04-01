package com.ibm.health.common.vaccination.app.extensions

import android.text.SpannableString
import android.text.TextPaint
import android.text.style.URLSpan
import android.widget.TextView

/**
 * Strips the underlines and set the new string to this [TextView].
 */
public fun TextView.stripUnderlines() {
    val spString = SpannableString(text)
    val spans = spString.getSpans(0, spString.length, URLSpan::class.java)
    for (span in spans) {
        val start = spString.getSpanStart(span)
        val end = spString.getSpanEnd(span)
        spString.removeSpan(span)
        val noUnderlineSpan = URLSpanNoUnderline(span.url)
        spString.setSpan(noUnderlineSpan, start, end, 0)
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
