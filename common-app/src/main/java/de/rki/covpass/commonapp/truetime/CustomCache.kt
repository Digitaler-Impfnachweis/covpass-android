/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.truetime

import com.ensody.reactivestate.DependencyAccessor
import com.instacart.library.truetime.CacheInterface
import de.rki.covpass.commonapp.dependencies.commonDeps

@OptIn(DependencyAccessor::class)
public class CustomCache : CacheInterface {

    private val sharedPrefs = commonDeps.trueTimeSharedPrefs

    override fun put(key: String, value: Long) {
        sharedPrefs.edit().putLong(key, value).apply()
    }

    override fun get(key: String, defaultValue: Long): Long =
        sharedPrefs.getLong(key, 0)

    override fun clear() {
        remove(CacheInterface.KEY_CACHED_BOOT_TIME)
        remove(CacheInterface.KEY_CACHED_DEVICE_UPTIME)
        remove(CacheInterface.KEY_CACHED_SNTP_TIME)
    }

    private fun remove(key: String) {
        sharedPrefs.edit().remove(key).apply()
    }
}
