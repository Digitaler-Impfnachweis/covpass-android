/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.dialog

/**
 * The user of this interface can receive click events on the alert dialogs.
 */
public interface DialogListener {

    /**
     * Callback invoked when an action is performed on the dialog.
     *
     * @param tag The fragment tag.
     * @param action The invoked [DialogAction].
     */
    public fun onDialogAction(tag: String, action: DialogAction)
}
