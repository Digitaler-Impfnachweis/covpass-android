package com.ibm.health.common.navigation.android

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.annotation.IdRes
import androidx.fragment.app.*
import com.ibm.health.common.android.utils.reactive.android.findInHierarchy
import com.ibm.health.common.annotations.Abort
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.annotations.Continue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.reflect.KClass

/**
 * Basic navigation primitives for `Fragment`s.
 *
 * Usage:
 *
 * 1. Your `Fragment` or `Activity` must implement [NavigatorOwner] and provide a `by lazy` [Navigator].
 * 2. In `Activity.onCreate`/`Fragment.onCreateView` check for state restoration. Only [push] if no state is restored.
 * 3. Now you can use [push]/[pop]/etc. to navigate between fragments.
 */
public interface Navigator {
    public val backStackEntryCount: StateFlow<Int>

    /** Checks if the `FragmentManager` is empty and thus if we're starting with a clean state or doing a restore. */
    public fun isEmpty(): Boolean

    /**
     * Pushes a [fragment] to the back stack and displays it.
     *
     * @param fragment The `Fragment` to be pushed to the stack and displayed.
     */
    public fun push(fragment: Fragment)

    /** Handles a back button press. */
    public fun onBackPressed(): Abortable

    /**
     * Pops the top state off the back stack.
     *
     * @return true if there was something to pop, false otherwise.
     */
    public fun pop(): Boolean

    /**
     * Pops the back stack until the given Fragment class is matched.
     *
     * @param cls The target fragment to pop up to.
     * @param includeMatch Whether to also pop the matching fragment. Defaults to `false`.
     */
    public fun popUntil(cls: KClass<out Fragment>, includeMatch: Boolean = false)

    /**
     * Pops the back stack until the given Fragment class is matched.
     *
     * This can be used to communicate back results that aren't parcelable/serializable.
     * WARNING: If this pops multiple fragments it can trigger multiple animations.
     * Usually you should use the class-based popUntil variant above.
     *
     * @param includeMatch Whether to also pop the matching fragment (for which [predicate] returns `true`).
     *                     Defaults to `false`.
     * @param predicate Pops until this function returns `true`.
     *
     * @return true if there was something to pop, false otherwise.
     */
    public fun popUntil(includeMatch: Boolean = false, predicate: (Any) -> Boolean): Boolean

    /** Clears the back stack. */
    public fun popAll()

    /**
     * Searches for a matching `Fragment` in the back stack.
     *
     * @return the matching [Fragment] or null if no matching fragment was not found.
     */
    public fun findFragment(func: (Fragment) -> Boolean): Fragment?
}

/**
 * Pushes a fragment via [FragmentNav].
 *
 * @param nav The [FragmentNav] to be pushed to the stack and displayed.
 */
public fun Navigator.push(nav: FragmentNav) {
    push(nav.build())
}

/** Checks if the `FragmentManager` is not empty and thus if we're doing a restore. See [Navigator.isEmpty]. */
public fun Navigator.isNotEmpty(): Boolean =
    !isEmpty()

/**
 * Interface for marking an activity or fragment as the owner of a [Navigator].
 */
public interface NavigatorOwner {
    public val navigator: Navigator
}

/** Executes a back button press. */
public fun Fragment.triggerBackPress() {
    requireActivity().onBackPressed()
}

/** Executes a back button press. */
public fun Activity.triggerBackPress() {
    onBackPressed()
}

/** Defines default animations and animation overrides. */
public interface NavigationAnimationConfig {
    /**
     * This is the main dispatcher for animations.
     *
     * By default this delegates to [FragmentTransaction.defaultNavigationAnimation].
     */
    public fun defaultAnimation(transaction: FragmentTransaction, fragment: Fragment) {
        transaction.defaultNavigationAnimation(fragment)
    }

    /** Applies animations for normal fragments. */
    public fun fragmentPaneAnimation(transaction: FragmentTransaction)

    /** Applies animations for [SheetPaneNavigation]. */
    public fun sheetPaneAnimation(transaction: FragmentTransaction)

    /** Applies animations for the overlay fragment behind [SheetPaneNavigation] sheets. */
    public fun modalPaneAnimation(transaction: FragmentTransaction)
}

/** The default animations. */
public class DefaultNavigationAnimationConfig : NavigationAnimationConfig {
    override fun fragmentPaneAnimation(transaction: FragmentTransaction) {
        transaction.fragmentPaneAnimation()
    }

    override fun sheetPaneAnimation(transaction: FragmentTransaction) {
        transaction.sheetPaneAnimation()
    }

    override fun modalPaneAnimation(transaction: FragmentTransaction) {
        transaction.modalPaneAnimation()
    }
}

