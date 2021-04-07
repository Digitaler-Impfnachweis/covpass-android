package com.ibm.health.common.android.utils

import android.content.Context
import android.graphics.Point
import android.view.WindowManager

/**
 * Gets the screen width and height and returns it as a [Point].
 *
 * The context used to get [Context.WINDOW_SERVICE] system service and display of this context.
 *
 * @return [Point] containing the screen width and height in pixels.
 */
public fun Context.getScreenSize(): Point {
    val windowManager = getSystemService(Context.WINDOW_SERVICE)
    val outSize = Point()
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        display?.getRealSize(outSize)
    } else {
        @Suppress("Deprecation")
        (windowManager as WindowManager).defaultDisplay.getRealSize(outSize)
    }
    return outSize
}
