package com.ibm.health.common.android.utils

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.ensody.reactivestate.android.onDestroyView
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

public inline fun <T : ViewBinding> AppCompatActivity.viewBinding(
    crossinline bindingInflater: (LayoutInflater) -> T,
): Lazy<T> =
    lazy(LazyThreadSafetyMode.NONE) {
        bindingInflater(layoutInflater)
    }

/**
 * Fragment view binding.
 *
 * Use this property delegate to bind the Fragment's rootView to the inferred Binding `T`.
 * This binding is nullified when the `invalidateOn` inner block is executed.
 * (E.g. when a LifecyleObserver gets its OnDestroy callback)
 *
 * Example:
 *
 * ```
 * private val binding by viewBinding(FragmentFeatureX::bind)
 * ```
 *
 * @param viewBinder: is invoked the first time the value is accessed (and null). `Fragment.requireView()` is passed in.
 * @param invalidateOn: block to handle the binding invalidation. Default: Fragment::onDestroyView.
 *
 * @return ReadOnlyProperty that binds to `viewBinder`'s return value.
 */
public fun <T> Fragment.viewBinding(
    viewBinder: (View) -> T,
    invalidateOn: (invalidate: () -> Unit) -> Any? = ::onDestroyView,
): ReadOnlyProperty<Fragment, T> =
    ViewBindingProperty(viewBinder = viewBinder, invalidateOn = invalidateOn)

private class ViewBindingProperty<T>(
    private val viewBinder: (View) -> T,
    private val invalidateOn: (invalidate: () -> Unit) -> Any?,
) : ReadOnlyProperty<Fragment, T> {

    private var binding: T? = null

    init {
        invalidateOn {
            binding = null
        }
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        return binding ?: viewBinder(thisRef.requireView()).also {
            binding = it
        }
    }
}
