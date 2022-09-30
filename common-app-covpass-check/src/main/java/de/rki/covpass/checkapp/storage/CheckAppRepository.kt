/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.storage

import com.ensody.reactivestate.SuspendMutableValueFlow
import de.rki.covpass.sdk.storage.CborSharedPrefsStore

/**
 * Repository that provides access to the information of what mode is on
 */
public class CheckAppRepository(store: CborSharedPrefsStore) {

    public val newRegulationNotificationShown: SuspendMutableValueFlow<Boolean> =
        store.getData("new_regulation_notification_shown", false)

    public val federalState: SuspendMutableValueFlow<String> =
        store.getData("federal_state_in_use", "BW")
}
