/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.errorhandling

import de.rki.covpass.commonapp.dialog.DialogModel
import de.rki.covpass.commonapp.errorhandling.CommonErrorHandler

/**
 * CovPass Check specific error handling. Overrides the abstract functions from [CommonErrorHandler].
 */
public class ErrorHandler : CommonErrorHandler() {

    // TODO add covpass-check specific errorhandling here later
    override fun getSpecificDialogModel(error: Throwable): DialogModel? = null
}