/** The currently active default [NavigationAnimationConfig]. */
public var navigationAnimationConfig: NavigationAnimationConfig = DefaultNavigationAnimationConfig()

/**
 * The default animator used for [Navigator].
 *
 * For [AnimatedNavigation] this uses the animation defined by the [fragment].
 * For anything else this uses `navigationAnimationConfig.fragmentPaneAnimation`.
 */
public fun FragmentTransaction.defaultNavigationAnimation(fragment: Fragment) {
    if (fragment is AnimatedNavigation) {
        fragment.animateNavigation(this)
    } else {
        navigationAnimationConfig.fragmentPaneAnimation(this)
    }
}

/** Extension method on [FragmentTransaction] that applies animations on sheet pane. */
public fun FragmentTransaction.sheetPaneAnimation(): FragmentTransaction {
    setCustomAnimations(
        R.anim.navigator_slide_up,
        R.anim.navigator_slide_down,
        R.anim.navigator_slide_up,
        R.anim.navigator_slide_down,
    )
    return this
}

/** Extension method on [FragmentTransaction] that applies animations on a dimmed overlay screen. */
public fun FragmentTransaction.modalPaneAnimation(): FragmentTransaction {
    setCustomAnimations(
        R.anim.navigator_fade_in,
        R.anim.navigator_fade_out,
        R.anim.navigator_fade_in,
        R.anim.navigator_fade_out,
    )
    return this
}

/** Extension method on [FragmentTransaction] that applies animations on fragment pane. */
public fun FragmentTransaction.fragmentPaneAnimation(): FragmentTransaction {
    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
    return this
}

/**
 * This interface indicates that the Navigator should add the fragment on top instead of replacing the previous one.
 * This is used for fragments that partially show the content underneath, such as [SheetPaneNavigation].
 */
public interface OverlayNavigation {
    /**
     * Gets the overlay fragment that should be added to the fragmentManager to surround the [OverlayNavigation].
     * The overlay fragment is only added if this method doesn't return null and it's only added once,
     * no matter how many [OverlayNavigation] fragments are pushed.
     *
     * Default is [ModalOverlayFragment] which adds a 82% black shadow.
     */
    public fun getModalOverlayFragment(): Fragment? = ModalOverlayFragment()

    /**
     * Callback invoked when the outside area of this fragment is clicked.
     */
    public fun onClickOutside() {}
}

/**
 * This interface allows fragments to define their own animation. Also, see [SheetPaneNavigation].
 */
public interface AnimatedNavigation {
    public fun animateNavigation(fragmentTransaction: FragmentTransaction)
}

/**
 * Interface marking a fragment to be a sheet pane overlay.
 *
 * For example this can be used in `BottomSheetFragments` where the bottom sheet needs
 * to be displayed over the current content fragment to still see the content in background.
 */
public interface SheetPaneNavigation : AnimatedNavigation, OverlayNavigation {
    // TODO: Add support for defining a screen side (top, bottom, left, right) so animation adapts correctly.
    // The screen side should be passed to sheetPaneAnimation() as an argument, so we can centrally override all
    // animations.

    override fun animateNavigation(fragmentTransaction: FragmentTransaction) {
        navigationAnimationConfig.sheetPaneAnimation(fragmentTransaction)
    }
}

public interface ModalPaneNavigation : AnimatedNavigation {
    override fun animateNavigation(fragmentTransaction: FragmentTransaction) {
        navigationAnimationConfig.modalPaneAnimation(fragmentTransaction)
    }
}

/**
 * Interface marking that a fragment's state should never be saved. Especially useful for security critical code.
 *
 * Use this for fragments where you enter passwords or PINs or deal with other security critical data that must never
 * be stored persistently.
 */
public interface StatelessNavigation

/**
 * Interface marking that a fragment's orientation is fixed.
 *
 * Use this for fragments where the content should be displayed only in landscape or only in portrait.
 *
 */
public interface FixedOrientation {
    /**
     * Enum constant containing the fixed orientation that this fragment should have.
     */
    public val orientation: Orientation
}

public enum class Orientation {
    PORTRAIT, LANDSCAPE
}

