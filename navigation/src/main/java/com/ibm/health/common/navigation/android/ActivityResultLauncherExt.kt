package com.ibm.health.common.navigation.android

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher

/**
 * Like Android's `launch` (for [Intent] based [ActivityResultLauncher]), but taking an [IntentDestination] (usually defined via [IntentNav]).
 */
public fun ActivityResultLauncher<Intent>.launch(context: Context, nav: IntentDestination) {
    launch(nav.toIntent(context))
}
