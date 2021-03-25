package com.ibm.health.common.android.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat

/**
 * Interface used to encapsulate resource fetching where a context is needed.
 * For example fetching a string.
 **/
public interface ResourceProvider {

    /** @see [Context.getString] */
    public fun getString(@StringRes resId: Int, vararg formatArgs: Any): String

    /** @see [Context.getDrawable] */
    public fun getDrawable(@DrawableRes resId: Int): Drawable?

    /** @see [Context.getInteger] */
    public fun getInteger(@IntegerRes resId: Int): Int
}

@Suppress("FunctionName")
public fun ResourceProvider(context: Context): ResourceProvider =
    ResourceProviderImpl(context)

private class ResourceProviderImpl(private val context: Context) : ResourceProvider {

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