private val Orientation.androidScreenOrientation: Int
    get() = when (this) {
        Orientation.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        Orientation.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

/**
 * Interface for fragments wanting to handle the system back button press.
 */
public interface OnBackPressedNavigation {
    public fun onBackPressed(): Abortable
}

/**
 * Finds the current [Navigator].
 *
 * This can either be a fragment or the hosting activity. This will throw an exception if no [Navigator] was found.
 *
 * @param skip How many [Navigator]s to skip. Defaults to 0.
 */
public fun Fragment.findNavigator(skip: Int = 0): Navigator =
    findInHierarchy(skip = skip) {
        (it as? NavigatorOwner)?.navigator
    }

/** Pops all currently visible [SheetPaneNavigation]s / bottom sheets. */
public fun <T> T.popSheetPanes(): Boolean where T : FragmentActivity, T : NavigatorOwner =
    navigator.popUntil { it !is SheetPaneNavigation }

/** Pops all currently visible [OverlayNavigation]s including dialogs and bottom sheets. */
public fun <T> T.popOverlays(): Boolean where T : FragmentActivity, T : NavigatorOwner =
    navigator.popUntil { it !is OverlayNavigation }

/** Pops all currently visible [SheetPaneNavigation]s / bottom sheets. */
public fun Fragment.popSheetPanes(): Boolean =
    findNavigator().popUntil { it !is SheetPaneNavigation }

/** Pops all currently visible [OverlayNavigation]s including dialogs and bottom sheets. */
public fun Fragment.popOverlays(): Boolean =
    findNavigator().popUntil { it !is OverlayNavigation }

/**
 * Creates a new [Navigator] instance on a fragment implementing [NavigatorOwner].
 *
 * @param containerId The container where fragments are added to.
 * @param animator Defines custom animations. Don't use this to globally override animations for the whole app!
 */
@Suppress("FunctionName")
public fun <T> T.Navigator(
    @IdRes containerId: Int,
    animator: FragmentTransaction.(fragment: Fragment) -> Unit = navigationAnimationConfig::defaultAnimation,
): Navigator where T : Fragment, T : NavigatorOwner =
    NavigatorImpl(requireActivity(), childFragmentManager, containerId, animator)

/**
 * Creates a new [Navigator] instance on an activity implementing [NavigatorOwner].
 *
 * @param containerId The container where fragments are added to.
 * @param animator Defines custom animations. Don't use this to globally override animations for the whole app!
 */
@Suppress("FunctionName")
public fun <T> T.Navigator(
    @IdRes containerId: Int = android.R.id.content,
    animator: FragmentTransaction.(fragment: Fragment) -> Unit = navigationAnimationConfig::defaultAnimation,
): Navigator where T : FragmentActivity, T : NavigatorOwner =
    NavigatorImpl(this, supportFragmentManager, containerId, animator)

private class NavigatorImpl(
    private val activity: FragmentActivity,
    private val fragmentManager: FragmentManager,
    @IdRes private val containerId: Int,
    private val animator: FragmentTransaction.(fragment: Fragment) -> Unit,
) : Navigator {

    override val backStackEntryCount = MutableStateFlow(0).apply {
        fragmentManager.addOnBackStackChangedListener {
            value = fragmentManager.backStackEntryCount
            updateOrientation()
        }
    }

    init {
        updateOrientation()
    }

    override fun isEmpty(): Boolean {
        fragmentManager.executePendingTransactions()
        return fragmentManager.fragments.isEmpty()
    }

    override fun push(fragment: Fragment) {
        // this prevents a crash when a pop or other transactions are still pending
        fragmentManager.executePendingTransactions()
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

    override fun onBackPressed(): Abortable {
        fragmentManager.executePendingTransactions()
        val result = fragmentManager.fragments.lastOrNull().let {
            (it as? OnBackPressedNavigation)?.onBackPressed()
                ?: (it as? NavigatorOwner)?.navigator?.onBackPressed()
        }
        return if (result == Abort || pop()) Abort else Continue
    }

    override fun popAll() {
        fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    override fun pop(): Boolean {
        fragmentManager.executePendingTransactions()

        return if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
            true
        } else false
    }

    override fun popUntil(cls: KClass<out Fragment>, includeMatch: Boolean) {
        val tag = getTagName(cls)
        fragmentManager.popBackStack(tag, if (includeMatch) FragmentManager.POP_BACK_STACK_INCLUSIVE else 0)
    }

    override fun popUntil(includeMatch: Boolean, predicate: (Any) -> Boolean): Boolean {
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

    override fun findFragment(func: (Fragment) -> Boolean): Fragment? =
        fragmentManager.fragments.lastOrNull(func)

    private fun Fragment.getTagName(): String? = getTagName(this::class)

    private fun getTagName(cls: KClass<out Fragment>): String? =
        if (StatelessNavigation::class.java.isAssignableFrom(cls.java)) null else cls.java.name

    private fun updateOrientation(fragment: Fragment? = null) {
        val fixedOrientation =
            (fragment ?: findFragment { it is FixedOrientation }) as? FixedOrientation
        activity.requestedOrientation = fixedOrientation?.orientation?.androidScreenOrientation
            ?: ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}
