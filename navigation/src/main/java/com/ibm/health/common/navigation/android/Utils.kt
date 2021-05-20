/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.navigation.android

import android.os.Bundle
import android.os.Parcelable

internal const val EXTRA_ARGS: String = "com.ibm.health.common.navigation.EXTRA_ARGS"

/**
 * Shortcut for adding a `Parcelable` to a `Bundle`.
 *
 * @return the original `Intent`.
 */
public fun Bundle.withArgs(value: Parcelable): Bundle =
    apply { putParcelable(EXTRA_ARGS, value) }

/**
 * Retrieves the `Parcelable` args added via [Bundle.withArgs].
 *
 * @throws [IllegalStateException] if the `Parcelable` is missing.
 *
 * Also see [Bundle.getOptionalArgs] if you want a `null` result instead of an exception.
 */
public fun <T : Parcelable> Bundle?.getArgs(): T =
    this?.getOptionalArgs()
        ?: throw IllegalStateException("No args found in Bundle")

/**
 * Retrieves the `Parcelable` args added via [Bundle.withArgs].
 *
 * @return the `Parcelable` or `null` if it doesn't exist.
 */
public fun <T : Parcelable> Bundle.getOptionalArgs(): T? =
    getParcelable(EXTRA_ARGS)
