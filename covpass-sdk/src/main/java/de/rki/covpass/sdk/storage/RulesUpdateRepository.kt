/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.storage

import com.ensody.reactivestate.SuspendMutableValueFlow
import de.rki.covpass.sdk.storage.DscRepository.Companion.NO_UPDATE_YET
import java.time.Instant

public class RulesUpdateRepository(
    store: CborSharedPrefsStore,
) {

    public val lastEuRulesUpdate: SuspendMutableValueFlow<Instant> =
        store.getData("last_rules_update", NO_UPDATE_YET)
    public val lastDomesticRulesUpdate: SuspendMutableValueFlow<Instant> =
        store.getData("last_domestic_rules_update", NO_UPDATE_YET)
    public val lastValueSetsUpdate: SuspendMutableValueFlow<Instant> =
        store.getData("last_value_sets_update", NO_UPDATE_YET)
    public val lastCountryListUpdate: SuspendMutableValueFlow<Instant> =
        store.getData("last_country_list_update", NO_UPDATE_YET)
    public val lastBoosterRulesUpdate: SuspendMutableValueFlow<Instant> =
        store.getData("last_booster_rules_update", NO_UPDATE_YET)
    public val localDatabaseVersion: SuspendMutableValueFlow<Int> =
        store.getData("local_database_update_version", 0)

    public suspend fun markEuRulesUpdated() {
        lastEuRulesUpdate.set(Instant.now())
    }

    public suspend fun markDomesticRulesUpdated() {
        lastDomesticRulesUpdate.set(Instant.now())
    }

    public suspend fun markValueSetsUpdated() {
        lastValueSetsUpdate.set(Instant.now())
    }

    public suspend fun markCountryListUpdated() {
        lastCountryListUpdate.set(Instant.now())
    }

    public suspend fun markBoosterRulesUpdated() {
        lastBoosterRulesUpdate.set(Instant.now())
    }

    public suspend fun updateLocalDatabaseVersion() {
        localDatabaseVersion.set(CURRENT_LOCAL_DATABASE_VERSION)
    }

    public companion object {
        public const val CURRENT_LOCAL_DATABASE_VERSION: Int = 5
    }
}
