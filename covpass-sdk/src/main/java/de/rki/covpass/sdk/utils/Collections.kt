/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

/** Like [groupBy], but assumes that the keys are distinct/unique, so creates a map with one value per key. */
public fun <T, G> Collection<T>.distinctGroupBy(block: (T) -> G): Map<G, T> =
    map { block(it) to it }.toMap()
