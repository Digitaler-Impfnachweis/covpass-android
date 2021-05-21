/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.storage

import com.ensody.reactivestate.SuspendMutableValueFlow
import de.rki.covpass.commonapp.utils.CborSharedPrefsStore
import java.time.LocalDateTime

/**
 * Repository that provides access to the Document Signer Certificates (DSC).
 */
public class DscRepository(store: CborSharedPrefsStore) {
    // TODO set the last update when the feature will be implemented
    public val lastUpdate: SuspendMutableValueFlow<LocalDateTime> = store.getData("last_update", NO_UPDATE_YET)

    // TODO add storage of dsc certs here

    public companion object {
        public val NO_UPDATE_YET: LocalDateTime = LocalDateTime.MIN
    }
}
