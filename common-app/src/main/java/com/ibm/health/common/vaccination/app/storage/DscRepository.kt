package com.ibm.health.common.vaccination.app.storage

import com.ensody.reactivestate.SuspendMutableValueFlow
import com.ibm.health.common.vaccination.app.utils.CborSharedPrefsStore
import java.time.LocalDateTime

public class DscRepository(store: CborSharedPrefsStore) {
    // TODO set the last update when the feature will be implemented
    public val lastUpdate: SuspendMutableValueFlow<LocalDateTime> = store.getData("last_update", NO_UPDATE_YET)

    // TODO add storage of dsc certs here

    public companion object {
        public val NO_UPDATE_YET: LocalDateTime = LocalDateTime.MIN
    }
}
