package com.ibm.health.common.navigation.android

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import kotlin.reflect.KClass

/**
 * Shortcut for adding a `Parcelable` to a `Fragment`'s `Fragment.arguments`.
 *
 * @return the original `Fragment`.
 */
public fun <T : Fragment> T.withArgs(value: Parcelable): T =
    apply { arguments = Bundle().withArgs(value) }

/**
 * Retrieves the `Parcelable` args passed to this `Fragment` via `Fragment.arguments`.
 *
 * @throws [IllegalStateException] if the `Parcelable` is missing.
 *
 * Also see [Fragment.getOptionalArgs] if you want a `null` result instead of an exception.
 */
public fun <T : Parcelable> Fragment.getArgs(): T =
    getOptionalArgs<T>()
        ?: throw IllegalStateException("No args were passed to ${this::class.java.simpleName}")

/**
 * Retrieves the `Parcelable` args passed to this `Fragment` via `Fragment.arguments`.
 *
 * @return the `Parcelable` or `null` if it doesn't exist.
 */
public fun <T : Parcelable> Fragment.getOptionalArgs(): T? =
    arguments?.getOptionalArgs()

/**
 * A `Fragment` navigation destination.
 *
 * Most APIs that only want to navigate somewhere should take this as an argument for maximum flexibility.
 * For defining such a destination, you can use [FragmentNav] and [SimpleFragmentNav].
 */
public interface FragmentDestination {
    public fun build(): Fragment
}

/**
 * Base interface for defining a `Fragment`-based navigation destination.
 *
 * In many cases you'll want to use the [SimpleFragmentNav] API, but sometimes you need to customize the
 * instantiation process e.g. to pass callbacks or modify the resulting Fragment.
 * In that case, you can use this interface:
 *
 * ```kotlin
 * @Parcelize
 * class InfoDialogFragmentNav(val dialogConfig: DialogConfig) : FragmentNav {
 *     override fun build(): Fragment = MyDialogFragment().withArgs(this).apply {
 *         isCancelable = dialogConfig.isCancelable
 *     }
 * }
 * ```
 */
public interface FragmentNav : FragmentDestination, Parcelable

/**
 * Base class for defining a `Parcelable` that can be used for easy and type-safe argument-passing to a `Fragment`.
 *
 * This API is similar to [IntentNav].
 *
 * ```kotlin
 * @Parcelize
 * class InfoDialogFragmentNav(val egaDialog: EgaDialog) : SimpleFragmentNav(InfoDialogFragment::class)
 * ```
 */
public abstract class SimpleFragmentNav(public val cls: KClass<out Fragment>) : FragmentNav {
    override fun build(): Fragment = cls.java.newInstance().withArgs(this)
}
