/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.updateinfo

import com.ensody.reactivestate.SuspendMutableValueFlow
import de.rki.covpass.sdk.storage.CborSharedPrefsStore

/**
 * Repository that provides access to the information of the current app version and if the update information was
 * shown in this device and which version of the data privacy was accepted
 */
public class UpdateInfoRepository(store: CborSharedPrefsStore) {
    public val updateInfoVersionShown: SuspendMutableValueFlow<Int> =
        store.getData("update_info_version_shown", 0)

    public val updateInfoNotificationActive: SuspendMutableValueFlow<Boolean> =
        store.getData("update_info_notification_active", true)

    public companion object {
        public const val CURRENT_UPDATE_VERSION: Int = 27
    }
}
