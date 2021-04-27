package com.ibm.health.common.android.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.IntegerRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat

/**
 * Interface used to encapsulate resource fetching where a context is needed.
 * For example fetching a string.
 **/
public interface ResourceProvider {

    /** @see [Resources.getQuantityString] */
    public fun getQuantityString(@PluralsRes resId: Int, quantity: Int): String

    /** @see [Context.getString] */
    public fun getString(@StringRes resId: Int, vararg formatArgs: Any): String

    /** @see [Context.getDrawable] */
    public fun getDrawable(@DrawableRes resId: Int): Drawable?

    /** @see [Resources.getInteger] */
    public fun getInteger(@IntegerRes resId: Int): Int
}

@Suppress("FunctionName")
public fun ResourceProvider(context: Context): ResourceProvider =
    ResourceProviderImpl(context)

private class ResourceProviderImpl(private val context: Context) : ResourceProvider {

    override fun getQuantityString(@PluralsRes resId: Int, quantity: Int): String =
        context().resources.getQuantityString(resId, quantity, quantity)

    @Suppress("SpreadOperator")
    override fun getString(@StringRes resId: Int, vararg formatArgs: Any) =
        context().getString(resId, *formatArgs)

    override fun getDrawable(@DrawableRes resId: Int) =
        ContextCompat.getDrawable(context(), resId)

    override fun getInteger(resId: Int) =
        context().resources.getInteger(resId)

    // We prefer activity context as configuration of baseContext may have changed at runtime
    private fun context() = androidDeps.currentActivityOrNull() ?: context
}
