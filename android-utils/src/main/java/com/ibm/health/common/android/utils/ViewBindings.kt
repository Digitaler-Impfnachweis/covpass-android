/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.android.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
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
 * (E.g. when a LifecyleObserver gets its onDestroyView callback)
 *
 * Example:
 *
 * ```
 * private val binding by viewBinding(FragmentFeatureX::inflate)
 * ```
 *
 * @param bindingInflater: the ViewBinding::inflate method
 * @param invalidateOn: block to handle the binding invalidation. Default: Fragment::onDestroyView.
 *
 * @return ReadOnlyProperty that binds to `viewBinder`'s return value.
 */
public fun <T : ViewBinding> BaseHookedFragment.viewBinding(
    bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> T,
    invalidateOn: (invalidate: () -> Unit) -> Any? = ::onDestroyView,
): ReadOnlyProperty<BaseHookedFragment, T> =
    ViewBindingInflaterProperty(fragment = this, bindingInflater = bindingInflater, invalidateOn = invalidateOn)

private class ViewBindingInflaterProperty<T : ViewBinding>(
    fragment: BaseHookedFragment,
    private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> T,
    private val invalidateOn: (invalidate: () -> Unit) -> Any?,
) : ReadOnlyProperty<BaseHookedFragment, T> {

    private var binding: T? = null

    init {
        fragment.inflaterHook = ::init
        fragment.lifecycleScope.launchWhenCreated {
            invalidateOn {
                binding = null
            }
        }
    }

    fun init(
        inflater: LayoutInflater,
        parent: ViewGroup?,
    ): View {
        val bindingInstance = bindingInflater(inflater, parent, false)
        binding = bindingInstance
        return bindingInstance.root
    }

    override fun getValue(thisRef: BaseHookedFragment, property: KProperty<*>): T =
        binding ?: throw UninitializedViewBindingException()
}

/** This exception is thrown when accessing an uninitialized ViewBinding (e.g. before onCreateView). */
public class UninitializedViewBindingException : IllegalStateException()

/**
 * Fragment view binding.
 *
 * Use this property delegate to bind the Fragment's rootView to the inferred Binding `T`.
 * This binding is nullified when the `invalidateOn` inner block is executed
 * (e.g. when a LifecyleObserver gets its onDestroyView callback).
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
public fun <T : ViewBinding> Fragment.viewBinding(
    viewBinder: (View) -> T,
    invalidateOn: (invalidate: () -> Unit) -> Any? = ::onDestroyView,
): ReadOnlyProperty<Fragment, T> =
    ViewBindingProperty(fragment = this, viewBinder = viewBinder, invalidateOn = invalidateOn)

private class ViewBindingProperty<T>(
    fragment: Fragment,
    private val viewBinder: (View) -> T,
    private val invalidateOn: (invalidate: () -> Unit) -> Any?,
) : ReadOnlyProperty<Fragment, T> {

    private var binding: T? = null

    init {
        fragment.lifecycleScope.launchWhenCreated {
            invalidateOn {
                binding = null
            }
        }
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        return binding ?: viewBinder(thisRef.requireView()).also {
            binding = it
        }
    }
}
