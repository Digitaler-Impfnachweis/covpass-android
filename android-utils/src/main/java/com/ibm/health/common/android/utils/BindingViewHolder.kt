/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.android.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * Base [RecyclerView.ViewHolder] with [ViewBinding]
 */
public open class BindingViewHolder<B : ViewBinding>(
    public val binding: B,
) : RecyclerView.ViewHolder(binding.root) {

    public constructor(
        parent: ViewGroup,
        inflater: (LayoutInflater, ViewGroup, Boolean) -> B,
    ) : this(
        inflater(
            LayoutInflater.from(parent.context),
            parent,
            false,
        ),
    )

    protected val context: Context
        get() = binding.root.context
}
