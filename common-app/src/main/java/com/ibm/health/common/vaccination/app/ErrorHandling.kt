package com.ibm.health.common.vaccination.app

import androidx.fragment.app.FragmentManager
import com.ibm.health.common.logging.Lumber

public fun handleError(error: Throwable, fragmentManager: FragmentManager) {
    // TODO: Handle errors
    Lumber.e(error)
}
