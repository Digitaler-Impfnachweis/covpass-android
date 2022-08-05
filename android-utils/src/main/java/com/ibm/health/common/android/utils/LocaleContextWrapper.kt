/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.android.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.ibm.health.common.android.utils.LocaleContextWrapper.Companion.defaultLanguage
import de.rki.covpass.logging.Lumber
import java.util.Locale

/**
 * This class can be used to provide an alternative string resource file in values-zu which will be
 * shown in debug-mode with debug strings enabled.
 * We use it to show the string resource identifiers instead of the values.
 * Matching XML-Resource files are generated with Twine.
 * Besides that, it can be used to configure which languages are supported.
 * If a non-supported locale is set on the device, it will be exchanged with the [defaultLanguage].
 */
public class LocaleContextWrapper(private var prefs: SharedPreferences) {

    private fun saveOriginalLanguage() =
        prefs.edit().putString(KEY_ORIGINAL_LANGUAGE, Locale.getDefault().country).apply()

    private fun getOriginalLocale(): Locale {
        val originalLocale = prefs.getString(KEY_ORIGINAL_LANGUAGE, null)?.let { Locale(it) } ?: Locale.getDefault()
        return if (supportedLanguages.any { it == originalLocale.language }) {
            originalLocale
        } else {
            Locale(defaultLanguage)
        }
    }

    private fun isDebugMode() =
        prefs.getBoolean(KEY_IS_DEBUG_MODE, false)

    public companion object {
        private const val DEBUG_LANGUAGE = "zu"
        private const val PREFS_NAME = "debug_language_preferences"
        private const val KEY_ORIGINAL_LANGUAGE = "original_language"
        private const val KEY_IS_DEBUG_MODE = "is_debug_mode"

        // Change this vars to configure which languages are supported. This solution is not really perfect,
        // but the problem is quite complex and there is no standard solution.
        public var defaultLanguage: String = Locale.GERMANY.language
        public var supportedLanguages: List<String> = listOf(defaultLanguage)

        private var instance: LocaleContextWrapper? = null

        /**
         * Use this method to switch between debug language and original device language
         * @return true if debug mode is enabled
         */
        public fun toggleDebugMode(activity: Activity) {
            getInstance(activity.baseContext).apply {
                val debug = !isDebugMode()
                if (debug) saveOriginalLanguage()
                prefs.edit().putBoolean(KEY_IS_DEBUG_MODE, debug).apply()
                activity.recreate()
            }
        }

        /**
         * Override super.attachBaseContext() of the activity and pass return value of this method
         * @param newBase pass the parameter from Activity.attachBaseContext(newBase) here
         */
        public fun wrapContext(newBase: Context): Context {
            return getInstance(newBase).let {
                wrapContextWithLocale(
                    newBase,
                    if (isDebuggable && it.isDebugMode()) Locale(DEBUG_LANGUAGE) else it.getOriginalLocale(),
                )
            }
        }

        /**
         * Use this method instead of wrapContext() if you want to pass individual Locale
         */
        public fun wrapContextWithLocale(baseContext: Context, locale: Locale): Context {
            Locale.setDefault(locale)
            return baseContext.createConfigurationContext(
                baseContext.resources.configuration.apply {
                    setLocale(locale)
                },
            )
        }

        /**
         * This may be used to get strings for which we dont provide a translation, e.g. for calendar
         * @return the last default locale before applying debug mode or current default if not in debug mode
         */
        public fun getOriginalLocale(): Locale =
            instance.let {
                try {
                    if (isDebuggable && it?.isDebugMode() == true) it.getOriginalLocale()
                    else Locale.GERMANY
                } catch (e: Exception) {
                    Lumber.w { "Dependencies not initialized, fallback to default locale." }
                    Locale.GERMANY
                }
            }

        private fun getInstance(baseContext: Context) =
            instance ?: LocaleContextWrapper(
                baseContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
            ).apply { instance = this }
    }
}
