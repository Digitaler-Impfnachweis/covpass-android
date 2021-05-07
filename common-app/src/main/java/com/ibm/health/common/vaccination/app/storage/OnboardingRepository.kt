package com.ibm.health.common.vaccination.app.storage

import com.ensody.reactivestate.SuspendMutableValueFlow
import com.ibm.health.common.vaccination.app.utils.CborSharedPrefsStore

public class OnboardingRepository(store: CborSharedPrefsStore) {
    public val onboardingDone: SuspendMutableValueFlow<Boolean> = store.getData("onboarding_shown", false)
}
