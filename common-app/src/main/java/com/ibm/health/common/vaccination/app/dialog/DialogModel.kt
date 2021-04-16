package com.ibm.health.common.vaccination.app.dialog

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import com.ibm.health.common.vaccination.app.R
import java.io.Serializable

/**
 * Class containing resources needed to display a specific kind of [androidx.fragment.app.DialogFragment].
 * Can be passed as [Serializable] to the [androidx.fragment.app.DialogFragment].
 *
 * @param styleRes The style to be used in this dialog.
 * @param titleRes The title resource ID to be set in this dialog.
 * @param titleParameter Format argument for [titleRes]].
 * @param messageRes The message resource ID to be set in this dialog.
 * @param messageParameter Format argument for [messageRes]].
 * @param positiveButtonTextRes The text resource ID to be used for the positive button of this dialog.
 * @param negativeButtonTextRes The text resource ID to be used for the negative button of this dialog.
 * @param neutralButtonTextRes The text resource ID to be used for the neutral button of this dialog.
 * @param positiveActionColorRes The color resource ID to be set for the positive button of this dialog.
 * @param negativeActionColorRes The color resource ID to be set for the negative button of this dialog.
 * @param neutralActionColorRes The color resource ID to be set for the neutral button of this dialog.
 * @param isCancelable If true, the dialog is cancelable. The default is true.
 * @param iconRes The icon resource ID to be set in this dialog.
 */
public data class DialogModel(
    @StyleRes val styleRes: Int = R.style.RoundedCornersDialogTheme,
    @StringRes val titleRes: Int?,
    val titleParameter: String = "",
    @StringRes val messageRes: Int,
    val messageParameter: String = "",
    @StringRes val positiveButtonTextRes: Int?,
    @StringRes val negativeButtonTextRes: Int? = null,
    @StringRes val neutralButtonTextRes: Int? = null,
    @ColorRes val positiveActionColorRes: Int = R.color.brandAccent,
    @ColorRes val negativeActionColorRes: Int = R.color.brandAccent,
    @ColorRes val neutralActionColorRes: Int = R.color.brandAccent,
    val isCancelable: Boolean = true,
    @DrawableRes val iconRes: Int = 0,
    val tag: String = TAG,
) : Serializable {

    private companion object {
        val TAG: String = DialogModel::class.java.canonicalName ?: "DialogModel"
    }
}
