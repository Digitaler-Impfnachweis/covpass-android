/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.storage

import com.ensody.reactivestate.SuspendMutableValueFlow
import de.rki.covpass.sdk.storage.CborSharedPrefsStore

/**
 * Repository that provides access to the information of the status of the acoustic feedback
 */
public class AcousticFeedbackRepository(store: CborSharedPrefsStore) {
    public val acousticFeedbackStatus: SuspendMutableValueFlow<Boolean> =
        store.getData(ACOUSTIC_FEEDBACK_STATUS, false)

    private companion object {
        const val ACOUSTIC_FEEDBACK_STATUS = "acoustic_feedback_status"
    }
}
