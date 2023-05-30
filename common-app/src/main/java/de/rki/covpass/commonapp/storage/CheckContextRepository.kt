/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.storage

import com.ensody.reactivestate.SuspendMutableValueFlow
import de.rki.covpass.sdk.storage.CborSharedPrefsStore

/**
 * Repository that provides access to the information of the domestic rules and notification
 */
public class CheckContextRepository(store: CborSharedPrefsStore) {
    public val isExpertModeOn: SuspendMutableValueFlow<Boolean> =
        store.getData("is_expert_mode_on", false)

    public val isOfflineRevocationOn: SuspendMutableValueFlow<Boolean> =
        store.getData("is_offline_revocation_on", false)

    public val showSunsetPopup: SuspendMutableValueFlow<Boolean> =
        store.getData("show_sunset_popup", true)
}
