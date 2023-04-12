/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.storage

import com.ensody.reactivestate.SuspendMutableValueFlow

public class RulesUpdateRepository(
    store: CborSharedPrefsStore,
) {

    public val localDatabaseVersion: SuspendMutableValueFlow<Int> =
        store.getData("local_database_update_version", 0)

    public suspend fun updateLocalDatabaseVersion() {
        localDatabaseVersion.set(CURRENT_LOCAL_DATABASE_VERSION)
    }

    public companion object {
        public const val CURRENT_LOCAL_DATABASE_VERSION: Int = 10
    }
}
