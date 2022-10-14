/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.storage

import com.ensody.reactivestate.SuspendMutableValueFlow
import de.rki.covpass.commonapp.utils.FederalStateResolver
import de.rki.covpass.sdk.storage.CborSharedPrefsStore

/**
 * Repository that provides access to the information of the selected federal state
 */
public class FederalStateRepository(store: CborSharedPrefsStore) {

    public val federalState: SuspendMutableValueFlow<String> =
        store.getData("federal_state_in_use", FederalStateResolver.defaultFederalState.regionId)
}
