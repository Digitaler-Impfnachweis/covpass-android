/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.navigation.android

import android.content.Context
import android.content.pm.ActivityInfo
import android.provider.Settings
import androidx.annotation.IdRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.ensody.reactivestate.DependencyAccessor
import com.ensody.reactivestate.derived
import com.ensody.reactivestate.get
import com.ibm.health.common.annotations.Abort
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.annotations.Continue
import de.rki.covpass.logging.Lumber
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
@OptIn(DependencyAccessor::class)
public class Navigator internal constructor(
    // constructor is internal to enforce the above contextual constructors
    private val activity: FragmentActivity,
    public val fragmentManager: FragmentManager,
    public val lifecycleOwner: LifecycleOwner,
    @IdRes private val containerId: Int,
) {

    /** Tracks the back stack. */
    public val backStack: StateFlow<List<FragmentManager.BackStackEntry>> =
        MutableStateFlow(generateBackStackList()).apply {
            // Fragment lifecycle events are needed on configuration changes
            // since the backstackChangedListener gets not triggered and the backstack stays empty
            fragmentManager.registerFragmentLifecycleCallbacks(
                object : FragmentManager.FragmentLifecycleCallbacks() {
                    override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
                        super.onFragmentAttached(fm, f, context)
                        value = generateBackStackList()
                    }

                    override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
                        super.onFragmentDetached(fm, f)
                        value = generateBackStackList()
                    }
                },
                false,
            )

            fragmentManager.addOnBackStackChangedListener {
                value = generateBackStackList()
            }
        }

    /** The number of entries currently on the back stack. */
    public val backStackEntryCount: StateFlow<Int> = derived { get(backStack).size }

    public val fragments: StateFlow<List<Fragment>> = MutableStateFlow(emptyList<Fragment>()).apply {
        fragmentManager.registerFragmentLifecycleCallbacks(
            object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
                    super.onFragmentAttached(fm, f, context)
                    value = fragmentManager.fragments.toList()
                    (activity as NavigatorOwner).onOverlayHasBeenAdded(f)
                }

                override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
                    super.onFragmentDetached(fm, f)
                    value = fragmentManager.fragments.toList()
                    (activity as NavigatorOwner).onOverlayHasBeenClosed(f)
                }
            },
            false,
        )
        fragmentManager.addOnBackStackChangedListener {
            value = fragmentManager.fragments.toList()
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

    // TODO: Remove me once OpenDocumentFragment and DocumentDetailActivity have been refactored (EBH-33326)
    public fun addToBackstack(fragment: Fragment, tagName: String) {
        fragmentManager.beginTransaction().apply {
            if (fragmentManager.fragments.isNotEmpty()) {
                val backStackEntryName =
                    "${BackStackPrefix}${fragmentManager.fragments.lastOrNull()?.getTagName()}|${fragment.getTagName()}"
                addToBackStack(backStackEntryName)
            }
            add(containerId, fragment, tagName)
            commit()
        }
    }

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
            // We inject one single modal overlay before the first OverlayNavigation fragment.
            // All subsequent OverlayNavigations just reuse the existing modal overlay.
            // This is done, so we don't have any flickering when navigating between multiple bottom sheets.
            // The modal overlay has its own fade in/out animation and when you navigate from the first
            // bottom sheet to the second one you don't want to repeat the modal animation anymore, but
            // only animate the bottom sheets (or whatever kind of OverlayNavigation you want to add).
            val overlayFragment = (fragment as? OverlayNavigation)?.getModalOverlayFragment()
            if (overlayFragment != null &&
                findFragment { if (it::class == overlayFragment::class) it else null } == null
            ) {
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
                val backStackEntryName =
                    "${BackStackPrefix}${fragmentManager.fragments.lastOrNull()?.getTagName()}|${fragment.getTagName()}"
                addToBackStack(backStackEntryName)
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
        val result = fragmentManager.fragments.lastOrNull()?.let {
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
    public fun pop(executePendingTransactions: Boolean = true): Boolean {
        if (executePendingTransactions) {
            fragmentManager.executePendingTransactions()
        }

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
     * Pops the back stack until the given back stack entry [name] is matched.
     *
     * @param name The back stack entry name ([FragmentManager.BackStackEntry]) to pop up to.
     * @param includeMatch Whether to also pop the matching fragment. Defaults to `false`.
     */
    public fun popUntil(name: String, includeMatch: Boolean = false) {
        val backStack = backStack.value
        // Each back stack entry is encoded with BackStackPrefix, origin and target (N:OriginScreen|TargetScreen).
        // The root screen can only be found via origin. The currently visible screen can only be found via target.
        for (index in backStack.indices) {
            val entryName = backStack[index].name
            // Whether we match origin or target (if no match we continue the loop)
            val matchesOrigin: Boolean = when {
                entryName == name -> false
                entryName?.startsWith(BackStackPrefix) == true ->
                    entryName.removePrefix(BackStackPrefix).split("|").indexOf(name)
                        .takeIf { it >= 0 }?.let { it == 0 } ?: continue
                else -> continue
            }
            if (matchesOrigin) {
                val target =
                    if (includeMatch) (index - 1).takeIf { it >= 0 }?.let { backStack[it] }
                    else backStack[index]
                fragmentManager.popBackStack(target?.name, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            } else {
                fragmentManager.popBackStack(
                    backStack[index].name,
                    if (includeMatch) FragmentManager.POP_BACK_STACK_INCLUSIVE else 0,
                )
            }
            return
        }
        throw IllegalStateException("No back stack entry found: $name")
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
        // The ModalPanelNavigation is not included in the backstack with its name,
        // so jump one behind the ModalPaneNavigation and delete everything excluding the new position
        fragmentManager.fragments.getOrNull(index - 1)?.getTagName()?.let {
            popUntil(name = it, includeMatch = false)
            return true
        } ?: run {
            // If the ModalPaneNavigation is the only element left in the backstack, just pop
            popAll()
        }

        return false
    }

    /** Clears the back stack. */
    public fun popAll(immediate: Boolean = false) {
        if (immediate) {
            fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        } else {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }

    /** Clears the whole [FragmentManager]. */
    public fun clear() {
        popAll(immediate = true)
        fragmentManager.beginTransaction().apply {
            for (fragment in fragmentManager.fragments) {
                remove(fragment)
            }
            commitNow()
        }
    }

    /**
     * Searches for a matching `Fragment` in the back stack.
     *
     * @return the matching [Fragment] or null if no matching fragment was found.
     */
    public fun <T> findFragment(block: (Fragment) -> T?): T? {
        for (fragment in fragmentManager.fragments.reversed()) {
            block(fragment)?.let { return it }
        }
        return null
    }

    /**
     * Searches inside the fragments list for a matching [Fragment] with the given type [T].
     *
     * execute the passed lambda with the matching [Fragment].
     */
    public inline fun <reified T : Any> findFragmentsAndExecute(block: T.() -> Unit) {
        findFragments<T> { it as? T }.forEach { it.block() }
    }

    /**
     * Searches inside the fragments list for a matching [Fragment] with the given lambda.
     *
     * @return a sequence of the matching [Fragment]
     */
    public fun <T> findFragments(block: (Fragment) -> T?): Sequence<T> =
        sequence {
            for (fragment in fragmentManager.fragments) {
                block(fragment)?.let {
                    yield(it)
                }
            }
        }

    /**
     * Searches for a matching `Fragment` in the back stack with the given type [T].
     *
     * @return the matching [Fragment] or null if no matching fragment was found.
     */
    public inline fun <reified T> findFragment(): T? = findFragment { it as? T }

    private fun generateBackStackList(): List<FragmentManager.BackStackEntry> = buildList {
        for (i in 0 until fragmentManager.backStackEntryCount) {
            add(i, fragmentManager.getBackStackEntryAt(i))
        }
    }

    private fun update() {
        updateOrientation()
        updateAnimations()
    }

    private fun updateOrientation(fragment: Fragment? = null) {
        // Do nothing, if the user has disabled orientation changes in the device setting.
        // Otherwise we get weird intermediate rotations, when navigating to new screens.
        if (isScreenRotationEnabledInSettings(activity)) {
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

    private fun isScreenRotationEnabledInSettings(context: Context): Boolean {
        return try {
            Settings.System.getInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION) == 1
        } catch (e: Settings.SettingNotFoundException) {
            Lumber.e(e)
            false
        }
    }
}

private const val BackStackPrefix = "N:"

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
        Orientation.USER -> ActivityInfo.SCREEN_ORIENTATION_FULL_USER
    }
