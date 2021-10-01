/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.checkerremark

import com.ensody.reactivestate.SuspendMutableValueFlow
import de.rki.covpass.sdk.storage.CborSharedPrefsStore

public class CheckerRemarkRepository(store: CborSharedPrefsStore) {
    public val checkerRemarkShown: SuspendMutableValueFlow<Int> =
        store.getData("checker_remark_shown", 0)

    public companion object {
        public const val CURRENT_CHECKER_REMARK_VERSION: Int = 1
    }
}
