/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.android.utils

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView

/**
 * Base [RecyclerView.Adapter] that prevents memory leaks by setting the `adapter` to `null` in `onDestroyView`.
 */
public abstract class BaseRecyclerViewAdapter<T : RecyclerView.ViewHolder>(public val parent: Fragment) :
    RecyclerView.Adapter<T>() {
    public open fun attachTo(recyclerView: RecyclerView) {
        parent.attachRecyclerView(this, recyclerView)
    }
}
