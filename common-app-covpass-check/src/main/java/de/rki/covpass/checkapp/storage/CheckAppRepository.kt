/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.storage

import com.ensody.reactivestate.SuspendMutableValueFlow
import de.rki.covpass.sdk.storage.CborSharedPrefsStore

/**
 * Repository that provides access to the information of what mode is on
 */
public class CheckAppRepository(store: CborSharedPrefsStore) {

    public val activatedCheckingMode: SuspendMutableValueFlow<CheckingMode> =
        store.getData("activated_checking_mode", CheckingMode.Mode3G)

    public fun is2GPlusOn(): Boolean = activatedCheckingMode.value == CheckingMode.Mode2GPlus

    public fun is2GPlusBOn(): Boolean = activatedCheckingMode.value == CheckingMode.Mode2GPlusB

    public fun is3GOn(): Boolean = activatedCheckingMode.value == CheckingMode.Mode3G
}

public enum class CheckingMode {
    Mode3G,
    Mode2GPlus,
    Mode2GPlusB
}
