/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.storage

import com.ensody.reactivestate.SuspendMutableValueFlow
import de.rki.covpass.sdk.storage.CborSharedPrefsStore

/**
 * Repository that provides access to the information of the current data privacy version and if onboarding was
 * already done on this device and which version of the data privacy was accepted
 */
public class OnboardingRepository(store: CborSharedPrefsStore) {
    public val dataPrivacyVersionAccepted: SuspendMutableValueFlow<Int> =
        store.getData("data_privacy_version_accepted", FIRST_DATA_PRIVACY_VERSION)

    public companion object {
        public const val CURRENT_DATA_PRIVACY_VERSION: Int = 20
        public const val FIRST_DATA_PRIVACY_VERSION: Int = 0
    }
}
