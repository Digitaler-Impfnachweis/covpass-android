package com.ibm.health.common.navigation.android

import android.transition.Fade
import android.transition.Slide
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction

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
    public fun fragmentPaneAnimation(transaction: FragmentTransaction, fragment: Fragment)

    /** Applies animations for [SheetPaneNavigation]. */
    public fun sheetPaneAnimation(fragment: Fragment)

    /** Applies animations for the overlay fragment behind [SheetPaneNavigation] sheets. */
    public fun modalPaneAnimation(fragment: Fragment)
}

/** The default animations. */
public class DefaultNavigationAnimationConfig(
    public val animationDuration: Long = 400,
) : NavigationAnimationConfig {

    override fun fragmentPaneAnimation(transaction: FragmentTransaction, fragment: Fragment) {
        transaction.fragmentPaneAnimation()
    }

    override fun sheetPaneAnimation(fragment: Fragment) {
        Slide().setDuration(animationDuration).let {
            fragment.enterTransition = it
            fragment.reenterTransition = it
        }
    }

    override fun modalPaneAnimation(fragment: Fragment) {
        Fade(Fade.IN).setDuration(animationDuration).let {
            fragment.enterTransition = it
            fragment.reenterTransition = it
        }

        Fade(Fade.OUT).setDuration(animationDuration).let {
            fragment.exitTransition = it
            fragment.returnTransition = it
        }
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
        fragment.animateNavigation(this, fragment)
    } else {
        navigationDeps.animationConfig.fragmentPaneAnimation(this, fragment)
    }
}

/** Extension method on [FragmentTransaction] that applies animations on fragment pane. */
public fun FragmentTransaction.fragmentPaneAnimation(): FragmentTransaction {
    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
    return this
}
