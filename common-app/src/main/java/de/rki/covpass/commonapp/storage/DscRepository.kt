/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.storage

import com.ensody.reactivestate.SuspendMutableValueFlow
import de.rki.covpass.commonapp.utils.CborSharedPrefsStore
import de.rki.covpass.sdk.cert.models.DscList
import de.rki.covpass.sdk.dependencies.sdkDeps
import java.time.Instant

/**
 * Repository that provides access to the Document Signer Certificates (DSC).
 */
public class DscRepository(store: CborSharedPrefsStore) {

    public val lastUpdate: SuspendMutableValueFlow<Instant> = store.getData("last_update", NO_UPDATE_YET)

    public val dscList: SuspendMutableValueFlow<DscList> = store.getData("dcs_list", sdkDeps.dscList)

    public suspend fun updateDscList(newDscList: DscList) {
        dscList.set(newDscList)
        lastUpdate.set(Instant.now())
    }

    public companion object {
        public val NO_UPDATE_YET: Instant = Instant.MIN
    }
}
