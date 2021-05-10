package com.ibm.health.common.vaccination.app.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ibm.health.common.navigation.android.FragmentDestination
import com.ibm.health.common.navigation.android.getArgs
import com.ibm.health.common.navigation.android.withArgs
import com.ibm.health.common.vaccination.app.databinding.DialogTitleBinding
import kotlinx.parcelize.Parcelize

@Parcelize
public class InfoDialogFragmentNav(public val dialogModel: DialogModel) : FragmentDestination {
    override fun build(): InfoDialogFragment = InfoDialogFragment().withArgs(this)
}

/**
 * Displaying an alert dialog corresponding to [DialogModel].
 * Pass a [DialogModel] as parameter for [InfoDialogFragmentNav] to create an instance.
 */
public class InfoDialogFragment : DialogFragment() {
    private val args: InfoDialogFragmentNav by lazy { getArgs() }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        args.let {
            createDialog(it.dialogModel)
        } ?: super.onCreateDialog(savedInstanceState)

    @SuppressWarnings("ComplexMethod")
    private fun createDialog(dialogModel: DialogModel): AlertDialog? = activity?.let {
        // Setting maxLines in DialogTitleTextStyle is somehow also working but has some very strange glitches,
        // so doing a workaround for maxlines with custom title here is safer.
        val titleBinding = DialogTitleBinding.inflate(LayoutInflater.from(context))
        titleBinding.titleTextview.text = dialogModel.titleRes?.let { titleResId ->
            getString(titleResId, dialogModel.titleParameter)
        }
        MaterialAlertDialogBuilder(activity as FragmentActivity, dialogModel.styleRes)
            .setIcon(
                dialogModel.iconRes.takeIf { it > 0 }
                    ?.let { res ->
                        activity?.let {
                            ContextCompat.getDrawable(
                                it, res
                            )
                        }
                    }
            )
            .setCustomTitle(titleBinding.titleTextview)
            .setMessage(getString(dialogModel.messageRes, dialogModel.messageParameter))
            .setPositiveButton(
                dialogModel.positiveButtonTextRes?.let { positiveButtonTextResId ->
                    getString(positiveButtonTextResId)
                }
            ) { _, _ ->
                dismiss()
                forwardDialogClickAction(dialogModel.tag, DialogAction.POSITIVE)
            }
            .setNegativeButton(
                dialogModel.negativeButtonTextRes?.let { negativeButtonTextResId ->
                    getString(negativeButtonTextResId)
                }
            ) { _, _ ->
                dismiss()
                forwardDialogClickAction(dialogModel.tag, DialogAction.NEGATIVE)
            }
            .setNeutralButton(
                dialogModel.neutralButtonTextRes?.let { neutralResId ->
                    getString(neutralResId)
                }
            ) { _, _ ->
                dismiss()
                forwardDialogClickAction(dialogModel.tag, DialogAction.NEUTRAL)
            }
            .setCancelable(dialogModel.isCancelable)
            .create().also { dialog ->
                dialog.applyButtonColors(dialogModel)
            }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        forwardDialogClickAction(args.dialogModel.tag, DialogAction.NEGATIVE)
    }

    private fun AlertDialog.applyButtonColors(dialogModel: DialogModel) =
        this.setOnShowListener {
            getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                ContextCompat.getColor(context, dialogModel.positiveActionColorRes)
            )
            getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                ContextCompat.getColor(context, dialogModel.negativeActionColorRes)
            )
            getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(
                ContextCompat.getColor(context, dialogModel.neutralActionColorRes)
            )
        }
}
