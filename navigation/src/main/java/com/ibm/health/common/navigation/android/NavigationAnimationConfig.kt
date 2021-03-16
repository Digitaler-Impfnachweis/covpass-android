package com.ibm.health.common.navigation.android

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction

/** The currently active default [NavigationAnimationConfig]. */
public var navigationAnimationConfig: NavigationAnimationConfig = DefaultNavigationAnimationConfig()

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
