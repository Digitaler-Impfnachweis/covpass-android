package de.rki.covpass.sdk.utils

import com.upokecenter.cbor.CBORObject
import com.upokecenter.cbor.CBORType

/**
 * Removes all CBOR tags from this [CBORObject].
 */
public fun CBORObject.untagAll(): CBORObject =
    when (this.type) {
        CBORType.Map -> {
            CBORObject.FromObject(
                entries.map { (key, value) ->
                    key.untagAll() to value.untagAll()
                }.toMap()
            )
        }
        CBORType.Array -> {
            CBORObject.FromObject(
                values.map {
                    it.untagAll()
                }
            )
        }
        else -> {
            Untag()
        }
    }
