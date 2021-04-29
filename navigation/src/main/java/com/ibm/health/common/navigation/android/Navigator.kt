package com.ibm.health.common.navigation.android

import android.content.pm.ActivityInfo
import androidx.annotation.IdRes
import androidx.fragment.app.*
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.ibm.health.common.annotations.Abort
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.annotations.Continue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.reflect.KClass

/**
 * Creates a new [Navigator] instance on a fragment implementing [NavigatorOwner].
 *
 * @param containerId The container where fragments are added to.
 */
public fun <T> T.Navigator(
    @IdRes containerId: Int,
): Navigator where T : Fragment, T : NavigatorOwner =
    Navigator(requireActivity(), childFragmentManager, this, containerId)

/**
 * Creates a new [Navigator] instance on an activity implementing [NavigatorOwner].
 *
 * @param containerId The container where fragments are added to.
 * @param animator Defines custom animations. Don't use this to globally override animations for the whole app!
 */
public fun <T> T.Navigator(
    @IdRes containerId: Int = android.R.id.content,
): Navigator where T : FragmentActivity, T : NavigatorOwner =
    Navigator(this, supportFragmentManager, this, containerId)

/**
 * Basic navigation primitives for `Fragment`s.
 *
 * Usage:
 *
 * 1. Your `Fragment` or `Activity` must implement [NavigatorOwner] and add a [Navigator] instance.
 * 2. In `Activity.onCreate`/`Fragment.onViewCreated` check for state restoration with [isEmpty].
 *    IMPORTANT: It's not enough to check for `savedInstanceState` because that can be null and still
 *    `childFragmentManager` can contain restored fragments.
 * 3. Only [push] the initial fragment if state isn't restored.
 * 4. Now you can use [push]/[pop]/etc. to navigate between fragments.
 */
