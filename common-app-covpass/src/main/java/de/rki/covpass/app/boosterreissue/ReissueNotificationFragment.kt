/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.boosterreissue

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.ensody.reactivestate.android.reactiveState
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.annotations.Abort
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.ReissueNotificationPopupContentBinding
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.commonapp.dialog.DialogAction
import de.rki.covpass.commonapp.dialog.DialogListener
import de.rki.covpass.commonapp.dialog.DialogModel
import de.rki.covpass.commonapp.dialog.showDialog
import de.rki.covpass.commonapp.uielements.showInfo
import de.rki.covpass.commonapp.utils.isLandscapeMode
import de.rki.covpass.sdk.cert.models.ReissueType
import kotlinx.parcelize.Parcelize

@Parcelize
public class ReissueNotificationFragmentNav(
    public val reissueType: ReissueType,
    public val listCertIds: List<String>,
) : FragmentNav(ReissueNotificationFragment::class)

public class ReissueNotificationFragment :
    ReissueBaseFragment(),
    DialogListener,
    ReissueNotificationEvents {

    private val binding by viewBinding(ReissueNotificationPopupContentBinding::inflate)
    private val viewModel by reactiveState { ReissueNotificationViewModel(scope, args.listCertIds) }
    private val args: ReissueNotificationFragmentNav by lazy { getArgs() }
    override val buttonTextRes: Int by lazy {
        getButtonText()
    }
    override val announcementAccessibilityRes: Int = R.string.accessibility_popup_renew_certificate_announce
    override val closingAnnouncementAccessibilityRes: Int =
        R.string.accessibility_popup_renew_certificate_closing_announce

    private val combinedCovCertificate by lazy {
        covpassDeps.certRepository.certs.value.getCombinedCertificate(args.listCertIds[0])
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.bottomSheetClose.isVisible = false
        bottomSheetBinding.bottomSheetTextButton.apply {
            setText(R.string.certificate_renewal_startpage_secondary_button)
            isVisible = true
            setOnClickListener {
                viewModel.updateHasSeenReissueNotification(args.reissueType, false)
            }
        }
        bottomSheetBinding.bottomSheetExtraButtonLayout.isVisible = true
        bottomSheetBinding.bottomSheetTitle.setText(
            if (args.reissueType == ReissueType.Booster) {
                R.string.certificate_renewal_startpage_headline
            } else {
                R.string.renewal_expiry_notification_title
            },
        )

        ReissueContentAdapter(this).apply {
            combinedCovCertificate?.let {
                it.toDetailItemCertificate()?.let { certificate ->
                    updateList(listOf(certificate))
                }
            }
            attachTo(binding.reissueNotificationCertificateList)
        }
        binding.reissueNotificationImage.isGone = resources.isLandscapeMode()
        binding.reissueNotificationNote.setText(
            when (args.reissueType) {
                ReissueType.Booster -> R.string.certificate_renewal_startpage_copy
                ReissueType.Recovery -> R.string.renewal_expiry_notification_copy_recovery
                ReissueType.Vaccination -> R.string.renewal_expiry_notification_copy_vaccination
                ReissueType.None -> R.string.certificate_renewal_startpage_copy
            },
        )
        binding.reissueNotificationInfoElement.showInfo(
            getString(R.string.certificate_renewal_startpage_copy_box),
            titleStyle = R.style.DefaultText_OnBackground,
            iconRes = R.drawable.info_icon,
        )
    }

    private fun getButtonText() = when (args.reissueType) {
        ReissueType.Booster -> R.string.certificate_renewal_startpage_main_button
        ReissueType.Vaccination -> R.string.renewal_expiry_notification_button_vaccination
        ReissueType.Recovery -> R.string.renewal_expiry_notification_button_recovery
        ReissueType.None -> R.string.certificate_renewal_startpage_main_button
    }

    override fun onClickOutside() {}

    override fun onActionButtonClicked() {
        viewModel.updateHasSeenReissueNotification(args.reissueType, true)
    }

    override fun onBackPressed(): Abortable {
        val dialogModel = DialogModel(
            titleRes = R.string.cancellation_share_certificate_title,
            positiveButtonTextRes = R.string.cancellation_share_certificate_action_button_yes,
            negativeButtonTextRes = R.string.cancellation_share_certificate_action_button_no,
            tag = REISSUE_NOTIFICATION_END_PROCESS,
        )
        showDialog(dialogModel, childFragmentManager)
        return Abort
    }

    override fun onDialogAction(tag: String, action: DialogAction) {
        if (tag == REISSUE_NOTIFICATION_END_PROCESS && action == DialogAction.POSITIVE) {
            viewModel.updateHasSeenReissueNotification(args.reissueType, false)
        }
    }

    override fun onUpdateHasSeenReissueNotificationFinish(continueReissue: Boolean) {
        if (continueReissue) {
            findNavigator().push(ReissueConsentFragmentNav(args.listCertIds, args.reissueType))
        } else {
            findNavigator().popUntil<ReissueCallback>()?.onReissueCancel()
        }
    }

    public companion object {
        public const val REISSUE_NOTIFICATION_END_PROCESS: String =
            "reissue_notification_end_process"
    }
}
