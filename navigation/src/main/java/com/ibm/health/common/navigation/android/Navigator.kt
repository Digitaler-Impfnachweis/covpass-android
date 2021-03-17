package com.ibm.health.common.navigation.android

import android.content.pm.ActivityInfo
import androidx.annotation.IdRes
import androidx.fragment.app.*
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
 * @param animator Defines custom animations. Don't use this to globally override animations for the whole app!
 */
@Suppress("FunctionName")
public fun <T> T.Navigator(
    @IdRes containerId: Int,
    animator: FragmentTransaction.(fragment: Fragment) -> Unit = navigationDeps.animationConfig::defaultAnimation,
): Navigator where T : Fragment, T : NavigatorOwner =
    Navigator(requireActivity(), childFragmentManager, containerId, animator)

/**
 * Creates a new [Navigator] instance on an activity implementing [NavigatorOwner].
 *
 * @param containerId The container where fragments are added to.
 * @param animator Defines custom animations. Don't use this to globally override animations for the whole app!
 */
@Suppress("FunctionName")
public fun <T> T.Navigator(
    @IdRes containerId: Int = android.R.id.content,
    animator: FragmentTransaction.(fragment: Fragment) -> Unit = navigationDeps.animationConfig::defaultAnimation,
): Navigator where T : FragmentActivity, T : NavigatorOwner =
    Navigator(this, supportFragmentManager, containerId, animator)

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
    private val fragmentManager: FragmentManager,
    @IdRes private val containerId: Int,
    private val animator: FragmentTransaction.(fragment: Fragment) -> Unit,
) {

    public val backStackEntryCount: StateFlow<Int> = MutableStateFlow(0).apply {
        fragmentManager.addOnBackStackChangedListener {
            value = fragmentManager.backStackEntryCount
            update()
        }
    }

    init {
        update()
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
     * Pushes a fragment via [FragmentNav].
     *
     * @param nav The [FragmentNav] to be pushed to the stack and displayed.
     */
    public fun push(nav: FragmentNav) {
        push(nav.build())
    }

    /**
     * Pushes a [fragment] to the back stack and displays it.
     *
     * @param fragment The `Fragment` to be pushed to the stack and displayed.
     */
    public fun push(fragment: Fragment) {
        fragmentManager.commit {

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

            animator(fragment)
            if (fragment is OverlayNavigation) {
                add(containerId, fragment, fragment.getTagName())
            } else {
                replace(containerId, fragment, fragment.getTagName())
            }

            // For SheetPaneNavigation we always want to allow popping the back stack to remove the pane, even if this
            // is the first fragment getting pushed (e.g. when using a normal activity in combination with bottom sheet)
            if (fragmentManager.fragments.isNotEmpty() || fragment is OverlayNavigation) {
                // TODO: Should StatelessNavigation ever be pushed on the back stack?
                addToBackStack(fragment.getTagName())
            } else if (fragmentManager.fragments.isEmpty()) {
                updateOrientation(fragment)
            }
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
     * @param includeMatch Whether to also pop the matching fragment (for which [predicate] returns `true`).
     *                     Defaults to `false`.
     * @param predicate Pops until this function returns `true`.
     *
     * @return true if there was something to pop, false otherwise.
     */
    public fun popUntil(includeMatch: Boolean = false, predicate: (Any) -> Boolean): Boolean {
        fragmentManager.executePendingTransactions()

        var popped = false
        while (fragmentManager.fragments.size > 0 && !predicate(fragmentManager.fragments.last())) {
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

    /**
     * Pops all entries from the back stack, that are any type of overlay navigation.
     *
     * @return true if there was something to pop, false otherwise.
     */
    public fun popOverlays(): Boolean {
        fragmentManager.executePendingTransactions()
        // Try to find the modal overlay and pop (including) until the fragment right after it
        var index = fragmentManager.fragments.indexOfLast { it is ModalPaneNavigation }
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
     * @return the matching [Fragment] or null if no matching fragment was not found.
     */
    public fun findFragment(func: (Fragment) -> Boolean): Fragment? =
        fragmentManager.fragments.lastOrNull(func)

    private fun update() {
        updateOrientation()
        updateAnimations()
    }

    private fun updateOrientation(fragment: Fragment? = null) {
        val fixedOrientation = (fragment ?: findFragment { it is FixedOrientation }) as? FixedOrientation
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
}

public fun Fragment.getTagName(): String? = getTagName(this::class)

public fun getTagName(cls: KClass<out Fragment>): String? =
    if (StatelessNavigation::class.java.isAssignableFrom(cls.java)) null else cls.java.name

private val Orientation.androidScreenOrientation: Int
    get() = when (this) {
        Orientation.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        Orientation.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        Orientation.SENSOR -> ActivityInfo.SCREEN_ORIENTATION_SENSOR
    }
