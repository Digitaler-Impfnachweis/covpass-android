/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.storage

import com.ensody.reactivestate.SuspendMutableValueFlow
import de.rki.covpass.commonapp.utils.CborSharedPrefsStore

/**
 * Repository that provides access to the information if onboarding was already done on this device.
 */
public class OnboardingRepository(store: CborSharedPrefsStore) {
    public val onboardingDone: SuspendMutableValueFlow<Boolean> = store.getData("onboarding_shown", false)
}
