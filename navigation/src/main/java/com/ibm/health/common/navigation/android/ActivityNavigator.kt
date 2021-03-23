package com.ibm.health.common.navigation.android

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.ibm.health.common.logging.Lumber
import kotlin.reflect.KClass

/**
 * Holds a reference to the [currentActivity] and offers some functions for activity navigation.
 */
public class ActivityNavigator {

    init {
        initActivityNavigator
    }

    /**
     * Start a new activity from the [currentActivity] with given intent flags.
     */
    public fun startActivity(activityToStart: Class<out Activity>, intentFlags: List<Int>? = null) {
        val intent = Intent(currentActivity, activityToStart)
        intentFlags?.forEach { intent.addFlags(it) }
        currentActivity.startActivity(intent)
    }

    /**
     * Start a new activity from the [currentActivity].
     */
    public fun startActivity(intentDestination: IntentDestination) {
        currentActivity.startActivity(intentDestination)
    }

    /**
     * Start a new activity from the [currentActivity].
     */
    public fun startActivity(intent: Intent) {
        currentActivity.startActivity(intent)
    }

    /**
     * Get an [IntentNav] for the [currentActivity].
     */
    public fun getCurrent(): IntentNav {
        // Cache the currentActivity in an immutable field, else it can change till execution of closure
        val activity = currentActivity
        return IntentNav(activity::class) {
            activity.intent.extras?.let {
                putExtras(it)
            }
        }
    }

    /**
     * Returns [currentActivity] if initialized, else null.
     */
    public fun getCurrentActivityOrNull(): Activity? =
        if (::currentActivity.isInitialized) currentActivity else null

    /**
     * Returns [currentActivity].
     */
    public fun getCurrentActivity(): Activity? = currentActivity

    /**
     * Calls [Activity.navigateUpTo].

     * @return true if up navigation successfully reached the activity indicated by upIntent and
     *         upIntent was delivered to it. false if an instance of the indicated activity could
     *         not be found and this activity was simply finished normally.
     */
    public fun navigateUpTo(intentDestination: IntentDestination): Boolean =
        currentActivity.navigateUpTo(intentDestination.toIntent(currentActivity))
}

private val initActivityNavigator by lazy {
    if (!LifecycleTracker.initialized) {
        navigationDeps.application.registerActivityLifecycleCallbacks(LifecycleTracker)
        LifecycleTracker.initialized = true
    }
}

@SuppressLint("StaticFieldLeak")
private lateinit var currentActivity: Activity

private object LifecycleTracker : Application.ActivityLifecycleCallbacks {

    var initialized: Boolean = false

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Lumber.d { "onActivityCreated, ${activity::class.java.name}" }
        currentActivity = activity
        (activity as? FragmentActivity)?.supportFragmentManager
            ?.registerFragmentLifecycleCallbacks(
                fragmentCallbacks,
                shouldLogFragmentLifecycleRecursive
            )
    }

    override fun onActivityStarted(activity: Activity) {
        Lumber.d { "onActivityStarted, ${activity::class.java.name}" }
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        Lumber.d { "onActivityResumed, ${activity::class.java.name}" }
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
        Lumber.d { "onActivityPaused, ${activity::class.java.name}" }
    }

    override fun onActivityStopped(activity: Activity) {
        Lumber.d { "onActivityStopped, ${activity::class.java.name}" }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        Lumber.d { "onActivitySaveInstanceState, ${activity::class.java.name}" }
    }

    override fun onActivityDestroyed(activity: Activity) {
        Lumber.d { "onActivityDestroyed, ${activity::class.java.name}" }
    }
}

private val fragmentCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
    override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
        super.onFragmentAttached(fm, f, context)
        logCallback("onFragmentAttached", f)
    }

    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        super.onFragmentCreated(fm, f, savedInstanceState)
        logCallback("onFragmentCreated", f)
    }

    override fun onFragmentViewCreated(
        fm: FragmentManager,
        f: Fragment,
        v: View,
        savedInstanceState: Bundle?
    ) {
        super.onFragmentViewCreated(fm, f, v, savedInstanceState)
        logCallback("onFragmentViewCreated", f)
    }

    override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
        super.onFragmentStarted(fm, f)
        logCallback("onFragmentStarted", f)
    }

    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
        super.onFragmentResumed(fm, f)
        logCallback("onFragmentResumed", f)
    }

    override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
        super.onFragmentPaused(fm, f)
        logCallback("onFragmentPaused", f)
    }

    override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
        super.onFragmentStopped(fm, f)
        logCallback("onFragmentStopped", f)
    }

    override fun onFragmentSaveInstanceState(fm: FragmentManager, f: Fragment, outState: Bundle) {
        super.onFragmentSaveInstanceState(fm, f, outState)
        logCallback("onFragmentSaveInstanceState", f)
    }

    override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
        super.onFragmentViewDestroyed(fm, f)
        logCallback("onFragmentViewDestroyed", f)
    }

    override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
        super.onFragmentDestroyed(fm, f)
        logCallback("onFragmentDestroyed", f)
    }

    override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
        super.onFragmentDetached(fm, f)
        logCallback("onFragmentDetached", f)
    }

    private fun logCallback(callback: String, fragment: Fragment) {
        if (!blacklistFragmentsLifecycleLogging.any { it.isInstance(fragment) }) {
            Lumber.d { "$callback, ${fragment::class.java.simpleName}" }
        }
    }
}

/**
 * List of fragment class types of which lifecycle methods shouldn't be logged.
 * Can be overridden by assigning a new value.
 */
public val blacklistFragmentsLifecycleLogging: MutableList<KClass<out Any>> = mutableListOf(
    DialogFragment::class,
)

/**
 * True to automatically register fragment lifecycle callbacks for all child FragmentManagers.
 */
public var shouldLogFragmentLifecycleRecursive: Boolean = true
