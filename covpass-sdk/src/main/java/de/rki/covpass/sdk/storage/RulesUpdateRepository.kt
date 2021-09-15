/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.storage

import com.ensody.reactivestate.SuspendMutableValueFlow
import de.rki.covpass.sdk.storage.DscRepository.Companion.NO_UPDATE_YET
import java.time.Instant

public class RulesUpdateRepository(
    store: CborSharedPrefsStore
) {

    public val lastRulesUpdate: SuspendMutableValueFlow<Instant> = store.getData("last_rules_update", NO_UPDATE_YET)

    public suspend fun markRulesUpdated() {
        lastRulesUpdate.set(Instant.now())
    }
}
