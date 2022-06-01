/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.boosterreissue

import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.commonapp.BaseBottomSheet

public abstract class ReissueBaseFragment : BaseBottomSheet() {

    override fun onError(error: Throwable) {
        covpassDeps.reissueErrorHandler.handleError(error, childFragmentManager)
    }
}
