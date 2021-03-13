package com.ibm.health.common.navigation.android

/**
 * If the current activity is [FragmentWrapperActivity] then the [fragmentNav] will be pushed
 * under the same activity using the [navigator].
 * Otherwise, a new [FragmentWrapperActivity] is started with the [fragmentNav] added automatically.
 */
@Deprecated(
    message = "Use this method only until the fragment-based navigation transition is complete.",
    replaceWith = ReplaceWith("push(fragmentNav)", "com.ibm.health.common.navigation.android.Navigator.push")
)
public fun ActivityNavigator.startOrPush(navigator: Navigator, fragmentNav: FragmentNav) {
    if (getCurrent().cls === FragmentWrapperActivity::class) {
        navigator.push(fragmentNav)
    } else {
        startActivity(FragmentWrapperActivityNav(fragmentNav))
    }
}
