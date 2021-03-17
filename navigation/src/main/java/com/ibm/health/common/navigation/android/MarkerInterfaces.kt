package com.ibm.health.common.navigation.android

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction

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
    public fun animateNavigation(fragmentTransaction: FragmentTransaction?, fragment: Fragment)
}

/** Interface marking a fragment to be a sheet pane overlay (e.g. a bottom sheet). */
public interface SheetPaneNavigation : AnimatedNavigation, OverlayNavigation {
    // TODO: Add support for defining a screen side (top, bottom, left, right) so animation adapts correctly.
    // The screen side should be passed to sheetPaneAnimation() as an argument, so we can centrally override all
    // animations.

    override fun animateNavigation(fragmentTransaction: FragmentTransaction?, fragment: Fragment) {
        navigationDeps.animationConfig.sheetPaneAnimation(fragment)
    }
}

/** Interface marking a modal overlay. This usually is inserted before the first [OverlayNavigation]. */
public interface ModalPaneNavigation : AnimatedNavigation {
    override fun animateNavigation(fragmentTransaction: FragmentTransaction?, fragment: Fragment) {
        navigationDeps.animationConfig.modalPaneAnimation(fragment)
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
 */
public interface FixedOrientation {
    /** The fixed orientation that this fragment should have. */
    public val orientation: Orientation
}

/** The possible fixed orientations for [FixedOrientation]. */
public enum class Orientation {
    PORTRAIT, LANDSCAPE, SENSOR,
}
