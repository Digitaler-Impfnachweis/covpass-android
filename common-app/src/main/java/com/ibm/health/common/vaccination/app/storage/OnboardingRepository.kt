/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.vaccination.app.storage

import com.ensody.reactivestate.SuspendMutableValueFlow
import com.ibm.health.common.vaccination.app.utils.CborSharedPrefsStore

/**
 * Repository that provides access to the information if onboarding was already done on this device.
 */
public class OnboardingRepository(store: CborSharedPrefsStore) {
    public val onboardingDone: SuspendMutableValueFlow<Boolean> = store.getData("onboarding_shown", false)
}
