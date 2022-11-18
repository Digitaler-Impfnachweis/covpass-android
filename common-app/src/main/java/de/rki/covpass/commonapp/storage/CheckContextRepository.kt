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
    public val checkContextNotificationVersionShown: SuspendMutableValueFlow<Int> =
        store.getData("check_context_notification_version_shown", 0)

    public val isExpertModeOn: SuspendMutableValueFlow<Boolean> =
        store.getData("is_expert_mode_on", false)

    public val isOfflineRevocationOn: SuspendMutableValueFlow<Boolean> =
        store.getData("is_offline_revocation_on", false)

    public val vaccinationProtectionMode: SuspendMutableValueFlow<VaccinationProtectionMode> =
        store.getData("vaccination_protection_mode", VaccinationProtectionMode.ModeIfsg)

    public enum class VaccinationProtectionMode {
        ModeIfsg,
        ModeEntryRules
    }

    public companion object {
        public const val CURRENT_CHECK_CONTEXT_NOTIFICATION_VERSION: Int = 1
    }
}
