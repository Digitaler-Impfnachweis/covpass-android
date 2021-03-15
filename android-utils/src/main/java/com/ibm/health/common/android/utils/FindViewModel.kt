package com.ibm.health.common.android.utils

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel

/** Searches the fragment hierarchy for a ViewModel of the given type [T]. */
public inline fun <reified T : ViewModel> Fragment.findViewModel(skip: Int = 0): T =
    findInHierarchy(skip = skip) {
        (it as? ViewModelOwner<*>)?.viewModel as? T
    }
