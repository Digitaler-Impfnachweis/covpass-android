package de.rki.covpass.commonapp.utils

import android.content.res.Configuration
import android.content.res.Resources

public fun Resources.isLandscapeMode(): Boolean =
    configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
