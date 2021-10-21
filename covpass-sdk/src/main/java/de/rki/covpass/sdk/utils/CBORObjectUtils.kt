/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import com.upokecenter.cbor.CBORObject
import com.upokecenter.cbor.CBORType

/**
 * Trims all CBOR strings from this [CBORObject].
 */
public fun CBORObject.trimAllStrings(): CBORObject =
    when (this.type) {
        CBORType.Map -> {
            CBORObject.FromObject(
                entries.map { (key, value) ->
                    key.trimAllStrings() to value.trimAllStrings()
                }.toMap()
            )
        }
        CBORType.Array -> {
            CBORObject.FromObject(
                values.map {
                    it.trimAllStrings()
                }
            )
        }
        CBORType.TextString -> {
            CBORObject.FromObject(AsString().trim())
        }
        else -> {
            this
        }
    }
