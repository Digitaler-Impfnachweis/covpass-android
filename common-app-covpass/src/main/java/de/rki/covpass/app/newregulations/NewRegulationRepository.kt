/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.newregulations

import com.ensody.reactivestate.SuspendMutableValueFlow
import de.rki.covpass.sdk.storage.CborSharedPrefsStore

public class NewRegulationRepository(store: CborSharedPrefsStore) {
    public val newRegulationOnboardingShown: SuspendMutableValueFlow<Int> =
        store.getData("new_regulation_onboarding_shown", 0)

    public companion object {
        public const val CURRENT_NEW_REGULATION_ONBOARDING_VERSION: Int = 1
    }
}
