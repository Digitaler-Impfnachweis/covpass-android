package com.ibm.health.common.navigation.android

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlin.reflect.KClass

/**
 * Shortcut for adding a `Parcelable` to an `Intent`.
 *
 * @return the original `Intent`.
 */
public fun Intent.withArgs(value: Parcelable): Intent =
    apply { putExtra(EXTRA_ARGS, value) }

/**
 * Retrieves the `Parcelable` args added via [Intent.withArgs].
 *
 * @throws [IllegalStateException] if the `Parcelable` is missing.
 *
 * Also see [Intent.getOptionalArgs] if you want a `null` result instead of an exception.
 */
public fun <T : Parcelable> Intent?.getArgs(): T =
    getOptionalArgs()
        ?: throw IllegalStateException("No args found in Intent")

/**
 * Retrieves the `Parcelable` args added via [Intent.withArgs].
 *
 * @return the `Parcelable` or `null` if it doesn't exist.
 */
public fun <T : Parcelable> Intent?.getOptionalArgs(): T? =
    this?.extras?.getParcelable(EXTRA_ARGS)

/**
 * Retrieves the `Parcelable` args passed to this `Activity` via [startActivity] + [IntentNav] or [Intent.withArgs].
 *
 * @throws [IllegalStateException] if the `Parcelable` is missing.
 *
 * Also see [Activity.getOptionalArgs] if you want a `null` result instead of an exception.
 */
public fun <T : Parcelable> Activity.getArgs(): T =
    getOptionalArgs()
        ?: throw IllegalStateException("No args were passed to ${this::class.java.simpleName}")

/**
 * Retrieves the `Parcelable` args passed to this `Activity` via [startActivity] + [IntentNav] or [Intent.withArgs].
 *
 * @return the `Parcelable` or `null` if it doesn't exist.
 */
public fun <T : Parcelable> Activity.getOptionalArgs(): T? =
    intent?.getOptionalArgs()

/** Base interface for the simplified [startActivity]. */
public interface IntentDestination {
    public fun toIntent(context: Context): Intent
}

/**
 * Base class for defining a `Parcelable` that can be used for easy and type-safe argument-passing to an `Activity`.
 *
 * You should usually use this in combination with `@Parcelize`.
 * In complex cases with distinct possible arguments you might even combine this with `sealed class`.
 *
 * Example:
 *
 * ```kotlin
 * @Parcelize
 * internal class EditDocumentActivityNav(val documentId: String) : IntentNav(EditDocumentActivity::class)
 *
 * class EditDocumentActivity : AppCompatActivity() {
 *     val args by lazy { getArgs<EditDocumentActivityNav>() }
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         // ...
 *         loadDocument(args.documentId)
 *     }
 * }
 * ```
 *
 * Then, within your `Activity` or `Context` you can start the activity in a type-safe way:
 *
 * ```kotlin
 * startActivity(EditDocumentActivityNav(someDocumentId))
 * ```
 *
 * This also works with [startActivityForResult].
 */
@Parcelize
public open class IntentNav internal constructor(private val javaCls: Class<*>) : IntentDestination, Parcelable {

    /**
     * Constructs a new [IntentNav] for the given activity's class [cls] with the given config as [intentConfig].
     */
    public constructor(cls: KClass<*>, config: Intent.() -> Unit = {}) : this(cls.java) {
        this.intentConfig = config
    }

    /**
     * The activity's [KClass].
     */
    @IgnoredOnParcel
    public val cls: KClass<*> = javaCls.kotlin

    /**
     * Additional function that will be applied to the Intent, to implement some special configuration.
     */
    @IgnoredOnParcel
    public var intentConfig: Intent.() -> Unit = {}
        private set

    override fun toIntent(context: Context): Intent =
        Intent(context, cls.java).withArgs(this).apply(intentConfig)

    public companion object {

        /**
         * Extend the current [intentConfig] by another function.
         */
        public fun <T : IntentNav> T.addConfig(config: Intent.() -> Unit): T =
            apply {
                val prevConfig = intentConfig
                intentConfig = {
                    prevConfig(this)
                    config()
                }
            }

        /**
         * Replace the current [intentConfig] by the given function.
         */
        public fun <T : IntentNav> T.withConfig(config: Intent.() -> Unit): T =
            apply {
                intentConfig = config
            }
    }
}

/** Like Android's `startActivity`, but taking an [IntentDestination] (usually defined via [IntentNav]). */
public fun Context.startActivity(nav: IntentDestination) {
    startActivity(nav.toIntent(this))
}

/** Like Android's `startActivity`, but taking an [IntentDestination] (usually defined via [IntentNav]). */
public fun Context.startActivity(nav: IntentDestination, options: Bundle) {
    startActivity(nav.toIntent(this), options)
}

/** Like Android's `startActivityForResult`, but taking an [IntentDestination] (usually defined via [IntentNav]). */
public fun Activity.startActivityForResult(nav: IntentDestination, requestCode: Int) {
    startActivityForResult(nav.toIntent(this), requestCode)
}

/** Like Android's `startActivityForResult`, but taking an [IntentDestination] (usually defined via [IntentNav]). */
public fun Activity.startActivityForResult(nav: IntentDestination, requestCode: Int, options: Bundle) {
    startActivityForResult(nav.toIntent(this), requestCode, options)
}
