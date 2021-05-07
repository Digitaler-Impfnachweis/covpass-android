package com.ibm.health.vaccination.app.certchecker.errorhandling

import com.ibm.health.common.vaccination.app.dialog.DialogModel
import com.ibm.health.common.vaccination.app.errorhandling.CommonErrorHandler

public class ErrorHandler : CommonErrorHandler() {

    // TODO add cert-checker specific errorhandling here later
    override fun getSpecificDialogModel(error: Throwable): DialogModel? = null
}