public class Navigator internal constructor(
    // constructor is internal to enforce the above contextual constructors
    private val activity: FragmentActivity,
    public val fragmentManager: FragmentManager,
    private val lifecycleOwner: LifecycleOwner,
    @IdRes private val containerId: Int,
) {

    /**
     * Return the number of entries currently in the back stack.
     */
    public val backStackEntryCount: StateFlow<Int> = MutableStateFlow(0).apply {
        fragmentManager.addOnBackStackChangedListener {
            value = fragmentManager.backStackEntryCount
            update()
        }
    }

    init {
        lifecycleOwner.lifecycleScope.launchWhenCreated {
            update()
        }
    }

    /** Checks if the `FragmentManager` is empty and thus if we're starting with a clean state or doing a restore. */
    public fun isEmpty(): Boolean {
        fragmentManager.executePendingTransactions()
        return fragmentManager.fragments.isEmpty()
    }

    /** Checks if the `FragmentManager` is not empty and thus if we're doing a restore. See [isEmpty]. */
    public fun isNotEmpty(): Boolean =
        !isEmpty()

    /**
     * Pushes a fragment via [FragmentDestination].
     *
     * @param nav The [FragmentDestination] to be pushed to the stack and displayed.
     *
     * @param suppressAddToBackstack Usually it is determined automatically if the push should be added to backstack.
     * If you want to suppress this and enforce the transaction not being added, pass true here.
     */
    public fun push(nav: FragmentDestination, suppressAddToBackstack: Boolean = false) {
        push(nav.build(), suppressAddToBackstack)
    }

    /**
     * Pushes a [fragment] to the back stack and displays it.
     *
     * @param fragment The `Fragment` to be pushed to the stack and displayed.
     *
     * @param suppressAddToBackstack Usually it is determined automatically if the push should be added to backstack.
     * If you want to suppress this and enforce the transaction not being added, pass true here.
     */
    public fun push(fragment: Fragment, suppressAddToBackstack: Boolean = false) {
        fragmentManager.beginTransaction().apply {

            val overlayFragment = (fragment as? OverlayNavigation)?.getModalOverlayFragment()
            if (overlayFragment != null && findFragment { it::class == overlayFragment::class } == null) {
                animator(overlayFragment)
                add(containerId, overlayFragment)
            }

            val lastFragment = fragmentManager.fragments.lastOrNull()
            if (lastFragment is OverlayNavigation && fragment is OverlayNavigation) {
                animator(lastFragment)
                hide(lastFragment)
            }

            // For SheetPaneNavigation we always want to allow popping the back stack to remove the pane, even if this
            // is the first fragment getting pushed (e.g. when using a normal activity in combination with bottom sheet)
            if (!suppressAddToBackstack && (fragmentManager.fragments.isNotEmpty() || fragment is OverlayNavigation)) {
                // TODO: Should StatelessNavigation ever be pushed on the back stack?
                // This has to happen before DialogFragment.show() because show() commits the transaction.
                addToBackStack(fragment.getTagName())
            } else if (fragmentManager.fragments.isEmpty()) {
                updateOrientation(fragment)
            }

            animator(fragment)
            when (fragment) {
                is DialogFragment -> {
                    fragment.show(this, fragment.getTagName())
                    return // skip the commit() below because show() already commits
                }
                is OverlayNavigation -> add(containerId, fragment, fragment.getTagName())
                else -> replace(containerId, fragment, fragment.getTagName())
            }
            commit()
        }
    }

    /** Handles a back button press. */
    public fun onBackPressed(): Abortable {
        fragmentManager.executePendingTransactions()
        val result = fragmentManager.fragments.lastOrNull().let {
            (it as? OnBackPressedNavigation)?.onBackPressed()
                ?: (it as? NavigatorOwner)?.navigator?.onBackPressed()
        }
        return if (result == Abort || pop()) Abort else Continue
    }

    /**
     * Pops the top state off the back stack.
     *
     * @return true if there was something to pop, false otherwise.
     */
    public fun pop(): Boolean {
        fragmentManager.executePendingTransactions()

        return if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
            true
        } else false
    }

    /**
     * Pops the back stack until the given Fragment class is matched.
     *
     * @param cls The target fragment to pop up to.
     * @param includeMatch Whether to also pop the matching fragment. Defaults to `false`.
     */
    public fun popUntil(cls: KClass<out Fragment>, includeMatch: Boolean = false) {
        popUntil(getTagName(cls) ?: return, includeMatch = includeMatch)
    }

    /**
     * Pops the back stack until the given [tag] name is matched.
     *
     * @param tag The tag name to pop up to.
     * @param includeMatch Whether to also pop the matching fragment. Defaults to `false`.
     */
    public fun popUntil(tag: String, includeMatch: Boolean = false) {
        fragmentManager.popBackStack(tag, if (includeMatch) FragmentManager.POP_BACK_STACK_INCLUSIVE else 0)
    }

    /**
     * Pops the back stack until the given Fragment class is matched.
     *
     * This can be used to communicate back results that aren't parcelable/serializable.
     *
     * WARNING: If this pops multiple fragments it can trigger multiple animations.
     * Usually you should use the class-based popUntil variant above.
     *
     * WARNING: If [includeMatch] is `true` the returned fragment will already be detached!
     *
     * @param includeMatch Whether to also pop the matching fragment. Defaults to `false`.
     *
     * @return The matching fragment or `null`.
     */
    @Suppress("UNCHECKED_CAST")
    public inline fun <reified T> popUntil(includeMatch: Boolean = false): T? =
        popUntilFound(includeMatch = includeMatch) { it as? T }

    /**
     * Pops the back stack until the given Fragment class is matched.
     *
     * This can be used to communicate back results that aren't parcelable/serializable.
     *
     * WARNING: If this pops multiple fragments it can trigger multiple animations.
     * Usually you should use the class-based popUntil variant above.
     *
     * WARNING: If [includeMatch] is `true` the returned fragment will already be detached!
     *
     * @param includeMatch Whether to also pop the matching fragment. Defaults to `false`.
     *
     * @return The matching fragment or `null`.
     */
    @Suppress("UNCHECKED_CAST")
    public fun <T> popUntilFound(includeMatch: Boolean = false, predicate: (Any) -> T?): T? {
        fragmentManager.executePendingTransactions()

        var target: T? = null
        while (true) {
            target = predicate(fragmentManager.fragments.lastOrNull() ?: break)
            if (target != null) {
                break
            }
            if (!fragmentManager.popBackStackImmediate()) {
                break
            }
        }

        if (includeMatch) {
            pop()
        }

        return target
    }

    /**
     * Pops the back stack until the given Fragment class is matched.
     *
     * This can be used to communicate back results that aren't parcelable/serializable.
     *
     * WARNING: If this pops multiple fragments it can trigger multiple animations.
     * Usually you should use the class-based popUntil variant above.
     *
     * @param includeMatch Whether to also pop the matching fragment (for which [predicate] returns `true`).
     *                     Defaults to `false`.
     * @param deep Whether to check [predicate] against the `childFragmentManager` of each fragment, too.
     * @param predicate Pops until this function returns `true`.
     *
     * @return true if there was something to pop, false otherwise.
     */
    public fun popUntil(includeMatch: Boolean = false, deep: Boolean = false, predicate: (Any) -> Boolean): Boolean {
        fragmentManager.executePendingTransactions()

        var popped = false
        while (fragmentManager.fragments.isNotEmpty() &&
            !checkPredicate(fragmentManager = fragmentManager, deep = deep, predicate = predicate)
        ) {
            if (!fragmentManager.popBackStackImmediate()) {
                break
            }
            popped = true
        }

        if (includeMatch) {
            popped = pop() || popped
        }

        return popped
    }

    private fun checkPredicate(fragmentManager: FragmentManager, deep: Boolean, predicate: (Any) -> Boolean): Boolean {
        val fragment = fragmentManager.fragments.lastOrNull() ?: return false
        return predicate(fragment) ||
            deep && checkPredicate(fragmentManager = fragment.childFragmentManager, deep = deep, predicate = predicate)
    }

    /**
     * Pops all entries from the back stack, that are any type of overlay navigation.
     *
     * @return true if there was something to pop, false otherwise.
     */
    public fun popOverlays(): Boolean {
        fragmentManager.executePendingTransactions()
        // Try to find the modal overlay and pop (including) until the fragment right after it
        val index = fragmentManager.fragments.indexOfLast { it is ModalPaneNavigation }
        if (index < 0) {
            return false
        }
        fragmentManager.fragments.getOrNull(index + 1)?.getTagName()?.let {
            popUntil(tag = it, includeMatch = true)
            return true
        }

        return false
    }

    /** Clears the back stack. */
    public fun popAll() {
        fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    /**
     * Searches for a matching `Fragment` in the back stack.
     *
     * @return the matching [Fragment] or null if no matching fragment was found.
     */
    public fun findFragment(func: (Fragment) -> Boolean): Fragment? =
        fragmentManager.fragments.lastOrNull(func)

    /**
     * Searches for a matching `Fragment` in the back stack with the given type [T].
     *
     * @return the matching [Fragment] or null if no matching fragment was found.
     */
    public inline fun <reified T> findFragment(): T? = findFragment { it is T } as? T

    private fun update() {
        updateOrientation()
        updateAnimations()
    }

    private fun updateOrientation(fragment: Fragment? = null) {
        val fixedOrientation =
            (fragment ?: findFragment<FixedOrientation>() ?: lifecycleOwner) as? FixedOrientation
                ?: try {
                    val navigator = (lifecycleOwner as? Fragment)?.findNavigator(skip = 1)
                    if (navigator != null) {
                        navigator.updateOrientation()
                        return
                    }
                    null
                } catch (e: NoSuchElementInHierarchy) {
                    null
                }
        val orientation = fixedOrientation?.orientation
            ?: navigationDeps.defaultScreenOrientation
        activity.requestedOrientation = orientation.androidScreenOrientation
    }

    private fun updateAnimations() {
        fragmentManager.fragments.forEach {
            // Updating the fragment transitions after fragment manager restoration
            (it as? AnimatedNavigation)?.animateNavigation(null, it)
        }
    }

    private fun FragmentTransaction.animator(fragment: Fragment) {
        navigationDeps.animationConfig.defaultAnimation(this, fragment)
    }
}

/**
 * Returns the tag name to use for the fragment.
 *
 * Usually returns the fragments class name, but if your fragment implements [StatelessNavigation]
 * this returns null.
 */
public fun Fragment.getTagName(): String? = getTagName(this::class)

/**
 * Returns the tag name to use for the fragment.
 *
 * Usually returns the fragments class name, but if your fragment implements [StatelessNavigation],
 * this returns null.
 */
public fun getTagName(cls: KClass<out Fragment>): String? =
    if (StatelessNavigation::class.java.isAssignableFrom(cls.java)) null else cls.java.name

private val Orientation.androidScreenOrientation: Int
    get() = when (this) {
        Orientation.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        Orientation.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        Orientation.SENSOR -> ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
    }
