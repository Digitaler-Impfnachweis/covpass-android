/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.dialog

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.ibm.health.common.navigation.android.findInHierarchyOrNull

public fun showDialog(dialogModel: DialogModel, fragmentManager: FragmentManager) {
    if (!fragmentManager.isDestroyed && !fragmentManager.isStateSaved) {
        val fragment = InfoDialogFragmentNav(dialogModel).build()
        fragment.show(fragmentManager, dialogModel.tag)
    }
}

/**
 * Forwards the click action on this dialog to the first listening parent of this [DialogFragment]
 * by searching in the hierarchy starting with the parent fragment up until the containing activity.
 *
 * @param action The invoked [DialogAction].
 * @param tag The fragment tag.
 * @return true if the action has been delivered to the listener, false if it could not find a listener.
 */
public fun DialogFragment.forwardDialogClickAction(tag: String, action: DialogAction): Boolean =
    findInHierarchyOrNull<DialogListener>()?.apply { onDialogAction(tag, action) } != null
